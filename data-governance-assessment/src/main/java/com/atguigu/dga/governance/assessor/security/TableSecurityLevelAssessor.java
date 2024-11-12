package com.atguigu.dga.governance.assessor.security;

import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;

@Component("SECURITY_LEVEL")
public class TableSecurityLevelAssessor extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException {
        System.out.println("TableSecurityLevelAssessor 查找问题..... ");

        //判断是否设置安全级别
        String securityLevel = assessParam.getTableMetaInfo().getTableMetaInfoExtra().getSecurityLevel();
        if(securityLevel == null || DgaConstant.SECURITY_LEVEL_UNSET.equals( securityLevel )){
            //未设定安全级别
            //给分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            //问题项
            governanceAssessDetail.setAssessProblem("未设定安全级别");
        }
    }
}
