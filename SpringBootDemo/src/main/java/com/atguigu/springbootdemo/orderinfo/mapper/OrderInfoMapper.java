package com.atguigu.springbootdemo.orderinfo.mapper;

import com.atguigu.springbootdemo.orderinfo.bean.OrderInfo;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-01
 */
@Mapper
@DS("gmall")
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

}
