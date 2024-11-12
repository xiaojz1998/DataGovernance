package com.atguigu.dga.governance.assessor.quality;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.governance.bean.TDsTaskInstance;
import com.atguigu.dga.governance.service.TDsTaskInstanceService;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.ctc.wstx.util.DataUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataUnit;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Component("TIME_LINESS")
public class TableProduceTimeAssessor extends Assessor {

    @Autowired
    TDsTaskInstanceService tDsTaskInstanceService;
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException {
        System.out.println("TableProduceTimeAssessor 查找问题");

        // 过滤掉ods表
        if(DgaConstant.DW_LEVEL_ODS.equals(assessParam.getTableMetaInfo().getTableMetaInfoExtra().getDwLevel())){
            return;
        }
        // 考评参数 一个是days，一个是percent
        String metricParamsJson = assessParam.getGovernanceMetric().getMetricParamsJson();
        JSONObject jsonObject = JSON.parseObject(metricParamsJson);
        Integer days = jsonObject.getInteger("days");
        Integer percent = jsonObject.getInteger("percent");

        // 被考评表和表名
        TableMetaInfo tableMetaInfo = assessParam.getTableMetaInfo();
        String name = tableMetaInfo.getSchemaName()+"."+tableMetaInfo.getTableName();

        // 取出当日任务实例
        TDsTaskInstance tDsTaskInstance = assessParam.getTDsTaskInstance();

        // 计算当日产出时效
        long currentProduceTime = tDsTaskInstance.getEndTime().getTime() - tDsTaskInstance.getStartTime().getTime();

        // 计算前days天的产出时效
        // 计算开始天
            // 得到当前日期
        String assessDateStr = assessParam.getAssessDate();
            // 转换成日期类型
        Date assessDate = DateUtils.parseDate(assessDateStr, "yyyy-MM-dd");
            // 计算开始日期
        Date startDate = DateUtils.addDays(assessDate, -days);
            // 转换成字符串类型
        String startDateStr = DateFormatUtils.format(startDate, "yyyy-MM-dd");

        // 得到过去所有天的最大实例
        List<TDsTaskInstance> beforeDaysTaskInstanceList = tDsTaskInstanceService.getBeforeDaysTaskInstanceList(name, startDateStr, assessDateStr);

        // 如果能查到数据
        if(beforeDaysTaskInstanceList.size()>0){
            Long totalProduceTime = 0L;
            for (TDsTaskInstance dsTaskInstance : beforeDaysTaskInstanceList) {
                totalProduceTime += dsTaskInstance.getEndTime().getTime() - dsTaskInstance.getStartTime().getTime();
            }
            // 计算平均时效
            long avgProduceTime = totalProduceTime/beforeDaysTaskInstanceList.size();
            // 考评即可
            if(currentProduceTime > avgProduceTime && (currentProduceTime-avgProduceTime)*100 /avgProduceTime > percent){
                // 给分
                governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
                // 在结果中设置问题项
                governanceAssessDetail.setAssessProblem("当日产出时效超过前 " + days + " 天平均产出时效的 " + percent + "%");
                // 备注
                governanceAssessDetail.setAssessComment("当日产出时效: " + currentProduceTime + " , 前 " + beforeDaysTaskInstanceList.size() + " 天平均产出时效: " + avgProduceTime );
            }
        }
    }
}
