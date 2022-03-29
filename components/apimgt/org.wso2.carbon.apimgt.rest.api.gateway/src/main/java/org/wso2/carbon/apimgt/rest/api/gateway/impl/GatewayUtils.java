package org.wso2.carbon.apimgt.rest.api.gateway.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIMetaDataDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.ApplicationKeyMappingDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.SubscriptionInfoDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.URLMappingDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;

public class GatewayUtils {

    public static String validateTenantDomain(String xWSO2Tenant, MessageContext messageContext) {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (xWSO2Tenant == null) {
            return tenantDomain;
        } else {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return xWSO2Tenant;
            } else {
                return tenantDomain;
            }
        }
    }

    public static APIInfoDTO generateAPIInfo(API api, List<Subscription> subscriptionsByAPIId,
                                             SubscriptionDataStore subscriptionDataStore) {

        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setApiId(api.getApiId());
        apiInfoDTO.setApiType(api.getApiType());
        apiInfoDTO.setName(api.getApiName());
        apiInfoDTO.setApiUUID(api.getUuid());
        apiInfoDTO.setContext(api.getContext());
        apiInfoDTO.setIsDefaultVersion(api.isDefaultVersion());
        apiInfoDTO.setPolicy(api.getApiTier());
        apiInfoDTO.setProvider(api.getApiProvider());
        apiInfoDTO.setStatus(api.getStatus());
        apiInfoDTO.setUrlMappings(convertUriTemplate(api.getResources()));
        apiInfoDTO.setSubscripitons(convertSubscriptionsToSubscriptionInfo(subscriptionsByAPIId,
                subscriptionDataStore));
        return apiInfoDTO;
    }

    private static List<SubscriptionInfoDTO> convertSubscriptionsToSubscriptionInfo(
            List<Subscription> subscriptions, SubscriptionDataStore subscriptionDataStore) {

        List<SubscriptionInfoDTO> subscriptionInfoDTOList = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            SubscriptionInfoDTO subscriptionInfoDTO = new SubscriptionInfoDTO();
            subscriptionInfoDTO.setStatus(subscription.getSubscriptionState());
            subscriptionInfoDTO.setSubscriptionUUID(subscription.getSubscriptionUUId());
            subscriptionInfoDTO.setSubscriptionPolicy(subscription.getPolicyId());
            subscriptionInfoDTO.setApplication(convertToApplicationDto(subscription.getAppId(), subscriptionDataStore));
            subscriptionInfoDTOList.add(subscriptionInfoDTO);
        }
        return subscriptionInfoDTOList;
    }

    private static ApplicationInfoDTO convertToApplicationDto(int applicationId,
                                                              SubscriptionDataStore subscriptionDataStore) {

        ApplicationInfoDTO applicationInfoDTO = new ApplicationInfoDTO();
        Application application = subscriptionDataStore.getApplicationById(applicationId);
        if (application != null) {
            applicationInfoDTO.setId(application.getId());
            applicationInfoDTO.setName(application.getName());
            applicationInfoDTO.setPolicy(application.getPolicy());
            applicationInfoDTO.setAttributes(application.getAttributes());
            applicationInfoDTO.setSubName(application.getSubName());
            applicationInfoDTO.setUuid(application.getUUID());
            applicationInfoDTO.setTokenType(application.getTokenType());
            applicationInfoDTO.setKeys(convertToApplicationKeyMapping(applicationId, subscriptionDataStore));
        }
        return applicationInfoDTO;
    }

    private static List<ApplicationKeyMappingDTO> convertToApplicationKeyMapping(int applicationId,
                                                                                 SubscriptionDataStore subscriptionDataStore) {

        List<ApplicationKeyMapping> keyMappingByApplicationId =
                subscriptionDataStore.getKeyMappingByApplicationId(applicationId);

        return convertApplicationKeyMappingDto(keyMappingByApplicationId);
    }

    private static List<ApplicationKeyMappingDTO> convertApplicationKeyMappingDto(List<ApplicationKeyMapping> keyMappingByApplicationId) {

        List<ApplicationKeyMappingDTO> applicationKeyMappingDTOList = new ArrayList<>();
        for (ApplicationKeyMapping applicationKeyMapping : keyMappingByApplicationId) {
            applicationKeyMappingDTOList.add(new ApplicationKeyMappingDTO().keyType(applicationKeyMapping.getKeyType())
                    .consumerKey(applicationKeyMapping.getConsumerKey()).keyManager(applicationKeyMapping.getKeyManager()));
        }
        return applicationKeyMappingDTOList;
    }

    private static List<URLMappingDTO> convertUriTemplate(List<URLMapping> resources) {

        List<URLMappingDTO> urlMappingDTOList = new ArrayList<>();
        for (URLMapping resource : resources) {
            URLMappingDTO urlMappingDTO =
                    new URLMappingDTO().urlPattern(resource.getUrlPattern()).authScheme(resource.getAuthScheme())
                            .httpMethod(resource.getHttpMethod()).throttlingPolicy(resource.getThrottlingPolicy())
                            .scopes(resource.getScopes());
            urlMappingDTOList.add(urlMappingDTO);
        }
        return urlMappingDTOList;
    }

    public static APIListDTO generateAPIListDTO(List<API> apiList) {

        APIListDTO apiListDTO = new APIListDTO();
        List<APIMetaDataDTO> apiMetaDataDTOList = new ArrayList<>();
        for (API api : apiList) {
            APIMetaDataDTO apiMetaDataDTO = new APIMetaDataDTO()
                    .apiId(api.getApiId())
                    .name(api.getApiName())
                    .version(api.getApiVersion())
                    .apiUUID(api.getUuid())
                    .apiType(api.getApiType())
                    .provider(api.getApiProvider())
                    .context(api.getContext())
                    .isDefaultVersion(api.isDefaultVersion())
                    .name(api.getApiName())
                    .policy(api.getApiTier())
                    .status(api.getStatus())
                    .apiType(api.getApiType());
            apiMetaDataDTOList.add(apiMetaDataDTO);
        }
        apiListDTO.setList(apiMetaDataDTOList);
        apiListDTO.count(apiMetaDataDTOList.size());
        return apiListDTO;
    }

    public static ApplicationListDTO generateApplicationList(List<Application> applicationList,
                                                             SubscriptionDataStore subscriptionDataStore) {

        ApplicationListDTO applicationListDTO = new ApplicationListDTO();
        List<ApplicationInfoDTO> applicationInfoDTOList = new ArrayList<>();
        for (Application application : applicationList) {
            ApplicationInfoDTO applicationInfoDTO = new ApplicationInfoDTO().id(application.getId())
                    .name(application.getName())
                    .policy(application.getPolicy())
                    .attributes(application.getAttributes())
                    .subName(application.getSubName())
                    .uuid(application.getUUID())
                    .tokenType(application.getTokenType())
                    .keys(convertToApplicationKeyMapping(application.getId(), subscriptionDataStore));
            applicationInfoDTOList.add(applicationInfoDTO);
        }
        applicationListDTO.setList(applicationInfoDTOList);
        applicationListDTO.setCount(applicationInfoDTOList.size());
        return applicationListDTO;
    }

    public static SubscriptionDTO convertToSubscriptionDto(Subscription subscription) {

        return new SubscriptionDTO()
                .apiId(subscription.getApiId())
                .subscriptionState(subscription.getSubscriptionState())
                .appId(subscription.getAppId())
                .policyId(subscription.getPolicyId())
                .subscriptionId(Integer.valueOf(subscription.getSubscriptionId()));
    }
}