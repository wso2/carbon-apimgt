package org.wso2.carbon.apimgt.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GatewayPolicyDeployer {
    private static final Log log = LogFactory.getLog(GatewayPolicyDeployer.class);
    private String tenantDomain;
    private String gatewayPolicyMappingUuid;

    public GatewayPolicyDeployer(String tenantDomain, String gatewayPolicyMappingUuid) {
        this.tenantDomain = tenantDomain;
        this.gatewayPolicyMappingUuid = gatewayPolicyMappingUuid;
    }

    public void deployGatewayPolicyMapping() {

    }

    public void undeployGatewayPolicyMapping() {

    }
}
