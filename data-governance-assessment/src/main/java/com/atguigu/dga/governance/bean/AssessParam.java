package com.atguigu.dga.governance.bean;

import com.atguigu.dga.meta.bean.TableMetaInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * 用于传递考评信息，包括这种参数，用对象是因为参数是变化的，用对象可以记录各种考评信息，没有的设置null
 * 1. 构建对象，用set方法
 * 2. 用构造方法
 * 3. 加build注解 用建造者模式
 *      3.1 看编译后的类，用的静态的内部类 内部类用属性名的方法来赋值
 *      3.2 Builder() 用来构建内部类，赋值方法都是内部类的方法
 *      3.3 build 用来建造最终类
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder //建造者模式
public class AssessParam {

    private String assessDate ;                     // 考评日期

    private TableMetaInfo tableMetaInfo ;           // 被考评表

    private GovernanceMetric governanceMetric ;     //考评指标

    // 可以往下加入其他需要的参数
    private List<TableMetaInfo> tableMetaInfoList ; // 待考评的所有表

    // 如果需要有别的参数传递， 直接添加即可。
    // ds中的任务定义
    private TDsTaskDefinition tDsTaskDefinition;
    // ds中的实例定义
    private TDsTaskInstance tDsTaskInstance;

}