package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FederatedAPIDiscoveryService;
import org.wso2.carbon.apimgt.api.model.DiscoveredAPI;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.FederatedApisApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FederatedApisApiServiceImpl implements FederatedApisApiService {

    private static final Log log = LogFactory.getLog(FederatedApisApiServiceImpl.class);

    @Override
    public Response discoverFederatedAPIs(String environment, MessageContext messageContext)
            throws APIManagementException {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Environment env = getEnvironmentByName(environment, organization);
            FederatedAPIDiscoveryService service = getFederatedDiscoveryService();
            if (service == null) {
                throw new APIManagementException("FederatedAPIDiscoveryService OSGi service is not available.");
            }

            Map<String, List<DiscoveredAPI>> discoveredApiMap = service.discoverExternalAPIs(env, organization);
            return Response.ok().entity(convertToListOfMaps(discoveredApiMap, env)).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while discovering federated APIs for environment: "
                    + environment, e, log);
            return null;
        }
    }

    @Override
    public Response importFederatedAPIs(String environment, List<String> requestBody, MessageContext messageContext)
            throws APIManagementException {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Environment env = getEnvironmentByName(environment, organization);
            FederatedAPIDiscoveryService service = getFederatedDiscoveryService();
            if (service == null) {
                throw new APIManagementException("FederatedAPIDiscoveryService OSGi service is not available.");
            }

            service.importNewExternalAPIs(requestBody, env, organization);
            return Response.ok().entity("{\"status\": \"APIs imported successfully\"}").build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while importing federated APIs for environment: "
                    + environment, e, log);
            return null;
        }
    }

    @Override
    public Response updateFederatedAPIs(String environment, List<String> requestBody, MessageContext messageContext)
            throws APIManagementException {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Environment env = getEnvironmentByName(environment, organization);
            FederatedAPIDiscoveryService service = getFederatedDiscoveryService();
            if (service == null) {
                throw new APIManagementException("FederatedAPIDiscoveryService OSGi service is not available.");
            }

            service.updateExternalAPIs(requestBody, env, organization);
            return Response.ok().entity("{\"status\": \"APIs updated successfully\"}").build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while updating federated APIs for environment: "
                    + environment, e, log);
            return null;
        }
    }

    private List<Map<String, Object>> convertToListOfMaps(Map<String, List<DiscoveredAPI>> categorizedApis,
                                                           Environment environment) {
        List<Map<String, Object>> result = new ArrayList<>();
        String discoveredAt = Instant.now().toString();

        for (Map.Entry<String, List<DiscoveredAPI>> entry : categorizedApis.entrySet()) {
            String status = entry.getKey();
            List<DiscoveredAPI> apis = entry.getValue();

            for (DiscoveredAPI discoveredAPI : apis) {
                Map<String, Object> map = new HashMap<>();
                if (discoveredAPI.getApi() != null) {
                    String generatedId = discoveredAPI.getApi().getUuid();
                    if (generatedId == null && discoveredAPI.getApi().getId() != null) {
                        generatedId = discoveredAPI.getApi().getId().getApiName() + ":"
                                + discoveredAPI.getApi().getId().getVersion();
                    }
                    map.put("id", generatedId);

                    if (discoveredAPI.getApi().getId() != null) {
                        map.put("apiName", discoveredAPI.getApi().getId().getApiName());
                        map.put("version", discoveredAPI.getApi().getId().getVersion());
                    }
                    map.put("description", discoveredAPI.getApi().getDescription());
                    map.put("context", discoveredAPI.getApi().getContext());
                }

                map.put("gatewayName", environment.getName());
                map.put("gatewayType", environment.getGatewayType());
                map.put("discoveredAt", discoveredAt);
                map.put("status", status);
                result.add(map);
            }
        }
        return result;
    }

    private Environment getEnvironmentByName(String environmentName, String organization)
            throws APIManagementException {
        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        Environment env = environments.get(environmentName);
        if (env == null) {
            throw new APIManagementException("Environment not found: " + environmentName);
        }

        APIAdminImpl apiAdmin = new APIAdminImpl();
        env = apiAdmin.getEnvironmentWithoutPropertyMasking(organization, env.getUuid());
        return apiAdmin.decryptGatewayConfigurationValues(env);
    }

    private FederatedAPIDiscoveryService getFederatedDiscoveryService() {
        return (FederatedAPIDiscoveryService) org.wso2.carbon.context.PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(FederatedAPIDiscoveryService.class, null);
    }
}
