package com.atguigu.dga.governance.mapper;

import com.atguigu.dga.governance.bean.TDsTaskDefinition;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

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
public interface TDsTaskDefinitionMapper extends BaseMapper<TDsTaskDefinition> {

}
