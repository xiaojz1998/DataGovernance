package com.atguigu.dga.governance.assessor.calc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.util.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component("TABLE_DATA_SKEW")
public class TableDataSkewAssessor extends Assessor{
    // 存储
    @Value("${spark.historyserver.url}")
    private String sparkHistoryServerUrl ;
    @Override
    public void checkProblems(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws ParseException {
        System.out.println("TableDataSkewAssessor 查找问题..... ");

        // 排除ODS的表
        if(DgaConstant.DW_LEVEL_ODS.equals( assessParam.getTableMetaInfo().getTableMetaInfoExtra().getDwLevel())){
            return ;
        }

        // 指标参数
        String metricParamsJson = assessParam.getGovernanceMetric().getMetricParamsJson();
        JSONObject jsonObject = JSON.parseObject(metricParamsJson);
        Integer percent = jsonObject.getInteger("percent");
        Long stage_dur_seconds = jsonObject.getLong("stage_dur_seconds");


        // yarn上任务id
        String yarnId = assessParam.getTDsTaskInstance().getAppLink();

        // 获取 "completed": true 的attemptId
        //http://hadoop102:18080/api/v1/applications/{yarnId}  获取完成的{attemptId}
        String completedAttemptId = getCompletedAttemptId(yarnId);

        // 获取所有的stageId
        //http://hadoop102:18080/api/v1/applications/{yarnId}/{attemptId}/stages
        List<String> completedStageId = getCompletedStageId(yarnId,completedAttemptId);

        // 获取每个stage的信息 ， 封装到自定义Stage对象中
        //http://hadoop102:18080/api/v1/applications/{yarnId}/{attemptId}/stages/{stageId}
        List<Stage> stageList = getStageList(yarnId , completedAttemptId , completedStageId);

        // 判断是否存在倾斜
        // 存在倾斜的stages集合
        List<Stage> stages = stageList.stream().filter(
                stage -> stage.getMaxTaskDuration() > stage_dur_seconds &&
                        stage.getTaskPercent() > percent
        ).collect(Collectors.toList());

        // 大于0则有倾斜
        if(stages.size()>0){
            // 给分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            // 问题项
            governanceAssessDetail.setAssessProblem("计算中存在数据倾斜");
        }
        governanceAssessDetail.setAssessComment("所有阶段: " + stageList + " ，存在倾斜的阶段: " + stages);


    }



    private List<Stage> getStageList(String yarnId, String completedAttemptId, List<String> stageIdList) {
        ArrayList<Stage> stages = new ArrayList<>();

        for (String stageId : stageIdList) {
            String url = sparkHistoryServerUrl + yarnId + "/" + completedAttemptId + "/stages/" + stageId ;
            String result = HttpUtil.get(url);
            List<JSONObject> stageList = JSON.parseArray(result, JSONObject.class);
            //过滤出 status: COMPLETE 的stage
            JSONObject completedStageJsonObj = stageList.stream()
                    .filter(jsonObj -> "COMPLETE".equals(jsonObj.getString("status")))
                    .collect(Collectors.toList())
                    .get(0);

            //取所有的task
            JSONObject taskJsonObj = completedStageJsonObj.getJSONObject("tasks");

            Long maxTaskDuration = Long.MIN_VALUE ;
            Long totalTaskDuration = 0L ;
            Long taskCount = 0L ;

            for (String key : taskJsonObj.keySet()) {
                JSONObject task = taskJsonObj.getJSONObject(key);

                if("SUCCESS".equals( task.getString("status"))){
                    maxTaskDuration = Math.max( maxTaskDuration , task.getLong( "duration" ));
                    totalTaskDuration += task.getLong( "duration" ) ;
                    taskCount ++ ;
                }
            }

            //封装Stage对象

            Long avgTaskDuration = taskCount == 1L ? maxTaskDuration : (totalTaskDuration - maxTaskDuration) / (taskCount - 1) ;
            Long taskPercent  = taskCount == 1L ? 0L : (maxTaskDuration - avgTaskDuration) * 100  / avgTaskDuration;

            Stage stage = Stage.builder()
                    .stageId(stageId)
                    .maxTaskDuration(maxTaskDuration)
                    .avgTaskDuration(avgTaskDuration)
                    .taskPercent(taskPercent)
                    .build();

            stages.add(stage);
        }
        return stages;

    }
    private List<String> getCompletedStageId(String yarnId, String completedAttemptId) {
        String url = sparkHistoryServerUrl + yarnId + "/" + completedAttemptId + "/stages" ;
        String result = HttpUtil.get(url);
        List<JSONObject> stageJsonObjList = JSON.parseArray(result, JSONObject.class);
        List<String> stageIdList = stageJsonObjList.stream()
                .filter(jsonObj -> "COMPLETE".equals(jsonObj.getString("status")))
                .map(jsonObj -> jsonObj.getString("stageId"))
                .collect(Collectors.toList());

        return stageIdList;
    }

    // 因为一个应用可以尝试多次
    // 获取completed: true的attemptId
    private String getCompletedAttemptId(String yarnId) {
        String url = sparkHistoryServerUrl + yarnId ;
        String result = HttpUtil.get(url);
        JSONObject jsonObj = JSON.parseObject(result);
        //取出 attempts
        JSONArray jsonArray = jsonObj.getJSONArray("attempts");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject attemptJsonObj = jsonArray.getJSONObject(i);
            if(attemptJsonObj.getBoolean("completed")){
                return attemptJsonObj.getString("attemptId");
            }
        }
        return null;
    }


    // 存储一个stage的相关信息，stage对象
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Stage{
        private String stageId ;
        private Long maxTaskDuration ;
        private Long avgTaskDuration ;
        private Long taskPercent ;
    }
}
