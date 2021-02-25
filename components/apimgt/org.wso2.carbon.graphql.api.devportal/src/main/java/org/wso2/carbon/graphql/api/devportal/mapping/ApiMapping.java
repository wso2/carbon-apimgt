package org.wso2.carbon.graphql.api.devportal.mapping;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.graphql.api.devportal.modules.api.APIEndpointURLsDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.APIURLsDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.AdvertiseDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.BusinessInformationDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.DefaultAPIURLsDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.DeploymentClusterInfoDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.IngressUrlDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelNameDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.Pagination;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierNameDTO;
import org.wso2.carbon.graphql.api.devportal.service.LabelService;
import org.wso2.carbon.graphql.api.devportal.service.RegistryPersistenceService;
import org.wso2.carbon.graphql.api.devportal.service.TierService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApiMapping {

    public ApiDTO fromDevpotralApiTOApiDTO(DevPortalAPI devPortalAPI) throws APIManagementException, APIPersistenceException {
        String id = devPortalAPI.getId();


        String name = devPortalAPI.getApiName();

        String description = devPortalAPI.getDescription();

        String context = devPortalAPI.getContext();
        String version = devPortalAPI.getVersion();
        String type = devPortalAPI.getType();
        String provider = devPortalAPI.getProviderName();
        String transport = devPortalAPI.getTransports();

        String thumbnailUrl = devPortalAPI.getThumbnail();
        boolean hasthumbnail = HasThumbnail(thumbnailUrl);

        Set<String> environmentSet = devPortalAPI.getEnvironments();
        String environments = environmentSet.toString();
        String wsdUrl = devPortalAPI.getWsdlUrl();
        String status = devPortalAPI.getStatus();

        boolean isSubscriptionAvailable = getSubscriptionAvailable(devPortalAPI);

        Set<String> tiers = devPortalAPI.getAvailableTierNames();

        boolean isDefault =devPortalAPI.getIsDefaultVersion();

        String authorizationHeader = devPortalAPI.getAuthorizationHeader();

        String apiSecurity = devPortalAPI.getApiSecurity();

        boolean isMonetizationEnabled = devPortalAPI.isMonetizationEnabled();

        String throttlingPolicies = getThrottlingPoliciesData(tiers);;

        Set<String> categoriesSet = devPortalAPI.getApiCategories();

        String categories = getCatogories(categoriesSet);

        List<String> allkeymangersList = devPortalAPI.getKeyManagers();
        String allkeyManagers = getKeymanagers(allkeymangersList);

        String businessOwner = devPortalAPI.getBusinessOwner();
        String businessOwnerEmail = devPortalAPI.getBusinessOwnerEmail();
        String technicalOwner = devPortalAPI.getTechnicalOwner();
        String technicalOwnerEmail = devPortalAPI.getTechnicalOwnerEmail();

        BusinessInformationDTO businessInformation = new BusinessInformationDTO(businessOwner,businessOwnerEmail,technicalOwner,technicalOwnerEmail);


        boolean advertised = devPortalAPI.isAdvertiseOnly();
        String originalStoreUrl = devPortalAPI.getRedirectURL();
        String apiOwner = devPortalAPI.getApiOwner();
        AdvertiseDTO advertiseInfo = new AdvertiseDTO(advertised,originalStoreUrl,apiOwner);

        List<APIEndpointURLsDTO> apiEndPointInformation = apiEndpointURLsDTO(devPortalAPI);

        TierService tierData = new TierService();
        List<TierNameDTO>  tierInformation = tierData.getTierName(devPortalAPI);

        LabelService labelData = new LabelService();
        List<LabelNameDTO> labelNameDTO = labelData.getLabelNames(devPortalAPI);

        List<IngressUrlDTO> ingressUrlDTOS = getIngressUrlData(devPortalAPI);

        return new ApiDTO(id,name,description,context,version,provider,type,transport,hasthumbnail,environments,wsdUrl,status,isSubscriptionAvailable,isDefault,authorizationHeader,apiSecurity,isMonetizationEnabled,throttlingPolicies,thumbnailUrl,categories,allkeyManagers,businessInformation,advertiseInfo,apiEndPointInformation,tierInformation,labelNameDTO,ingressUrlDTOS);
    }

    public String getThrottlingPoliciesData(Set<String> tierNames)  {

        String throttlingPolicy = "";

        for (String tierName : tierNames) {
            throttlingPolicy += tierName;
        }

        return throttlingPolicy;
    }
    public String getKeymanagers(List<String> keyManagersList){

        String keyManagers = null;
        if(keyManagersList!=null){
            keyManagers = String.join(",",keyManagersList);
        }
        else{
            keyManagers = null;
        }

        return keyManagers;

    }

    public boolean HasThumbnail(String thumbnailUrl){

        boolean hasthumbnail;
        if (!StringUtils.isBlank(thumbnailUrl) ){
            hasthumbnail = true;
        }
        else{
            hasthumbnail = false;
        }
        return hasthumbnail;
    }

    public String getEnvironmentList(String Id) throws APIPersistenceException {

        RegistryPersistenceService artifactData = new RegistryPersistenceService();
        Set<String> environmentset = artifactData.getApiFromUUID(Id).getEnvironments();
        String environments = null;
        if(environmentset!=null){
            List<String> environmentList = new ArrayList<>(environmentset);
            environments = String.join(",",environmentList);
        }else{
            environments = null;
        }
        return environments;


    }
    public static Set<String> getEnvironments(String environments) {
        if(environments != null) {
            String[] publishEnvironmentArray = environments.split(",");
            return new HashSet<String>(Arrays.asList(publishEnvironmentArray));
        }
        return null;
    }

    public String getCatogories(Set<String> catogoriesSet){
        String catogories = null;

        if (catogoriesSet!=null){
            catogories = String.join(",",catogoriesSet);
        }
        return catogories;
    }
    public boolean getSubscriptionAvailable(DevPortalAPI devPortalAPI) {
        String apiTenant = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(devPortalAPI.getProviderName()));
        String subscriptionAvailability = devPortalAPI.getSubscriptionAvailability();
        String subscriptionAllowedTenants =devPortalAPI.getSubscriptionAvailableOrgs();
        boolean IsSubscriptionAvailability = isSubscriptionAvailable(apiTenant,subscriptionAvailability,subscriptionAllowedTenants);
        return IsSubscriptionAvailability;
    }
    private static boolean isSubscriptionAvailable(String apiTenant, String subscriptionAvailability,
                                                   String subscriptionAllowedTenants) {

        String userTenant = RestApiUtil.getRequestedTenantDomain("");
        boolean subscriptionAllowed = false;
        if (!userTenant.equals(apiTenant)) {
            if (APIConstants.SUBSCRIPTION_TO_ALL_TENANTS.equals(subscriptionAvailability)) {
                subscriptionAllowed = true;
            } else if (APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS.equals(subscriptionAvailability)) {
                String allowedTenants[] = null;
                if (subscriptionAllowedTenants != null) {
                    allowedTenants = subscriptionAllowedTenants.split(",");
                    if (allowedTenants != null) {
                        for (String tenant : allowedTenants) {
                            if (tenant != null && tenant.trim().equals(userTenant)) {
                                subscriptionAllowed = true;
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            subscriptionAllowed = true;
        }
        return subscriptionAllowed;
    }

    public List<APIEndpointURLsDTO> apiEndpointURLsDTO(DevPortalAPI devPortalAPI) throws APIManagementException {

        String Id = devPortalAPI.getId();
        String tenantDomain = RestApiUtil.getRequestedTenantDomain(null);

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();

        Set<String> environmentsPublishedByAPI = devPortalAPI.getEnvironments();//new HashSet<>(apiTypeWrapper.getApi().getEnvironments());
        environmentsPublishedByAPI.remove("none");

        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

        List<APIEndpointURLsDTO> apiEndpointURLsDTOS = new ArrayList<APIEndpointURLsDTO>();

        Set<String> apiTransports = new HashSet<>(Arrays.asList(devPortalAPI.getTransports().split(",")));
        for (String environmentName : environmentsPublishedByAPI) {
            Environment environment = environments.get(environmentName);
            if(environment!=null){
                String envName = environment.getName();
                String environmentType = environment.getType();

                APIURLsDTO apiurLsDTO = new APIURLsDTO();

                DefaultAPIURLsDTO defaultAPIURLsDTO =  new DefaultAPIURLsDTO();

                String[] gwEndpoints = null;
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

                    if (customGatewayUrl != null) {
                        int index = endpointBuilder.indexOf("//");
                        endpointBuilder.replace(index + 2, endpointBuilder.length(), customGatewayUrl);
                        endpointBuilder.append(devPortalAPI.getContext().replace("/t/" + tenantDomain, ""));
                    } else {
                        endpointBuilder.append(devPortalAPI.getContext());
                    }


                    if (gwEndpoint.contains("http:") && apiTransports.contains("http")){
                        apiurLsDTO.setHttp(endpointBuilder.toString());
                    }else if(gwEndpoint.contains("https:") && apiTransports.contains("https")){
                        apiurLsDTO.setHttps(endpointBuilder.toString());
                    }else if (gwEndpoint.contains("ws:")) {
                        apiurLsDTO.setWs(endpointBuilder.toString());
                    } else if (gwEndpoint.contains("wss:")) {
                        apiurLsDTO.setWss(endpointBuilder.toString());
                    }
                    if(devPortalAPI.getIsDefaultVersion()){

                        int index = endpointBuilder.lastIndexOf(devPortalAPI.getVersion());
                        endpointBuilder.replace(index, endpointBuilder.length(), "");
                        endpointBuilder.replace(index, endpointBuilder.length(), "");
                        if (gwEndpoint.contains("http:") && apiTransports.contains("http")){
                            defaultAPIURLsDTO.setHttp(endpointBuilder.toString());
                        }else if(gwEndpoint.contains("https:") && apiTransports.contains("https")){
                            defaultAPIURLsDTO.setHttps(endpointBuilder.toString());
                        }else if (gwEndpoint.contains("ws:")) {
                            defaultAPIURLsDTO.setWs(endpointBuilder.toString());
                        } else if (gwEndpoint.contains("wss:")) {
                            defaultAPIURLsDTO.setWss(endpointBuilder.toString());
                        }
                    }
                }

                apiEndpointURLsDTOS.add(new APIEndpointURLsDTO(Id,envName, environmentType, apiurLsDTO,defaultAPIURLsDTO));

            }
        }
        return  apiEndpointURLsDTOS;
    }

    public List<IngressUrlDTO>  getIngressUrlData(DevPortalAPI devPortalAPI) throws APIManagementException, APIPersistenceException {

        List<IngressUrlDTO> apiDeployedIngressURLs = new ArrayList<>();

        Set<org.wso2.carbon.apimgt.persistence.dto.DeploymentEnvironments> deploymentEnvironments = devPortalAPI.getDeploymentEnvironments();//extractDeploymentsForAPI(deployments);

        if (deploymentEnvironments != null && !deploymentEnvironments.isEmpty()) {
            Set<org.wso2.carbon.apimgt.persistence.dto.DeploymentEnvironments> selectedDeploymentEnvironments = deploymentEnvironments;
            if (selectedDeploymentEnvironments != null && !selectedDeploymentEnvironments.isEmpty()) {
                for (org.wso2.carbon.apimgt.persistence.dto.DeploymentEnvironments deploymentEnvironment : selectedDeploymentEnvironments) {
                    IngressUrlDTO ingressUrlDTO = new IngressUrlDTO();
                    JSONArray clusterConfigs = APIUtil.getAllClustersFromConfig();
                    for (Object clusterConfig : clusterConfigs) {
                        JSONObject clusterConf = (JSONObject) clusterConfig;
                        List<DeploymentClusterInfoDTO> clusterInfoArray = new ArrayList<>();
                        if (clusterConf.get(ContainerBasedConstants.TYPE).toString().equalsIgnoreCase(deploymentEnvironment.getType())) {
                            JSONArray containerMgtInfoArray = (JSONArray) (clusterConf.get(ContainerBasedConstants.CONTAINER_MANAGEMENT_INFO));
                            for (Object containerMgtInfoObj : containerMgtInfoArray) {
                                JSONObject containerMgtInfo = (JSONObject) containerMgtInfoObj;
                                DeploymentClusterInfoDTO deploymentClusterInfoDTO = new DeploymentClusterInfoDTO();
                                if (deploymentEnvironment.getClusterNames().contains(containerMgtInfo.get(ContainerBasedConstants.CLUSTER_NAME).toString())) {

                                    String ClusterName =  containerMgtInfo.get(ContainerBasedConstants.CLUSTER_NAME).toString();
                                    deploymentClusterInfoDTO.setClusterName(ClusterName);
                                    String ClusterDisplayName = containerMgtInfo.get(ContainerBasedConstants.DISPLAY_NAME).toString();
                                    deploymentClusterInfoDTO.setClusterDisplayName(ClusterDisplayName);
                                    String IngressURL=null;
                                    if(((JSONObject) containerMgtInfo.get(ContainerBasedConstants.PROPERTIES)).get(ContainerBasedConstants.ACCESS_URL) != null){
                                        IngressURL = ((JSONObject) containerMgtInfo.get(ContainerBasedConstants.PROPERTIES))
                                                .get(ContainerBasedConstants.ACCESS_URL).toString();
                                        deploymentClusterInfoDTO.setIngressURL(IngressURL);
                                    }
                                    clusterInfoArray.add(deploymentClusterInfoDTO);
                                }
                            }
                            if (!clusterInfoArray.isEmpty()) {
                                ingressUrlDTO.setClusterDetails(clusterInfoArray);
                                ingressUrlDTO.setDeploymentEnvironmentName(deploymentEnvironment.getType());
                            }
                        }

                    }
                    if (ingressUrlDTO.getDeploymentEnvironmentName() != null && !ingressUrlDTO.getClusterDetails()
                            .isEmpty()) {
                        apiDeployedIngressURLs.add(ingressUrlDTO);
                    }

                }
            }



        }
        return apiDeployedIngressURLs;
    }
    public Pagination getPaginationData(int offset, int limit) throws APIPersistenceException, APIManagementException {
        RegistryPersistenceService artifactData = new RegistryPersistenceService();
        int size = artifactData.apiCount(offset, limit);
        String paginatedPrevious = "";
        String paginatedNext = "";
        String query = "";

        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }
        return new Pagination(offset,limit,size,paginatedNext,paginatedPrevious);
    }
}
