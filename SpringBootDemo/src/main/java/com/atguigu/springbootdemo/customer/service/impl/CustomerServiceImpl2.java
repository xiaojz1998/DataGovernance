package com.atguigu.springbootdemo.customer.service.impl;


import com.atguigu.springbootdemo.bean.Customer;
import com.atguigu.springbootdemo.customer.mapper.CustomerMapper;
import com.atguigu.springbootdemo.customer.service.CustomerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("csi2")
public class CustomerServiceImpl2 extends ServiceImpl<CustomerMapper,Customer> implements CustomerService {
    @Override
    public Customer getCustomer(long id) {
        return null;
    }

    @Override
    public void addCustomer(Customer customer) {

    }

    @Override
    public void deleteCustomer(long id) {

    }

    @Override
    public void updateCustomer(Customer customer) {

    }

    @Override
    public List<Customer> selectCustomers(String name, Integer age) {
        return null;
    }
}
