package com.atguigu.springbootdemo.orderinfo.service.impl;

import com.atguigu.springbootdemo.orderinfo.bean.OrderInfo;
import com.atguigu.springbootdemo.orderinfo.mapper.OrderInfoMapper;
import com.atguigu.springbootdemo.orderinfo.service.OrderInfoService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-01
 */
@Service
@DS("gmall")
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

}
