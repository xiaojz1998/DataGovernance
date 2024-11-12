package com.atguigu.dga.governance.service.impl;

import com.atguigu.dga.governance.service.*;
import com.atguigu.dga.meta.service.TableMetaInfoService;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MainAssessServiceImpl implements MainAssessService {
    @Autowired
    TableMetaInfoService tableMetaInfoService ;

    @Autowired
    GovernanceAssessDetailService governanceAssessDetailService ;

    @Autowired
    GovernanceAssessTableService governanceAssessTableService ;

    @Autowired
    GovernanceAssessTecOwnerService governanceAssessTecOwnerService ;

    @Autowired
    GovernanceAssessGlobalService governanceAssessGlobalService ;

    @Value("${data.warehouse.default.db}")
    private String  dataWarehouseDefaultDb ;

    @Override
    public void startGovernanceAssess(String schemaName, String assessDate) throws Exception {
        // 1. 提取元数据
        tableMetaInfoService.initTableMetaInfo(schemaName , assessDate);

        // 2. 考评
        governanceAssessDetailService.mainAssess(assessDate);

        // 3. 核算分数
        // 表级分数
        governanceAssessTableService.calcTableScore( assessDate );
        // 人级分数
        governanceAssessTecOwnerService.calcTecOwnerScore( assessDate );
        // 全局分数
        governanceAssessGlobalService.calcGlobalScore( assessDate );
    }

    @Scheduled(cron = "00 19 14 * * *")
    @Override
    public void startGovernanceAssess() throws Exception {

        // 每日自动调度， assessDate一定是当天
        String assessDate = DateFormatUtils.format( new Date() , "yyyy-MM-dd") ;
        startGovernanceAssess( dataWarehouseDefaultDb , assessDate )  ;
    }

    @Override
    public void startGovernanceAssess(String assessDate) throws Exception {

        startGovernanceAssess( dataWarehouseDefaultDb , assessDate)  ;
    }
}
