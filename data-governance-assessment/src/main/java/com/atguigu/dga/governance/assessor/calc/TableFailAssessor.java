package com.atguigu.dga.governance.assessor.calc;

import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.governance.bean.TDsTaskInstance;
import com.atguigu.dga.governance.service.TDsTaskInstanceService;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 检测该表是否有运行失败的实例
 */
@Component("TASK_FAILED")
public class TableFailAssessor extends Assessor {

    @Autowired
    TDsTaskInstanceService tDsTaskInstanceService;
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) {
        System.out.println("TableFailAssessor 查找问题");

        // 查找当前表运行实例中是否有状态为6的，即为失败
        // 被考评表
        TableMetaInfo tableMetaInfo = assessParam.getTableMetaInfo();
        String name = tableMetaInfo.getSchemaName()+"."+tableMetaInfo.getTableName();

        List<TDsTaskInstance> failTDsTaskInstanceList = tDsTaskInstanceService.getFailTDsTaskInstanceList(name, assessParam.getAssessDate());

        if(failTDsTaskInstanceList.size()>0) {
            // 给分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            // 有报错
            governanceAssessDetail.setAssessProblem("当日任务有报错");
        }

    }
}
