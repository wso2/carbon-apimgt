package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoSolaceTopicsObjectDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoSolaceURLsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SolaceTopicsDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.solace.dtos.SolaceDeployedEnvironmentDTO;
import org.wso2.carbon.apimgt.solace.dtos.SolaceTopicsObjectDTO;
import org.wso2.carbon.apimgt.solace.dtos.SolaceURLsDTO;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;
import org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils;
import org.wso2.carbon.apimgt.solace.utils.SolaceStoreUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
        List<AdditionalSubscriptionInfoDTO> additionalSubscriptionInfoDTOs = additionalSubscriptionInfoListDTO.
                getList();
        if (additionalSubscriptionInfoDTOs == null) {
            additionalSubscriptionInfoDTOs = new ArrayList<>();
            additionalSubscriptionInfoListDTO.setList(additionalSubscriptionInfoDTOs);
        }

        //Identifying the proper start and end indexes
        int size = subscriptions.size();
        if (size > 0) {
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
                api = apiConsumer.getLightweightAPIByUUID(apiId.getUUID(), organization);
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
        additionalSubscriptionInfoDTO.setIsSolaceAPI(SolaceNotifierUtils.checkWhetherAPIDeployedToSolaceUsingRevision
                (api));

        if (additionalSubscriptionInfoDTO.isIsSolaceAPI()) {
            //Set Solace organization details if API is a Solace API
            additionalSubscriptionInfoDTO.setSolaceOrganization(SolaceNotifierUtils.
                    getThirdPartySolaceBrokerOrganizationNameOfAPIDeployment(api));

            Map<String, Environment> gatewayEnvironmentMap = APIUtil.getReadOnlyGatewayEnvironments();
            Environment solaceEnvironment = null;
            for (Map.Entry<String, Environment> entry : gatewayEnvironmentMap.entrySet()) {
                if (SolaceConstants.SOLACE_ENVIRONMENT.equals(entry.getValue().getProvider())) {
                    solaceEnvironment = entry.getValue();
                }
            }

            if (solaceEnvironment != null) {
                List<SolaceDeployedEnvironmentDTO> solaceDeployedEnvironmentsDTOS = SolaceStoreUtils.
                        getSolaceDeployedEnvsInfo(solaceEnvironment, additionalSubscriptionInfoDTO.
                                getSolaceOrganization(), application.getUUID());
                List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO> solaceEnvironments = new ArrayList<>();

                for (SolaceDeployedEnvironmentDTO solaceDeployedEnvironmentEntry : solaceDeployedEnvironmentsDTOS) {
                    // Set Solace environment details
                    AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO solaceDeployedEnvironmentsDTO =
                            new AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO();
                    solaceDeployedEnvironmentsDTO.setEnvironmentName(solaceDeployedEnvironmentEntry.
                            getEnvironmentName());
                    solaceDeployedEnvironmentsDTO.setEnvironmentDisplayName(solaceDeployedEnvironmentEntry.
                            getEnvironmentDisplayName());
                    solaceDeployedEnvironmentsDTO.setOrganizationName(solaceDeployedEnvironmentEntry.
                            getOrganizationName());

                    //Set Solace URLs
                    List<AdditionalSubscriptionInfoSolaceURLsDTO> endpointUrls = new
                            ArrayList<>();
                    List<SolaceURLsDTO> solaceURLsDTOS = solaceDeployedEnvironmentEntry.getSolaceURLs();
                    for (SolaceURLsDTO entry : solaceURLsDTOS) {
                        AdditionalSubscriptionInfoSolaceURLsDTO solaceURLsDTO = new
                                AdditionalSubscriptionInfoSolaceURLsDTO();
                        solaceURLsDTO.setProtocol(entry.getProtocol());
                        solaceURLsDTO.setEndpointURL(entry.getEndpointURL());
                        endpointUrls.add(solaceURLsDTO);
                    }
                    solaceDeployedEnvironmentsDTO.setSolaceURLs(endpointUrls);

                    // Set Solace Topic Objects
                    solaceDeployedEnvironmentsDTO.setSolaceTopicsObject(mapSolaceTopicObjects(solaceDeployedEnvironmentEntry.
                            getSolaceTopicsObject()));

                    solaceEnvironments.add(solaceDeployedEnvironmentsDTO);
                }
                additionalSubscriptionInfoDTO.setSolaceDeployedEnvironments(solaceEnvironments);


            }
        }
        return additionalSubscriptionInfoDTO;
    }

    /**
     * Map SolaceTopicsObjectDTO details from Solace package to DevPortal DTOs
     *
     * @param solaceTopicsObject SolaceTopicsObjectDTO object from Solace package
     * @return AdditionalSubscriptionInfoSolaceTopicsObjectDTO object
     */
    private static AdditionalSubscriptionInfoSolaceTopicsObjectDTO mapSolaceTopicObjects(SolaceTopicsObjectDTO
                                                                                                 solaceTopicsObject) {
        AdditionalSubscriptionInfoSolaceTopicsObjectDTO solaceTopicsObjectDTO =
                new AdditionalSubscriptionInfoSolaceTopicsObjectDTO();
        // Set default syntax object
        org.wso2.carbon.apimgt.solace.dtos.SolaceTopicsDTO defaultSyntaxObject = solaceTopicsObject.getDefaultSyntax();
        SolaceTopicsDTO storeDefaultSolaceTopicObject = new SolaceTopicsDTO();
        storeDefaultSolaceTopicObject.setPublishTopics(defaultSyntaxObject.getPublishTopics());
        storeDefaultSolaceTopicObject.setSubscribeTopics(defaultSyntaxObject.getSubscribeTopics());
        solaceTopicsObjectDTO.setDefaultSyntax(storeDefaultSolaceTopicObject);

        // Set mqtt syntax object
        org.wso2.carbon.apimgt.solace.dtos.SolaceTopicsDTO mqttSyntaxObject = solaceTopicsObject.getMqttSyntax();
        SolaceTopicsDTO storeMQTTSolaceTopicObject = new SolaceTopicsDTO();
        storeMQTTSolaceTopicObject.setPublishTopics(mqttSyntaxObject.getPublishTopics());
        storeMQTTSolaceTopicObject.setSubscribeTopics(mqttSyntaxObject.getSubscribeTopics());
        solaceTopicsObjectDTO.setMqttSyntax(storeMQTTSolaceTopicObject);

        return solaceTopicsObjectDTO;
    }

    /**
     * Sets the solace environment details For Solace API subscription with the protocol details
     *
     * @param api          API object
     * @param tenantDomain Tenant Domain
     * @return List containing AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO
     * @throws APIManagementException if error occurred when retrieving protocols URLs
     */
    private static List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO>
    setSolaceEnvironmentDetailsForSubscription (API api,String tenantDomain) throws APIManagementException {

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
                        if (SolaceConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(environment.getProvider())) {
                            AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO solaceEnvironmentDTO = new
                                    AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO();
                            solaceEnvironmentDTO.setEnvironmentName(environment.getName());
                            solaceEnvironmentDTO.setEnvironmentDisplayName(environment.getDisplayName());
                            solaceEnvironmentDTO.setOrganizationName(environment.getAdditionalProperties().
                                    get(SolaceConstants.SOLACE_ENVIRONMENT_ORGANIZATION));

                            // Get Solace endpoint URLs for provided protocols
                            solaceEnvironmentDTO.setSolaceURLs(mapSolaceURLsToStoreDTO(environment.
                                    getAdditionalProperties().get(SolaceConstants.SOLACE_ENVIRONMENT_ORGANIZATION),
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
        List<AdditionalSubscriptionInfoSolaceDeployedEnvironmentsDTO> urlsList =
                setSolaceEnvironmentDetailsForSubscription(api,tenantDomain);
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
    private static List<AdditionalSubscriptionInfoSolaceURLsDTO> mapSolaceURLsToStoreDTO(String organizationName,
               String environmentName, List<String> availableProtocols) throws APIManagementException {

        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        Environment solaceEnvironment = null;
        // Get Solace broker environment details
        for (Map.Entry<String, Environment> entry : gatewayEnvironments.entrySet()) {
            if (SolaceConstants.SOLACE_ENVIRONMENT.equals(entry.getValue().getProvider())) {
                solaceEnvironment = entry.getValue();
            }
        }

        if (solaceEnvironment != null) {
            List<SolaceURLsDTO> solaceURLsDTOEntries = SolaceStoreUtils.getSolaceURLsInfo(solaceEnvironment, organizationName,
                    environmentName, availableProtocols);
            List<AdditionalSubscriptionInfoSolaceURLsDTO> solaceURLsDTOs = new ArrayList<>();

            for (SolaceURLsDTO entry : solaceURLsDTOEntries) {
                AdditionalSubscriptionInfoSolaceURLsDTO subscriptionInfoSolaceProtocolURLsDTO =
                        new AdditionalSubscriptionInfoSolaceURLsDTO();
                subscriptionInfoSolaceProtocolURLsDTO.setProtocol(entry.getProtocol());
                subscriptionInfoSolaceProtocolURLsDTO.setEndpointURL(entry.getEndpointURL());
                solaceURLsDTOs.add(subscriptionInfoSolaceProtocolURLsDTO);
            }
            return solaceURLsDTOs;
        } else {
            throw new APIManagementException("Solace Environment configurations are not provided properly");
        }
    }

    /**
     * Sets pagination urls for a AdditionalSubscriptionInfoListDTO object given pagination parameters and url
     * parameters
     *
     * @param additionalSubscriptionInfoListDTO a AdditionalSubscriptionInfoListDTO object
     * @param apiId               uuid/id of API
     * @param groupId             group id of the applications to be returned
     * @param limit               max number of objects returned
     * @param offset              starting index
     * @param size                max offset
     */
    public static void setPaginationParams(AdditionalSubscriptionInfoListDTO additionalSubscriptionInfoListDTO,
                                           String apiId, String groupId, int limit, int offset, int size) {

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
