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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.notifier.ApplicationNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;
import org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class controls the Solace Broker deployed Application deletion and update flows
 */
public class SolaceApplicationNotifier extends ApplicationNotifier {

    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(SolaceApplicationNotifier.class);


    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (SolaceNotifierUtils.isSolaceEnvironmentDefined()) {
            apiMgtDAO = ApiMgtDAO.getInstance();
            process(event);
        }
        return true;
    }

    /**
     * Process Application notifier event related to Solace applications
     *
     * @param event related to Application handling
     * @throws NotifierException if error occurs when casting event
     */
    private void process(Event event) throws NotifierException {
        ApplicationEvent applicationEvent;
        applicationEvent = (ApplicationEvent) event;

        if (APIConstants.EventType.APPLICATION_DELETE.name().equals(event.getType())) {
            removeSolaceApplication(applicationEvent);
        } else if (APIConstants.EventType.APPLICATION_UPDATE.name().equals(event.getType())) {
            renameSolaceApplication(applicationEvent);
        }
    }

    /**
     * Remove applications from Solace broker
     *
     * @param event ApplicationEvent to remove Solace applications
     * @throws NotifierException if error occurs when removing applications from Solace broker
     */
    private void removeSolaceApplication(ApplicationEvent event) throws NotifierException {
        // get list of subscribed APIs in the application
        Subscriber subscriber = new Subscriber(event.getSubscriber());
        try {

            Set<SubscribedAPI> subscriptions = apiMgtDAO.getSubscribedAPIs(subscriber, event.getApplicationName(),
                    event.getGroupId());
            List<SubscribedAPI> subscribedApiList = new ArrayList<>(subscriptions);
            boolean hasSubscribedAPIDeployedInSolace = false;
            String organizationNameOfSolaceDeployment = null;

            Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
            labelOne:
            for (SubscribedAPI api : subscribedApiList) {
                List<APIRevisionDeployment> deployments = apiMgtDAO.getAPIRevisionDeploymentByApiUUID(api.getUUID());
                for (APIRevisionDeployment deployment : deployments) {
                    if (gatewayEnvironments.containsKey(deployment.getDeployment())) {
                        if (SolaceConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(gatewayEnvironments.get(deployment.
                                getDeployment()).getProvider())) {
                            hasSubscribedAPIDeployedInSolace = true;
                            organizationNameOfSolaceDeployment = gatewayEnvironments.get(deployment.getDeployment()).
                                    getAdditionalProperties().get(SolaceConstants.SOLACE_ENVIRONMENT_ORGANIZATION);
                            break labelOne;
                        }
                    }
                }
            }

            boolean applicationFoundInSolaceBroker = false;
            if (hasSubscribedAPIDeployedInSolace) {
                SolaceAdminApis solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();

                // check existence of application in Solace Broker
                CloseableHttpResponse response1 = solaceAdminApis.applicationGet(organizationNameOfSolaceDeployment,
                        event.getUuid(), "default");
                if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    applicationFoundInSolaceBroker = true;

                    if (log.isDebugEnabled()) {
                        log.info("Found application '" + event.getApplicationName() + "' in Solace broker");
                        log.info("Waiting until application removing workflow gets finished");
                    }

                } else if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    throw new NotifierException("Application '" + event.getApplicationName() + "' cannot be found in " +
                            "Solace Broker");
                } else {
                    if (log.isDebugEnabled()) {
                        log.error("Error while searching for application '" + event.getApplicationName() + "'" +
                                " in Solace Broker. : " + response1.getStatusLine().toString());
                    }
                    throw new NotifierException("Error while searching for application '" + event.getApplicationName() +
                            "' in Solace Broker");
                }
            }

            if (applicationFoundInSolaceBroker) {
                log.info("Deleting application from Solace Broker");
                // delete application from solace
                SolaceAdminApis solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();
                CloseableHttpResponse response2 = solaceAdminApis.deleteApplication(organizationNameOfSolaceDeployment,
                        event.getUuid());
                if (response2.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                    log.info("Successfully deleted application '" + event.getApplicationName() + "' " +
                            "in Solace Broker");
                } else {
                    if (log.isDebugEnabled()) {
                        log.error("Error while deleting application " + event.getApplicationName() + " in Solace. :"
                        + response2.getStatusLine().toString());
                    }
                    throw new NotifierException("Error while deleting application '" + event.getApplicationName() +
                            "' in Solace");
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }
    }

    /**
     * Rename applications on the Solace broker
     *
     * @param event ApplicationEvent to rename Solace applications
     * @throws NotifierException if error occurs when renaming applications on the Solace broker
     */
    private void renameSolaceApplication(ApplicationEvent event) throws NotifierException {

        // get list of subscribed APIs in the application
        Subscriber subscriber = new Subscriber(event.getSubscriber());

        try {
            Application application = apiMgtDAO.getApplicationByUUID(event.getUuid());
            Set<SubscribedAPI> subscriptions = apiMgtDAO.getSubscribedAPIs(subscriber, event.getApplicationName(),
                    event.getGroupId());
            Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
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
            // Renaming application using Solace Admin Apis
            if (isContainsSolaceApis) {
                SolaceNotifierUtils.renameSolaceApplication(organizationNameOfSolaceDeployment, application);
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }
    }
}
