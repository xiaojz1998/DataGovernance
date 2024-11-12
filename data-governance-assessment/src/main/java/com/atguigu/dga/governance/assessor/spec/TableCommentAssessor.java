package com.atguigu.dga.governance.assessor.spec;


import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("TABLE_COMMENT")
public class TableCommentAssessor extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) {
        System.out.println("TableCommentAssessor 查找问题..... ");
        // 找到备注表
        String tableComment = assessParam.getTableMetaInfo().getTableComment();
        // 为空则赋值+问题项
        if(tableComment == null || tableComment.trim().isEmpty()){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("没有表备注");
        }
    }
}
