package com.atguigu.dga.governance.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.governance.bean.TDsTaskDefinition;
import com.atguigu.dga.governance.mapper.TDsTaskDefinitionMapper;
import com.atguigu.dga.governance.service.TDsTaskDefinitionService;
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
public class TDsTaskDefinitionServiceImpl extends ServiceImpl<TDsTaskDefinitionMapper, TDsTaskDefinition> implements TDsTaskDefinitionService {

    // 查找所有tds_task_definition表
    @Override
    public Map<String, TDsTaskDefinition> getTDsTaskDefinitionMap() {

        // 找到所有的表
        List<TDsTaskDefinition> tDsTaskDefinitions = this.list(
                new QueryWrapper<TDsTaskDefinition>()
                        .eq("task_type", "SHELL")
        );

        // 提取任务定义中的SQL
        tDsTaskDefinitions.forEach(this::extractTaskSql);
        // 构成map
        HashMap<String , TDsTaskDefinition> tDsTaskDefinitionHashMap = new HashMap<>();
        for (TDsTaskDefinition tDsTaskDefinition : tDsTaskDefinitions) {
            tDsTaskDefinitionHashMap.put(tDsTaskDefinition.getName(), tDsTaskDefinition);
        }

        return tDsTaskDefinitionHashMap;
    }

    private void extractTaskSql(TDsTaskDefinition tDsTaskDefinition) {
        // 从任务定义中提取任务参数
        String taskParams = tDsTaskDefinition.getTaskParams();
        // json 使用方法
        JSONObject taskParamJsonObj = JSON.parseObject(taskParams);

        // 从参数中提取rawScript 因为sql在rawScript中
        String rawScript = taskParamJsonObj.getString("rawScript");

        // 提取sql
        // 开始位置
        // 先找with位置
        int startIndex =-1;
        int withIndex = rawScript.indexOf("with");
        if(withIndex == -1){
            // 没找到with的情况
            startIndex = rawScript.indexOf("insert");
        }else{
            startIndex = withIndex;
        }
        // 判断刚开始位置是否找到，如果没找到则return
        if(startIndex == -1) return ;

        // 结束位置
        int endIndex = -1;
        // 从开始位置找，找到第一个分号的位置
        int fenhaoIndex = rawScript.indexOf(";", startIndex);
        if(fenhaoIndex == -1){
            // 没找到分号
            endIndex = rawScript.indexOf("\"",startIndex);
        }else{
            endIndex = fenhaoIndex;
        }

        // 截取sql
        String sql = rawScript.substring(startIndex, endIndex);

        // 赋值给任务定义
        tDsTaskDefinition.setTaskSql(sql);


    }
}
