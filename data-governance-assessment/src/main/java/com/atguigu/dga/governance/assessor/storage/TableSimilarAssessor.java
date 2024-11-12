package com.atguigu.dga.governance.assessor.storage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component("TABLE_SIMILAR")
public class TableSimilarAssessor extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException {
        System.out.println("TableSimilarAssessor 查找问题..... ");

        // 取目标参数
        String metricParamsJson = assessParam.getGovernanceMetric().getMetricParamsJson();
        JSONObject paramJsonObject = JSON.parseObject(metricParamsJson);
        Integer percent = paramJsonObject.getInteger("percent");

        // 获取当前表
        TableMetaInfo tableMetaInfo = assessParam.getTableMetaInfo();

        // 当前表的字段信息
        String colNameJson = tableMetaInfo.getColNameJson();
        List<JSONObject> jsonObjects = JSON.parseArray(colNameJson, JSONObject.class);
        // 获取列名
        List<String> name = jsonObjects.stream().map(jsonObject -> jsonObject.getString("name")).collect(Collectors.toList());

        // 所有表
        List<TableMetaInfo> tableMetaInfoList = assessParam.getTableMetaInfoList();
        // 获取、对比同层次表
        List<TableMetaInfo> sameDwLevelTableMetaInfoList = tableMetaInfoList.stream().filter(
                j -> j.getTableMetaInfoExtra().getDwLevel().equals(tableMetaInfo.getTableMetaInfoExtra().getDwLevel())
                        && !j.getTableName().equals(tableMetaInfo.getTableName())
        ).collect(Collectors.toList());

        //判断是否相似
        ArrayList<String> sameTableNameList = new ArrayList<>();
        for (TableMetaInfo o : sameDwLevelTableMetaInfoList) {
            // 其他表的字段信息
            String ocolNameJson = o.getColNameJson();
            List<JSONObject> ojsonObjects = JSON.parseArray(ocolNameJson, JSONObject.class);

            // 获取列名
            List<String> oname = ojsonObjects.stream().map(jsonObject -> jsonObject.getString("name")).collect(Collectors.toList());
            //两张表的相同列
            Collection sameColNameList = CollectionUtils.intersection(name, oname);

            //判断是否超过指标参数建议值
            if( sameColNameList.size() * 100  / name.size() > percent){
                //记录相似表
                sameTableNameList.add( o.getTableName() ) ;
            }
        }
        if(sameTableNameList.size()>0){
            //给分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            //问题项
            governanceAssessDetail.setAssessProblem("存在相似表");
            //备注
            governanceAssessDetail.setAssessComment("相似表为: " + sameTableNameList );
        }


    }
}
