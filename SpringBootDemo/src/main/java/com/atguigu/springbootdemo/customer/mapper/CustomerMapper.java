package com.atguigu.springbootdemo.customer.mapper;

import com.atguigu.springbootdemo.bean.Customer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;
/**
 * @author WEIYUNHUI
 * @date 2024/11/1 9:58
 *
 * @Mapper:  将当前类的实现类标识为数据层组件(Component)。
 *           MyBatis在运行时会通过动态代理技术为当前接口生成实现类。
 *           Spring会将该实现类的对象进行管理。管理到SpringIOC容器中。
 *
 *    给SQL中的参数占位符传参
 *
 *     #{} : 比较智能 ， 会识别参数的类型会自动补全单引 , 本身字符串值中的特殊符号会被替换、转义
 *
 *     ${} : 参数原值（一般程序中动态组合sql）
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
    @Select("select id,name,age from customer where id = #{id}")
    Customer selectCustomerById(long id);

    @Insert("insert into customer(id,name,age)values(#{c.id},#{c.name},#{c.age})")
    void insertCustomer(@Param("c") Customer customer);

    @Delete("delete from customer where id = #{id}")
    void deleteCustomerById(Long id );

    @Update("update customer set name = #{d.name}, age = #{d.age} where id = #{d.id}")
    void updateCustomer(@Param("d") Customer customer);

    @Select("${sql}")
    List<Customer> selectCustomers(String sql);
}
