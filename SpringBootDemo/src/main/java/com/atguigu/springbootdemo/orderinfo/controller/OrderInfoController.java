package com.atguigu.springbootdemo.orderinfo.controller;

import com.atguigu.springbootdemo.orderinfo.bean.OrderInfo;
import com.atguigu.springbootdemo.orderinfo.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-01
 */
@RestController
@RequestMapping("/orderinfo")
public class OrderInfoController {
    @Autowired
    OrderInfoService orderInfoService;

    @GetMapping("/get/{id}")
    public OrderInfo get(@PathVariable("id") Long id  ){
        OrderInfo orderInfo = orderInfoService.getById(id);

        return orderInfo ;
    }
}
