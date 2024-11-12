package com.atguigu.dga.meta.service;

import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoQuery;
import com.atguigu.dga.meta.bean.TableMetaInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.stringtemplate.v4.ST;

import java.util.List;

/**
 * <p>
 * 元数据表 服务类
 * </p>
 *
 * @author xiaojianzhe
 * @since 2024-11-02
 */
public interface TableMetaInfoService extends IService<TableMetaInfo> {

    void initTableMetaInfo(String schemaName,String assessDate) throws Exception;


    List<TableMetaInfoVO> getTableMetaInfoListByConditionAndPage(TableMetaInfoQuery tableMetaInfoQuery);

    Integer getTableMetaInfoCountByCondition(TableMetaInfoQuery tableMetaInfoQuery);
}
