package com.atguigu.dga.meta.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoQuery;
import com.atguigu.dga.meta.bean.TableMetaInfoVO;
import com.atguigu.dga.meta.mapper.TableMetaInfoMapper;
import com.atguigu.dga.meta.service.TableMetaInfoExtraService;
import com.atguigu.dga.meta.service.TableMetaInfoService;
import com.atguigu.dga.util.SqlUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.conf.MetastoreConf;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * <p>
 * 元数据表 服务实现类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-02
 */
@Service
public class TableMetaInfoServiceImpl extends ServiceImpl<TableMetaInfoMapper, TableMetaInfo> implements TableMetaInfoService {


    @Autowired
    TableMetaInfoExtraService tableMetaInfoExtraService;




    @Override
    public List<TableMetaInfoVO> getTableMetaInfoListByConditionAndPage(TableMetaInfoQuery tableMetaInfoQuery) {
        // 处理条件
        // 两个表联合，因为tableMetaInfoVO中
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT \n" +
                        "ti.id , \n" +
                        "ti.table_name, \n" +
                        "ti.schema_name,\n" +
                        "ti.table_size, \n" +
                        "ti.table_total_size, \n" +
                        "ti.table_comment , \n" +
                        "te.tec_owner_user_name,\n" +
                        "te.busi_owner_user_name,\n" +
                        "ti.table_last_modify_time,\n" +
                        "ti.table_last_access_time   \n" +
                        "FROM table_meta_info  ti JOIN  table_meta_info_extra te \n" +
                        "ON ti.schema_name = te.schema_name \n" +
                        "AND ti.table_name = te.table_name\n" +
                        "WHERE ti.assess_date = ( SELECT MAX(assess_date) FROM table_meta_info )\n"
        );
        // 加上 三个用户输入的字段来进行where
        // 处理schema_name
        if(tableMetaInfoQuery.getSchemaName()!=null && !tableMetaInfoQuery.getSchemaName().trim().isEmpty()){
            sqlBuilder.append("and ti.schema_name= '"+ SqlUtil.filterUnsafeSql(tableMetaInfoQuery.getSchemaName().trim())+"'\n");
        }
        // 处理table_name
        if(tableMetaInfoQuery.getTableName()!=null && !tableMetaInfoQuery.getTableName().trim().isEmpty()){
            sqlBuilder.append( "AND ti.table_name like '%" + tableMetaInfoQuery.getTableName().trim() + "%'\n");        }
        // 处理dw_level
        if(tableMetaInfoQuery.getDwLevel() != null && !tableMetaInfoQuery.getDwLevel().trim().isEmpty()){
            //值等判断
            sqlBuilder.append( "AND te.dw_level = '" + tableMetaInfoQuery.getDwLevel().trim() + "'\n");
        }
        //处理分页
        //例如:  每页显示2条数据
        //      第1页  ->  从 0 取
        //      第2页  ->  从 2 取
        //      第3页  ->  从 4 取
        //      第n页  ->  从 (n - 1 ) * 2 取
        // 根据pageNo  和 pageSize 计算开始行:  start = ( pageNo - 1 ) * pageSize
        Integer start = (tableMetaInfoQuery.getPageNo()-1)*tableMetaInfoQuery.getPageSize();
        // 加入sql
        sqlBuilder.append("limit "+start+" , "+tableMetaInfoQuery.getPageSize()+" \n");

        // 测试sqlBuilder
        System.out.println(sqlBuilder);
        // 调用 mapper
        List<TableMetaInfoVO> tableMetaInfoVOS = this.getBaseMapper().selectTableMetaInfoVOList(sqlBuilder.toString());
        return tableMetaInfoVOS;

    }

    // 查询总数量
    @Override
    public Integer getTableMetaInfoCountByCondition(TableMetaInfoQuery tableMetaInfoQuery) {
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT \n" +
                        "count(*) \n" +
                        "FROM table_meta_info  ti JOIN  table_meta_info_extra te \n" +
                        "ON ti.schema_name = te.schema_name \n" +
                        "AND ti.table_name = te.table_name\n" +
                        "WHERE ti.assess_date = ( SELECT MAX(assess_date) FROM table_meta_info )\n"
        );
        //处理schema_name
        if(tableMetaInfoQuery.getSchemaName() != null && !tableMetaInfoQuery.getSchemaName().trim().isEmpty()){
            //值等判断
            sqlBuilder.append( "AND ti.schema_name = '" + SqlUtil.filterUnsafeSql(tableMetaInfoQuery.getSchemaName().trim()) + "' \n"  ) ;
        }
        //处理table_name
        if(tableMetaInfoQuery.getTableName() != null && !tableMetaInfoQuery.getTableName().trim().isEmpty()){
            //模糊匹配
            sqlBuilder.append( "AND ti.table_name like '%" + tableMetaInfoQuery.getTableName().trim() + "%'\n");
        }

        //处理dw_level
        if(tableMetaInfoQuery.getDwLevel() != null && !tableMetaInfoQuery.getDwLevel().trim().isEmpty()){
            //值等判断
            sqlBuilder.append( "AND te.dw_level = '" + tableMetaInfoQuery.getDwLevel().trim() + "'\n");
        }
        Integer total = this.baseMapper.selectTableMetaInfoVOCount(sqlBuilder.toString());
        return total;


    }


    /**
     * 提取表的元数据
     * @param schemaName
     * @param assessDate
     * @throws Exception
     * 步骤:
     *    0. 删除当前考评日期对应的数据
     *    1. 提取Hive的元数据
     *        1）在hadoop102上启动hive元数据服务
     *        2）创建元数据客户端对象，提取元数据
     *    2. 提取HDFS的元数据
     *    3. 将取到的元数据写入到数据库表中 table_meta_info表
     *    4. 初始化表辅助信息
     *
     */
    @Override
    public void initTableMetaInfo(String schemaName, String assessDate) throws Exception {

        // 因为之前数据超出字段写入失败，所以要清除掉已经写入的当天数据
        this.remove(
                new QueryWrapper<TableMetaInfo>().eq("assess_date",assessDate)
        );

        // 获取指定库下所有的表名
        List<String> allTables = hiveClient.getAllTables(schemaName);
        //List<String> allDatabases = hiveClient.getAllDatabases();

        // 创建集合，保存处理好的tableMetaInfo 对象
        final ArrayList<TableMetaInfo> tableMetaInfos = new ArrayList<>(allTables.size());

        // 循环获取每个表对象
        for (String tableName : allTables) {
            Table table = hiveClient.getTable(schemaName, tableName);
            // 从table中获取元数据，存到tableMetaInfo对象中
            TableMetaInfo tableMetaInfo = extractTableMetaInfoFromHive(table);


            // 从hdfs中提取元数据
            extractTableMetaInfoFromHDFS(tableMetaInfo);
//            System.out.println("tableMetaInfo = " + tableMetaInfo);

            // 考评日期
            tableMetaInfo.setAssessDate( assessDate );
            // 创建时间
            tableMetaInfo.setCreateTime( new Date() );

            // 逐条写入
            //this.save(tableMetaInfo)
            // 攒批次
            tableMetaInfos.add(tableMetaInfo);
        }
        //将取到的元数据写入到数据库表中 table_meta_info表
        this.saveBatch( tableMetaInfos ) ;

        // 初始化辅助信息 table_meta_info表
        System.out.println("success");

        //初始化辅助信息
        tableMetaInfoExtraService.initTableMetaInfoExtra( tableMetaInfos );
    }




    /**
     * 从hdfs中提取元数据
     * extractTableMetaInfoFromHDFS
     */
    public void extractTableMetaInfoFromHDFS(TableMetaInfo tableMetaInfo) throws Exception{
        // 获取hdfs文件系统对象
        FileSystem fs = FileSystem.get(new URI(tableMetaInfo.getTableFsPath()),new Configuration(),tableMetaInfo.getTableFsOwner());

        // 获取表路径下所有内容
        FileStatus[] fileStatuses = fs.listStatus(new Path(tableMetaInfo.getTableFsPath()));

        // 递归处理
        addHDFSInfo(fs , fileStatuses ,  tableMetaInfo ) ;

        // hdfs容量大小
        tableMetaInfo.setFsCapcitySize(fs.getStatus().getCapacity());
        tableMetaInfo.setFsUsedSize(fs.getStatus().getUsed());
        tableMetaInfo.setFsRemainSize(fs.getStatus().getRemaining());
    }
    /**
     * 递归汇总 表的大小 、 表的总大小（考虑副本数） 、 表的最后修改时间 、 表的最后访问时间
     */
    public void addHDFSInfo(FileSystem fs, FileStatus [] fileStatuses,TableMetaInfo tableMetaInfo) throws Exception {
        for (FileStatus fileStatus : fileStatuses) {
            if(fileStatus.isFile()){
                // 汇总表的大小，表的总大小，表的最后修改时间，表的访问时间
                // 表的大小
                tableMetaInfo.setTableSize((tableMetaInfo.getTableSize()==null?0L: tableMetaInfo.getTableSize())+fileStatus.getLen());
                // 表的总大小
                tableMetaInfo.setTableTotalSize((tableMetaInfo.getTableTotalSize()==null?0L: tableMetaInfo.getTableTotalSize())+fileStatus.getLen()*fileStatus.getReplication());
                // 表的最后修改时间
                // 当前文件的当前修改时间
                long fileModifyTime = fileStatus.getModificationTime();
                long currentModifyTime = tableMetaInfo.getTableLastModifyTime()==null?Long.MIN_VALUE:tableMetaInfo.getTableLastModifyTime().getTime();
                long currentLastModifyTime = Math.max(fileModifyTime, currentModifyTime);
                tableMetaInfo.setTableLastModifyTime(new Date(currentLastModifyTime));

                // 表的最后访问时间
                tableMetaInfo.setTableLastAccessTime( new Date( Math.max( fileStatus.getAccessTime() , tableMetaInfo.getTableLastAccessTime() ==null? Long.MIN_VALUE : tableMetaInfo.getTableLastAccessTime().getTime())));

            }else{
                // 是目录的情况
                // 获取当前目录的fileStatus数组
                FileStatus[] fileStatuses1 = fs.listStatus(fileStatus.getPath());
                // 递归
                addHDFSInfo(fs,fileStatuses1,tableMetaInfo);
            }
        }
    }


    /**
     *  从Table对象中提取元数据， 存到TableMetaInfo对象中
     */
    public TableMetaInfo extractTableMetaInfoFromHive(Table table){
        //创建tableMetaInfo对象
        TableMetaInfo tableMetaInfo = new TableMetaInfo();
        // 表名
        tableMetaInfo.setTableName(table.getTableName());
        // 库名
        tableMetaInfo.setSchemaName(table.getDbName());
        // 列信息
        // 转成Json字符串的同时，过滤掉不需要的数据
        // 用的是阿里巴巴的fastJson中的方法
        PropertyPreFilters propertyPreFilters = new PropertyPreFilters();
        PropertyPreFilters.MySimplePropertyPreFilter mySimplePropertyPreFilter = propertyPreFilters.addFilter("comment", "name", "type");
        // 导入过滤器
        tableMetaInfo.setColNameJson(JSON.toJSONString(table.getSd().getCols(),mySimplePropertyPreFilter));
        // 分区列信息
        tableMetaInfo.setPartitionColNameJson(JSON.toJSONString(table.getPartitionKeys()));
        // 所属者
        tableMetaInfo.setTableFsOwner(table.getOwner());
        //表参数
        tableMetaInfo.setTableParametersJson(JSON.toJSONString(table.getParameters()));
        //表备注
        tableMetaInfo.setTableComment(table.getParameters().get("comment"));
        //表路径
        tableMetaInfo.setTableFsPath(table.getSd().getLocation());
        //输入格式
        tableMetaInfo.setTableInputFormat(table.getSd().getInputFormat());
        //输出格式
        tableMetaInfo.setTableOutputFormat(table.getSd().getOutputFormat());
        //行格式
        tableMetaInfo.setTableRowFormatSerde(table.getSd().getSerdeInfo().getSerializationLib());
        //表创建时间
        tableMetaInfo.setTableCreateTime(new Date(table.getCreateTime()*1000L));
        //表类型
        tableMetaInfo.setTableType(table.getTableType());
        //分桶表信息
        tableMetaInfo.setTableBucketColsJson(JSON.toJSONString(table.getSd().getBucketCols()));
        //分桶个数
        tableMetaInfo.setTableBucketNum((long)table.getSd().getNumBuckets());
        //分桶排序字段
        tableMetaInfo.setTableSortColsJson(JSON.toJSONString(table.getSd().getSortCols()));

        return tableMetaInfo;


    }



    // hiveClient对象存储在 私有成员变量中
    private IMetaStoreClient hiveClient;
    @Value("${hive.metastore.server.url}")
    private String hiveMetastoreUris;
    // 1. 首先创建hive客户端对象 因为我们要调用hive客户端对metastore的api
    @PostConstruct
    public void createHiveClient(){
        Configuration conf = new Configuration();
        // 执行hive元数据的位置
        MetastoreConf.setVar(conf ,MetastoreConf.ConfVars.THRIFT_URIS,hiveMetastoreUris);

        try{
            hiveClient = new HiveMetaStoreClient(conf);
            System.out.println("hiveClient = "+ hiveClient);
        } catch (MetaException e){
            throw new RuntimeException("获取hive客户端失败");
        }


    }
}
