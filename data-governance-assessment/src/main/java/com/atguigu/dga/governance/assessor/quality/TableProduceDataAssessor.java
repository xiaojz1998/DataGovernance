package com.atguigu.dga.governance.assessor.quality;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Component("PRODUCE_DATA_SIZE")
public class TableProduceDataAssessor  extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException, URISyntaxException, IOException, InterruptedException {
        System.out.println("TableProduceDataAssessor 查找问题..... ");
        //计算出当日产出数据量， 前 days 天的平均数据量， 进行比较，判断是否超过或者低于指标参数的建议值
        //必须是日分区表
        if(!DgaConstant.LIFECYCLE_TYPE_DAY.equals(assessParam.getTableMetaInfo().getTableMetaInfoExtra().getLifecycleType())){
            return;
        }

        //取指标参数
        String metricParamsJson = assessParam.getGovernanceMetric().getMetricParamsJson();
        JSONObject paramJsonObj = JSON.parseObject(metricParamsJson);
        Integer paramDays = paramJsonObj.getInteger("days");
        Integer paramUpperLimit = paramJsonObj.getInteger("upper_limit");
        Integer paramLowerLimit = paramJsonObj.getInteger("lower_limit");

        // 考评日期
        String assessDate = assessParam.getAssessDate();
        // 转成日期格式
        Date todayDate = DateUtils.parseDate(assessDate, "yyyy-MM-dd");
        //当日（考评日期前一天）
        Date currentDate = DateUtils.addDays(todayDate, -1);
        // 转换成字符串
        String currentDateStr = DateFormatUtils.format(currentDate, "yyyy-MM-dd");

        // 表路径
        String tableFsPath = assessParam.getTableMetaInfo().getTableFsPath();
        //取分区信息
        String partitionColNameJson = assessParam.getTableMetaInfo().getPartitionColNameJson();
        List<JSONObject> partitionList = JSON.parseArray(partitionColNameJson, JSONObject.class);
        String partitionName = partitionList.get(0).getString("name");

        //处理当日的分区路径
        String currentFsPath = tableFsPath + "/" + partitionName + "=" + currentDateStr ;

        //创建文件系统对象
        FileSystem fileSystem = FileSystem.get(new URI(currentFsPath), new Configuration(), assessParam.getTableMetaInfo().getTableFsOwner());
        //计算当日产出数据量
        Long currentDataSize = calcProduceData(fileSystem, currentFsPath);
        //计算前days天的平均产出数据量
        Long beforeTotalProduceData = 0L;
        Long realBeforeDays = 0L ;
        for (int i = 1; i <= paramDays; i++) {
            Date beforeDate = DateUtils.addDays(currentDate, -i);
            String beforeDateStr = DateFormatUtils.format(beforeDate, "yyyy-MM-dd");
            //处理分区路径
            String beforeFsPath = tableFsPath + "/" + partitionName + "=" + beforeDateStr ;
            //计算分区的数据量大小
            Long beforeDataSize = calcProduceData(fileSystem, beforeFsPath);
            if(beforeDataSize!=null){
                //该分区真实存在
                beforeTotalProduceData += beforeDataSize ;
                realBeforeDays ++ ;
            }
        }
        //平均产出数据量
        if(realBeforeDays != 0L){
            // 计算平均产出数据量
            Long avgProduceData = beforeTotalProduceData/realBeforeDays;
            //判断是否超过upper_limt
            if(currentDataSize>avgProduceData && (currentDataSize-avgProduceData)*100/avgProduceData>paramUpperLimit){
                //给分
                governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
                //问题项
                governanceAssessDetail.setAssessProblem("当前产出数据量超过前 " + paramDays + " 天平均产出数据量的 "+ paramUpperLimit + "%" );
                //备注
                governanceAssessDetail.setAssessComment("当前分区数据大小为 " + currentDataSize + ",  前 " + realBeforeDays + " 天平均数据量为 " + avgProduceData + " , 实际超过了 " + ((currentDataSize - avgProduceData) * 100  / avgProduceData) + "%");
            }
            //判断是否低于lower_limit
            if( currentDataSize < avgProduceData && currentDataSize  * 100  / avgProduceData < paramLowerLimit ){
                //给分
                governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
                //问题项
                governanceAssessDetail.setAssessProblem("当前产出数据量低于前 " + paramDays + " 天平均产出数据量的 "+ paramLowerLimit + "%" );
                //备注
                governanceAssessDetail.setAssessComment("当前分区数据大小为 " + currentDataSize + ",  前 " + realBeforeDays + " 天平均数据量为 " + avgProduceData + " , 实际低于了 " + (currentDataSize * 100  / avgProduceData) + "%");
            }

        }

    }


    /**
     * 计算给定分区的数据量大小
     *
     * 因为数仓中只有一级分区， 因此直接循环所有的文件统计大小即可，
     * 如果有多级分区，需要通过递归的方法统计大小
     * @param fs
     * @param currentFsPath
     * @return
     */
    private Long calcProduceData(FileSystem fs, String currentFsPath) throws IOException{
        //判断分区路径是否存在
        if(!fs.exists(new Path(currentFsPath))){
            return null;
        }
        FileStatus[] fileStatuses = fs.listStatus(new Path(currentFsPath));
        Long totalSize = 0L ;

        for (FileStatus fileStatus : fileStatuses) {
            totalSize += fileStatus.getLen();
        }
        return totalSize ;
    }
}
