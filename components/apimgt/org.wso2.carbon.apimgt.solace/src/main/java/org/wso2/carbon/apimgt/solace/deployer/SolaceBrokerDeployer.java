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
package org.wso2.carbon.apimgt.solace.deployer;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.deployer.ExternalGatewayDeployer;
import org.wso2.carbon.apimgt.impl.deployer.exceptions.DeployerException;
import org.wso2.carbon.apimgt.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils;
import org.wso2.carbon.context.CarbonContext;

import java.util.List;


@Component(
        name = "solace.external.gateway.deployer.component",
        immediate = true,
        service = ExternalGatewayDeployer.class
)
/**
 *  This is to register Solace Deployer as connector
 */
public class SolaceBrokerDeployer implements ExternalGatewayDeployer {

    private static final Log log = LogFactory.getLog(SolaceBrokerDeployer.class);
    protected ApiMgtDAO apiMgtDAO;

    /**
     * Get external vendor type as Solace
     *
     * @return String Solace type
     */
    @Override
    public String getType() {
        return APIConstants.SOLACE_ENVIRONMENT;
    }

    /**
     * Deploy API artifact to provided environment
     *
     * @param api         API to be deployed into Solace broker
     * @param environment Environment to be deployed
     * @throws DeployerException if error occurs when deploying APIs to Solace broker
     */
    @Override
    public boolean deploy(API api, Environment environment) throws DeployerException {
        String apiDefinition = api.getAsyncApiDefinition();
        Aai20Document aai20Document = (Aai20Document) Library.readDocumentFromJSONString(apiDefinition);
        String apiNameForRegistration = api.getId().getApiName() + "-" + api.getId().getVersion();
        String[] apiContextParts = api.getContext().split("/");
        String apiNameWithContext = environment.getName() + "-" + api.getId().getName() + "-" + apiContextParts[1] +
                "-" + apiContextParts[2];
        SolaceAdminApis solaceAdminApis;
        try {
            solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();
        } catch (APIManagementException e) {
            throw new DeployerException(e.getMessage());
        }

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
                        throw new DeployerException(response3.getStatusLine().toString());
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
                                throw new DeployerException(response6.getStatusLine().toString());
                            }
                            throw new DeployerException(response3.getStatusLine().toString());
                        }
                    } else {
                        log.error("Error while registering API in Solace - '" + apiNameForRegistration + "'");
                        throw new DeployerException(response2.getStatusLine().toString());
                    }
                } else {
                    log.error("Error while finding API '" + apiNameForRegistration + "' in Solace");
                    throw new DeployerException(response5.getStatusLine().toString());
                }
            } else {
                log.error("Error while finding API product '" + apiNameWithContext + "' in Solace");
                throw new DeployerException(response4.getStatusLine().toString());
            }
        } else {
            log.error("Cannot find specified Solace environment - '" + environment.getName() + "'");
            throw new DeployerException(response1.getStatusLine().toString());
        }
    }

    /**
     * Undeploy API artifact from provided environment
     *
     * @param apiName     Name of the API to be undeployed from Solace broker
     * @param apiVersion  Version of the API to be undeployed from Solace broker
     * @param apiContext  Context of the API to be undeployed from Solace broker
     * @param environment Environment needed to be undeployed API from
     * @throws DeployerException if error occurs when undeploying APIs from Solace broker
     */
    @Override
    public boolean undeploy(String apiName, String apiVersion, String apiContext, Environment environment) throws DeployerException {
        String apiNameForRegistration = apiName + "-" + apiVersion;
        String[] apiContextParts = apiContext.split("/");
        String apiNameWithContext = environment.getName() + "-" + apiName + "-" + apiContextParts[1] +
                "-" + apiContextParts[2];
        SolaceAdminApis solaceAdminApis;
        try {
            solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();
        } catch (APIManagementException e) {
            throw new DeployerException(e.getMessage());
        }
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
                throw new DeployerException(response2.getStatusLine().toString());
            }
        } else if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT) {
            log.error("Cannot undeploy. Solace API product '" + apiNameWithContext + "' is subscribed for a solace " +
                    "Application");
            throw new DeployerException(response1.getStatusLine().toString());
        } else {
            log.error("Error occurred while deleting the API Product '" + apiNameWithContext + "' from Solace Broker");
            throw new DeployerException(response1.getStatusLine().toString());
        }
    }

    /**
     * Undeploy API artifact from provided environment in the external gateway when Api is retired
     *
     * @param api         API to be undeployed from the external gateway
     * @param environment Environment needed to be undeployed API from the external gateway
     * @throws DeployerException if error occurs when undeploying APIs from the external gateway
     */
    public boolean undeployWhenRetire(API api, Environment environment) throws DeployerException {
        apiMgtDAO = ApiMgtDAO.getInstance();
        Application application;
        APIProvider apiProvider;

        // Remove subscription
        try {
            apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            List<SubscribedAPI> apiUsages = apiProvider.getAPIUsageByAPIId(api.getUuid(), api.getOrganization());
            for (SubscribedAPI usage : apiUsages) {
                application = usage.getApplication();
                //Check whether the subscription is belongs to an API deployed in Solace
                if (APIConstants.SOLACE_ENVIRONMENT.equals(api.getGatewayVendor())) {
                    SolaceNotifierUtils.unsubscribeAPIProductFromSolaceApplication(api, application);
                }
            }
        } catch (APIManagementException e) {
            throw new DeployerException("Error occurred when removing subscriptions of the API to be retired", e);
        }

        // undeploy API from Solace
        boolean deletedFromSolace = undeploy(api.getId().getName(),api.getId().getVersion(),api.getContext(),
                environment);
        if (!deletedFromSolace) {
            throw new DeployerException("Error while deleting API product from Solace broker");
        }
        return true;
    }

}
