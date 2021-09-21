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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.notifier.SubscriptionsNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;
import java.util.*;

public class SolaceSubscriptionsNotifier extends SubscriptionsNotifier {
    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(SolaceDeployAPIInGatewayNotifier.class);


    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        apiMgtDAO = ApiMgtDAO.getInstance();
        process(event);
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
        try {
            subscriptionEvent = (SubscriptionEvent) event;
        } catch (ExceptionInInitializerError e) {
            throw new NotifierException("Event type is not provided correctly");
        }

        if (APIConstants.EventType.SUBSCRIPTIONS_CREATE.name().equals(event.getType())) {
            crateSubscription(subscriptionEvent);
        } else if (APIConstants.EventType.SUBSCRIPTIONS_UPDATE.name().equals(event.getType())) {
            updateSubscription(subscriptionEvent);
        }
    }

    /**
     * Create subscriptions to Solace APIs
     *
     * @param event SubscriptionEvent to create Solace API subscriptions
     * @throws NotifierException if error occurs when creating subscription for Solace APIs
     */
    private void crateSubscription(SubscriptionEvent event) throws NotifierException {

        String apiUUID = event.getApiUUID();
        String applicationUUID = event.getApplicationUUID();

        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPI(apiMgtDAO.getAPIIdentifierFromUUID(apiUUID));
            Application application = apiProvider.getApplicationByUUID(applicationUUID);

            //Check whether the subscription is belongs to an API deployed in Solace
            if (APIConstants.SOLACE_ENVIRONMENT.equals(api.getGatewayVendor())) {
                ArrayList<String> solaceApiProducts = new ArrayList<>();
                List<Environment> deployedSolaceEnvironments = getDeployedSolaceEnvironmentsFromRevisionDeployments(api);
                String applicationOrganizationName = getSolaceOrganizationName(deployedSolaceEnvironments);
                if (applicationOrganizationName != null) {
                    try {
                        boolean apiProductDeployedIntoSolace = checkApiProductAlreadyDeployedIntoSolaceEnvironments(api, deployedSolaceEnvironments);
                        if (apiProductDeployedIntoSolace) {
                            for (Environment environment : deployedSolaceEnvironments) {
                                solaceApiProducts.add(generateApiProductNameForSolaceBroker(api, environment.getName()));
                            }
                            deployApplicationToSolaceBroker(application, solaceApiProducts, applicationOrganizationName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    log.error("Cannot create solace application with API product deployed in different organizations...");
                    throw new APIManagementException("Cannot create solace application with API product deployed in different organizations...");
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
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
            API api = apiProvider.getAPI(apiMgtDAO.getAPIIdentifierFromUUID(apiUUID));
            Application application = apiProvider.getApplicationByUUID(applicationUUID);

            //Check whether the subscription is belongs to an API deployed in Solace
            if (APIConstants.SOLACE_ENVIRONMENT.equals(api.getGatewayVendor())) {
                ArrayList<String> solaceApiProducts = new ArrayList<>();
                List<Environment> deployedSolaceEnvironments =
                        getDeployedSolaceEnvironmentsFromRevisionDeployments(api);
                String applicationOrganizationName = getSolaceOrganizationName(deployedSolaceEnvironments);
                if (applicationOrganizationName != null) {
                    try {
                        boolean apiProductDeployedIntoSolace = checkApiProductAlreadyDeployedIntoSolaceEnvironments
                                (api, deployedSolaceEnvironments);
                        if (apiProductDeployedIntoSolace) {
                            for (Environment environment : deployedSolaceEnvironments) {
                                solaceApiProducts.add(generateApiProductNameForSolaceBroker(api, environment.getName()));
                            }
                            deployApplicationToSolaceBroker(application, solaceApiProducts, applicationOrganizationName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    log.error("Cannot create solace application with API product deployed in " +
                            "different organizations...");
                    throw new APIManagementException("Cannot create solace application with API product deployed " +
                            "in different organizations...");
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
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
            API api = apiProvider.getAPI(apiMgtDAO.getAPIIdentifierFromUUID(apiUUID));
            Application application = apiProvider.getApplicationByUUID(applicationUUID);

            //Check whether the subscription is belongs to an API deployed in Solace
            if (APIConstants.SOLACE_ENVIRONMENT.equals(api.getGatewayVendor())) {
                unsubscribeAPIProductFromSolaceApplication(api, application);
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }
    }


    /**
     * Generate a name for the API product in Solace broker
     *
     * @param api             Name of the API
     * @param environmentName Name of the environment
     * @return APIProduct name for Solace broker
     */
    private String generateApiProductNameForSolaceBroker(API api, String environmentName) {
        String[] apiContextParts = api.getContext().split("/");
        return environmentName + "-" + api.getId().getName() + "-" + apiContextParts[1] + "-" + apiContextParts[2];
    }

    /**
     * Check whether the given API product is already deployed in the Solace broker
     *
     * @param api          Name of the API
     * @param organization Name of the organization
     * @return returns true if the given API product is already deployed in the Solace
     * @throws APIManagementException If an error occurs when checking API product availability
     */
    private boolean checkApiProductAlreadyDeployedInSolace(API api, String organization) throws IOException,
            APIManagementException {

        Map<String, Environment> environmentMap = APIUtil.getReadOnlyGatewayEnvironments();
        Environment solaceEnvironment = environmentMap.get(APIConstants.SOLACE_ENVIRONMENT);
        if (solaceEnvironment != null) {
            SolaceAdminApis solaceAdminApis = new SolaceAdminApis(solaceEnvironment.getServerURL(), solaceEnvironment.getUserName(),
                    solaceEnvironment.getPassword(), solaceEnvironment.getAdditionalProperties().get(APIConstants.
                    SOLACE_ENVIRONMENT_DEV_NAME));
            String apiNameWithContext = generateApiProductNameForSolaceBroker(api,
                    getThirdPartySolaceBrokerEnvironmentNameOfAPIDeployment(api));
            HttpResponse response = solaceAdminApis.apiProductGet(organization, apiNameWithContext);

            if (response != null) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    log.info("API product found in Solace Broker");
                    return true;
                } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    log.error("API product not found in Solace broker");
                    log.error(EntityUtils.toString(response.getEntity()));
                    throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().
                            getReasonPhrase());
                } else {
                    log.error("Cannot find API product in Solace Broker");
                    log.error(EntityUtils.toString(response.getEntity()));
                    throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().
                            getReasonPhrase());
                }
            }
            return false;
        } else {
            throw new APIManagementException("Solace Environment configurations are not provided properly");
        }
    }

    /**
     * Check whether the given API product is already deployed in the Solace environment
     *
     * @param api          Name of the API
     * @param environments List of the environments
     * @return returns true if the given API product is already deployed in one of environments
     * @throws IOException            If an error occurs when checking API product availability
     * @throws APIManagementException if an error occurs when getting Solace config
     */
    private boolean checkApiProductAlreadyDeployedIntoSolaceEnvironments(API api, List<Environment> environments)
            throws IOException, APIManagementException {
        int numberOfDeployedEnvironmentsInSolace = 0;
        for (Environment environment : environments) {
            String apiNameWithContext = generateApiProductNameForSolaceBroker(api, environment.getName());
            SolaceAdminApis solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();
            HttpResponse response = solaceAdminApis.apiProductGet(environment.getAdditionalProperties().get(APIConstants.
                    SOLACE_ENVIRONMENT_ORGANIZATION), apiNameWithContext);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                log.info("API product found in Solace Broker");
                numberOfDeployedEnvironmentsInSolace++;
            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                log.error("API product not found in Solace broker");
                log.error(EntityUtils.toString(response.getEntity()));
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().
                        getReasonPhrase());
            } else {
                log.error("Cannot find API product in Solace Broker");
                log.error(EntityUtils.toString(response.getEntity()));
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().
                        getReasonPhrase());
            }
        }
        return numberOfDeployedEnvironmentsInSolace == environments.size();
    }

    /**
     * Deploy an application to Solace broker
     *
     * @param application  Application to be deployed
     * @param apiProducts  Api products to be subscribed to Application
     * @param organization Name of the organization
     * @throws IOException            If an error occurs when deploying the application
     * @throws APIManagementException if an error occurs when getting Solace config
     */
    private void deployApplicationToSolaceBroker(Application application, ArrayList<String> apiProducts, String organization)
            throws IOException, APIManagementException {

        SolaceAdminApis solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();

        // check existence of the developer
        HttpResponse response1 = solaceAdminApis.developerGet(organization);
        if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            log.info("Developer found in Solace Broker");

            //check application status
            HttpResponse response2 = solaceAdminApis.applicationGet(organization, application, "default");
            if (response2.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // app already exists
                log.info("Solace application '" + application.getName() + "' already exists in Solace." +
                        " Updating Application......");
                HttpResponse response3 = solaceAdminApis.applicationPatchAddSubscription(organization, application,
                        apiProducts);
                if (response3.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    log.info("Solace application '" + application.getName() + "' updated successfully");
                } else {
                    log.error("Error while updating Solace application '" + application.getName() + "'");
                    throw new HttpResponseException(response3.getStatusLine().getStatusCode(), response3.getStatusLine()
                            .getReasonPhrase());
                }
            } else if (response2.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

                String responseString = EntityUtils.toString(response2.getEntity());
                if (responseString.contains(String.valueOf(HttpStatus.SC_NOT_FOUND))) {
                    // create new app
                    log.info("Solace application '" + application.getName() + "' not found in Solace Broker." +
                            "Creating new application......");
                    HttpResponse response4 = solaceAdminApis.createApplication(organization, application,
                            apiProducts);
                    if (response4.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                        log.info("Solace application '" + application.getName() + "' created successfully");
                    } else {
                        log.error("Error while creating Solace application '" + application.getName() + "'");
                        throw new HttpResponseException(response4.getStatusLine().getStatusCode(), response4.
                                getStatusLine().getReasonPhrase());
                    }
                } else {
                    log.error("Error while searching for application '" + application.getName() + "'");
                    throw new HttpResponseException(response2.getStatusLine().getStatusCode(), response2.
                            getStatusLine().getReasonPhrase());
                }
            } else {
                log.error("Error while searching for application '" + application.getName() + "'");
                throw new HttpResponseException(response2.getStatusLine().getStatusCode(), response2.
                        getStatusLine().getReasonPhrase());
            }
        } else if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            log.error("Developer not found in Solace Broker");
            throw new HttpResponseException(response1.getStatusLine().getStatusCode(), response1.getStatusLine().
                    getReasonPhrase());
        } else {
            log.error("Error while finding developer in Solace Broker");
            throw new HttpResponseException(response1.getStatusLine().getStatusCode(), response1.getStatusLine().
                    getReasonPhrase());
        }
    }

    /**
     * Build the request body for Application creation request
     *
     * @param appName     Name of the application to be deployed
     * @param apiProducts Api products to be subscribed to Application
     * @return org.json.JSON Object of request body
     */
    private org.json.JSONObject buildRequestBodyForCreatingApp(String appName, ArrayList<String> apiProducts) {

        org.json.JSONObject requestBody = new org.json.JSONObject();
        requestBody.put("name", appName);
        requestBody.put("expiresIn", -1);

        //add api products
        org.json.JSONArray apiProductsArray = new org.json.JSONArray();
        for (String x : apiProducts) {
            apiProductsArray.put(x);
        }
        requestBody.put("apiProducts", apiProductsArray);

        //add credentials
        org.json.JSONObject credentialsBody = new org.json.JSONObject();
        credentialsBody.put("expiresAt", -1);
        org.json.JSONObject credentialsSecret = new org.json.JSONObject();
        credentialsSecret.put("consumerKey", "elevator-app-key");
        credentialsSecret.put("consumerSecret", "elevator-app-secret");
        credentialsBody.put("secret", credentialsSecret);
        requestBody.put("credentials", credentialsBody);

        return requestBody;
    }

    /**
     * Check whether the given API is already deployed in the Solace using revision
     *
     * @param api Name of the API
     * @return returns true if the given API is already deployed
     * @throws APIManagementException If an error occurs when checking API product availability
     */
    public boolean checkWhetherAPIDeployedToSolaceUsingRevision(API api) throws APIManagementException {
        Map<String, Environment> thirdPartyEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        List<APIRevisionDeployment> deployments = apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(api.getUuid());
        for (APIRevisionDeployment deployment : deployments) {
            if (deployment.isDisplayOnDevportal()) {
                String environmentName = deployment.getDeployment();
                if (thirdPartyEnvironments.containsKey(environmentName)) {
                    Environment deployedEnvironment = thirdPartyEnvironments.get(environmentName);
                    if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(deployedEnvironment.getProvider())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get third party Solace broker organization Name for API deployment
     *
     * @param api Name of the API
     * @return String of the name of organization in Solace broker
     * @throws APIManagementException is error occurs when getting the name of the organization name
     */
    public String getThirdPartySolaceBrokerOrganizationNameOfAPIDeployment(API api) throws APIManagementException {
        Map<String, Environment> thirdPartyEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        List<APIRevisionDeployment> deployments = apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(api.getUuid());
        for (APIRevisionDeployment deployment : deployments) {
            if (deployment.isDisplayOnDevportal()) {
                String environmentName = deployment.getDeployment();
                if (thirdPartyEnvironments.containsKey(environmentName)) {
                    Environment deployedEnvironment = thirdPartyEnvironments.get(environmentName);
                    if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(deployedEnvironment.getProvider())) {
                        return deployedEnvironment.getAdditionalProperties().get(APIConstants.SOLACE_ENVIRONMENT_ORGANIZATION);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get third party Solace broker organization Name
     *
     * @param environments List of the environments
     * @return String of the name of organization in Solace broker
     */
    public String getSolaceOrganizationName(List<Environment> environments) {
        HashSet<String> organizationNames = new HashSet<>();
        for (Environment environment : environments) {
            if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(environment.getProvider())) {
                organizationNames.add(environment.getAdditionalProperties().get(APIConstants.SOLACE_ENVIRONMENT_ORGANIZATION));
            }
        }
        if (organizationNames.size() == 1) {
            return organizationNames.toArray()[0].toString();
        } else {
            return null;
        }
    }

    /**
     * Get third party Solace broker environment Name for API deployment
     *
     * @param api Name of the API
     * @return String of the name of environment in Solace broker
     * @throws APIManagementException is error occurs when getting the name of the environment name
     */
    private String getThirdPartySolaceBrokerEnvironmentNameOfAPIDeployment(API api) throws APIManagementException {
        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        List<APIRevisionDeployment> deployments = apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(api.getUuid());
        for (APIRevisionDeployment deployment : deployments) {
            String environmentName = deployment.getDeployment();
            if (gatewayEnvironments.containsKey(environmentName)) {
                Environment deployedEnvironment = gatewayEnvironments.get(environmentName);
                if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(deployedEnvironment.getProvider())) {
                    return environmentName;
                }
            }
        }
        return null;
    }

    /**
     * Get deployed solace environment name form the revision deployments
     *
     * @param api Name of the API
     * @return List<ThirdPartyEnvironment> List of deployed solace environments
     * @throws APIManagementException is error occurs when getting the list of solace environments
     */
    private List<Environment> getDeployedSolaceEnvironmentsFromRevisionDeployments(API api) throws
            APIManagementException {
        List<Environment> deployedSolaceEnvironments = new ArrayList<>();
        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        List<APIRevisionDeployment> deployments = apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(api.getUuid());
        for (APIRevisionDeployment deployment : deployments) {
            String environmentName = deployment.getDeployment();
            if (gatewayEnvironments.containsKey(environmentName)) {
                Environment deployedEnvironment = gatewayEnvironments.get(environmentName);
                if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(deployedEnvironment.getProvider())) {
                    deployedSolaceEnvironments.add(deployedEnvironment);
                }
            }
        }
        return deployedSolaceEnvironments;
    }

    /**
     * Unsubscribe the given API product from the Solace application
     *
     * @param api         API object to be unsubscribed
     * @param application Solace application
     * @throws APIManagementException is error occurs when unsubscribing the API from application
     */
    private void unsubscribeAPIProductFromSolaceApplication(API api, Application application) throws APIManagementException {
        List<Environment> deployedSolaceEnvironments = getDeployedSolaceEnvironmentsFromRevisionDeployments(api);
        String applicationOrganizationName = getSolaceOrganizationName(deployedSolaceEnvironments);
        ArrayList<String> solaceApiProducts = new ArrayList<>();
        if (applicationOrganizationName != null) {
            for (Environment environment : deployedSolaceEnvironments) {
                solaceApiProducts.add(generateApiProductNameForSolaceBroker(api, environment.getName()));
            }
            SolaceAdminApis solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();
            HttpResponse response = solaceAdminApis.applicationPatchRemoveSubscription(applicationOrganizationName,
                    application, solaceApiProducts);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                log.info("API product unsubscribed from Solace application '" + application.getName() + "'");
                try {
                    String responseString = EntityUtils.toString(response.getEntity());
                    org.json.JSONObject jsonObject = new org.json.JSONObject(responseString);
                    if (jsonObject.getJSONArray("apiProducts") != null) {
                        if (jsonObject.getJSONArray("apiProducts").length() == 0) {
                            // delete application in Solace because of 0 number of api products
                            HttpResponse response2 = solaceAdminApis.deleteApplication(applicationOrganizationName,
                                    application);
                            if (response2.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                                log.info("Successfully deleted application '" + application.getName() + "' in " +
                                        "Solace Broker");
                            } else {
                                log.error("Error while deleting application '" + application.getName() + "' in Solace");
                                throw new APIManagementException("Error while deleting application '" +
                                        application.getName() + "' in Solace");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                log.error("Error while unsubscribing API product from Solace Application '" + application.getName()
                        + "'");
                throw new APIManagementException(response.getStatusLine().getStatusCode() + "-" + response.getStatusLine()
                        .getReasonPhrase());
            }
        } else {
            throw new APIManagementException("Multiple Solace organizations found");
        }
    }

}
