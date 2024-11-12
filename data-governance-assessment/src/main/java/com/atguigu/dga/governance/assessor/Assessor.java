package com.atguigu.dga.governance.assessor;

import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;

public abstract class Assessor {
    public final GovernanceAssessDetail doAssess(AssessParam assessParam){
        //1. 初始化返回的考评结果
        //   用建造者模式
        GovernanceAssessDetail governanceAssessDetail = GovernanceAssessDetail.builder()
                .assessDate(assessParam.getAssessDate())
                .tableName(assessParam.getTableMetaInfo().getTableName())
                .schemaName(assessParam.getTableMetaInfo().getSchemaName())
                .metricId(assessParam.getGovernanceMetric().getId().toString())
                .metricName(assessParam.getGovernanceMetric().getMetricName())
                .governanceType(assessParam.getGovernanceMetric().getGovernanceType())
                .tecOwner(assessParam.getTableMetaInfo().getTableMetaInfoExtra().getTecOwnerUserName())
                .assessScore(BigDecimal.TEN)    // 默认满分，查找问题中，再按照实际情况重新给分
                .createTime(new Date())
                .build();

        //2. 查找问题
        try {
            checkProblems(governanceAssessDetail,assessParam);
        }catch (Exception e){
            // 首先先给分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            // 设置错误标记
            governanceAssessDetail.setIsAssessException("1");
            // 把错误信息写入结果
            // 下面如何用printWriter把异常信息写入到stringWriter中
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            governanceAssessDetail.setAssessExceptionMsg(stringWriter.toString().substring(0,Math.min(stringWriter.toString().length(),2000)));
        }
        //3. 处理治理链接
        //   eg /table_meta/table_meta/detail?tableId={tableId}
        String governanceUrl = assessParam.getGovernanceMetric().getGovernanceUrl();
        if(governanceUrl!=null && governanceAssessDetail.getAssessScore().intValue() < 10){
            // 将{table_id} 替换成考评表的id
            governanceUrl.replace("{tableid}",assessParam.getTableMetaInfo().getId()+"");
            governanceAssessDetail.setGovernanceUrl(governanceUrl);
        }

        return governanceAssessDetail;
    }

    public abstract void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException, URISyntaxException, IOException, InterruptedException;
}
