package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;

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

    public void addEndpoint(API api, APITemplateBuilder builder) throws AxisFault {
        try {
            ArrayList<String> arrayList = getEndpointType(api);
            for (String type : arrayList) {
                String endpointConfigContext = builder.getConfigStringForEndpointTemplate(type);
                endpointAdminStub.addEndpoint(endpointConfigContext);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while generating Endpoint file in Gateway " + e.getMessage(), e);
        }
    }

    public void deleteEndpoint(API api) throws AxisFault {
        try {
            String endpointName = api.getId().getApiName() + "--" + api.getId().getVersion();
            ArrayList<String> arrayList = getEndpointType(api);
            for (String type : arrayList) {
                String t = type.replace("_endpoints", "");
                endpointAdminStub.deleteEndpoint(endpointName + "_API" + t + "Endpoint");
            }
        } catch (Exception e) {
            throw new AxisFault("Error while deleting Endpoint from the gateway. " + e.getMessage(), e);
        }
    }

    private ArrayList<String> getEndpointType(API api) throws ParseException {
        ArrayList<String> arrayList = new ArrayList<>();
        if (APIUtil.isProductionEndpointsExists(api) && !APIUtil.isSandboxEndpointsExists(api)) {
            arrayList.add(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
        } else if (APIUtil.isSandboxEndpointsExists(api) && !APIUtil.isProductionEndpointsExists(api)) {
            arrayList.add(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
        } else {
            arrayList.add(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
            arrayList.add(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
        }
        return arrayList;
    }

//    public void saveEndpoint(APITemplateBuilder builder) throws AxisFault {
//        try {
//            String endpointConfig = builder.getConfigStringForEndpointTemplate(environment);
//            endpointAdminStub.saveEndpoint(endpointConfig);
//        } catch (Exception e) {
//            throw new AxisFault("Error updating Endpoint file" + e.getMessage(), e);
//        }
//    }
}
