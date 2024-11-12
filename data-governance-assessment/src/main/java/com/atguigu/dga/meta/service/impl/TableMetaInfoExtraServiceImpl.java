package com.atguigu.dga.meta.service.impl;

import com.atguigu.dga.constant.DgaConstant;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoExtra;
import com.atguigu.dga.meta.mapper.TableMetaInfoExtraMapper;
import com.atguigu.dga.meta.service.TableMetaInfoExtraService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 元数据表附加信息 服务实现类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-02
 */
@Service
public class TableMetaInfoExtraServiceImpl extends ServiceImpl<TableMetaInfoExtraMapper, TableMetaInfoExtra> implements TableMetaInfoExtraService {

    /**
     * 初始化(给一些没有实际意义的初始值)辅助信息
     * 如果表的辅助信息没有被初始化过，进行首次初始化
     * 如果表的辅助信息已经被初始化过，不再初始化
     */
    @Override
    public void initTableMetaInfoExtra(List<TableMetaInfo> tableMetaInfoList) {
        // 创建集合，保存需要初始化的TableMetaInfoExtra
        ArrayList<TableMetaInfoExtra> tableMetaInfoExtras = new ArrayList<>(tableMetaInfoList.size());

        // 循环处理每个tableMetaInfo
        for (TableMetaInfo tableMetaInfo  : tableMetaInfoList) {
            // 判断表是否初始化过
            // 判断依据: 当前tableMetaInfo 在辅助信息表中是否存在对应的数据
            TableMetaInfoExtra one = this.getOne(
                    new QueryWrapper<TableMetaInfoExtra>().eq("schema_name", tableMetaInfo.getSchemaName())
                            .eq("table_name", tableMetaInfo.getTableName())
            );

            // 已经初始化过， 直接跳过，不再进行初始化
            // 没有初始化过，进行初始化操作
            if(one == null){
                one = new TableMetaInfoExtra();
                one .setSchemaName(tableMetaInfo.getSchemaName());
                one .setTableName(tableMetaInfo.getTableName());
                one.setTecOwnerUserName(DgaConstant.TEC_OWNER_USER_NAME_UNSET);
                one.setBusiOwnerUserName(DgaConstant.BUSI_OWNER_USER_NAME_UNSET);
                one.setLifecycleType(DgaConstant.LIFECYCLE_TYPE_UNSET);
                one.setLifecycleDays(-1L);
                one.setSecurityLevel(DgaConstant.SECURITY_LEVEL_UNSET);
                one.setDwLevel( getDwLevel(tableMetaInfo.getTableName()) );
                one.setCreateTime(new Date());

                // 攒批次
                tableMetaInfoExtras.add( one );
            }


        }
        this.saveBatch(tableMetaInfoExtras);
    }

    public String getDwLevel(String tableName){
        if(tableName.startsWith("ods")){
            return DgaConstant.DW_LEVEL_ODS;
        }else if ( tableName.startsWith("dim")){
            return DgaConstant.DW_LEVEL_DIM;
        }else if ( tableName.startsWith("dwd")){
            return DgaConstant.DW_LEVEL_DWD;
        }else if ( tableName.startsWith("dws")){
            return DgaConstant.DW_LEVEL_DWS;
        }else if ( tableName.startsWith("ads")){
            return DgaConstant.DW_LEVEL_ADS;
        }else{
            return DgaConstant.DW_LEVEL_OTHER;
        }
    }
}
