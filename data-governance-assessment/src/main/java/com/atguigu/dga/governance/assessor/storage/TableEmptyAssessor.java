package com.atguigu.dga.governance.assessor.storage;

import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("TABLE_EMPTY")
public class TableEmptyAssessor extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) {
        System.out.println("TableEmptyAssessor 查找问题");
        // 判断表中是否有数据
        Long tableSize = assessParam.getTableMetaInfo().getTableSize();
        if(tableSize == null || tableSize==0){
            // 空表
            // 赋值零分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            // 问题项目
            governanceAssessDetail.setAssessProblem("表没有数据");

        }
    }
}
