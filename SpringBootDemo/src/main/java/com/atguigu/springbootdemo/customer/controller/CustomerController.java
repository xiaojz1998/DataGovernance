package com.atguigu.springbootdemo.customer.controller;


import com.atguigu.springbootdemo.bean.Customer;
import com.atguigu.springbootdemo.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 *
 *
 * 注解告诉springboot框架这是个controller
 * @RestController: 将当前的类标识为控制层组件（Component）
 *                  Spring 会将该类对象进行管理，会自动创建该类的对象，并管理到springIOC容器中
 */
@RestController
public class CustomerController {

    @Autowired
    @Qualifier("csi2")
    CustomerService customerService;

    /**
     * 动态参数
     *
     * 通过 name 和 age 进行查询，但是每次传递的参数不确定，
     */

    @GetMapping("/selectcustomers")
    public List<Customer> selectCustomers(@RequestParam(value="name",required = false)String name,
                                          @RequestParam(value="age",required = false)Integer age){
        List<Customer> customers = customerService.selectCustomers(name, age);
        return customers;
    }

    /**
     * 修改
     *
     * 客户端请求: http://localhost:8080/updatecustomer
     *
     * 请求方式： POST
     *
     * 请求体参数: {"id":1005 , "name": "tianxiaoqi" , "age" : 16}
     */
    @PostMapping("/updatecustomer")
    public String updateCustomer(@RequestBody Customer customer){
        //customerService.updateCustomer(customer);
        customerService.updateById(customer);
        return "success";
    }

    /**
     * 删除
     *
     * 客户端请求: http://localhost:8080/deletecustomer/1001
     *
     * 请求方式: POST
     *
     */
    @PostMapping ("/deletecustomer/{id}")
    public String deleteCustomer(@PathVariable("id") long id){

        //customerService.deleteCustomer(id);
        customerService.removeById(id);
        return "success";
    }

    /**
     *  新增
     *  客户端请求：http://localhost:8080/addcustomer
     *  请求方式 post
     *  请求体参数: {"id":1005 , "name": "tianqi" , "age" : 26}
     */

    @PostMapping("/addcustomer")
    public String addCustomer(@RequestBody Customer customer){
        //customerService.addCustomer(customer);
        customerService.save(customer);
        return "success";
    }

    /**
     *  查询
     *  客户端请求：http://localhost:8080/getcustomer?id=1001
     *  请求方式 get
     */
    @GetMapping("/getcustomer")
    public Customer getCustomer (@RequestParam("id") long id){
        //Customer customer = customerService.getCustomer(id);
        Customer customer = customerService.getById(id);
        return customer;
    }

//-----------------------------------------------------------
// day1
//-----------------------------------------------------------
    /**
     * 请求处理方法
     *
     * 客户端请求：http://localhost:8080/hello
     * @RequestMapping: 将客户端的请求与请求处理方法进行映射
     */
    @RequestMapping(value = "/hello")
    public String hello(){
        System.out.println("客户端发送请求过来了。。。");
        return "success";
    }

    /**
     *  请求参数：键值对参数
     */
    @RequestMapping("/param1")
    public String param1(@RequestParam("name") String name,
                         @RequestParam("age")Integer age){
        System.out.println(" name: "+name+" age: "+age);
        return "success";
    }
    /**
     *  请求参数：路径中的参数
     */
    @RequestMapping("/param2/{name}/{age}")
    public String param2(@PathVariable("name") String name,
                         @PathVariable("age")Integer age){
        System.out.println(" name: "+name+" age: "+age);
        return "success";
    }
    /**
     *  请求参数：请求体参数
     */
    @RequestMapping("/param3")
    public String param3(@RequestBody Customer c){
        System.out.println("Customer: "+ c);
        return "success";
    }

}
