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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SolaceConfig;
import org.wso2.carbon.apimgt.impl.notifier.ApplicationRegistrationNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationRegistrationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.api.v2.SolaceV2ApiHolder;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;
import org.wso2.carbon.context.CarbonContext;

import java.util.Set;

/**
 * Handles Solace App Registration credentials upon WSO2 API Manager DevPortal Application key generation.
 */
public class SolaceKeyGenNotifier extends ApplicationRegistrationNotifier {

    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(SolaceKeyGenNotifier.class);

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        try {
            SolaceConfig solaceConfig = APIUtil.getSolaceConfig();
            if (solaceConfig != null && solaceConfig.isEnabled()) {
                apiMgtDAO = ApiMgtDAO.getInstance();
                ApplicationRegistrationEvent applicationRegistrationEvent = (ApplicationRegistrationEvent) event;
                processKeyGenerationEvent(applicationRegistrationEvent);
            }
            return true;
        } catch (APIManagementException e) {
            throw new NotifierException("Error while adding credentials to the Solace app registration ", e);
        }
    }

    private void processKeyGenerationEvent(ApplicationRegistrationEvent applicationRegistrationEvent)
            throws APIManagementException {
        Application application = apiMgtDAO.getApplicationByUUID(applicationRegistrationEvent.getApplicationUUID());
        Set<SubscribedAPI> subscriptions = apiMgtDAO.getSubscribedAPIsByApplication(application);
        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                getThreadLocalCarbonContext().getUsername());

        boolean isSolaceApiSubscriptionExists = false;
        for (SubscribedAPI subscribedAPI : subscriptions) {
            String apiUUID = subscribedAPI.getAPIUUId();
            API api = apiProvider.getAPIbyUUID(apiUUID, apiMgtDAO.getOrganizationByAPIUUID(apiUUID));
            if (SolaceConstants.SOLACE_ENVIRONMENT.equals(api.getGatewayType())) {
                isSolaceApiSubscriptionExists = true;
                break;
            }
        }

        if (isSolaceApiSubscriptionExists) {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(
                    CarbonContext.getThreadLocalCarbonContext().getUsername());
            Set<APIKey> keys = apiConsumer.getApplicationKeysOfApplication(application.getId());
            for (APIKey key : keys) {
                SolaceV2ApiHolder.getInstance()
                        .createCredentials(application.getUUID(), key.getConsumerKey(), key.getConsumerSecret());
            }
        }
    }

}
