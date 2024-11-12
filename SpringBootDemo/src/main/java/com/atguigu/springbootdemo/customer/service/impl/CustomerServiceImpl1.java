package com.atguigu.springbootdemo.customer.service.impl;

import com.atguigu.springbootdemo.bean.Customer;
import com.atguigu.springbootdemo.customer.mapper.CustomerMapper;
import com.atguigu.springbootdemo.customer.service.CustomerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("csi1")
public class CustomerServiceImpl1 extends ServiceImpl<CustomerMapper,Customer>  implements CustomerService {
    @Autowired
    CustomerMapper customerMapper;
    @Override
    public Customer getCustomer(long id) {
        Customer customer = customerMapper.selectById(id);
        return customer;
    }

    @Override
    public void addCustomer(Customer customer) {
        customerMapper.insert(customer);
    }

    @Override
    public void deleteCustomer(long id) {
        customerMapper.deleteById(id);
    }

    @Override
    public void updateCustomer(Customer customer) {
        customerMapper.updateById(customer);
    }

    @Override
    public List<Customer> selectCustomers(String name, Integer age) {
        QueryWrapper<Customer> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(name!=null && !name.trim().isEmpty(),"name",name)
                .eq(age!=null,"age",age);
        List<Customer> customers = customerMapper.selectList(queryWrapper);
        return customers;
    }
}
