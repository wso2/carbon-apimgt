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
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.events.APIEvent;
import org.wso2.carbon.apimgt.core.models.events.ApplicationEvent;
import org.wso2.carbon.apimgt.core.models.events.EndpointEvent;
import org.wso2.carbon.apimgt.core.models.events.GatewayEvent;
import org.wso2.carbon.apimgt.core.models.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.BrokerUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
            saveApi(api.getName(), api.getVersion(), gwHome, gatewayConfig, false);
            if (api.isDefaultVersion()) {
                saveApi(api.getName(), api.getVersion(), gwHome, defaultConfig, true);
            }
        }
    }

    @Override
    public void addCompositeAPI(CompositeAPI api) throws GatewayException {
        String gatewayConfig = api.getGatewayConfig();
        String defaultConfig = null;

        if (gwHome == null) {

            // build the message to send
            APIEvent gatewayDTO = new APIEvent(APIMgtConstants.GatewayEventTypes.API_CREATE);
            gatewayDTO.setLabels(api.getLabels());
            APISummary apiSummary = new APISummary();
            apiSummary.setName(api.getName());
            apiSummary.setVersion(api.getVersion());
            apiSummary.setContext(api.getContext());
            gatewayDTO.setApiSummary(apiSummary);
            publishToPublisherTopic(gatewayDTO);
        } else {
            saveApi(api.getName(), api.getVersion(), gwHome, gatewayConfig, false);
        }
    }

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

    @Override
    public void deleteCompositeAPI(CompositeAPI api) throws GatewayException {
        if (gwHome == null) {
            // build the message to send
            APIEvent gatewayDTO = new APIEvent(APIMgtConstants.GatewayEventTypes.API_DELETE);
            gatewayDTO.setLabels(api.getLabels());
            APISummary apiSummary = new APISummary();
            apiSummary.setName(api.getName());
            apiSummary.setVersion(api.getVersion());
            apiSummary.setContext(api.getContext());
            gatewayDTO.setApiSummary(apiSummary);
            publishToPublisherTopic(gatewayDTO);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Gateway home is not properly configured (null)");
            }
        }
    }

    @Override
    public void addAPISubscription(List<SubscriptionValidationData> subscriptionValidationDataList) throws
            GatewayException {
        if (gwHome == null) {
            SubscriptionEvent subscriptionAddEvent = new SubscriptionEvent(APIMgtConstants.GatewayEventTypes
                    .SUBSCRIPTION_CREATE);
            subscriptionAddEvent.setSubscriptionsList(subscriptionValidationDataList);
            publishToStoreTopic(subscriptionAddEvent);
        }
    }

    @Override
    public void updateAPISubscriptionStatus(List<SubscriptionValidationData> subscriptionValidationDataList) throws
            GatewayException {
        if (gwHome == null) {
            SubscriptionEvent subscriptionBlockEvent = new SubscriptionEvent(APIMgtConstants.GatewayEventTypes
                    .SUBSCRIPTION_STATUS_CHANGE);
            subscriptionBlockEvent.setSubscriptionsList(subscriptionValidationDataList);
            publishToStoreTopic(subscriptionBlockEvent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAPISubscription(List<SubscriptionValidationData> subscriptionValidationDataList) throws
            GatewayException {
        if (gwHome == null) {
            SubscriptionEvent subscriptionDeleteEvent = new SubscriptionEvent(
                    APIMgtConstants.GatewayEventTypes.SUBSCRIPTION_DELETE);
            subscriptionDeleteEvent.setSubscriptionsList(subscriptionValidationDataList);
            publishToStoreTopic(subscriptionDeleteEvent);
        }
    }

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

    @Override
    public void deleteEndpoint(Endpoint endpoint) throws GatewayException {

        if (gwHome == null) {
            EndpointEvent dto = new EndpointEvent(APIMgtConstants.GatewayEventTypes.ENDPOINT_DELETE);
            dto.setEndpoint(endpoint);
            publishToPublisherTopic(dto);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Gateway home is not properly configured (null)");
            }
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
     * @param apiName      API Name
     * @param apiVersion   API Version
     * @param gwHome       path of the gateway
     * @param content      API config
     * @param isDefaultApi mark this as the default version of this API. Setting this to <code>true</code>
     *                     will allow accessing API without version number prefix in the URL
     */
    private void saveApi(String apiName, String apiVersion, String gwHome, String content, boolean isDefaultApi) {
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
            path = deploymentDirPath + File.separator + apiName + gatewayFileExtension;
        } else {
            path = deploymentDirPath + File.separator + apiName + '_' + apiVersion + gatewayFileExtension;
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
            if (log.isDebugEnabled()) {
                log.debug("Gateway home is not properly configured (null)");
            }
        }
    }

    @Override
    public void addApplication(Application application) throws GatewayException {
        if (application != null) {
            ApplicationEvent applicationEvent = new ApplicationEvent(APIMgtConstants.GatewayEventTypes
                    .APPLICATION_CREATE);
            applicationEvent.setApplicationId(application.getId());
            applicationEvent.setName(application.getName());
            applicationEvent.setThrottlingTier(application.getPolicyId());
            applicationEvent.setSubscriber(application.getCreatedUser());
            publishToStoreTopic(applicationEvent);
        }
    }


    @Override
    public void updateApplication(Application application) throws GatewayException {
        if (application != null) {
            ApplicationEvent applicationEvent = new ApplicationEvent(APIMgtConstants.GatewayEventTypes
                    .APPLICATION_UPDATE);
            applicationEvent.setApplicationId(application.getId());
            applicationEvent.setName(application.getName());
            applicationEvent.setThrottlingTier(application.getPolicyId());
            applicationEvent.setSubscriber(application.getCreatedUser());
            publishToStoreTopic(applicationEvent);
        }
    }

    @Override
    public void deleteApplication(String applicationId) throws GatewayException {
        if (applicationId != null) {
            ApplicationEvent applicationEvent = new ApplicationEvent(APIMgtConstants.GatewayEventTypes
                    .APPLICATION_DELETE);
            applicationEvent.setApplicationId(applicationId);
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
        apiSummary.setLifeCycleStatus(api.getLifeCycleStatus());
        apiSummary.setLifeCycleStatus(api.getLifeCycleStatus());
        apiSummary.setCreatedTime(api.getCreatedTime());
        apiSummary.setLastUpdatedTime(api.getLastUpdatedTime());
        return apiSummary;
    }

}
