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

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.notifier.DeployAPIInGatewayNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


public class SolaceDeployAPIInGatewayNotifier extends DeployAPIInGatewayNotifier {

    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(SolaceDeployAPIInGatewayNotifier.class);


    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        apiMgtDAO = ApiMgtDAO.getInstance();
        process(event);
        return true;
    }

    /**
     * Process gateway notifier events related to Solace broker deployments
     *
     * @param event related to deployments
     * @throws NotifierException if error occurs when casting event
     */
    private void process (Event event) throws NotifierException {
        DeployAPIInGatewayEvent deployAPIInGatewayEvent;
        try {
            deployAPIInGatewayEvent = (DeployAPIInGatewayEvent) event;
        } catch (ExceptionInInitializerError e) {
            throw new NotifierException("Event types is not provided correctly");
        }

        if (APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(event.getType())) {
            deployApi(deployAPIInGatewayEvent);
        } else {
            unDeployApi(deployAPIInGatewayEvent);
        }
    }

    /**
     * Deploy APIs to Solace broker
     *
     * @param deployAPIInGatewayEvent DeployAPIInGatewayEvent to deploy APIs to Solace broker
     * @throws NotifierException if error occurs when deploying APIs to Solace broker
     */
    private void deployApi(DeployAPIInGatewayEvent deployAPIInGatewayEvent) throws NotifierException {

        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        boolean deployedToSolace;
        Set<String> gateways = deployAPIInGatewayEvent.getGatewayLabels();
        String apiId = deployAPIInGatewayEvent.getUuid();

        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPI(apiMgtDAO.getAPIIdentifierFromUUID(apiId));

            for (String deploymentEnv : gateways) {
                if (gatewayEnvironments.containsKey(deploymentEnv)) {
                    if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(gatewayEnvironments.get(deploymentEnv).getProvider())) {
                        try {
                            deployedToSolace = deployToSolaceBroker(api, gatewayEnvironments.get(deploymentEnv));
                            if (!deployedToSolace) {
                                throw new APIManagementException("Error while deploying API product to Solace broker");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new APIManagementException(e.getMessage());
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }
    }

    /**
     * Undeploy APIs from Solace broker
     *
     * @param deployAPIInGatewayEvent DeployAPIInGatewayEvent to undeploy APIs from Solace broker
     * @throws NotifierException if error occurs when undeploying APIs from Solace broker
     */
    private void unDeployApi(DeployAPIInGatewayEvent deployAPIInGatewayEvent) throws NotifierException {

        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        boolean deletedFromSolace;
        Set<String> gateways = deployAPIInGatewayEvent.getGatewayLabels();
        String apiId = deployAPIInGatewayEvent.getUuid();

        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPI(apiMgtDAO.getAPIIdentifierFromUUID(apiId));

            for (String deploymentEnv : gateways) {
                if (gatewayEnvironments.containsKey(deploymentEnv)) {
                    if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(
                            gatewayEnvironments.get(deploymentEnv).getProvider())) {
                        try {
                            deletedFromSolace = undeployFromSolaceBroker(api, gatewayEnvironments.get(deploymentEnv));
                            if (!deletedFromSolace) {
                                throw new NotifierException("Error while deleting API product from Solace broker");
                            }
                        } catch (HttpResponseException e) {
                            throw new NotifierException(e.getMessage());
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }

    }

    /**
     * Deploy API artifact to provided environment
     *
     * @param api API to be deployed into Solace broker
     * @param environment Environment to be deployed
     * @throws APIManagementException if error occurs when deploying APIs to Solace broker
     * @throws HttpResponseException if error occurs when using Solace admin APIs
     */
    private boolean deployToSolaceBroker(API api, Environment environment) throws HttpResponseException, APIManagementException {
        String apiDefinition = api.getAsyncApiDefinition();
        Aai20Document aai20Document = (Aai20Document) Library.readDocumentFromJSONString(apiDefinition);
        String apiNameForRegistration = api.getId().getApiName() + "-" + api.getId().getVersion();
        String[] apiContextParts = api.getContext().split("/");
        String apiNameWithContext = environment.getName() + "-" + api.getId().getName() + "-" + apiContextParts[1] +
                "-" + apiContextParts[2];
        SolaceAdminApis solaceAdminApis = org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils.getSolaceAdminApis();

        // check availability of environment
        HttpResponse response1 = solaceAdminApis.environmentGET(environment.getAdditionalProperties().get(APIConstants.
                SOLACE_ENVIRONMENT_ORGANIZATION), environment.getName());
        if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            log.info("environment '" + environment.getName() + "' found in Solace broker");
            // check api product already exists in solace
            HttpResponse response4 = solaceAdminApis.apiProductGet(environment.getAdditionalProperties().get(APIConstants.
                    SOLACE_ENVIRONMENT_ORGANIZATION), apiNameWithContext);
            if (response4.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // api Product Already found in solace. No need to deploy again into Solace
                log.info("API product '" + apiNameWithContext + "' already found in Solace. No need to create again");
                return true;
            } else if (response4.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // api product not found in solace. check existence of registered API in solace
                log.info("API product '" + apiNameWithContext + "' not found in Solace. Checking the existence of API");
                HttpResponse response5 = solaceAdminApis.registeredAPIGet(environment.getAdditionalProperties().get(APIConstants.
                                SOLACE_ENVIRONMENT_ORGANIZATION), apiNameForRegistration);
                if (response5.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    log.info("API '" + apiNameForRegistration + "' already registered in Solace. Creating API product " +
                            "using registered API");
                    // create API product only
                    HttpResponse response3 = solaceAdminApis.createAPIProduct(environment.getAdditionalProperties().get(APIConstants.
                                    SOLACE_ENVIRONMENT_ORGANIZATION), environment.getName(), aai20Document,
                            apiNameWithContext, apiNameForRegistration);
                    if (response3.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                        log.info("API product '" + apiNameWithContext + "' has been created in Solace broker");
                        return true;
                    } else {
                        log.error("Error while creating API product in Solace");
                        throw new HttpResponseException(response3.getStatusLine().getStatusCode(),
                                response3.getStatusLine().getReasonPhrase());
                    }
                } else if (response5.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    log.info("API '" + apiNameForRegistration + "' not registered in Solace. Creating both API and " +
                            "API product");
                    // register the API in Solace Broker
                    HttpResponse response2 = solaceAdminApis.registerAPI(environment.getAdditionalProperties().get(APIConstants.
                                    SOLACE_ENVIRONMENT_ORGANIZATION), apiNameForRegistration, apiDefinition);
                    if (response2.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                        log.info("API '" + apiNameForRegistration + "' has been registered in Solace broker");
                        //create API Product in Solace broker
                        HttpResponse response3 = solaceAdminApis.createAPIProduct(environment.getAdditionalProperties().get(APIConstants.
                                        SOLACE_ENVIRONMENT_ORGANIZATION), environment.getName(), aai20Document,
                                apiNameWithContext, apiNameForRegistration);
                        if (response3.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                            log.info("API product '" + apiNameWithContext + "' has been created in Solace broker");
                            return true;
                        } else {
                            log.error("Error while creating API product in Solace");
                            // delete registered API in solace
                            HttpResponse response6 = solaceAdminApis.deleteRegisteredAPI(environment.getAdditionalProperties().get(APIConstants.
                                            SOLACE_ENVIRONMENT_ORGANIZATION), apiNameForRegistration);
                            if (response6.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                                log.info("Successfully deleted registered API '" + apiNameForRegistration + "' " +
                                        "from Solace");
                            } else {
                                log.error("Error while deleting registered API '" + apiNameForRegistration + "' " +
                                        "in Solace");
                                throw new HttpResponseException(response6.getStatusLine().getStatusCode(),
                                        response6.getStatusLine().getReasonPhrase());
                            }
                            throw new HttpResponseException(response3.getStatusLine().getStatusCode(),
                                    response3.getStatusLine().getReasonPhrase());
                        }
                    } else {
                        log.error("Error while registering API in Solace - '" + apiNameForRegistration + "'");
                        throw new HttpResponseException(response2.getStatusLine().getStatusCode(),
                                response2.getStatusLine().getReasonPhrase());
                    }
                } else {
                    log.error("Error while finding API '" + apiNameForRegistration + "' in Solace");
                    throw new HttpResponseException(response5.getStatusLine().getStatusCode(),
                            response5.getStatusLine().getReasonPhrase());
                }
            } else {
                log.error("Error while finding API product '" + apiNameWithContext + "' in Solace");
                throw new HttpResponseException(response4.getStatusLine().getStatusCode(), response4.getStatusLine().
                        getReasonPhrase());
            }
        } else {
            log.error("Cannot find specified Solace environment - '" + environment.getName() + "'");
            throw new HttpResponseException(response1.getStatusLine().getStatusCode(), response1.getStatusLine().
                    getReasonPhrase());
        }
    }

    /**
     * Undeploy API artifact from provided environment
     *
     * @param api API to be undeployed from Solace broker
     * @param environment Environment needed to be undeployed API from
     * @throws APIManagementException if error occurs when undeploying APIs from Solace broker
     * @throws HttpResponseException if error occurs when using Solace admin APIs
     */
    private boolean undeployFromSolaceBroker(API api, Environment environment) throws HttpResponseException,
            APIManagementException {
        String apiNameForRegistration = api.getId().getApiName() + "-" + api.getId().getVersion();
        String[] apiContextParts = api.getContext().split("/");
        String apiNameWithContext = environment.getName() + "-" + api.getId().getName() + "-" + apiContextParts[1] +
                "-" + apiContextParts[2];
        SolaceAdminApis solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();

        //delete API product from Solace
        HttpResponse response1 = solaceAdminApis.deleteApiProduct(environment.getAdditionalProperties().get(APIConstants.
                SOLACE_ENVIRONMENT_ORGANIZATION), apiNameWithContext);
        if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {

            log.info("API product '" + apiNameWithContext + "' has been deleted from Solace Broker");
            //delete registered API from Solace
            HttpResponse response2 = solaceAdminApis.deleteRegisteredAPI(environment.getAdditionalProperties().get(APIConstants.
                            SOLACE_ENVIRONMENT_ORGANIZATION), apiNameForRegistration);
            if (response2.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                log.info("API product '" + apiNameWithContext + "' and API '" + apiNameForRegistration + "' have " +
                        "been deleted from Solace broker");
                return true;
            } else if (response2.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT) {
                log.info("Registered API '" + apiNameWithContext + "' is still referenced for another API product. " +
                        "Skipping API Deletion....");
                return true;
            } else {
                log.error("Error occurred while deleting the API '" + apiNameForRegistration + "' from Solace Broker");
                throw new HttpResponseException(response2.getStatusLine().getStatusCode(), response2.getStatusLine().
                        getReasonPhrase());
            }
        } else if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT) {
            log.error("Cannot undeploy. Solace API product '" + apiNameWithContext + "' is subscribed for a solace " +
                    "Application");
            throw new HttpResponseException(response1.getStatusLine().getStatusCode(), response1.getStatusLine().
                    getReasonPhrase());
        } else {
            log.error("Error occurred while deleting the API Product '" + apiNameWithContext + "' from Solace Broker");
            throw new HttpResponseException(response1.getStatusLine().getStatusCode(), response1.getStatusLine().
                    getReasonPhrase());
        }
    }

}
