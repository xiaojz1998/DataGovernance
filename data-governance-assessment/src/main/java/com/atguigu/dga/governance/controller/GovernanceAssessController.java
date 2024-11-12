package com.atguigu.dga.governance.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.governance.bean.GovernanceAssessGlobal;
import com.atguigu.dga.governance.bean.GovernanceAssessTecOwner;
import com.atguigu.dga.governance.service.GovernanceAssessDetailService;
import com.atguigu.dga.governance.service.GovernanceAssessGlobalService;
import com.atguigu.dga.governance.service.GovernanceAssessTecOwnerService;
import com.atguigu.dga.governance.service.MainAssessService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/governance")
public class GovernanceAssessController {

    @Autowired
    GovernanceAssessGlobalService governanceAssessGlobalService;
    @Autowired
    GovernanceAssessTecOwnerService governanceAssessTecOwnerService;

    @Autowired
    GovernanceAssessDetailService governanceAssessDetailService;


    @Autowired
    MainAssessService mainAssessService;
    /**
     * 重新评估接口
     *
     * 接口口径: /governance/assess/{date}
     *
     * 请求方式: POST
     *
     * 请求参数: {date}
     *
     * 返回结果: success
     */
    @PostMapping("/assess/{date}")
    public String assess(@PathVariable String date){
        try {
            mainAssessService.startGovernanceAssess(date);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "success" ;
    }

    /**
     * 问题列表接口
     * 接口路径: /governance/problemList/{governType}/{pageNo}/{pageSize}
     * 请求方式: GET
     * 请求参数:  {governType}
     *          {pageNo}
     *          {pageSize}
     * 返回结果: [
     *              {"assessComment":"","assessDate":"2023-05-01","assessProblem":"缺少技术OWNER","assessScore":0.00,"commentLog":"","createTime":1682954933000,"governanceType":"SPEC","governanceUrl":"/table_meta/table_meta/detail?tableId=1803","id":21947,"isAssessException":"0","metricId":1,"metricName":"是否有技术Owner","schemaName":"gmall","tableName":"ads_page_path"},
     *              {"assessComment":"","assessDate":"2023-05-01","assessProblem":"缺少业务OWNER","assessScore":0.00,"commentLog":"","createTime":1682954933000,"governanceType":"SPEC","governanceUrl":"/table_meta/table_meta/detail?tableId=1803","id":21948,"isAssessException":"0","metricId":2,"metricName":"是否有业务Owner","schemaName":"gmall","tableName":"ads_page_path"},
     *              {"assessComment":"","assessDate":"2023-05-01","assessProblem":"缺少技术OWNER","assessScore":0.00,"commentLog":"","createTime":1682954933000,"governanceType":"SPEC","governanceUrl":"/table_meta/table_meta/detail?tableId=1804","id":21964,"isAssessException":"0","metricId":1,"metricName":"是否有技术Owner","schemaName":"gmall","tableName":"ads_user_change"},
     *              {"assessComment":"","assessDate":"2023-05-01","assessProblem":"缺少业务OWNER","assessScore":0.00,"commentLog":"","createTime":1682954933000,"governanceType":"SPEC","governanceUrl":"/table_meta/table_meta/detail?tableId=1804","id":21965,"isAssessException":"0","metricId":2,"metricName":"是否有业务Owner","schemaName":"gmall","tableName":"ads_user_change"},
     *              {"assessComment":"","assessDate":"2023-05-01","assessProblem":"缺少技术OWNER","assessScore":0.00,"commentLog":"","createTime":1682954933000,"governanceType":"SPEC","governanceUrl":"/table_meta/table_meta/detail?tableId=1805","id":21981,"isAssessException":"0","metricId":1,"metricName":"是否有技术Owner","schemaName":"gmall","tableName":"ads_user_retention"}
     *              .....
     *         ]
     */

    @GetMapping("/problemList/{governType}/{pageNo}/{pageSize}")
    public String problemList(@PathVariable String governType,
                              @PathVariable Integer pageNo,
                              @PathVariable Integer pageSize){

        //计算开始行
        Integer start = (pageNo - 1) * pageSize ;

        List<GovernanceAssessDetail> governanceAssessDetailList = governanceAssessDetailService.list(
                new QueryWrapper<GovernanceAssessDetail>()
                        .eq("governance_type", governType)
                        .lt("assess_score", 10)
                        .inSql("assess_date", "select max(assess_date) from governance_assess_detail")
                        .last("limit " + start + " , " + pageSize)

        );

        return JSON.toJSONString( governanceAssessDetailList );

    }

    /**
     * 问题个数
     * 接口路径: /governance/problemNum
     * 请求方式: GET
     * 请求参数: 无
     * 返回结果: {"SPEC":1, "STORAGE":4,"CALC":12,"QUALITY":34,"SECURITY":12}
     */
    @GetMapping("/problemNum")
    public String problemNum(){

        // 从考评结果明细表中查询每个版块的问题个数
        List<Map<String, Object>> listMaps = governanceAssessDetailService.listMaps(
                new QueryWrapper<GovernanceAssessDetail>()
                        .select("governance_type", "count(*) as problem_num")
                        .lt("assess_score", 10)
                        .inSql("assess_date", "select max(assess_date) from governance_assess_detail")
                        .groupBy("governance_type")
        );

        // { "governance_type" : "SPEC" , "problem_num" : 100 }  => {"SPEC":100}
        String result = listMaps.stream().map(map -> "\"" + map.get("governance_type") + "\":" + map.get("problem_num"))
                .collect(Collectors.joining(","));
        // { a , b , c , d } => a,b,c,d

        return "{" + result + "}";
    }



    /**
     * 分组人员排行榜（治理考评榜）
     * 接口路径: /governance/rankList
     * 请求方式: GET
     * 请求参数: 无
     * 返回结果:
     *      [
     *          {"tecOwner":"zhang3" ,"score":99},
     *          {"tecOwner":"li4" ,"score":98},
     *          {"tecOwner": "wang5","score":97}
     *     ]
     */
    @GetMapping("/rankList")
    public String rankList(){
        List<Map<String, Object>> maps = governanceAssessTecOwnerService.listMaps(
                new QueryWrapper<GovernanceAssessTecOwner>()
                        .select("tec_owner as tecOwner", "score")
                        .inSql("assess_date", "select max(assess_date) from governance_assess_tec_owner")
                        .orderByDesc("score")
        );
        return JSON.toJSONString(maps);
    }


    /**
     * 全局分数接口
     * 接口路径: /governance/globalScore
     * 请求方式: GET
     * 请求参数: 无
     * 返回结果: { "assessDate":"2023-04-01" ,"sumScore":90, "scoreList":[20,40,34,55,66] }
     */
    @GetMapping("/globalScore")
    public  String globalScore(){
        GovernanceAssessGlobal governanceAssessGlobal = governanceAssessGlobalService.getOne(
                new QueryWrapper<GovernanceAssessGlobal>()
                        .orderByDesc("assess_date")
                        .last("limit 0,1")
        );
        List<BigDecimal> scores = Arrays.asList(
                governanceAssessGlobal.getScoreSpec(),
                governanceAssessGlobal.getScoreStorage(),
                governanceAssessGlobal.getScoreCalc(),
                governanceAssessGlobal.getScoreQuality(),
                governanceAssessGlobal.getScoreSecurity()
        );

        // 封装结果
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("assessDate" , governanceAssessGlobal.getAssessDate()) ;
        jsonObject.put("sumScore" , governanceAssessGlobal.getScore()) ;
        jsonObject.put("scoreList" , scores) ;

        return jsonObject.toJSONString() ;
    }
}
