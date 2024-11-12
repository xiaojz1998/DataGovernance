package com.atguigu.dga.governance.assessor.spec;

import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 *  检测表是否有负责人
 */
@Component ("TEC_OWNER")
public class TableTecOwnerAssessor extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) {
        System.out.println("TableTecOwnerAssessor 查找问题中");


        // 首先获得负责人
        String tecOwnerUserName = assessParam.getTableMetaInfo().getTableMetaInfoExtra().getTecOwnerUserName();

        if (tecOwnerUserName == null || DgaConstant.TEC_OWNER_USER_NAME_UNSET.equals(tecOwnerUserName)){
            // 给分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            // 在结果中设置问题项
            governanceAssessDetail.setAssessProblem("表没有负责人");

            // 考评备注
            //governanceAssessDetail.setAssessComment("表没有负责人，请及时分配");
        }
    }
}
