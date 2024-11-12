package com.atguigu.dga.governance.assessor.calc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoExtra;
import com.atguigu.dga.util.SqlUtil;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hive.ql.lib.Dispatcher;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  判断该sql是否存在简单计算
 */
@Component("TABLE_SIMPLE_PROCESS")
public class TableSimpleProcessAssessor extends Assessor {


    @Value("${data.warehouse.default.db}")
    private String dataWarehouseDefaultDb ;

    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException {
        System.out.println("TableSimpleProcessAssessor 查找问题..... ");

        // 排除ODS的表
        if(DgaConstant.DW_LEVEL_ODS.equals( assessParam.getTableMetaInfo().getTableMetaInfoExtra().getDwLevel())){
            return ;
        }

        // 取SQL
        String taskSql = assessParam.getTDsTaskDefinition().getTaskSql();

        //解析SQL
        SimpleProcessDispathcer simpleProcessDispathcer = new SimpleProcessDispathcer(dataWarehouseDefaultDb);
        SqlUtil.parseSql( taskSql , simpleProcessDispathcer );

        // 判断处理
        // 是否包含复杂计算
        if(simpleProcessDispathcer.sqlComplicateTokSet.size() > 0){
            governanceAssessDetail.setAssessComment("sql中包含的复杂计算: " + simpleProcessDispathcer.sqlComplicateTokSet);
            return ;
        }
        // 判断过滤字段是否是分区字段
        // 取得所有表名
        Set<String> sqlTableName = simpleProcessDispathcer.getSqlTableName();
        // 从参数中取得所有表，形成一个map好使用
        Map<String, TableMetaInfo> tableMetaInfoMap = assessParam.getTableMetaInfoList().stream()
                .collect(
                        Collectors.toMap(tableMetaInfo -> tableMetaInfo.getSchemaName() + "." + tableMetaInfo.getTableName()
                                , tableMetaInfo -> tableMetaInfo)
                );
        //将所有被查询表的分区字段收集到一起
        HashSet<String> partitonNameList = new HashSet<>();

        for (String name : sqlTableName) {
            TableMetaInfo tableMetaInfo = tableMetaInfoMap.get(name);
            String partitionColNameJson = tableMetaInfo.getPartitionColNameJson();
            List<JSONObject> jsonObjects = JSON.parseArray(partitionColNameJson, JSONObject.class);
            partitonNameList.addAll( jsonObjects.stream().map(jsonObj -> jsonObj.getString("name")).collect(Collectors.toList()));
        }

        //取差集
        Collection subtract = CollectionUtils.subtract(simpleProcessDispathcer.sqlFilterColumnName, partitonNameList);

        if(subtract.size() > 0 ){
            //给备注
            governanceAssessDetail.setAssessComment("sql中非分区字段: " + subtract + " , 被查询表的分区字段 : " + partitonNameList);
            return ;
        }

        //给分
        governanceAssessDetail.setAssessScore( BigDecimal.ZERO );
        //问题项
        governanceAssessDetail.setAssessProblem("sql为简单加工");
    }

    public static class SimpleProcessDispathcer implements Dispatcher{

        private String dataWarehouseDefaultDb ;
        public SimpleProcessDispathcer(String dataWarehouseDefaultDb) {
            this.dataWarehouseDefaultDb = dataWarehouseDefaultDb;
        }
        //定义一个集合， 哪些计算是复杂计算
        Set<Integer> complicateTokSet = Sets.newHashSet(
                HiveParser.TOK_JOIN ,   // join ,包含通过where连接的情况
                HiveParser.TOK_GROUPBY , // group by
                HiveParser.TOK_LEFTOUTERJOIN , // left join
                HiveParser.TOK_RIGHTOUTERJOIN , //right join
                HiveParser.TOK_FULLOUTERJOIN , // full join
                HiveParser.TOK_FUNCTION , // count(1)
                HiveParser.TOK_FUNCTIONDI, // count(distinct xx)
                HiveParser.TOK_FUNCTIONSTAR , // count(*)
                HiveParser.TOK_SELECTDI , // distinct
                HiveParser.TOK_UNIONALL // union
        ) ;

        //定义一个集合，where后面的条件操作符
        Set<Integer> operatorSet = Sets.newHashSet(
                HiveParser.EQUAL ,   // =
                HiveParser.GREATERTHAN , // >
                HiveParser.LESSTHAN, // <
                HiveParser.GREATERTHANOREQUALTO , // >=
                HiveParser.LESSTHANOREQUALTO , // <=
                HiveParser.NOTEQUAL , // <>
                HiveParser.KW_LIKE // like
        ) ;
        // 复杂计算存储
        @Getter
        Set<String> sqlComplicateTokSet = new HashSet<>();

        // 表名存储
        @Getter
        Set<String> sqlTableName = new HashSet<>();

        // 过滤列名存储
        @Getter
        Set<String> sqlFilterColumnName = new HashSet<>();


        @Override
        public Object dispatch(Node node, Stack<Node> stack, Object... objects) throws SemanticException {
            //类型转换
            ASTNode astNode = (ASTNode) node;

            // 记录SQL中的存在的复杂计算
            if(complicateTokSet.contains( astNode.getType() )){
                sqlComplicateTokSet.add( astNode.getText());
            }
            // 记录SQL中的被查询表
            // 判断是否为TOK_TABNAME节点  ，且祖先是 TOK_FROM
            if( astNode.getType() == HiveParser.TOK_TABNAME && astNode.getAncestor( HiveParser.TOK_FROM) != null  ){
                // 判断有几个孩子，如果1个，说明直接就是表名  ， 如果是2个， 说明是库名.表名
                if(astNode.getChildren().size() == 1 ){
                    String tableName = astNode.getChild(0).getText();
                    String fullName = dataWarehouseDefaultDb + "." + tableName ;
                    sqlTableName.add( fullName );
                }else{
                    String schemaName = astNode.getChild(0).getText();
                    String tableName = astNode.getChild(1).getText();
                    String fullName  = schemaName + "." + tableName ;
                    sqlTableName.add( fullName );
                }
            }
            // 记录SQL中的过滤字段
            // 判断是否为 operatorSet中定义的操作符， 且 祖先是 TOK_WHERE
            // 是. 点取第二个孩子
            // 是TOK_TABLE_OR_COL 取第一个孩子
            if(operatorSet.contains( astNode.getType() ) && astNode.getAncestor( HiveParser.TOK_WHERE ) != null ){
                if(astNode.getChild(0).getType() == HiveParser.DOT ){
                    String columnName = astNode.getChild(0).getChild(1).getText();
                    sqlFilterColumnName.add( columnName ) ;
                }else if(astNode.getChild(0).getType() == HiveParser.TOK_TABLE_OR_COL ){
                    String columnName = astNode.getChild(0).getChild(0).getText();
                    sqlFilterColumnName.add( columnName );
                }
            }



            return null;
        }
    }
}
