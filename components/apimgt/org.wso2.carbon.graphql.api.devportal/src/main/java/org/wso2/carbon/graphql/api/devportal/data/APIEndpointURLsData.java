package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.APIEndpointURLsDTO;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.graphql.api.devportal.modules.APIURLsDTO;
import org.wso2.carbon.graphql.api.devportal.modules.DefaultAPIURLsDTO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.*;

public class APIEndpointURLsData {

    public List<APIEndpointURLsDTO> apiEndpointURLsDTO(DevPortalAPI devPortalAPI) throws APIPersistenceException, APIManagementException {

        String env = "";


        String Id = devPortalAPI.getId();

        //ArtifactData artifactData = new ArtifactData();
        String tenantDomain = RestApiUtil.getRequestedTenantDomain(null);

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();

        Set<String> environmentsPublishedByAPI = devPortalAPI.getEnvironments();//new HashSet<>(apiTypeWrapper.getApi().getEnvironments());
        environmentsPublishedByAPI.remove("none");

        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

        List<APIEndpointURLsDTO> apiEndpointURLsDTOS = new ArrayList<APIEndpointURLsDTO>();
        //APIUrlsData apiUrlsData = new APIUrlsData();
       // DefaultAPIURLsData defaultAPIURLsData = new DefaultAPIURLsData();

        Set<String> apiTransports = new HashSet<>(Arrays.asList(devPortalAPI.getTransports().split(","))); //artifactData.getApiFromUUID(Id).getTransports().
//
//        String http = null;
//        String https = null;
//        String ws = null;
//        String wss = null;
//
//        String dhttp = null;
//        String dhttps = null;
//        String dws = null;
//        String dwss = null;
        for (String environmentName : environmentsPublishedByAPI) {
            env += environmentName;
            Environment environment = environments.get(environmentName);
            if(environment!=null){
                String envName = environment.getName();
                String environmentType = environment.getType();

                APIURLsDTO apiurLsDTO = new APIURLsDTO();

                DefaultAPIURLsDTO defaultAPIURLsDTO =  new DefaultAPIURLsDTO();

                String[] gwEndpoints = null;//environment.getApiGatewayEndpoint().split(",");
                if ("WS".equalsIgnoreCase(devPortalAPI.getType())) {
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
                        endpointBuilder.append(devPortalAPI.getContext().replace("/t/" + tenantDomain, ""));
                    } else {
                        endpointBuilder.append(devPortalAPI.getContext());
                    }


                    if (gwEndpoint.contains("http:") && apiTransports.contains("http")){
                        //http = endpointBuilder.toString();
                        apiurLsDTO.setHttp(endpointBuilder.toString());
                    }else if(gwEndpoint.contains("https:") && apiTransports.contains("https")){
                        apiurLsDTO.setHttps(endpointBuilder.toString());
                        //https = endpointBuilder.toString();
                    }else if (gwEndpoint.contains("ws:")) {
                       // ws = endpointBuilder.toString();
                        apiurLsDTO.setWs(endpointBuilder.toString());
                    } else if (gwEndpoint.contains("wss:")) {
                        //wss = endpointBuilder.toString();
                        apiurLsDTO.setWss(endpointBuilder.toString());
                    }
                    if(devPortalAPI.getIsDefaultVersion()){

                        int index = endpointBuilder.lastIndexOf(devPortalAPI.getVersion());
                        endpointBuilder.replace(index, endpointBuilder.length(), "");
                        //int index = endpointBuilder.indexOf(devPortalAPI.getVersion());
                        endpointBuilder.replace(index, endpointBuilder.length(), "");
                        if (gwEndpoint.contains("http:") && apiTransports.contains("http")){
                            //dhttp = endpointBuilder.toString();
                            defaultAPIURLsDTO.setHttp(endpointBuilder.toString());
                        }else if(gwEndpoint.contains("https:") && apiTransports.contains("https")){
                            //dhttps = endpointBuilder.toString();
                            defaultAPIURLsDTO.setHttps(endpointBuilder.toString());
                        }else if (gwEndpoint.contains("ws:")) {
                           // dws = endpointBuilder.toString();
                            defaultAPIURLsDTO.setWs(endpointBuilder.toString());
                        } else if (gwEndpoint.contains("wss:")) {
                           // dwss = endpointBuilder.toString();
                            defaultAPIURLsDTO.setWss(endpointBuilder.toString());
                        }
                    }
                }

                apiEndpointURLsDTOS.add(new APIEndpointURLsDTO(Id,envName, environmentType, apiurLsDTO,defaultAPIURLsDTO));

            }
        }
        return  apiEndpointURLsDTOS;
    }
}
