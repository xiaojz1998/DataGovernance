package com.atguigu.dga.governance.assessor.calc;

import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;

import com.atguigu.dga.util.SqlUtil;
import lombok.Getter;
import org.apache.hadoop.hive.ql.lib.Dispatcher;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Stack;

@Component("TABLE_SELECT_ALL")
public class TableSelectAllAssessor extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException {
        System.out.println("TableSelectAllAssessor 查找问题..... ");

        // 排除ODS的表
        if(DgaConstant.DW_LEVEL_ODS.equals(assessParam.getTableMetaInfo().getTableMetaInfoExtra().getDwLevel())) return;

        // 取得sql
        String taskSql = assessParam.getTDsTaskDefinition().getTaskSql();

        // 解析sql
        SelectAllDispatcher selectAllDispatcher = new SelectAllDispatcher();
        SqlUtil.parseSql(taskSql, selectAllDispatcher);

        // 判断是否包含select all
        if(selectAllDispatcher.isContainsSelectAll()){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("SQL包含select *");
        }

    }

    public static class SelectAllDispatcher implements Dispatcher{

        @Getter
        private boolean isContainsSelectAll = false ;
        /**
         *  遍历到每一个节点都会使用一次该方法
         * @param node 当前被遍历的节点
         * @param stack
         * @param objects
         * @return
         * @throws SemanticException
         */
        @Override
        public Object dispatch(Node node, Stack<Node> stack, Object... objects) throws SemanticException {
            // 强制转换成ASTNode

            ASTNode astNode =(ASTNode) node;

            if(astNode.getType() == HiveParser.TOK_ALLCOLREF){
                isContainsSelectAll = true ;
            }
            return null;
        }
    }


}
