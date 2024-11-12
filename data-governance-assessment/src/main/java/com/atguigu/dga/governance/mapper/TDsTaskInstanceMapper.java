package com.atguigu.dga.governance.mapper;

import com.atguigu.dga.governance.bean.TDsTaskInstance;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-06
 */
@Mapper
@DS("dolphinscheduler")
public interface TDsTaskInstanceMapper extends BaseMapper<TDsTaskInstance> {

    @Select(
            "select\n" +
                    "    *\n" +
                    "from t_ds_task_instance\n" +
                    "join (select\n" +
                    "    max(id) max_id\n" +
                    "from t_ds_task_instance\n" +
                    "where date_format(start_time,'%Y-%m-%d') = #{assessDate}\n" +
                    "and state = '7'\n" +
                    "group by name) t1\n" +
                    "on id = t1.max_id"
    )
    List<TDsTaskInstance> selectTdsTaskInstanceList(String assessDate);
}
