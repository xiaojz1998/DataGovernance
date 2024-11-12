package com.atguigu.dga.governance.mapper;

import com.atguigu.dga.governance.bean.GovernanceAssessTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 表治理考评情况 Mapper 接口
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-11
 */
@Mapper
public interface GovernanceAssessTableMapper extends BaseMapper<GovernanceAssessTable> {

    @Select(
            "SELECT \n" +
                    "    assess_date , \n" +
                    "    TABLE_NAME ,\n" +
                    "    SCHEMA_NAME ,\n" +
                    "    tec_owner ,\n" +
                    "    AVG(IF(governance_type = 'SPEC' , assess_score , NULL )) AS score_spec_avg , \n" +
                    "    AVG(IF(governance_type = 'STORAGE' , assess_score , NULL )) AS score_storage_avg ,\n" +
                    "    AVG(IF(governance_type = 'CALC' , assess_score , NULL )) AS score_calc_avg ,\n" +
                    "    AVG(IF(governance_type = 'QUALITY' , assess_score , NULL )) AS score_quality_avg ,\n" +
                    "    AVG(IF(governance_type = 'SECURITY' , assess_score , NULL )) AS score_security_avg ,\n" +
                    "    -- score_on_type_weight   代码中计算\n" +
                    "    SUM(IF(assess_score < 10 , 1 , 0 )) AS problem_num , \n" +
                    "    NOW() AS create_time    \n" +
                    "FROM governance_assess_detail   \n" +
                    "WHERE assess_date = #{assessDate}  \n" +
                    "GROUP BY TABLE_NAME , SCHEMA_NAME ,  assess_date , tec_owner \n"
    )
    List<GovernanceAssessTable> selectTableScore(String assessDate );
}
