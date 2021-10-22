package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils;

import java.io.IOException;
import java.util.*;


public class AdditionalSubscriptionInfoMappingUtil {

    private static final Log log = LogFactory.getLog(AdditionalSubscriptionInfoMappingUtil.class);

    /**
     * Converts a List object of SubscribedAPIs into a DTO
     *
     * @param subscriptions a list of SubscribedAPI objects
     * @param limit         max number of objects returned
     * @param offset        starting index
     * @param organization  identifier of the organization
     * @return SubscriptionListDTO object containing SubscriptionDTOs
     * @throws APIManagementException if error occurred when creating AdditionalSubscriptionInfoListDTO
     */
    public static AdditionalSubscriptionInfoListDTO fromAdditionalSubscriptionInfoListToDTO(List<SubscribedAPI>
         subscriptions, Integer limit, Integer offset, String organization) throws APIManagementException {

        AdditionalSubscriptionInfoListDTO additionalSubscriptionInfoListDTO = new AdditionalSubscriptionInfoListDTO();
        List<AdditionalSubscriptionInfoDTO> additionalSubscriptionInfoDTOs = additionalSubscriptionInfoListDTO.getList();
        if (additionalSubscriptionInfoDTOs == null) {
            additionalSubscriptionInfoDTOs = new ArrayList<>();
            additionalSubscriptionInfoListDTO.setList(additionalSubscriptionInfoDTOs);
        }

        //Identifying the proper start and end indexes
        int size = subscriptions.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit -1 : size - 1;

        for (int i = start; i <= end; i++) {
            try {
                SubscribedAPI subscription = subscriptions.get(i);
                additionalSubscriptionInfoDTOs.add(fromAdditionalSubscriptionInfoToDTO(subscription, organization));
            } catch (APIManagementException e) {
                log.error("Error while obtaining additional info of subscriptions", e);
            }
        }
        // Set count for list
        additionalSubscriptionInfoListDTO.setCount(additionalSubscriptionInfoDTOs.size());
        return additionalSubscriptionInfoListDTO;
    }

    /**
     * Converts a AdditionalSubscriptionInfo object into AdditionalSubscriptionInfoDTO
     *
     * @param subscription SubscribedAPI object
     * @param organization Identifier of the organization
     * @return SubscriptionDTO corresponds to SubscribedAPI object
     */
    public static AdditionalSubscriptionInfoDTO fromAdditionalSubscriptionInfoToDTO(SubscribedAPI subscription,
                                                String organization) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        AdditionalSubscriptionInfoDTO additionalSubscriptionInfoDTO = new AdditionalSubscriptionInfoDTO();
        additionalSubscriptionInfoDTO.setSubscriptionId(subscription.getUUID());
        APIIdentifier apiId = subscription.getApiId();
        API api = null;

        if (apiId != null) {
            try {
                api = apiConsumer.getLightweightAPI(apiId,organization);
                api = apiConsumer.getAPIbyUUID(api.getUuid(), organization);
            } catch (APIManagementException e) {
                String msg = "User :" + username + " does not have access to the API " + apiId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }

        additionalSubscriptionInfoDTO.setApiId(api.getUuid());
        // Set Application information
        Application application = subscription.getApplication();
        application = apiConsumer.getApplicationByUUID(application.getUUID());
        additionalSubscriptionInfoDTO.setApplicationId(subscription.getApplication().getUUID());
        additionalSubscriptionInfoDTO.setApplicationName(application.getName());
        additionalSubscriptionInfoDTO.setIsSolaceAPI(SolaceNotifierUtils.checkWhetherAPIDeployedToSolaceUsingRevision(api));

        if (additionalSubscriptionInfoDTO.isIsSolaceAPI()) {
            //Set Solace organization details if API is a Solace API
            additionalSubscriptionInfoDTO.setSolaceOrganization(SolaceNotifierUtils.getThirdPartySolaceBrokerOrganizationNameOfAPIDeployment(api));

            Map<String, Environment> gatewayEnvironmentMap = APIUtil.getReadOnlyGatewayEnvironments();
            Environment solaceEnvironment = null;
            for (Map.Entry<String,Environment> entry: gatewayEnvironmentMap.entrySet()) {
                if (APIConstants.SOLACE_ENVIRONMENT.equals(entry.getValue().getProvider())) {
                    solaceEnvironment = entry.getValue();
                }
            }

            if (solaceEnvironment != null) {
                // Create solace admin APIs instance
                SolaceAdminApis solaceAdminApis = new SolaceAdminApis(solaceEnvironment.getServerURL(), solaceEnvironment.getUserName(),
                        solaceEnvironment.getPassword(), solaceEnvironment.getAdditionalProperties().
                        get(APIConstants.SOLACE_ENVIRONMENT_DEV_NAME));
                HttpResponse response = solaceAdminApis.applicationGet(additionalSubscriptionInfoDTO.
                        getSolaceOrganization(), application.getUUID(), "default");
                List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO> solaceEnvironments = new ArrayList<>();
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    try {
                        String responseString = EntityUtils.toString(response.getEntity());
                        org.json.JSONObject jsonObject = new org.json.JSONObject(responseString);
                        // Get solace environments attached with the Solace application
                        if (jsonObject.getJSONArray("environments") != null) {
                            JSONArray environmentsArray = jsonObject.getJSONArray("environments");
                            for (int i = 0; i < environmentsArray.length(); i++) {
                                AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO solaceDeployedEnvironmentsDTO =
                                        new AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO();
                                org.json.JSONObject environmentObject = environmentsArray.getJSONObject(i);
                                // Get details of Solace environment attached to the solace application
                                if (environmentObject.getString("name") != null) {
                                    String environmentName = environmentObject.getString("name");
                                    Environment gatewayEnvironment = gatewayEnvironmentMap.get(environmentName);
                                    if (gatewayEnvironment != null) {
                                        // Set Solace environment details
                                        solaceDeployedEnvironmentsDTO.setEnvironmentName(gatewayEnvironment.getName());
                                        solaceDeployedEnvironmentsDTO.setEnvironmentDisplayName(gatewayEnvironment.getDisplayName());
                                        solaceDeployedEnvironmentsDTO.setOrganizationName(gatewayEnvironment.
                                                getAdditionalProperties().get(APIConstants.SOLACE_ENVIRONMENT_ORGANIZATION));

                                        boolean containsMQTTProtocol = false;
                                        // Get messaging protocols from the response body
                                        if (environmentObject.getJSONArray("messagingProtocols") != null) {
                                            List<AdditionalSubscriptionInfoSolaceURLsDTO> endpointUrls = new ArrayList<>();
                                            JSONArray protocolsArray = environmentObject.getJSONArray("messagingProtocols");
                                            for (int j = 0; j < protocolsArray.length(); j++) {
                                                AdditionalSubscriptionInfoSolaceURLsDTO solaceURLsDTO = new AdditionalSubscriptionInfoSolaceURLsDTO();
                                                String protocol = protocolsArray.getJSONObject(j).getJSONObject("protocol").getString("name");
                                                if (APIConstants.MQTT_TRANSPORT_PROTOCOL_NAME.equalsIgnoreCase(protocol)) {
                                                    containsMQTTProtocol = true;
                                                }
                                                String uri = protocolsArray.getJSONObject(j).getString("uri");
                                                solaceURLsDTO.setProtocol(protocol);
                                                solaceURLsDTO.setEndpointURL(uri);
                                                endpointUrls.add(solaceURLsDTO);
                                            }
                                            solaceDeployedEnvironmentsDTO.setSolaceURLs(endpointUrls);
                                        }
                                        // Get topic permissions from the solace application response body
                                        if (environmentObject.getJSONObject("permissions") != null) {
                                            org.json.JSONObject permissionsObject = environmentObject.getJSONObject("permissions");
                                            AdditionalSubscriptionInfoSolaceTopicsObjectDTO solaceTopicsObjectDTO = new AdditionalSubscriptionInfoSolaceTopicsObjectDTO();
                                            populateSolaceTopics(solaceTopicsObjectDTO, permissionsObject, "default");
                                            // Handle the special case of MQTT protocol
                                            if (containsMQTTProtocol) {
                                                HttpResponse responseForMqtt = solaceAdminApis.applicationGet
                                                        (additionalSubscriptionInfoDTO.getSolaceOrganization(),
                                                                application.getUUID(), APIConstants.
                                                                        MQTT_TRANSPORT_PROTOCOL_NAME.toUpperCase());

                                                org.json.JSONObject permissionsObjectForMqtt =
                                                        extractPermissionsFromSolaceApplicationGetResponse(
                                                        responseForMqtt, i, gatewayEnvironmentMap);

                                                if (permissionsObjectForMqtt != null) {
                                                    populateSolaceTopics(solaceTopicsObjectDTO, permissionsObjectForMqtt,
                                                            APIConstants.MQTT_TRANSPORT_PROTOCOL_NAME.toUpperCase());
                                                }
                                            }
                                            solaceDeployedEnvironmentsDTO.setSolaceTopicsObject(solaceTopicsObjectDTO);
                                        }
                                    }
                                }
                                solaceEnvironments.add(solaceDeployedEnvironmentsDTO);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    additionalSubscriptionInfoDTO.setSolaceDeployedEnvironments(solaceEnvironments);
                } else {
                    throw new APIManagementException("Solace Environment configurations are not provided properly");
                }
            } else {
                throw new APIManagementException("Solace broker Environment is not provided");
            }
        }
        return additionalSubscriptionInfoDTO;
    }

    /**
     * Sets the solace environment details For Solace API subscription with the protocol details
     *
     * @param api          API object
     * @param tenantDomain Tenant Domain
     * @return List containing AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO
     * @throws APIManagementException if error occurred when retrieving protocols URLs
     */
    private static List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO> setSolaceEnvironmentDetailsForSubscription
                    (API api,String tenantDomain) throws APIManagementException {

        APIDTO apidto = APIMappingUtil.fromAPItoDTO(api, tenantDomain);
        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        List<APIRevisionDeployment> revisionDeployments = apiConsumer.getAPIRevisionDeploymentListOfAPI(apidto.getId());
        List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO> solaceEndpointURLsList = new ArrayList<>();

        // Get revision list of APIs and check Solace deployment environment
        for (APIRevisionDeployment revisionDeployment : revisionDeployments) {
            if (revisionDeployment.isDisplayOnDevportal()) {
                if (gatewayEnvironments != null) {
                    // Deployed environment
                    Environment environment = gatewayEnvironments.get(revisionDeployment.getDeployment());
                    if (environment != null) {
                        // Set solace environment details if deployment is in Solace broker
                        if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(environment.getProvider())) {
                            AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO solaceEnvironmentDTO = new
                                    AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO();
                            solaceEnvironmentDTO.setEnvironmentName(environment.getName());
                            solaceEnvironmentDTO.setEnvironmentDisplayName(environment.getDisplayName());
                            solaceEnvironmentDTO.setOrganizationName(environment.getAdditionalProperties().
                                    get(APIConstants.SOLACE_ENVIRONMENT_ORGANIZATION));

                            // Get Solace endpoint URLs for provided protocols
                            solaceEnvironmentDTO.setSolaceURLs(getSolaceURLs(environment.
                                    getAdditionalProperties().get(APIConstants.SOLACE_ENVIRONMENT_ORGANIZATION),
                                    environment.getName(), apidto.getAsyncTransportProtocols()));
                            solaceEndpointURLsList.add(solaceEnvironmentDTO);
                        }
                    }
                }
            }
        }
        return solaceEndpointURLsList;
    }


    /**
     * Sets the Endpoint URLs for the APIDTO object using solace protocols
     *
     * @param api          API object
     * @param tenantDomain Tenant Domain
     * @return List containing AdditionalSubscriptionInfoSolaceEndpointURLsDTOs
     * @throws APIManagementException if error occurred when retrieving protocols URLs
     */
    public static List<String> setEndpointURLsForApiDto(API api, String tenantDomain) throws APIManagementException {
        List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO> urlsList = setSolaceEnvironmentDetailsForSubscription(api,tenantDomain);
        List<String> urlsStringList = new ArrayList<>();
        if (!urlsList.isEmpty()) {
            for (AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO item: urlsList) {
                List<AdditionalSubscriptionInfoSolaceURLsDTO> protocols = item.getSolaceURLs();
                for (AdditionalSubscriptionInfoSolaceURLsDTO protocol : protocols) {
                    // Create Json string to return
                    JSONObject asyncProtocolsObj =new JSONObject();
                    asyncProtocolsObj.put("protocol", protocol.getProtocol());
                    asyncProtocolsObj.put("endPointUrl", protocol.getEndpointURL());
                    urlsStringList.add(asyncProtocolsObj.toString());
                }
            }
        }
        return urlsStringList;
    }
    /**
     * Sets the Endpoint URLs For Solace API according to the protocols
     *
     * @param organizationName Solace broker organization name
     * @param environmentName      Name of the  Solace environment
     * @param availableProtocols List of available protocols
     * @return List containing AdditionalSubscriptionInfoSolaceURLsDTO
     * @throws APIManagementException if error occurred when retrieving protocols URLs from Solace broker
     */
    private static List<AdditionalSubscriptionInfoSolaceURLsDTO> getSolaceURLs(String organizationName,
                   String environmentName, List<String> availableProtocols) throws APIManagementException {

        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        Environment solaceEnvironment = null;
        // Get Solace broker environment details
        for (Map.Entry<String,Environment> entry: gatewayEnvironments.entrySet()) {
            if (APIConstants.SOLACE_ENVIRONMENT.equals(entry.getValue().getProvider())) {
                solaceEnvironment = entry.getValue();
            }
        }

        if (solaceEnvironment != null) {
            // Create solace admin APIs instance
            SolaceAdminApis solaceAdminApis = new SolaceAdminApis(solaceEnvironment.getServerURL(), solaceEnvironment.
                    getUserName(), solaceEnvironment.getPassword(), solaceEnvironment.getAdditionalProperties().
                    get(APIConstants.SOLACE_ENVIRONMENT_DEV_NAME));
            List<AdditionalSubscriptionInfoSolaceURLsDTO> solaceURLsDTOs = new ArrayList<>();
            HttpResponse response = solaceAdminApis.environmentGET(organizationName, environmentName);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseString = null;
                try {
                    responseString = EntityUtils.toString(response.getEntity());
                    org.json.JSONObject jsonObject = new org.json.JSONObject(responseString);
                    JSONArray protocols = jsonObject.getJSONArray("messagingProtocols");
                    for (int i = 0; i < protocols.length(); i++) {
                        org.json.JSONObject protocolDetails = protocols.getJSONObject(i);
                        String protocolName = protocolDetails.getJSONObject("protocol").getString("name");
                        // Get solace protocol URLs for available protocols
                        if (availableProtocols.contains(protocolName)) {
                            String endpointURI = protocolDetails.getString("uri");
                            AdditionalSubscriptionInfoSolaceURLsDTO subscriptionInfoSolaceProtocolURLsDTO =
                                    new AdditionalSubscriptionInfoSolaceURLsDTO();
                            subscriptionInfoSolaceProtocolURLsDTO.setProtocol(protocolName);
                            subscriptionInfoSolaceProtocolURLsDTO.setEndpointURL(endpointURI);
                            solaceURLsDTOs.add(subscriptionInfoSolaceProtocolURLsDTO);
                        }
                    }
                } catch (IOException e) {
                    throw new APIManagementException("Error occurred when retrieving protocols URLs from Solace " +
                            "admin apis");
                }
            }
            return solaceURLsDTOs;
        } else {
            throw new APIManagementException("Solace Environment configurations are not provided properly");
        }
    }

    private static org.json.JSONObject extractPermissionsFromSolaceApplicationGetResponse
            (HttpResponse response, int environmentIndex, Map<String, Environment> gatewayEnvironmentMap) throws IOException {

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String responseString = EntityUtils.toString(response.getEntity());
            org.json.JSONObject jsonObject = new org.json.JSONObject(responseString);
            if (jsonObject.getJSONArray("environments") != null) {
                JSONArray environmentsArray = jsonObject.getJSONArray("environments");
                org.json.JSONObject environmentObject = environmentsArray.getJSONObject(environmentIndex);
                if (environmentObject.getString("name") != null) {
                    String environmentName = environmentObject.getString("name");
                    Environment gatewayEnvironment = gatewayEnvironmentMap.get(environmentName);
                    if (gatewayEnvironment != null) {
                        if (environmentObject.getJSONObject("permissions") != null) {
                            return environmentObject.getJSONObject("permissions");
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void populateSolaceTopics(AdditionalSubscriptionInfoSolaceTopicsObjectDTO subscriptionInfoSolaceTopicsObjectDTO
            , org.json.JSONObject permissionsObject, String syntax) {

        SolaceTopicsDTO topicsDTO = new SolaceTopicsDTO();
        if (permissionsObject.getJSONArray("publish") != null) {
            List<String> publishTopics = new ArrayList<>();
            for (int j = 0; j < permissionsObject.getJSONArray("publish").length(); j++) {
                org.json.JSONObject channelObject = permissionsObject.getJSONArray("publish").getJSONObject(j);
                for (Object x : channelObject.keySet()) {
                    org.json.JSONObject channel = channelObject.getJSONObject(x.toString());
                    JSONArray channelPermissions = channel.getJSONArray("permissions");
                    for (int k = 0; k < channelPermissions.length(); k++) {
                        publishTopics.add(channelPermissions.getString(k));
                    }
                }
            }
            topicsDTO.setPublishTopics(publishTopics);
        }
        if (permissionsObject.getJSONArray("subscribe") != null) {
            List<String> subscribeTopics = new ArrayList<>();
            for (int j = 0; j < permissionsObject.getJSONArray("subscribe").length(); j++) {
                org.json.JSONObject channelObject = permissionsObject.getJSONArray("subscribe").getJSONObject(j);
                for (Object x : channelObject.keySet()) {
                    org.json.JSONObject channel = channelObject.getJSONObject(x.toString());
                    JSONArray channelPermissions = channel.getJSONArray("permissions");
                    for (int k = 0; k < channelPermissions.length(); k++) {
                        subscribeTopics.add(channelPermissions.getString(k));
                    }
                }
            }
            topicsDTO.setSubscribeTopics(subscribeTopics);
        }
        if (APIConstants.MQTT_TRANSPORT_PROTOCOL_NAME.equalsIgnoreCase(syntax)) {
            subscriptionInfoSolaceTopicsObjectDTO.setMqttSyntax(topicsDTO);
        } else {
            subscriptionInfoSolaceTopicsObjectDTO.setDefaultSyntax(topicsDTO);
        }
    }

    /**
     * Sets pagination urls for a AdditionalSubscriptionInfoListDTO object given pagination parameters and url parameters
     *
     * @param additionalSubscriptionInfoListDTO a AdditionalSubscriptionInfoListDTO object
     * @param apiId               uuid/id of API
     * @param groupId             group id of the applications to be returned
     * @param limit               max number of objects returned
     * @param offset              starting index
     * @param size                max offset
     */
    public static void setPaginationParams(AdditionalSubscriptionInfoListDTO additionalSubscriptionInfoListDTO, String apiId,
                                           String groupId, int limit, int offset, int size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getSubscriptionPaginatedURLForAPIId(
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), apiId, groupId);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getSubscriptionPaginatedURLForAPIId(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), apiId, groupId);
        }

        PaginationDTO pagination = new PaginationDTO();
        pagination.setOffset(offset);
        pagination.setLimit(limit);
        pagination.setNext(paginatedNext);
        pagination.setPrevious(paginatedPrevious);
        pagination.setTotal(size);
        additionalSubscriptionInfoListDTO.setPagination(pagination);
    }

}
