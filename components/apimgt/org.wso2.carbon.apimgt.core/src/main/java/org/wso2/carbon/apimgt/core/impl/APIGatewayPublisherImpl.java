/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.core.impl;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIKey;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.events.APIEvent;
import org.wso2.carbon.apimgt.core.models.events.ApplicationEvent;
import org.wso2.carbon.apimgt.core.models.events.EndpointEvent;
import org.wso2.carbon.apimgt.core.models.events.GatewayEvent;
import org.wso2.carbon.apimgt.core.models.events.SubscriptionDTO;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.BrokerUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This is responsible for handling API gateway related operations
 */
public class APIGatewayPublisherImpl implements APIGateway {
    private static final Logger log = LoggerFactory.getLogger(APIGatewayPublisherImpl.class);
    private APIMConfigurations config;
    private String gatewayFileExtension = ".bal";
    private String endpointConfigName = "endpoint";
    private String gwHome;

    public APIGatewayPublisherImpl() {
        config = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        gwHome = System.getProperty("gwHome");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAPI(API api) throws GatewayException {

        String gatewayConfig = api.getGatewayConfig();
        String defaultConfig = null;
        if (api.isDefaultVersion()) {
            //change the context name without version
            String newContext = "@http:BasePath(\"/" + api.getContext() + "\")";
            defaultConfig = gatewayConfig.replaceAll("@http:BasePath\\(\"(.)*\"\\)", newContext);
            //set the service name with api name only
            String newServiceName = "service " + api.getName() + " {";
            defaultConfig = defaultConfig
                    .replaceAll("service( )*" + api.getName() + "_[0-9]*( )*\\{", newServiceName);
        }
        if (gwHome == null) {

            // build the message to send
            APIEvent apiCreateEvent = new APIEvent(APIMgtConstants.GatewayEventTypes.API_CREATE);
            apiCreateEvent.setLabels(api.getLabels());
            apiCreateEvent.setApiSummary(toAPISummary(api));
            publishToPublisherTopic(apiCreateEvent);

        } else {
            saveApi(api, gwHome, gatewayConfig, false);
            if (api.isDefaultVersion()) {
                saveApi(api, gwHome, defaultConfig, true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAPI(API api) throws GatewayException {

        if (gwHome == null) {
            // build the message to send
            APIEvent apiUpdateEvent = new APIEvent(APIMgtConstants.GatewayEventTypes.API_UPDATE);
            apiUpdateEvent.setLabels(api.getLabels());
            apiUpdateEvent.setApiSummary(toAPISummary(api));
            publishToPublisherTopic(apiUpdateEvent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAPI(API api) throws GatewayException {

        if (gwHome == null) {
            // build the message to send
            APIEvent apiDeleteEvent = new APIEvent(APIMgtConstants.GatewayEventTypes.API_DELETE);
            apiDeleteEvent.setLabels(api.getLabels());
            apiDeleteEvent.setApiSummary(toAPISummary(api));
            publishToPublisherTopic(apiDeleteEvent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAPISubscription(Subscription subscription) throws GatewayException {
        if (gwHome == null) {
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO(
                    APIMgtConstants.GatewayEventTypes.SUBSCRIPTION_CREATE);
            subscriptionDTO.setSubscriptionsList(toSubscriptionValidationData(subscription));
            publishToStoreTopic(subscriptionDTO);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAPISubscription(Subscription subscription) throws GatewayException {
        if (gwHome == null) {
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO(
                    APIMgtConstants.GatewayEventTypes.SUBSCRIPTION_DELETE);
            subscriptionDTO.setSubscriptionsList(toSubscriptionValidationData(subscription));
            publishToStoreTopic(subscriptionDTO);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEndpoint(Endpoint endpoint) throws GatewayException {

        if (gwHome == null) {
            EndpointEvent dto = new EndpointEvent(APIMgtConstants.GatewayEventTypes.ENDPOINT_CREATE);
            dto.setEndpoint(endpoint);
            publishToPublisherTopic(dto);
        } else {
            saveEndpointConfig(gwHome, endpoint.getEndpointConfig());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEndpoint(Endpoint endpoint) throws GatewayException {
        if (gwHome == null) {
            EndpointEvent dto = new EndpointEvent(APIMgtConstants.GatewayEventTypes.ENDPOINT_UPDATE);
            dto.setEndpoint(endpoint);
            publishToPublisherTopic(dto);
        } else {
            saveEndpointConfig(gwHome, endpoint.getEndpointConfig());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteEndpoint(Endpoint endpoint) throws GatewayException {

        if (gwHome == null) {
            EndpointEvent dto = new EndpointEvent(APIMgtConstants.GatewayEventTypes.ENDPOINT_DELETE);
            dto.setEndpoint(endpoint);
            publishToPublisherTopic(dto);
        }
    }

    /**
     * Publish event to publisher topic
     *
     * @param gatewayDTO gateway data transfer object
     * @throws GatewayException If there is a failure to publish to gateway
     */
    private void publishToPublisherTopic(GatewayEvent gatewayDTO) throws GatewayException {
        BrokerUtil.publishToTopic(config.getBrokerConfiguration().getPublisherTopic(), gatewayDTO);
    }

    /**
     * Publish event to store topic
     *
     * @param gatewayDTO gateway data transfer object
     * @throws GatewayException If there is a failure to publish to gateway
     */
    private void publishToStoreTopic(GatewayEvent gatewayDTO) throws GatewayException {
        BrokerUtil.publishToTopic(config.getBrokerConfiguration().getStoreTopic(), gatewayDTO);
    }

    /**
     * Save API into FS
     *
     * @param api     API object
     * @param gwHome  path of the gateway
     * @param content API config
     */
    private void saveApi(API api, String gwHome, String content, boolean isDefaultApi) {
        String deploymentDirPath = gwHome + File.separator + config.getGatewayPackageNamePath();
        File deploymentDir = new File(deploymentDirPath);
        if (!deploymentDir.exists()) {
            log.info("Creating deployment dir in: " + deploymentDirPath);
            boolean created = deploymentDir.mkdirs();
            if (!created) {
                log.error("Error creating directory: " + deploymentDirPath);
            }
        }

        String path;
        if (isDefaultApi) {
            path = deploymentDirPath + File.separator + api.getName() + gatewayFileExtension;
        } else {
            path = deploymentDirPath + File.separator + api.getName() + '_' + api.getVersion() + gatewayFileExtension;
        }
        try (OutputStream outputStream = new FileOutputStream(path)) {
            IOUtils.write(content, outputStream, "UTF-8");
        } catch (IOException e) {
            log.error("Error saving API configuration in " + path, e);
        }
    }

    /**
     * Save config into FS
     *
     * @param gwHome  path of the gateway
     * @param content endpoint config
     */
    private void saveEndpointConfig(String gwHome, String content) {
        String deploymentDirPath = gwHome + File.separator + config.getGatewayPackageNamePath();
        File deploymentDir = new File(deploymentDirPath);
        if (!deploymentDir.exists()) {
            log.info("Creating deployment dir in: " + deploymentDirPath);
            boolean created = deploymentDir.mkdirs();
            if (!created) {
                log.error("Error creating directory: " + deploymentDirPath);
            }
        }

        String path = deploymentDirPath + File.separator + endpointConfigName + gatewayFileExtension;
        try (OutputStream outputStream = new FileOutputStream(path)) {
            IOUtils.write(content, outputStream, "UTF-8");
        } catch (IOException e) {
            log.error("Error saving API configuration in " + path, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeAPIState(API api, String status) throws GatewayException {
        if (gwHome == null) {
            //create the message to be sent to the gateway. This contains the basic details of the API and target
            //lifecycle state.
            APIEvent gatewayDTO = new APIEvent(APIMgtConstants.GatewayEventTypes.API_STATE_CHANGE);
            gatewayDTO.setLabels(api.getLabels());
            gatewayDTO.setApiSummary(toAPISummary(api));
            publishToPublisherTopic(gatewayDTO);
        } else {
            //TODO save to file system: need to consider editor mode scenario
        }
    }

    @Override
    public void addApplication(Application application) throws GatewayException {
        if (application != null) {
            ApplicationEvent applicationEvent = new ApplicationEvent(APIMgtConstants.GatewayEventTypes
                    .APPLICATION_CREATE);
            applicationEvent.setId(application.getId());
            applicationEvent.setName(application.getName());
            applicationEvent.setPolicy(application.getPolicyId());
            applicationEvent.setCreatedUser(application.getCreatedUser());
            publishToStoreTopic(applicationEvent);
        }
    }


    @Override
    public void updateApplication(Application application) throws GatewayException {
        if (application != null) {
            ApplicationEvent applicationEvent = new ApplicationEvent(APIMgtConstants.GatewayEventTypes
                    .APPLICATION_UPDATE);
            applicationEvent.setId(application.getId());
            applicationEvent.setName(application.getName());
            applicationEvent.setPolicy(application.getPolicyId());
            applicationEvent.setCreatedUser(application.getCreatedUser());
            publishToStoreTopic(applicationEvent);
        }
    }


    @Override
    public void deleteApplication(String applicationId) throws GatewayException {
        if (applicationId != null) {
            ApplicationEvent applicationEvent = new ApplicationEvent(APIMgtConstants.GatewayEventTypes
                    .APPLICATION_DELETE);
            applicationEvent.setId(applicationId);
            publishToStoreTopic(applicationEvent);
        }
    }

    /**
     * Convert API definition into APISummary
     *
     * @param api API definition
     * @return The summary of the API
     */
    private APISummary toAPISummary(API api) {
        APISummary apiSummary = new APISummary();
        apiSummary.setId(api.getId());
        apiSummary.setName(api.getName());
        apiSummary.setVersion(api.getVersion());
        apiSummary.setContext(api.getContext());
        apiSummary.setLifeCycleState(api.getLifeCycleStatus());
        apiSummary.setLifeCycleState(api.getLifeCycleStatus());
        apiSummary.setCreatedTime(api.getCreatedTime());
        apiSummary.setLastUpdatedTime(api.getLastUpdatedTime());
        return apiSummary;
    }

    /**
     * Converts Subscription into a list of SubscriptionValidationData
     *
     * @param subscription subscription details
     * @return list of SubscriptionValidationData
     */
    private List<SubscriptionValidationData> toSubscriptionValidationData(Subscription subscription) {
        List<SubscriptionValidationData> subscriptionDataList = new ArrayList<>();
        API subscribedAPI = subscription.getApi();
        Application subscribedApp = subscription.getApplication();
        List<APIKey> keyList = subscribedApp.getKeys();
        for (int i = 0; i < keyList.size(); i++) {
            SubscriptionValidationData subscriptionValidationData = new SubscriptionValidationData(subscribedAPI
                    .getContext(), subscribedAPI.getVersion(), keyList.get(i).getConsumerKey());
            subscriptionValidationData.setKeyEnvType(keyList.get(i).getType());
            subscriptionValidationData.setApiName(subscribedAPI.getName());
            subscriptionValidationData.setApiProvider(subscribedAPI.getProvider());
            subscriptionValidationData.setApplicationId(subscribedApp.getId());
            subscriptionValidationData.setSubscriptionPolicy(subscription.getSubscriptionPolicyId());
            subscriptionDataList.add(subscriptionValidationData);
        }
        return subscriptionDataList;

    }
}
