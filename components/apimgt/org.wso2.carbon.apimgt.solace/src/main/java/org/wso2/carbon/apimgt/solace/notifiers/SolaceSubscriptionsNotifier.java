/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.apimgt.solace.notifiers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SolaceConfig;
import org.wso2.carbon.apimgt.impl.notifier.SubscriptionsNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.api.v2.SolaceV2ApiHolder;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;
import org.wso2.carbon.context.CarbonContext;

/**
 * Handles Solace App Registration, its credentials and Solace App requests, 
 * upon subscription to a Solace API in WSO2 API Manager DevPortal Application.
 */
public class SolaceSubscriptionsNotifier extends SubscriptionsNotifier {
    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(SolaceSubscriptionsNotifier.class);

    private static final String INFO_OBJECT_KEY = "info";
    private static final String DEFAULT_APP_SOURCE_AND_OWNER = "wso2apim";
    private static final String EVENT_API_PRODUCT_ID = "x-ep-event-api-product-id";
    private static final String EVENT_API_PRODUCT_NAME = "x-ep-event-api-product-name";
    private static final String EVENT_API_PRODUCT_VERSION = "x-ep-event-api-product-version";
    private static final String APPLICATION_DOMAIN_ID = "x-ep-application-domain-id";

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        try {
            SolaceConfig solaceConfig = APIUtil.getSolaceConfig();
            if (solaceConfig != null && solaceConfig.isEnabled()) {
                apiMgtDAO = ApiMgtDAO.getInstance();
                SubscriptionEvent subscriptionEvent = (SubscriptionEvent) event;
                String apiUUID = subscriptionEvent.getApiUUID();
                String organization = apiMgtDAO.getOrganizationByAPIUUID(apiUUID);
                APIConsumer apiConsumer = APIManagerFactory.getInstance()
                        .getAPIConsumer(CarbonContext.getThreadLocalCarbonContext().getUsername());
                API api = apiConsumer.getAPIorAPIProductByUUID(apiUUID, organization)
                        .getApi();
                String asyncApiDefinition = apiConsumer.getAsyncAPIDefinition(apiUUID, organization);

                if (SolaceConstants.SOLACE_ENVIRONMENT.equals(api.getGatewayType())) {
                    if (APIConstants.EventType.SUBSCRIPTIONS_CREATE.name().equals(event.getType())) {
                        createAccessRequest(subscriptionEvent, asyncApiDefinition, apiConsumer);
                    } else if (APIConstants.EventType.SUBSCRIPTIONS_DELETE.name().equals(event.getType())) {
                        deleteAccessRequest(subscriptionEvent, asyncApiDefinition, apiConsumer);
                    }
                }
            }
            return true;
        } catch (APIManagementException e) {
            throw new NotifierException("Error while processing subscription event", e);
        }
    }

    private void createAccessRequest(SubscriptionEvent subscriptionEvent, String asyncApiDefinitionString,
            APIConsumer apiConsumer) throws APIManagementException {
        JsonObject asyncApiDefinition = new JsonParser().parse(asyncApiDefinitionString).getAsJsonObject();
        JsonObject infoObject = asyncApiDefinition.getAsJsonObject(INFO_OBJECT_KEY);
        String eventApiProductId = infoObject.get(EVENT_API_PRODUCT_ID).getAsString();
        String applicationDomainId = infoObject.get(APPLICATION_DOMAIN_ID).getAsString();
        String wso2ApimPolicyName = subscriptionEvent.getPolicyId();
        String solacePlanId = SolaceV2ApiHolder.getInstance()
                .getEventApiProductPlanId(eventApiProductId, wso2ApimPolicyName);

        if (solacePlanId != null) {
            Application wso2ApimDevPortalApplication = apiConsumer.getApplicationByUUID(
                    subscriptionEvent.getApplicationUUID());
            String appRegistrationId = wso2ApimDevPortalApplication.getUUID();
            String appName = wso2ApimDevPortalApplication.getName();

            /*
             Create the app registration if it does not exist, only when a subscription is made to a Solace API,
             because we don't want to create an app registration unnecessarily, if the dev portal app is not going to
             be subscribed to a Solace API.
             */
            if (!SolaceV2ApiHolder.getInstance().isAppRegistrationExists(appRegistrationId)) {
                SolaceV2ApiHolder.getInstance()
                        .createAppRegistration(appRegistrationId, DEFAULT_APP_SOURCE_AND_OWNER, appName,
                                DEFAULT_APP_SOURCE_AND_OWNER, applicationDomainId);

                // Add each consumer key and consumer secret of the WSO2 APIM DevPortal app as a credential
                for (APIKey key : wso2ApimDevPortalApplication.getKeys()) {
                    SolaceV2ApiHolder.getInstance()
                            .createCredentials(appRegistrationId, key.getConsumerKey(), key.getConsumerSecret());
                }
            }

            SolaceV2ApiHolder.getInstance().createAccessRequest(eventApiProductId, solacePlanId, appRegistrationId);
        } else {
            String eventApiProductName = infoObject.get(EVENT_API_PRODUCT_NAME).getAsString();
            String eventApiProductVersion = infoObject.get(EVENT_API_PRODUCT_VERSION).getAsString();
            throw new APIManagementException("Cannot find a Solace Plan with the name: '" + wso2ApimPolicyName +
                    "' in the Solace Event API Product: " + eventApiProductName + ": " +
                    eventApiProductVersion);
        }
    }

    private void deleteAccessRequest(SubscriptionEvent subscriptionEvent, String asyncApiDefinitionString,
            APIConsumer apiConsumer) throws APIManagementException {
        JsonObject asyncApiDefinition = new JsonParser().parse(asyncApiDefinitionString).getAsJsonObject();
        JsonObject infoObject = asyncApiDefinition.getAsJsonObject(INFO_OBJECT_KEY);
        String eventApiProductId = infoObject.get(EVENT_API_PRODUCT_ID).getAsString();

        Application wso2ApimDevPortalApplication = apiConsumer.getApplicationByUUID(
                subscriptionEvent.getApplicationUUID());
        String appRegistrationId = wso2ApimDevPortalApplication.getUUID();
        SolaceV2ApiHolder.getInstance().deleteAccessRequest(appRegistrationId, eventApiProductId);
    }

}
