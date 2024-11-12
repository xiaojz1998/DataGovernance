package com.atguigu.dga.governance.service.impl;

import com.atguigu.dga.governance.bean.TDsTaskInstance;
import com.atguigu.dga.governance.mapper.TDsTaskInstanceMapper;
import com.atguigu.dga.governance.service.TDsTaskInstanceService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-06
 */
@Service
@DS("dolphinscheduler")
public class TDsTaskInstanceServiceImpl extends ServiceImpl<TDsTaskInstanceMapper, TDsTaskInstance> implements TDsTaskInstanceService {


    @Override
    public List<TDsTaskInstance> getBeforeDaysTaskInstanceList(String name, String startDate, String assessDate) {
        List<TDsTaskInstance> tDsTaskInstances = this.list(
                new QueryWrapper<TDsTaskInstance>()
                        .inSql("id", "select\n" +
                                "max(id) max_id\n" +
                                "from t_ds_task_instance\n" +
                                "where DATE_FORMAT(start_time,'%Y-%m-%d') >= '" + startDate + "'\n" +
                                "  and DATE_FORMAT(end_time,'%Y-%m-%d') <='" + assessDate + "'\n" +
                                "and name = '" + name + "'\n" +
                                "and state = 7\n" +
                                "group by name,DATE_FORMAT(start_time,'%Y-%m-%d')")
        );

        return tDsTaskInstances;
    }

    @Override
    public Map<String, TDsTaskInstance> getTDsTaskInstanceMapByIn(String assessDate) {
        List<TDsTaskInstance> tDsTaskInstances = this.list(
                new QueryWrapper<TDsTaskInstance>()
                        .inSql("id", "SELECT \n" +
                                "MAX(id) max_id    \n" +
                                "FROM t_ds_task_instance \n" +
                                "WHERE  DATE_FORMAT( start_time, '%Y-%m-%d') = '" + assessDate + "' \n" +
                                "AND state = '7' \n" +
                                "GROUP BY NAME")
        );

        Map<String, TDsTaskInstance> tDsTaskInstanceHashMap = new HashMap<>();

        tDsTaskInstances.forEach(tDsTaskInstance -> tDsTaskInstanceHashMap.put(tDsTaskInstance.getName(), tDsTaskInstance));


        return tDsTaskInstanceHashMap;
    }

    @Override
    public Map<String, TDsTaskInstance> getTDsTaskInstanceMapByJoin(String assessDate) {
        // 找到所有的instance 因为有多个instance实例，所以找最晚的那一个
        List<TDsTaskInstance> tDsTaskInstances = getBaseMapper().selectTdsTaskInstanceList(assessDate);
        // 创建结果返回map
        Map<String , TDsTaskInstance> tDsTaskInstanceHashMap = new HashMap<>();
        // 为map赋值
        tDsTaskInstances.forEach(tDsTaskInstance -> tDsTaskInstanceHashMap.put(tDsTaskInstance.getName(), tDsTaskInstance));
        // 返回结果
        return tDsTaskInstanceHashMap;
    }

    @Override
    public List<TDsTaskInstance> getFailTDsTaskInstanceList(String name, String assessDate) {
        final List<TDsTaskInstance> tDsTaskInstances = this.list(
                new QueryWrapper<TDsTaskInstance>()
                        .eq("name", name)
                        .eq("DATE_FORMAT(start_time,'%Y-%m-%d')", assessDate)
                        .eq("state", 6)
        );
        return tDsTaskInstances;
    }
}
