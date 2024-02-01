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
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.notifier.SubscriptionsNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;
import org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * This class controls the Solace Broker deployed API subscription flows
 */
public class SolaceSubscriptionsNotifier extends SubscriptionsNotifier {
    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(SolaceSubscriptionsNotifier.class);


    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (SolaceNotifierUtils.isSolaceEnvironmentDefined()) {
            apiMgtDAO = ApiMgtDAO.getInstance();
            process(event);
        }
        return true;
    }

    /**
     * Process gateway notifier events related to Solace API subscriptions
     *
     * @param event related to subscription handling
     * @throws NotifierException if error occurs when casting event
     */
    private void process(Event event) throws NotifierException {
        SubscriptionEvent subscriptionEvent;
        subscriptionEvent = (SubscriptionEvent) event;


        if (APIConstants.EventType.SUBSCRIPTIONS_CREATE.name().equals(event.getType())) {
            createSubscription(subscriptionEvent);
        } else if (APIConstants.EventType.SUBSCRIPTIONS_UPDATE.name().equals(event.getType())) {
            updateSubscription(subscriptionEvent);
        } else if (APIConstants.EventType.SUBSCRIPTIONS_DELETE.name().equals(event.getType())) {
            removeSubscription(subscriptionEvent);
        }
    }

    /**
     * Create subscriptions to Solace APIs
     *
     * @param event SubscriptionEvent to create Solace API subscriptions
     * @throws NotifierException if error occurs when creating subscription for Solace APIs
     */
    private void createSubscription(SubscriptionEvent event) throws NotifierException {

        String apiUUID = event.getApiUUID();
        String applicationUUID = event.getApplicationUUID();

        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPIbyUUID(apiUUID, apiMgtDAO.getOrganizationByAPIUUID(apiUUID));

            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            Application application = apiMgtDAO.getApplicationByUUID(applicationUUID);
            Set<APIKey> consumerKeys  = apiConsumer.getApplicationKeysOfApplication(application.getId());
            for (APIKey apiKey : consumerKeys) {
                if (SolaceConstants.OAUTH_CLIENT_PRODUCTION.equals(apiKey.getType())) {
                    application.addKey(apiKey);
                }
            }
            // Send only the production keys to Solace broker.
            if (application.getKeys().isEmpty()) {
                return;
            }
            deployApplication(api, application);
        } catch (APIManagementException e) {
            throw new NotifierException("Error while creating application solace Broker " + e.getMessage());
        }  catch (IOException e) {
            throw new NotifierException("I/O Error while creating application solace Broker " + e.getMessage());
        }
    }

    /**
     * Update subscriptions related to Solace APIs
     *
     * @param event SubscriptionEvent to update Solace API subscriptions
     * @throws NotifierException if error occurs when updating subscription for Solace APIs
     */
    private void updateSubscription(SubscriptionEvent event) throws NotifierException {
        String apiUUID = event.getApiUUID();
        String applicationUUID = event.getApplicationUUID();

        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPIbyUUID(apiUUID, apiMgtDAO.getOrganizationByAPIUUID(apiUUID));

            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            Application application = apiMgtDAO.getApplicationByUUID(applicationUUID);
            Set<APIKey> consumerKeys  = apiConsumer.getApplicationKeysOfApplication(application.getId());
            for (APIKey apiKey : consumerKeys) {
                if (SolaceConstants.OAUTH_CLIENT_PRODUCTION.equals(apiKey.getType())) {
                    application.addKey(apiKey);
                }
            }
            // Send only the production keys to Solace broker.
            if (application.getKeys().isEmpty()) {
                return;
            }
            deployApplication(api, application);
        } catch (APIManagementException e) {
            throw new NotifierException("Error while updating application solace Broker " + e.getMessage());
        }  catch (IOException e) {
            throw new NotifierException("I/O Error while updating application solace Broker " + e.getMessage());
        }
    }

    /**
     * Remove subscriptions from Solace APIs
     *
     * @param event SubscriptionEvent to remove Solace API subscriptions
     * @throws NotifierException if error occurs when deleting subscriptions from Solace APIs
     */
    private void removeSubscription(SubscriptionEvent event) throws NotifierException {
        String apiUUID = event.getApiUUID();
        String applicationUUID = event.getApplicationUUID();

        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPIbyUUID(apiUUID, apiMgtDAO.getOrganizationByAPIUUID(apiUUID));
            Application application = apiProvider.getApplicationByUUID(applicationUUID);

            //Check whether the subscription is belongs to an API deployed in Solace
            if (SolaceConstants.SOLACE_ENVIRONMENT.equals(api.getGatewayVendor())) {
                // Application Deletion Event came before Subscription removal event.
                if (application != null) {
                    SolaceNotifierUtils.unsubscribeAPIProductFromSolaceApplication(api, application);
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException("Error while removing application solace Broker " + e.getMessage());
        }
    }


    /**
     * Deploy the application to solace Broker
     *
     * @param api Subscribed API of the application
     * @param application Application which needs to be created/updated in solace broker
     * @throws APIManagementException if error occurs when creating subscriptions from Solace APIs
     */
    public void deployApplication(API api, Application application) throws APIManagementException, IOException {
        try {
            //Check whether the subscription belong to an API deployed in Solace
            if (SolaceConstants.SOLACE_ENVIRONMENT.equals(api.getGatewayVendor())) {
                ArrayList<String> solaceApiProducts = new ArrayList<>();
                List<Environment> deployedSolaceEnvironments =
                        SolaceNotifierUtils.getDeployedSolaceEnvironmentsFromRevisionDeployments(api);
                String applicationOrganizationName = SolaceNotifierUtils.getSolaceOrganizationName
                        (deployedSolaceEnvironments);
                if (applicationOrganizationName != null) {
                    boolean apiProductDeployedIntoSolace = SolaceNotifierUtils.
                            checkApiProductAlreadyDeployedIntoSolaceEnvironments(api, deployedSolaceEnvironments);
                    if (apiProductDeployedIntoSolace) {
                        for (Environment environment : deployedSolaceEnvironments) {
                            solaceApiProducts.add(SolaceNotifierUtils.generateApiProductNameForSolaceBroker
                                    (api, environment.getName()));
                        }
                        SolaceNotifierUtils.deployApplicationToSolaceBroker(application, solaceApiProducts,
                                applicationOrganizationName);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.error("Cannot create solace application " + application.getName() + "with API product "
                                + "deployed in different organizations...");
                    }
                    throw new APIManagementException("Cannot create solace application " + application.getName() +
                            "with API product deployed in different organizations...");
                }
            }
        } catch (APIManagementException e) {
            if (log.isDebugEnabled()) {
                log.error("Error while creating application solace Broker" + e.getMessage());
            }
            throw new APIManagementException("I/O Error while creating application solace Broker" + e.getMessage());
        }  catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.error("I/O Error while creating application solace Broker" + e.getMessage());
            }
            throw new IOException("I/O Error while creating application solace Broker" + e.getMessage());
        }
    }

}
