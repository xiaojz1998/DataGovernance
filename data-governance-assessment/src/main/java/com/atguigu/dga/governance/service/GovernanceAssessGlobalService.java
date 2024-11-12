package com.atguigu.dga.governance.service;

import com.atguigu.dga.governance.bean.GovernanceAssessGlobal;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 治理总考评表 服务类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-11
 */
public interface GovernanceAssessGlobalService extends IService<GovernanceAssessGlobal> {

    void calcGlobalScore(String assessDate);
}
