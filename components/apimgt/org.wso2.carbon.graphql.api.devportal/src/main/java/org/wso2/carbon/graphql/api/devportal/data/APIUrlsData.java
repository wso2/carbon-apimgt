package org.wso2.carbon.graphql.api.devportal.data;


import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.APIURLsDTO;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;


import java.util.*;

public class APIUrlsData {




    public APIURLsDTO apiURLsDTO(Set<String> environmentsPublishedByAPI, String transport, String type, String context) throws APIPersistenceException, APIManagementException {

       // ArtifactData artifactData = new ArtifactData();

        //DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);
        //String tenantDomain="";
        String tenantDomain = RestApiUtil.getRequestedTenantDomain(null);

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();

        //Set<String> environmentsPublishedByAPI = devPortalAPI.getEnvironments();//ApiDetails.getEnvironments(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS));;
        environmentsPublishedByAPI.remove("none");

        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

        String http = null;
        String https = null;
        String ws = null;
        String wss = null;
        Set<String> apiTransports = new HashSet<>(Arrays.asList(transport.split(","))); //artifactData.getApiFromUUID(Id).getTransports().
        //APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
        for (String environmentName : environmentsPublishedByAPI) {
            Environment environment = environments.get(environmentName);
            if(environment !=null){
                String[] gwEndpoints = null;//environment.getApiGatewayEndpoint().split(",");
                if ("WS".equalsIgnoreCase(type)) {
                    gwEndpoints = environment.getWebsocketGatewayEndpoint().split(",");
                } else {
                    gwEndpoints = environment.getApiGatewayEndpoint().split(",");
                }

                Map<String, String> domains = new HashMap<>();
                if (tenantDomain != null) {
                    domains = apiConsumer.getTenantDomainMappings(tenantDomain,
                            APIConstants.API_DOMAIN_MAPPINGS_GATEWAY);
                }

                String customGatewayUrl = null;
                if (domains != null) {
                    customGatewayUrl = domains.get(APIConstants.CUSTOM_URL);
                }
                for (String gwEndpoint : gwEndpoints) {
                    StringBuilder endpointBuilder = new StringBuilder(gwEndpoint);
                    //endpointBuilder.append(devPortalAPI.getType());

                    if (customGatewayUrl != null) {
                        int index = endpointBuilder.indexOf("//");
                        endpointBuilder.replace(index + 2, endpointBuilder.length(), customGatewayUrl);
                        endpointBuilder.append(context.replace("/t/" + tenantDomain, ""));
                    } else {
                        endpointBuilder.append(context);
                    }


                     if (gwEndpoint.contains("http:") && apiTransports.contains("http")){
                         http = endpointBuilder.toString();
                     }else if(gwEndpoint.contains("https:") && apiTransports.contains("https")){
                         https = endpointBuilder.toString();
                     }else if (gwEndpoint.contains("ws:")) {
                         ws = endpointBuilder.toString();
                     } else if (gwEndpoint.contains("wss:")) {
                         wss = endpointBuilder.toString();
                     }
                }
            }
        }







        return  new APIURLsDTO(http,https,ws,wss);
    }

//    public String getApiUrls(String Id){
//        RegistryData registryData = new RegistryData();
//        ApiTypeWrapper apiTypeWrapper  = registryData.getApiData(Id);
//        //Set<String> env = apiTypeWrapper.getApi().getEnvironmentList();
//        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
//                .getAPIManagerConfiguration();
//        Map<String, Environment> environments = config.getApiGatewayEnvironments();
//
//        Set<String> environmentsPublishedByAPI = new HashSet<>(apiTypeWrapper.getApi().getEnvironmentList());
//        String x = null;
//        for (String environmentName : environmentsPublishedByAPI) {
//            Environment environment = environments.get(environmentName);
//            if (environment != null) {
//                x = "null";
//            }
//            else{
//                x = "not null";
//            }
//
//        }
//        return "xxx";
//    }
    public String getUrl(String Id){
        return Id;
    }
}
