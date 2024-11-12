package com.atguigu.dga.governance.service;

import com.atguigu.dga.governance.bean.TDsTaskDefinition;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-06
 */

public interface TDsTaskDefinitionService extends IService<TDsTaskDefinition> {

    public Map<String, TDsTaskDefinition> getTDsTaskDefinitionMap();

}
