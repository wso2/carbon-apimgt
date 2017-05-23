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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.APIMConfigurations;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.dto.APIDTO;
import org.wso2.carbon.apimgt.core.dto.EndpointDTO;
import org.wso2.carbon.apimgt.core.dto.GatewayDTO;
import org.wso2.carbon.apimgt.core.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.BrokerUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

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

        //TODO:Remove this once broker is integrated
        gwHome = System.getProperty("gwHome");
        if (gwHome == null) {
            gwHome = System.getProperty("carbon.home");
        }
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
            APIDTO gatewayDTO = new APIDTO(APIMgtConstants.GatewayEventTypes.API_CREATE);
            gatewayDTO.setLabels(api.getLabels());
            APISummary apiSummary = new APISummary(api.getId());
            apiSummary.setName(api.getName());
            apiSummary.setVersion(api.getVersion());
            apiSummary.setContext(api.getContext());
            gatewayDTO.setApiSummary(apiSummary);
            publishToPublisherTopic(gatewayDTO);

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
            APIDTO gatewayDTO = new APIDTO(APIMgtConstants.GatewayEventTypes.API_UPDATE);
            gatewayDTO.setLabels(api.getLabels());
            APISummary apiSummary = new APISummary(api.getId());
            apiSummary.setName(api.getName());
            apiSummary.setVersion(api.getVersion());
            apiSummary.setContext(api.getContext());
            gatewayDTO.setApiSummary(apiSummary);
            publishToPublisherTopic(gatewayDTO);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAPI(API api) throws GatewayException {

        if (gwHome == null) {
            // build the message to send
            APIDTO gatewayDTO = new APIDTO(APIMgtConstants.GatewayEventTypes.API_DELETE);
            gatewayDTO.setLabels(api.getLabels());
            APISummary apiSummary = new APISummary(api.getId());
            apiSummary.setName(api.getName());
            apiSummary.setVersion(api.getVersion());
            apiSummary.setContext(api.getContext());
            gatewayDTO.setApiSummary(apiSummary);
            publishToPublisherTopic(gatewayDTO);
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
            subscriptionDTO.setSubscription(subscription);
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
            subscriptionDTO.setSubscription(subscription);
            publishToStoreTopic(subscriptionDTO);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEndpoint(Endpoint endpoint) throws GatewayException {

        if (gwHome == null) {
            EndpointDTO dto = new EndpointDTO(APIMgtConstants.GatewayEventTypes.ENDPOINT_CREATE);
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
            EndpointDTO dto = new EndpointDTO(APIMgtConstants.GatewayEventTypes.ENDPOINT_UPDATE);
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
            EndpointDTO dto = new EndpointDTO(APIMgtConstants.GatewayEventTypes.ENDPOINT_DELETE);
            dto.setEndpoint(endpoint);
            publishToPublisherTopic(dto);
        }
    }

    /**
     * Publish event to publisher topic
     *
     * @param gatewayDTO    gateway data transfer object
     * @throws GatewayException     If there is a failure to publish to gateway
     */
    private void publishToPublisherTopic(GatewayDTO gatewayDTO) throws GatewayException {
        BrokerUtil.publishToTopic(config.getPublisherTopic(), gatewayDTO);
    }

    /**
     * Publish event to store topic
     *
     * @param gatewayDTO    gateway data transfer object
     * @throws GatewayException     If there is a failure to publish to gateway
     */
    private void publishToStoreTopic(GatewayDTO gatewayDTO) throws GatewayException {
        BrokerUtil.publishToTopic(config.getStoreTopic(), gatewayDTO);
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
        Writer writer = null;
        PrintWriter printWriter = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
            printWriter = new PrintWriter(writer);
            printWriter.println(content);
        } catch (IOException e) {
            log.error("Error saving API configuration in " + path, e);
        } finally {
            try {
                if (printWriter != null) {
                    printWriter.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                log.error("Error closing connections", e);
            }
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
        Writer writer = null;
        PrintWriter printWriter = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
            printWriter = new PrintWriter(writer);
            printWriter.println(content);
        } catch (IOException e) {
            log.error("Error saving endpoint configuration in " + path, e);
        } finally {
            try {
                if (printWriter != null) {
                    printWriter.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                log.error("Error closing connections", e);
            }
        }
    }
}
