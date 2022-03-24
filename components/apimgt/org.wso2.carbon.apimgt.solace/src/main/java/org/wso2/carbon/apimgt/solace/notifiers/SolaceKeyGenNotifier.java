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
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.notifier.ApplicationRegistrationNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationRegistrationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;
import org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;
import java.util.ArrayList;
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
        if (SolaceNotifierUtils.isSolaceEnvironmentDefined()) {
            apiMgtDAO = ApiMgtDAO.getInstance();
            process(event);
        }
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

        // Get list of subscribed APIs in the application
        try {
            Application application = apiMgtDAO.getApplicationByUUID(event.getApplicationUUID());
            Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
            Set<SubscribedAPI> subscriptions = apiMgtDAO.getSubscribedAPIsByApplication(application);
            boolean isContainsSolaceApis = false;
            String organizationNameOfSolaceDeployment = null;
            List<API> subscribedAPIs = new ArrayList<>();

            //Check whether the application needs to be updated has a Solace API subscription
            for (SubscribedAPI api : subscriptions) {
                List<APIRevisionDeployment> deployments = apiMgtDAO.getAPIRevisionDeploymentByApiUUID(api.
                        getIdentifier().getUUID());
                for (APIRevisionDeployment deployment : deployments) {
                    if (gatewayEnvironments.containsKey(deployment.getDeployment())) {
                        if (SolaceConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(gatewayEnvironments.get(deployment.
                                getDeployment()).getProvider())) {
                            API subscribedAPI = apiMgtDAO.getLightWeightAPIInfoByAPIIdentifier(api.getApiId(),
                                    event.tenantDomain);
                            subscribedAPI.setGatewayVendor(SolaceConstants.SOLACE_ENVIRONMENT);
                            subscribedAPIs.add(subscribedAPI);
                            isContainsSolaceApis = true;
                            organizationNameOfSolaceDeployment = gatewayEnvironments.get(deployment.getDeployment()).
                                    getAdditionalProperties().get(SolaceConstants.SOLACE_ENVIRONMENT_ORGANIZATION);
                        }
                    }
                }
            }

            if (isContainsSolaceApis) {
                if (application.getKeys() != null) {
                    String consumerSecret = null;
                    APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(CarbonContext.
                            getThreadLocalCarbonContext().getUsername());
                    Set<APIKey> consumerKeys  = apiConsumer.getApplicationKeysOfApplication(application.getId());
                    for (APIKey key : consumerKeys) {
                        if (key.getConsumerKey().equals(event.getConsumerKey()) && SolaceConstants
                                .OAUTH_CLIENT_PRODUCTION.equals(key.getType())) {
                            consumerSecret = key.getConsumerSecret();
                            application.addKey(key);
                        }
                    }
                    // Send only the production keys to Solace broker.
                    if (application.getKeys().isEmpty()) {
                        return;
                    }
                    SolaceAdminApis solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();

                    try (CloseableHttpResponse isApplicationExistsResponse = solaceAdminApis.applicationGet(
                            organizationNameOfSolaceDeployment, application.getUUID(), "default")) {
                        if (isApplicationExistsResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            SolaceNotifierUtils.patchSolaceApplicationClientId(organizationNameOfSolaceDeployment,
                                    application, event.getConsumerKey(), consumerSecret);
                        } else if (isApplicationExistsResponse.getStatusLine().getStatusCode() ==
                                HttpStatus.SC_NOT_FOUND) {
                            // Create applications in solace
                            for (API api : subscribedAPIs) {
                                SolaceSubscriptionsNotifier solaceSubscriptionsNotifier = new
                                        SolaceSubscriptionsNotifier();
                                solaceSubscriptionsNotifier.deployApplication(api, application);
                            }

                        } else {
                            String msg = "Error while searching for application '" + application.getName() + ". : " +
                                    isApplicationExistsResponse.getStatusLine().toString();
                            if (log.isDebugEnabled()) {
                                log.error(msg);
                            }
                            throw new NotifierException(msg);
                        }
                    }
                } else {
                    throw new NotifierException("Application keys are not found in the application : " +
                            application.getName());
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException("Error while persisting generated keys in solace Broker " + e.getMessage());
        }  catch (IOException e) {
            throw new NotifierException("I/O Error while persisting generated keys in solace Broker " + e.getMessage());
        }
    }

}
