package com.atguigu.dga;

import com.atguigu.dga.governance.service.*;
import com.atguigu.dga.meta.service.impl.TableMetaInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// 主要用作测试service里面的各个类   你
@SpringBootTest
class DataGovernanceAssessmentApplicationTests {


    @Autowired
    GovernanceAssessDetailService governanceAssessDetailService;
    @Autowired
    TableMetaInfoServiceImpl tableMetaInfoServiceImpl;

    @Autowired
    GovernanceAssessTableService governanceAssessTableService;

    @Autowired
    GovernanceAssessTecOwnerService governanceAssessTecOwnerService;

    @Autowired
    GovernanceAssessGlobalService governanceAssessGlobalService;

    @Autowired
    MainAssessService mainAssessService;

    @Test
    void testStartGovernanceAssess() throws Exception {
        mainAssessService.startGovernanceAssess("gmall" , "2023-05-02");
    }

    @Test
    void testGlabalScore(){
        governanceAssessGlobalService.calcGlobalScore( "2023-05-02");
    }
    @Test
    void testTecOwnerScore(){
        governanceAssessTecOwnerService.calcTecOwnerScore( "2023-05-02");
    }

    @Test
    void testTableScore(){
        governanceAssessTableService.calcTableScore( "2023-05-02");
    }


    @Test
    void contextLoads() {
    }

    // 进行考评
    @Test
    void testMainAssess() throws Exception {
        governanceAssessDetailService.mainAssess("2023-05-02");
    }
    // 初始化数据库的测试
    // 直接用到service层
    @Test
    void testInitTableMetaInfo() throws Exception {
        tableMetaInfoServiceImpl.initTableMetaInfo("gmall","2023-05-02");
    }

    // 初始化 hiveclient的测试
    // 直接用到service层
    // 其中createHiveClient会在类创建时候执行，直接建立连接
    @Test
    void testHiveClient(){
        tableMetaInfoServiceImpl.createHiveClient();
    }

    @Test
    void myTest(){
        System.out.println(Long.MIN_VALUE);
    }

}
