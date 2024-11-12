package com.atguigu.dga.governance.assessor.calc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component("NO_ACCESS")
public class TableAccessAssessor extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException {
        System.out.println("TableAccessAssessor 查找问题..... ");

        // 使用表的最后访问时间 与 当前的考评日期进行差值计算，判断是否超过指标参数的建议值

        //取指标参数
        String metricParamsJson = assessParam.getGovernanceMetric().getMetricParamsJson();
        JSONObject paramJsonObject = JSON.parseObject(metricParamsJson);
        Integer paramDays = paramJsonObject.getInteger("days");

        // 取表最后的访问时间
        Date tableLastAccessTime = assessParam.getTableMetaInfo().getTableLastAccessTime();
        // 截断到天
        Date tableLastAssessDayDate = DateUtils.truncate(tableLastAccessTime, Calendar.DATE);
        // 处理成毫秒值
        long tableLastAccessDayDateMs = tableLastAssessDayDate.getTime();

        // 当日考评日期
        String assessDateStr = assessParam.getAssessDate();
        //转换成日期
        Date assessDate = DateUtils.parseDate(assessDateStr, "yyyy-MM-dd");
        //处理成毫秒值
        long assessDateMs = assessDate.getTime();

        // 取差值
        //正常情况下，考评日期一定是大于等于最后的访问时间
        long diffMs = Math.abs(assessDateMs - tableLastAccessDayDateMs);
        // 转换恒天
        long diffDay = TimeUnit.DAYS.convert(diffMs, TimeUnit.MILLISECONDS);

        if(diffDay > paramDays){
            // 给分
            governanceAssessDetail.setAssessScore(BigDecimal.valueOf(0));
            // 问题项
            governanceAssessDetail.setAssessProblem("长期未访问");
        }

        //考评备注
        governanceAssessDetail.setAssessComment("实际超过 " + diffDay + " 天没有被访问");
    }
}
