package com.atguigu.springbootdemo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("customer")
public class Customer {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    @TableField("name")
    private String name;
    private Integer age;
}
