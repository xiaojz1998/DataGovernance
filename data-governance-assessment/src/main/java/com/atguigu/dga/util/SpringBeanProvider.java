package com.atguigu.dga.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

// 容器通用注解 service也是component
// 要实现ApplicationContextAware 接口
// 框架自动调用此方法传回来容器对象
@Component
public class SpringBeanProvider implements ApplicationContextAware {
    private ApplicationContext applicationContext ;
    /**
     * 通过指定的 bean 的名字，到容器中取对应的bean对象
     */
    public <T> T getBean(String beanName , Class<T> classType){
        // 用 applicationContext.getBean 来获得对象
        T bean = applicationContext.getBean(beanName, classType);
        return bean ;
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext ;
    }
}
