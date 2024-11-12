package com.atguigu.dga.governance.assessor.storage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

@Component("LIFECYCLE")
public class TableLifecycleAssessor extends Assessor {

    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException {
        System.out.println("TableLifecycleAssessor 查找问题..... ");

        // 获得指标参数
        String metricParamsJson = assessParam.getGovernanceMetric().getMetricParamsJson();
        JSONObject paramJsonObject = JSON.parseObject(metricParamsJson);
        Integer days = paramJsonObject.getInteger("days");

        //表的生命周期类型
        String lifecycleType = assessParam.getTableMetaInfo().getTableMetaInfoExtra().getLifecycleType();

        if(lifecycleType == null || DgaConstant.LIFECYCLE_TYPE_UNSET.equals( lifecycleType )){
            //未设定生命周期类型
            //给分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            //问题项
            governanceAssessDetail.setAssessProblem("未设定生命周期类型");
            return ;
        }
        //生命周期类型为日分区
        if(DgaConstant.LIFECYCLE_TYPE_DAY.equals( lifecycleType)){
            // 判断表是否设置过分区字段
            String colNameJson = assessParam.getTableMetaInfo().getColNameJson();
            List<JSONObject> partitionList = JSON.parseArray(colNameJson, JSONObject.class);

            if(partitionList == null || partitionList.size() == 0){
                //无分区信息
                //给分
                governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
                //问题项
                governanceAssessDetail.setAssessProblem("日分区表无分区信息");
                return ;
            }
            Long lifecycleDays = assessParam.getTableMetaInfo().getTableMetaInfoExtra().getLifecycleDays();
            if(lifecycleDays == -1L){
                //没有设定生命周期天数
                //给分
                governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
                //问题项
                governanceAssessDetail.setAssessProblem("日分区表没有设定生命周期天数");
                return;
            }
            //判断设定的生命周期天数是否超过指标参数建议值
            if(lifecycleDays > days ){
                //给分
                governanceAssessDetail.setAssessScore( BigDecimal.valueOf( days * 10  / lifecycleDays) );
                //问题项
                governanceAssessDetail.setAssessProblem("日分区表设定的生命周期天数超过指标参数建议值");
                //备注
                governanceAssessDetail.setAssessComment("建议天数: " + days + " , 设置天数: " + lifecycleDays );
            }

        }



    }
}
