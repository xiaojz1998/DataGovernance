package com.atguigu.dga.governance.service.impl;

import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.governance.bean.GovernanceAssessTable;
import com.atguigu.dga.governance.bean.GovernanceType;
import com.atguigu.dga.governance.mapper.GovernanceAssessTableMapper;
import com.atguigu.dga.governance.service.GovernanceAssessTableService;
import com.atguigu.dga.governance.service.GovernanceTypeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 表治理考评情况 服务实现类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-11
 */
@Service
public class GovernanceAssessTableServiceImpl extends ServiceImpl<GovernanceAssessTableMapper, GovernanceAssessTable> implements GovernanceAssessTableService {

    @Autowired
    GovernanceTypeService governanceTypeService;

    @Override
    public void calcTableScore(String assessDate) {
        // 清理考评日期当天的数据
        remove(
                new QueryWrapper<GovernanceAssessTable>()
                        .eq("assess_date" , assessDate)
        );

        // 获得每张表的初步综合计算结果，未加权
        List<GovernanceAssessTable> governanceAssessTables = getBaseMapper().selectTableScore(assessDate);

        //指标版块权重
        // 从数据库中获得权重数据
        List<GovernanceType> governanceTypes = governanceTypeService.list();
        Map<String, BigDecimal> typeWeightMap = governanceTypes.stream().collect(
                Collectors.toMap(
                        g -> g.getTypeCode(),
                        g -> g.getTypeWeight()
                )
        );
        governanceAssessTables.forEach(
                g ->{
                    // spec
                    BigDecimal specScore = getScore(g.getScoreSpecAvg(), typeWeightMap.get(DgaConstant.GOVERNANCE_TYPE_SPEC));
                    // storage
                    BigDecimal storageScore = getScore(g.getScoreStorageAvg(), typeWeightMap.get(DgaConstant.GOVERNANCE_TYPE_STORAGE));
                    // calc
                    BigDecimal calcScore = getScore(g.getScoreCalcAvg(), typeWeightMap.get(DgaConstant.GOVERNANCE_TYPE_CALC));
                    // quality
                    BigDecimal qualityScore = getScore(g.getScoreQualityAvg(), typeWeightMap.get(DgaConstant.GOVERNANCE_TYPE_QUALITY));
                    // security
                    BigDecimal securityScore = getScore(g.getScoreSecurityAvg(), typeWeightMap.get(DgaConstant.GOVERNANCE_TYPE_SECURITY));

                    // 求和
                    BigDecimal totalScore = specScore.add(storageScore).add(calcScore).add(qualityScore).add(securityScore);

                    g.setScoreOnTypeWeight(totalScore);
                }
        );
        saveBatch(governanceAssessTables);


    }

    private BigDecimal getScore(BigDecimal avgScore, BigDecimal typeWeight){
        BigDecimal weightScore = avgScore.multiply(typeWeight).multiply(BigDecimal.TEN);
        return weightScore;
    }
}
