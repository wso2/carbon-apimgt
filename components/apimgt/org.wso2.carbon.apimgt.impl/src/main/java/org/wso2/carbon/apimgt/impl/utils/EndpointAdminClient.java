package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminStub;
import org.wso2.carbon.utils.CarbonUtils;

public class EndpointAdminClient {
    private static final Logger log = LoggerFactory.getLogger(EndpointAdminClient.class);

    private final String serviceName = "EndpointAdmin";
    private EndpointAdminStub endpointAdminStub;
    private Environment environment;

    public EndpointAdminClient(APIIdentifier apiId, Environment environment) throws AxisFault {
        String endpoint = environment.getServerURL() + serviceName;
        endpointAdminStub = new EndpointAdminStub(endpoint);
        this.environment = environment;
        CarbonUtils.setBasicAccessSecurityHeaders(environment.getUserName(), environment.getPassword(),
                endpointAdminStub._getServiceClient());
    }

    public void addEndpoint(APITemplateBuilder builder, String tenantDomain, APIIdentifier apiId) throws AxisFault {
        try {
            String endpointConfig = builder.getConfigStringForEndpointTemplate(environment);
            endpointAdminStub.addEndpoint(endpointConfig);
        } catch (Exception e) {
            throw new AxisFault("Error while generating Endpoint file in Gateway " + e.getMessage(), e);
        }
    }

    public void deleteEndpoint(String endpointName) throws AxisFault {
        try {
            endpointAdminStub.deleteEndpoint(endpointName);
        } catch (Exception e) {
            throw new AxisFault("Error while deleting Endpoint from the gateway. " + e.getMessage(), e);
        }
    }
}
