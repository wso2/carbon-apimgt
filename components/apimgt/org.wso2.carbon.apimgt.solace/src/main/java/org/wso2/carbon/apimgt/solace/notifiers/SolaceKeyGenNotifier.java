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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.notifier.ApplicationRegistrationNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationRegistrationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;
import org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class controls the Solace Broker deployed Application Key generation flow
 */
public class SolaceKeyGenNotifier extends ApplicationRegistrationNotifier {

    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(SolaceKeyGenNotifier.class);

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        apiMgtDAO = ApiMgtDAO.getInstance();
        process(event);
        return true;
    }

    /**
     * Process Application notifier event related to key generation related to Solace Applications
     *
     * @param event related to Key generation handling
     * @throws NotifierException if error occurs when casting event
     */
    private void process(Event event) throws NotifierException {
        ApplicationRegistrationEvent applicationRegistrationEvent;
        applicationRegistrationEvent = (ApplicationRegistrationEvent) event;

        if (APIConstants.EventType.APPLICATION_REGISTRATION_CREATE.name().equals(event.getType())) {
            syncSolaceApplicationClientId(applicationRegistrationEvent);
        }
    }

    /**
     * Syncing consumer key of the dev portal applications with applications on the Solace broker
     *
     * @param event ApplicationEvent to sync Solace applications with dev portal applications
     * @throws NotifierException if error occurs when patching applications on the Solace broker
     */
    private void syncSolaceApplicationClientId(ApplicationRegistrationEvent event) throws NotifierException {

        // get list of subscribed APIs in the application
        try {
            Application application = apiMgtDAO.getApplicationByUUID(event.getApplicationUUID());
            Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
            Set<SubscribedAPI> subscriptions = apiMgtDAO.getSubscribedAPIs(application.getSubscriber(),
                    application.getName(), application.getGroupId());
            boolean isContainsSolaceApis = false;
            String organizationNameOfSolaceDeployment = null;
            labelOne:
            //Check whether the application needs to be updated has a Solace API subscription
            for (SubscribedAPI api : subscriptions) {
                List<APIRevisionDeployment> deployments = apiMgtDAO.getAPIRevisionDeploymentByApiUUID(api.
                        getIdentifier().getUUID());
                for (APIRevisionDeployment deployment : deployments) {
                    if (gatewayEnvironments.containsKey(deployment.getDeployment())) {
                        if (SolaceConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(gatewayEnvironments.get(deployment.
                                getDeployment()).getProvider())) {
                            isContainsSolaceApis = true;
                            organizationNameOfSolaceDeployment = gatewayEnvironments.get(deployment.getDeployment()).
                                    getAdditionalProperties().get(SolaceConstants.SOLACE_ENVIRONMENT_ORGANIZATION);
                            break labelOne;
                        }
                    }
                }
            }
            // Patching consumerKey to Solace application using Admin Apis
            if (isContainsSolaceApis) {
                String consumerSecret = application.getName() + "-application-secret";
                for (APIKey key : application.getKeys()) {
                    if (key.getConsumerKey().equals(event.getConsumerKey())) {
                        consumerSecret = key.getConsumerSecret();
                    }
                }
                SolaceNotifierUtils.patchSolaceApplicationClientId(organizationNameOfSolaceDeployment,
                        application, event.getConsumerKey(), consumerSecret);
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }
    }

}
