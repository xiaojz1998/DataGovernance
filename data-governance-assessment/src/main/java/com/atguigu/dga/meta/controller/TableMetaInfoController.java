package com.atguigu.dga.meta.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoExtra;
import com.atguigu.dga.meta.bean.TableMetaInfoQuery;
import com.atguigu.dga.meta.bean.TableMetaInfoVO;
import com.atguigu.dga.meta.service.TableMetaInfoExtraService;
import com.atguigu.dga.meta.service.TableMetaInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 元数据表 前端控制器
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-02
 */
@RestController
@RequestMapping("/tableMetaInfo")
public class TableMetaInfoController {

    @Autowired
    TableMetaInfoService tableMetaInfoService;
    @Autowired
    TableMetaInfoExtraService tableMetaInfoExtraService;

    /**
     * 手动更新全库元数据
     * 接口路径: /tableMetaInfo/init-tables/{database}/{assessdate}
     * 请求方式: POST
     * 接口参数: 接口路径中的{database}和{assessdate}
     * 返回结果: 保存成功后返回  "success" 即可。
     */
    @PostMapping("/init-tables/{database}/{assessdate}")
    public String initTables(@PathVariable String database, @PathVariable String assessdate) throws Exception {
        tableMetaInfoService.initTableMetaInfo(database,assessdate);
        return "success";
    }


    /**
     * 辅助信息修改接口
     * 接口路径: /tableMetaInfo/tableExtra
     * 请求方式: POST
     * 请求参数: {
     *           "id": 1,
     *           "tableName": "ads_traffic_stats_by_channel",
     *           "schemaName": "gmall",
     *           "tecOwnerUserName": "weiyunhui",
     *           "busiOwnerUserName": "weiyunhui",
     *           "lifecycleDays": 12,
     *           "securityLevel": "1",
     *           "dwLevel": "ADS",
     *           "createTime": "2023-04-15T07:35:09.000+00:00",
     *           "updateTime": null
     *        }
     * 返回结果 ：success
     */
    @PostMapping("/tableExtra")
    public  String tableExtra(@RequestBody TableMetaInfoExtra tableMetaInfoExtra){
        tableMetaInfoExtraService.updateById(tableMetaInfoExtra);
        return "success";
    }

    /**
     * 单表信息详情接口
     * 接口路径: /tableMetaInfo/table/{tableMetaInfoId}
     * 请求方式: GET
     * 接口参数: 请求路径中的tableMetaInfoId
     * 返回结果:{"id":1186,"tableName":"ads_traffic_stats_by_channel","schemaName":"gmall","colNameJson":"[{\"comment\":\"统计日期\",\"name\":\"dt\",\"type\":\"string\"},{\"comment\":\"最近天数,1:最近1天,7:最近7天,30:最近30天\",\"name\":\"recent_days\",\"type\":\"bigint\"},{\"comment\":\"渠道\",\"name\":\"channel\",\"type\":\"str  ing\"},{\"comment\":\"访客人数\",\"name\":\"uv_count\",\"type\":\"bigint\"},{\"comment\":\"会话平均停留时长，单位为秒\",\"name\":\"avg_duration_sec\",\"type\":\"bigint\"},{\"comment\":\"会话平均浏览页面数\",\"name\":\"avg_page_count\",\"type\":\"bigint\"},{\"comment\":\"会话数\",\"name\":\"sv_count\",\"type\":\"bigint\"},{\"comment\":\"跳出率\",\"name\":\"bounce_rate\",\"type\":\"decimal(16,2)\"}]","partitionColNameJson":"[]","tableFsOwner":"atguigu","tableParametersJson":"{\"totalSize\":\"2254\",\"EXTERNAL\":\"TRUE\",\"numFiles\":\"2\",\"transient_lastDdlTime\":\"1680235106\",\"bucketing_version\":\"2\",\"comment\":\"各渠道流量统计\"}","tableComment":"各渠道流量统计","tableFsPath":"hdfs://hadoop102:8020/warehouse/gmall/ads/ads_traffic_stats_by_channel","tableInputFormat":"org.apache.hadoop.mapred.TextInputFormat","tableOutputFormat":"org.apache.hadoop.mapred.TextInputFormat","tableRowFormatSerde":"org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe","tableCreateTime":"2023-03-31T03:58:26.000+00:00","tableType":"EXTERNAL_TABLE","tableBucketColsJson":null,"tableBucketNum":-1,"tableSortColsJson":null,"tableSize":2254,"tableTotalSize":6762,"tableLastModifyTime":"2023-02-07T09:52:58.000+00:00","tableLastAccessTime":"2023-02-07T09:52:58.000+00:00","fsCapcitySize":141652144128,"fsUsedSize":10058940416,"fsRemainSize":84828000256,"assessDate":"2023-04-15","createTime":"2023-04-15T07:35:05.000+00:00","updateTime":null,"tableMetaInfoExtra":{"id":1,"tableName":"ads_traffic_stats_by_channel","schemaName":"gmall","tecOwnerUserName":"weiyunhui","busiOwnerUserName":"weiyunhui","lifecycleDays":12,"securityLevel":"1","dwLevel":"ADS","createTime":"2023-04-15T07:35:09.000+00:00","updateTime":null}}
     */

    @GetMapping("/table/{tableMetaInfoId}")
    public String table(@PathVariable Long tableMetaInfoId){
        // 直接用mbp中的getById方法
        TableMetaInfo tableMetaInfo = tableMetaInfoService.getById(tableMetaInfoId);

        // 查询辅助信息
        TableMetaInfoExtra one = tableMetaInfoExtraService.getOne(
                new QueryWrapper<TableMetaInfoExtra>()
                        .eq("schema_name", tableMetaInfo.getSchemaName())
                        .eq("table_name", tableMetaInfo.getTableName())
        );
        tableMetaInfo.setTableMetaInfoExtra(one);
        return JSON.toJSONString(tableMetaInfo);
    }








    /**
     * 表信息列表接口
     *
     * 接口路径(前端发的请求):  /tableMetaInfo/table-list
     * 请求方式: GET
     * 请求参数: (通过键值对的方式来传递的)
     *          pageNo,
     *          pageSize,
     *          tableName,
     *          schemaName,
     *          dwLevel
     * 返回结果:
     *       {
     *        "total": 79,
     *        "list": [
     *          { "id": 1186,
     *            "tableName": "ads_traffic_stats_by_channel",
     *            "schemaName": "gmall",
     *            "tableSize": 2254,
     *            "tableTotalSize": 6762,
     *            "tableComment": "各渠道流量统计",
     *            "tecOwnerUserName": "weiyunhui",
     *            "busiOwnerUserName": "weiyunhui",
     *            "tableLastModifyTime": "2023-02-07T09:52:58.000+00:00",
     *            "tableLastAccessTime": "2023-02-07T09:52:58.000+00:00"
     *          },
     *          { "id": 1187,
     *            ….
     *          }
     *        ]
     *       }
     */
    @GetMapping("/table-list")
    public String tableList(TableMetaInfoQuery tableMetaInfoQuery){
        // 返回的list部分
        List<TableMetaInfoVO> tableMetaInfoList = tableMetaInfoService.getTableMetaInfoListByConditionAndPage(tableMetaInfoQuery);

        // 查询总数部分
        Integer total = tableMetaInfoService.getTableMetaInfoCountByCondition( tableMetaInfoQuery );

        // 封装返回结果
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("list",tableMetaInfoList);
        jsonObject.put("total", total);

        return jsonObject.toJSONString();
    }

}
