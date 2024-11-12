package com.atguigu.dga.governance.service;

import com.atguigu.dga.governance.bean.TDsTaskInstance;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-06
 */

public interface TDsTaskInstanceService extends IService<TDsTaskInstance> {


    List<TDsTaskInstance> getBeforeDaysTaskInstanceList(String name,String startDate,String assessDate);
    public Map<String ,TDsTaskInstance> getTDsTaskInstanceMapByIn(String assessDate);
    public Map<String, TDsTaskInstance> getTDsTaskInstanceMapByJoin(String assessDate);

    public List<TDsTaskInstance> getFailTDsTaskInstanceList (String name, String assessDate);
}
