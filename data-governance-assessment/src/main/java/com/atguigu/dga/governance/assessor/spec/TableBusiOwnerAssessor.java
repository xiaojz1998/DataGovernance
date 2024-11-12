package com.atguigu.dga.governance.assessor.spec;

import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("BUSI_OWNER")
public class TableBusiOwnerAssessor extends Assessor {

    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) {
        System.out.println("TableBusiOwnerAssessor 查找问题..... ");

        String busiOwnerUserName = assessParam.getTableMetaInfo().getTableMetaInfoExtra().getBusiOwnerUserName();
        if(busiOwnerUserName == null || DgaConstant.BUSI_OWNER_USER_NAME_UNSET.equals(busiOwnerUserName)){
            // 赋值0分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            // 设置问题项
            governanceAssessDetail.setAssessProblem("表没有业务负责人");
        }

    }
}
