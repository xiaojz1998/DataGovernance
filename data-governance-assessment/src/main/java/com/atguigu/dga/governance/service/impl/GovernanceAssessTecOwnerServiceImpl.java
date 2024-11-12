package com.atguigu.dga.governance.service.impl;

import com.atguigu.dga.governance.bean.GovernanceAssessTecOwner;
import com.atguigu.dga.governance.mapper.GovernanceAssessTecOwnerMapper;
import com.atguigu.dga.governance.service.GovernanceAssessTecOwnerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 技术负责人治理考评表 服务实现类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-11
 */
@Service
public class GovernanceAssessTecOwnerServiceImpl extends ServiceImpl<GovernanceAssessTecOwnerMapper, GovernanceAssessTecOwner> implements GovernanceAssessTecOwnerService {

    @Override
    public void calcTecOwnerScore(String assessDate) {
        // 清理考评日期当天的数据
        remove(
                new QueryWrapper<GovernanceAssessTecOwner>()
                        .eq("assess_date" , assessDate)
        );

        List<GovernanceAssessTecOwner> governanceAssessTecOwners = getBaseMapper().selectTecOwnerScore(assessDate);

        saveBatch(governanceAssessTecOwners);
    }
}
