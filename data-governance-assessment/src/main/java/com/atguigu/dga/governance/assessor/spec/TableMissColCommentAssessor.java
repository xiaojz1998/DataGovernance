package com.atguigu.dga.governance.assessor.spec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@Component("FIELD_COMMENT")
public class TableMissColCommentAssessor extends Assessor {
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException {
        System.out.println("TableMissColCommentAssessor 查找问题..... ");

        // 获取所有字段信息
        String colNameJson = assessParam.getTableMetaInfo().getColNameJson();
        // 转换成json数组对象
        List<JSONObject> colList = JSON.parseArray(colNameJson, JSONObject.class);

        // 找出没有设置备注的字段
        List<String> missColList = colList.stream()
                .filter(jsonObject -> jsonObject.getString("comment") == null || jsonObject.getString("comment").trim().isEmpty())
                .map(jsonObject -> jsonObject.getString("name"))
                .collect(Collectors.toList());

        // 判断
        if(missColList.size() > 0 ){
            //给分
            governanceAssessDetail.setAssessScore( BigDecimal.valueOf( ( colList.size() - missColList.size()) * 10L / colList.size() )  );
            //问题项
            governanceAssessDetail.setAssessProblem("有字段缺失备注");
            //给备注
            governanceAssessDetail.setAssessComment("缺失备注的字段为: " + missColList );
        }
    }
}
