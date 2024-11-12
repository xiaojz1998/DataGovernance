package com.atguigu.dga.governance.service.impl;

import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.*;
import com.atguigu.dga.governance.mapper.GovernanceAssessDetailMapper;
import com.atguigu.dga.governance.service.GovernanceAssessDetailService;
import com.atguigu.dga.governance.service.GovernanceMetricService;
import com.atguigu.dga.governance.service.TDsTaskDefinitionService;
import com.atguigu.dga.governance.service.TDsTaskInstanceService;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoExtra;
import com.atguigu.dga.meta.mapper.TableMetaInfoMapper;
import com.atguigu.dga.meta.service.TableMetaInfoExtraService;
import com.atguigu.dga.meta.service.TableMetaInfoService;
import com.atguigu.dga.util.SpringBeanProvider;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 治理考评结果明细 服务实现类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-05
 */
@Service
public class GovernanceAssessDetailServiceImpl extends ServiceImpl<GovernanceAssessDetailMapper, GovernanceAssessDetail> implements GovernanceAssessDetailService {

    @Autowired
    TableMetaInfoMapper tableMetaInfoMapper;

    @Autowired
    GovernanceMetricService governanceMetricService;

    @Autowired
    SpringBeanProvider springBeanProvider;

    @Autowired
    TableMetaInfoExtraService tableMetaInfoExtraService;

    @Autowired
    TableMetaInfoService tableMetaInfoService;

    @Autowired
    TDsTaskDefinitionService tDsTaskDefinitionService;

    @Autowired
    TDsTaskInstanceService tDsTaskInstanceService;

    // 线程池
    ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(20 , 20 , 10 , TimeUnit.SECONDS , new LinkedBlockingQueue<>(1500));


    /**
     * 核心考评方法
     * @param assessDate
     *
     * 考评原则: 每个表 ， 每个指标， 逐一进行考评
     * 步骤：
     *      0. 清理考评日期对应的结果
     *      1. 获取所有表
     *      2. 获取所有指标
     *      3. 遍历所有表，遍历所有指标，进行考评
     *      4. 保存考评结果到数据库中
     *
     */
    @Override
    public void mainAssess(String assessDate) throws Exception {
        //0. 清理考评日期对应的结果
        this.remove(
                new QueryWrapper<GovernanceAssessDetail>().eq("assess_date",assessDate)
        );
        //1. 获取所有表
        // 方式一: 先将table_meta_info表中的数据查询出来，
        //        再遍历每张表， 通过表的schema_name 和 table_name 到table_meta_info_extra表查询对应的辅助信息

//        List<TableMetaInfo> tableMetaInfoList = tableMetaInfoService.list(
//                                 new QueryWrapper<TableMetaInfo>()
//                                         .eq("assess_date", assessDate)
//                         );
//        for (TableMetaInfo tableMetaInfo : tableMetaInfoList) {
//                  //查询对应的辅助信息
//                  //缺点: 将与外部组件（当前是mysql数据库）的过程写到了循环中。
//                  TableMetaInfoExtra tableMetaInfoExtra = tableMetaInfoExtraService.getOne(
//                                         new QueryWrapper<TableMetaInfoExtra>()
//                                                 .eq("schema_name", tableMetaInfo.getSchemaName())
//                                                 .eq("table_name", tableMetaInfo.getTableName())
//                                 );
//                  tableMetaInfo.setTableMetaInfoExtra( tableMetaInfoExtra );
//              }


        // 方式二: 先将table_meta_info表中的数据查询出来封装到tableMetaInfoList，
        //        再将table_meta_info_extra表中的数据查询出来封装到TableMetaInfoExtraList
        //        遍历tableMetaInfoList, 通过表的schema_name 和 table_name 到TableMetaInfoExtraList查询对应的辅助信息
        //        将tableMetaInfoList处理成Map： k(schemaName + tableName)  v(tableMetaInfoExtra)

//        List<TableMetaInfo> tableMetaInfoList = tableMetaInfoService.list(
//                                 new QueryWrapper<TableMetaInfo>()
//                                         .eq("assess_date", assessDate)
//                         );
//        List<TableMetaInfoExtra> tableMetaInfoExtraList = tableMetaInfoExtraService.list();

        // 以下用循环的方式赋值，不推荐
              //for (TableMetaInfo tableMetaInfo : tableMetaInfoList) {
              //    for (TableMetaInfoExtra tableMetaInfoExtra : tableMetaInfoExtraList) {
              //        if( tableMetaInfo.getSchemaName().equals(tableMetaInfoExtra.getSchemaName())
              //            &&
              //            tableMetaInfo.getTableName().equals(tableMetaInfoExtra.getTableName())
              //          ){
              //            tableMetaInfo.setTableMetaInfoExtra(tableMetaInfoExtra);
              //        }
              //    }
              //}

              //将tableMetaInfoList处理成Map： k(schemaName + tableName)  v(tableMetaInfoExtra)
//              Map<String, TableMetaInfoExtra> tableMetaInfoMap = tableMetaInfoExtraList.stream().collect(
//                                 Collectors.toMap(
//                                         tableMetaInfoExtra -> tableMetaInfoExtra.getSchemaName() + "_" + tableMetaInfoExtra.getTableName(),
//                              tableMetaInfoExtra -> tableMetaInfoExtra
//                                 )
//              );
//              for (TableMetaInfo tableMetaInfo : tableMetaInfoList) {
//                  tableMetaInfo.setTableMetaInfoExtra( tableMetaInfoMap.get( tableMetaInfo.getSchemaName() + "_" + tableMetaInfo.getTableName()));
//              }





        // 方式三: 使用join的方式，一次性将两张表的数据查询出来， 通过自定义MyBatis的ResultMap完成数据封装
        //        用的是手写sql的方式在mapper中定义一个方法来返回数据表
        List<TableMetaInfo> tableMetaInfoList = tableMetaInfoMapper.selectTableMetaInfoListWithExtra(assessDate);
        //System.out.println(tableMetaInfoList);


        //2. 获取所有指标
        List<GovernanceMetric> governanceMetricList = governanceMetricService.list(
                new QueryWrapper<GovernanceMetric>()
                        .eq("is_disabled", "1")
        );
        // System.out.println(governanceMetricList);
        //  查询ds中的任务定义
        Map<String, TDsTaskDefinition> tDsTaskDefinitionMap = tDsTaskDefinitionService.getTDsTaskDefinitionMap();
        //System.out.println(tDsTaskDefinitionMap);
        //  查询ds中的任务实例
        Map<String, TDsTaskInstance> tDsTaskInstanceMap = tDsTaskInstanceService.getTDsTaskInstanceMapByJoin(assessDate);
        //System.out.println(tDsTaskInstanceMap);


        // 3. 遍历所有表，遍历所有指标，进行考评
              //3.1 指标设计成考评器
                         // 需要将每个指标设计成一个具体的类，类中具有方法，用于查找问题
              // 例如: 是否有技术owner指标  => TableTecOwnerAssessor  => checkProblem()
              //      是否有业务owner指标  => TableBusiOwnerAssessor => checkProblem()
              //      xxx指标 .......... => TableXxxAssessor       => checkProblem()

              // 所有指标的考评过程是一致的，但是查找问题的细节不一样，可以将统一的考评过程提取到父Assessor中，通过 doAssess()方法来实现。
              // 而在doAssess()方法的执行过程中，会有一步是每个指标查找具体的问题，只需要将查找问题的方法设计成抽象，让每个指标具体实现即可。
              // 通过父类控制整个考评流程（子类不允许修改） ， 各个子类实现具体的查找问题细节。 可以通过模板设计模式来完成

        //3.2 如何通过指标获取对应的考评器？
        // 1）硬编码， 不推荐。
        //if( 技术负责人指标){
        //    new TableTecOwnerAssessor();
        //}else if (业务负责人指标){
        //    new TableBusiOwnerAssessor();
        //}else if( .... ){
        //    new  xxxx();
        //}
        // 方法一：约定 + 反射
        // 约定:
        //  1. 考评器的类名必须使用指标编码来命名  TABLE_TEC_OWNER  => TableTecOwnerAssessor
        //  2. 考评器所在包 com.atguigu.dga.governance.assessor.(当前指标的治理类型)
        // 反射:
        //  Class cls =  Class.forName("全类名")
        //  cls.newInstance()
        //处理类名
        //String metricCode = governanceMetric.getMetricCode();
        //String className = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, metricCode) + "Assessor";
        //处理包名
        //String packageName = "com.atguigu.dga.governance.assessor." + governanceMetric.getGovernanceType().toLowerCase();
        //全类名
        //String fullClassName = packageName + "." + className ;
        //反射
        //Class<?> cls = Class.forName(fullClassName);
        //Assessor assessor  = (Assessor)cls.newInstance();
        //assessor.doAssess();


        // 方法二：约定 + Spring容器

        //定义集合， 保存考评结果
        //ArrayList<GovernanceAssessDetail> governanceAssessDetails = new ArrayList<GovernanceAssessDetail>(tableMetaInfoList.size()*governanceMetricList.size());

        //定义集合，保存异步任务
        ArrayList<CompletableFuture<GovernanceAssessDetail>> futures
                = new ArrayList<>(tableMetaInfoList.size() * governanceMetricList.size());



        for (TableMetaInfo tableMetaInfo: tableMetaInfoList){
            for (GovernanceMetric governanceMetric : governanceMetricList) {
                // 处理白名单
                String skipAssessTables = governanceMetric.getSkipAssessTables();
                if(skipAssessTables !=null && !skipAssessTables.trim().isEmpty()){
                    // 把白名单切开
                    List<String> skipAssessTableList = Arrays.asList(skipAssessTables.split(","));
                    if(skipAssessTableList.contains(tableMetaInfo.getTableName())){
                        // 跳过考评
                        continue;
                    }

                }




                // 每张表，每个指标，逐一进行考评
                // 约定 + Spring容器
                // 约定: 将所有的考评器都交给容器管理， 且给每个考评器取名为 指标编码
                String metricCode = governanceMetric.getMetricCode();
                Assessor assessor = springBeanProvider.getBean(metricCode, Assessor.class);

                // 建造考评参数
                AssessParam assessParam = AssessParam.builder()
                        .assessDate( assessDate)                // 考评日期
                        .tableMetaInfo( tableMetaInfo )         // 考评表
                        .governanceMetric( governanceMetric )   // 考评指标
                        .tableMetaInfoList( tableMetaInfoList )
                        .tDsTaskDefinition( tDsTaskDefinitionMap.get(tableMetaInfo.getSchemaName()+"."+tableMetaInfo.getTableName()))
                        .tDsTaskInstance( tDsTaskInstanceMap.get(tableMetaInfo.getSchemaName()+"."+tableMetaInfo.getTableName()))
                        .build();

                //开始考评
                // 下面是串行的
                //GovernanceAssessDetail governanceAssessDetail = assessor.doAssess( assessParam );

                // 下面是并行的
                CompletableFuture<GovernanceAssessDetail> governanceAssessDetailCompletableFuture = CompletableFuture.supplyAsync(
                        () -> assessor.doAssess(assessParam), threadPoolExecutor
                );

                //攒批
                //governanceAssessDetails.add( governanceAssessDetail );

                futures.add( governanceAssessDetailCompletableFuture);
            }
        }
        // 异步执行
        // join 方法会等待当前任务结束返回结果
        List<GovernanceAssessDetail> collect = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());

        // 提交到数据库
        this.saveBatch(collect);
    }
}
