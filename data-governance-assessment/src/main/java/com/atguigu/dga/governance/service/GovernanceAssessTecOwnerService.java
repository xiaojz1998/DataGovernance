package com.atguigu.dga.governance.service;

import com.atguigu.dga.governance.bean.GovernanceAssessTecOwner;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 技术负责人治理考评表 服务类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-11
 */
public interface GovernanceAssessTecOwnerService extends IService<GovernanceAssessTecOwner> {
    void calcTecOwnerScore(String assessDate) ;
}
