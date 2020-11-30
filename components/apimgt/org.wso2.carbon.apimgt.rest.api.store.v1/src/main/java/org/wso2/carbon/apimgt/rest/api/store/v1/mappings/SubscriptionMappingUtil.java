/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;

/** This class is responsible for mapping APIM core subscription related objects into REST API subscription related DTOs 
 *
 */
public class SubscriptionMappingUtil {

    private static final Log log = LogFactory.getLog(SubscriptionMappingUtil.class);

    /** Converts a SubscribedAPI object into SubscriptionDTO
     *
     * @param subscription SubscribedAPI object
     * @return SubscriptionDTO corresponds to SubscribedAPI object
     */
    public static SubscriptionDTO fromSubscriptionToDTO(SubscribedAPI subscription)
            throws APIManagementException {
        APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getUUID());
        APIIdentifier apiId = subscription.getApiId();
        APIProductIdentifier apiProdId = subscription.getProductId();
        if (apiId != null) {
            API api = apiConsumer.getLightweightAPI(apiId);
            subscriptionDTO.setApiId(api.getUUID());
            APIInfoDTO apiInfo = APIMappingUtil.fromAPIToInfoDTO(api);
            subscriptionDTO.setApiInfo(apiInfo);
        }
        if (apiProdId != null) {
            APIProduct apiProduct = apiConsumer.getAPIProduct(apiProdId);
            subscriptionDTO.setApiId(apiProduct.getUuid());
            APIInfoDTO apiInfo = APIMappingUtil.fromAPIToInfoDTO(apiProduct);
            subscriptionDTO.setApiInfo(apiInfo);
        }
        Application application = subscription.getApplication();
        application = apiConsumer.getLightweightApplicationByUUID(application.getUUID());
        subscriptionDTO.setApplicationId(subscription.getApplication().getUUID());
        subscriptionDTO.setStatus(SubscriptionDTO.StatusEnum.valueOf(subscription.getSubStatus()));
        subscriptionDTO.setThrottlingPolicy(subscription.getTier().getName());
        subscriptionDTO.setRequestedThrottlingPolicy(subscription.getRequestedTier().getName());


        ApplicationInfoDTO applicationInfoDTO = ApplicationMappingUtil.fromApplicationToInfoDTO(application);
        subscriptionDTO.setApplicationInfo(applicationInfoDTO);

        return subscriptionDTO;
    }

    public static SubscriptionDTO fromSubscriptionToDTO(SubscribedAPI subscription, ApiTypeWrapper apiTypeWrapper)
            throws APIManagementException {
        APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getUUID());
        if (apiTypeWrapper !=null && !apiTypeWrapper.isAPIProduct()) {
            API api = apiTypeWrapper.getApi();
            subscriptionDTO.setApiId(api.getUUID());
            APIInfoDTO apiInfo = APIMappingUtil.fromAPIToInfoDTO(api);
            subscriptionDTO.setApiInfo(apiInfo);
        } else {
            APIProduct apiProduct = apiTypeWrapper.getApiProduct();
            subscriptionDTO.setApiId(apiProduct.getUuid());
            APIInfoDTO apiInfo = APIMappingUtil.fromAPIToInfoDTO(apiProduct);
            subscriptionDTO.setApiInfo(apiInfo);
        }
        Application application = subscription.getApplication();
        application = apiConsumer.getLightweightApplicationByUUID(application.getUUID());
        subscriptionDTO.setApplicationId(subscription.getApplication().getUUID());
        subscriptionDTO.setStatus(SubscriptionDTO.StatusEnum.valueOf(subscription.getSubStatus()));
        subscriptionDTO.setThrottlingPolicy(subscription.getTier().getName());
        subscriptionDTO.setRequestedThrottlingPolicy(subscription.getRequestedTier().getName());

        ApplicationInfoDTO applicationInfoDTO = ApplicationMappingUtil.fromApplicationToInfoDTO(application);
        subscriptionDTO.setApplicationInfo(applicationInfoDTO);

        return subscriptionDTO;
    }

    /** Converts a List object of SubscribedAPIs into a DTO
     *
     * @param subscriptions a list of SubscribedAPI objects
     * @param limit max number of objects returned
     * @param offset starting index
     * @return SubscriptionListDTO object containing SubscriptionDTOs
     */
    public static SubscriptionListDTO fromSubscriptionListToDTO(List<SubscribedAPI> subscriptions, Integer limit,
            Integer offset, ApiTypeWrapper apiTypeWrapper) throws APIManagementException {

        SubscriptionListDTO subscriptionListDTO = new SubscriptionListDTO();
        List<SubscriptionDTO> subscriptionDTOs = subscriptionListDTO.getList();
        if (subscriptionDTOs == null) {
            subscriptionDTOs = new ArrayList<>();
            subscriptionListDTO.setList(subscriptionDTOs);
        }

        for (SubscribedAPI subscription : subscriptions) {
            try {
                subscriptionDTOs.add(fromSubscriptionToDTO(subscription, apiTypeWrapper));
            } catch (APIManagementException e) {
                log.error("Error while obtaining api metadata", e);
            }
        }

        subscriptionListDTO.setCount(subscriptionDTOs.size());
        return subscriptionListDTO;
    }
}
