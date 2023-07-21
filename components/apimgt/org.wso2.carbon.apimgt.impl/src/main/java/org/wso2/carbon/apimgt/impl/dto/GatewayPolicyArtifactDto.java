package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;

import java.util.List;

public class GatewayPolicyArtifactDto {
    List<OperationPolicyData> gatewayPolicyDataList;
    List<OperationPolicy> gatewayPolicyList;
    String tenantDomain;

    public List<OperationPolicyData> getGatewayPolicyDataList() {
        return gatewayPolicyDataList;
    }

    public void setGatewayPolicyDataList(List<OperationPolicyData> gatewayPolicyDataList) {
        this.gatewayPolicyDataList = gatewayPolicyDataList;
    }

    public List<OperationPolicy> getGatewayPolicyList() {
        return gatewayPolicyList;
    }

    public void setGatewayPolicyList(List<OperationPolicy> gatewayPolicyList) {
        this.gatewayPolicyList = gatewayPolicyList;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
