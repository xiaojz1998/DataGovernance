package com.atguigu.dga.governance.service;

import com.atguigu.dga.governance.bean.GovernanceAssessTable;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 表治理考评情况 服务类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-11
 */
public interface GovernanceAssessTableService extends IService<GovernanceAssessTable> {

    // 计算每张表的综合考评分
    public void calcTableScore(String assessDate);
}
