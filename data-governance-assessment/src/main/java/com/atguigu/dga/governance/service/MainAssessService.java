package com.atguigu.dga.governance.service;

public interface MainAssessService {
    void startGovernanceAssess( String schemaName ,  String assessDate ) throws Exception;

    void startGovernanceAssess() throws Exception;

    void startGovernanceAssess( String assessDate ) throws Exception;
}
