package com.atguigu.springbootdemo.customer.service;

import com.atguigu.springbootdemo.bean.Customer;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CustomerService extends IService<Customer> {
    Customer getCustomer(long id);
    void addCustomer(Customer customer);
    void deleteCustomer(long id);
    void updateCustomer(Customer customer);
    List<Customer> selectCustomers(String name,Integer age);
}
