package com.atguigu.springbootdemo.customer.service.impl;

import com.atguigu.springbootdemo.bean.Customer;
import com.atguigu.springbootdemo.customer.mapper.CustomerMapper;
import com.atguigu.springbootdemo.customer.service.CustomerService;
import com.atguigu.springbootdemo.util.SqlUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("csi")
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper,Customer>  implements CustomerService {

    @Autowired
    CustomerMapper customerMapper;

    @Override
    public Customer getCustomer(long id) {
        Customer customer = customerMapper.selectCustomerById(id);
        return customer;
    }

    @Override
    public void addCustomer(Customer customer) {
        customerMapper.insertCustomer(customer);
    }

    @Override
    public void deleteCustomer(long id) {
        customerMapper.deleteCustomerById(id);
    }

    @Override
    public void updateCustomer(Customer customer) {
        customerMapper.updateCustomer(customer);
    }

    @Override
    public List<Customer> selectCustomers(String name, Integer age) {
        StringBuilder sqlBuilder = new StringBuilder("select id,name,age from customer where 1=1");
        if(name != null && !name.trim().isEmpty()) sqlBuilder.append(" and name  like '%" + SqlUtil.filterUnsafeSql(name)  + "%' ");
        if(age != null) sqlBuilder.append("and age = "+age);
        List<Customer> customers = customerMapper.selectCustomers(sqlBuilder.toString());
        return customers;
    }
}
