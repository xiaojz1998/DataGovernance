package com.atguigu.dga.governance.mapper;

import com.atguigu.dga.governance.bean.GovernanceAssessGlobal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 治理总考评表 Mapper 接口
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-11
 */
@Mapper
public interface GovernanceAssessGlobalMapper extends BaseMapper<GovernanceAssessGlobal> {

    @Select(
            "SELECT \n" +
                    "    assess_date ,\n" +
                    "    AVG(score_spec_avg) * 10  AS score_spec , \n" +
                    "    AVG(score_storage_avg) * 10 AS score_storage  ,\n" +
                    "    AVG(score_calc_avg) * 10  AS  score_calc ,\n" +
                    "    AVG(score_quality_avg) * 10 AS  score_quality ,\n" +
                    "    AVG(score_security_avg) * 10  AS  score_security ,\n" +
                    "    AVG(score_on_type_weight) AS  score , \n" +
                    "    COUNT(*) table_num , \n" +
                    "    SUM(problem_num) AS problem_num , \n" +
                    "    NOW() AS create_time\n" +
                    "FROM governance_assess_table  \n" +
                    "WHERE assess_date = #{assessDate}"
    )
    GovernanceAssessGlobal selectGlobalScore(String assessDate);
}
