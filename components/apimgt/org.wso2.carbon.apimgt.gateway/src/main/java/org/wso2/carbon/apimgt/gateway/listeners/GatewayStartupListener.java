/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.listeners;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.EndpointCertificateDeployer;
import org.wso2.carbon.apimgt.gateway.GoogleAnalyticsConfigDeployer;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTTokensRetriever;
import org.wso2.carbon.apimgt.gateway.throttling.util.BlockingConditionRetriever;
import org.wso2.carbon.apimgt.gateway.throttling.util.KeyTemplateRetriever;
import org.wso2.carbon.apimgt.gateway.webhooks.WebhooksDataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.certificatemgt.reloader.CertificateReLoaderUtil;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.apimgt.jms.listener.utils.JMSTransportHandler;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Class for loading synapse artifacts to memory on initial server startup
 */

public class GatewayStartupListener extends AbstractAxis2ConfigurationContextObserver
        implements ServerStartupObserver, ServerShutdownHandler {

    private static final Log log = LogFactory.getLog(GatewayStartupListener.class);
    private boolean debugEnabled = log.isDebugEnabled();
    private JMSTransportHandler jmsTransportHandlerForTrafficManager;
    private JMSTransportHandler jmsTransportHandlerForEventHub;
    private ThrottleProperties throttleProperties;
    private GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;
    private boolean isAPIsDeployedInSyncMode = false;
    private int syncModeDeploymentCount = 0;
    private int retryCount = 10;

    public GatewayStartupListener() {

        gatewayArtifactSynchronizerProperties =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration()
                        .getGatewayArtifactSynchronizerProperties();
        throttleProperties = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getThrottleProperties();
        ThrottleProperties.JMSConnectionProperties jmsConnectionProperties =
                throttleProperties.getJmsConnectionProperties();
        this.jmsTransportHandlerForTrafficManager =
                new JMSTransportHandler(jmsConnectionProperties.getJmsConnectionProperties());
        EventHubConfigurationDto.EventHubReceiverConfiguration eventHubReceiverConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto()
                        .getEventHubReceiverConfiguration();
        if (eventHubReceiverConfiguration != null) {
            this.jmsTransportHandlerForEventHub =
                    new JMSTransportHandler(eventHubReceiverConfiguration.getJmsConnectionParameters());
        }
    }

    @Override
    public void completingServerStartup() {

        try {
            CertificateMgtUtils.backupOriginalTrustStore();
            CertificateMgtUtils.startListenerCertificateReLoader();
        } catch (CertificateManagementException e) {
            log.error("Error while Backup Truststore", e);
        }
        cleanDeployment(CarbonUtils.getAxis2Repo());
    }

    private boolean deployArtifactsAtStartup(String tenantDomain) throws ArtifactSynchronizerException {

        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
        boolean flag = false;
        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
            InMemoryAPIDeployer inMemoryAPIDeployer = new InMemoryAPIDeployer();
            flag = inMemoryAPIDeployer.deployAllAPIsAtGatewayStartup(gatewayArtifactSynchronizerProperties
                    .getGatewayLabels(), tenantDomain);
        }
        return flag;
    }

    private void cleanDeployment(String artifactRepositoryPath) {

        InMemoryAPIDeployer inMemoryAPIDeployer = new InMemoryAPIDeployer();
        inMemoryAPIDeployer.cleanDeployment(artifactRepositoryPath);
    }

    @Override
    public void completedServerStartup() {

        new Thread(() -> {

            try {
                new EndpointCertificateDeployer(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)
                        .deployCertificatesAtStartup();
                new GoogleAnalyticsConfigDeployer(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME).deploy();
            } catch (APIManagementException e) {
                log.error(e);
            }
        }).start();
        SubscriptionDataHolder.getInstance()
                .registerTenantSubscriptionStore(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        ServiceReferenceHolder.getInstance().addLoadedTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        retrieveAndDeployArtifacts(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        retrieveBlockConditionsAndKeyTemplates();
        WebhooksDataHolder.getInstance().registerTenantSubscriptionStore(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        jmsTransportHandlerForTrafficManager
                .subscribeForJmsEvents(APIConstants.TopicNames.TOPIC_THROTTLE_DATA, new JMSMessageListener());
        jmsTransportHandlerForEventHub.subscribeForJmsEvents(APIConstants.TopicNames.TOPIC_TOKEN_REVOCATION,
                new GatewayTokenRevocationMessageListener());
        jmsTransportHandlerForEventHub.subscribeForJmsEvents(APIConstants.TopicNames.TOPIC_CACHE_INVALIDATION,
                new APIMgtGatewayCacheMessageListener());
        jmsTransportHandlerForEventHub
                .subscribeForJmsEvents(APIConstants.TopicNames.TOPIC_NOTIFICATION, new GatewayJMSMessageListener());
        jmsTransportHandlerForEventHub.subscribeForJmsEvents(APIConstants.TopicNames.TOPIC_ASYNC_WEBHOOKS_DATA,
                new GatewayJMSMessageListener());
    }

    private void retrieveAndDeployArtifacts(String tenantDomain) {

        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
            if (APIConstants.GatewayArtifactSynchronizer.GATEWAY_STARTUP_SYNC
                    .equals(gatewayArtifactSynchronizerProperties.getGatewayStartup())) {
                try {
                    deployAPIsInSyncMode(tenantDomain);
                } catch (ArtifactSynchronizerException e) {
                    log.error("Error in Deploying APIs to gateway", e);
                }
            } else {
                deployAPIsInAsyncMode(tenantDomain);
            }
        }
    }

    private void deployAPIsInSyncMode(String tenantDomain) throws ArtifactSynchronizerException {

        if (debugEnabled) {
            log.debug("Deploying Artifacts in synchronous mode");
        }
        syncModeDeploymentCount++;
        isAPIsDeployedInSyncMode = deployArtifactsAtStartup(tenantDomain);
        if (!isAPIsDeployedInSyncMode) {
            log.error("Deployment attempt : " + syncModeDeploymentCount + " was unsuccessful");
            if (!(syncModeDeploymentCount > retryCount)) {
                deployAPIsInSyncMode(tenantDomain);
            } else {
                log.error("Maximum retry limit exceeded. Server is starting without deploying all synapse artifacts");
            }
        } else {
            log.info("Deployment attempt : " + syncModeDeploymentCount + " was successful");
        }
    }

    @Override
    public void invoke() {

        CertificateReLoaderUtil.shutDownCertificateReLoader();
        if (jmsTransportHandlerForTrafficManager != null) {
            // This method will make shutdown the Listener.
            log.debug("Unsubscribe from JMS Events...");
            jmsTransportHandlerForTrafficManager.unSubscribeFromEvents();
        }
        if (jmsTransportHandlerForEventHub != null) {
            log.debug("Unsubscribe from JMS Events...");
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
        }
    }

    public void deployAPIsInAsyncMode(String tenantDomain) {

        new Thread(new AsyncAPIDeployment(tenantDomain)).start();
    }

    private void deployArtifactsInGateway(String tenantDomain) throws ArtifactSynchronizerException {

        if (debugEnabled) {
            log.debug("Deploying Artifacts in asynchronous mode");
        }

        long retryDuration = gatewayArtifactSynchronizerProperties.getRetryDuartion();
        double reconnectionProgressionFactor = 2.0;
        long maxReconnectDuration = 1000 * 60 * 60; // 1 hour

        while (true) {
            boolean isArtifactsDeployed = deployArtifactsAtStartup(tenantDomain);
            if (isArtifactsDeployed) {
                log.info("Synapse Artifacts deployed Successfully in the Gateway");
                break;
            } else {
                retryDuration = (long) (retryDuration * reconnectionProgressionFactor);
                if (retryDuration > maxReconnectDuration) {
                    retryDuration = maxReconnectDuration;
                }
                log.error("Unable to deploy synapse artifacts at gateway. Next retry in " + (retryDuration / 1000)
                        + " seconds");
                try {
                    Thread.sleep(retryDuration);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    private void retrieveBlockConditionsAndKeyTemplates() {

        if (throttleProperties.getBlockCondition().isEnabled()) {
            BlockingConditionRetriever webServiceThrottleDataRetriever = new BlockingConditionRetriever();
            webServiceThrottleDataRetriever.startWebServiceThrottleDataRetriever();
            KeyTemplateRetriever webServiceBlockConditionsRetriever = new KeyTemplateRetriever();
            webServiceBlockConditionsRetriever.startKeyTemplateDataRetriever();

            // Start web service based revoked JWT tokens retriever.
            // Advanced throttle properties & blocking conditions have to be enabled for JWT token
            // retrieval due to the throttle config dependency for this feature.
            RevokedJWTTokensRetriever webServiceRevokedJWTTokensRetriever = new RevokedJWTTokensRetriever();
            webServiceRevokedJWTTokensRetriever.startRevokedJWTTokensRetriever();
        }

    }

    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        SubscriptionDataHolder.getInstance().registerTenantSubscriptionStore(tenantDomain);
        WebhooksDataHolder.getInstance().registerTenantSubscriptionStore(tenantDomain);

        cleanDeployment(configContext.getAxisConfiguration().getRepository().getPath());
        new Thread(() -> {
            try {
                new EndpointCertificateDeployer(tenantDomain).deployCertificatesAtStartup();
                new GoogleAnalyticsConfigDeployer(tenantDomain).deploy();
            } catch (APIManagementException e) {
                log.error(e);
            }
        }).start();
        retrieveAndDeployArtifacts(tenantDomain);
        ServiceReferenceHolder.getInstance().addLoadedTenant(tenantDomain);
    }

    @Override
    public void terminatedConfigurationContext(ConfigurationContext configCtx) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        ServiceReferenceHolder.getInstance().removeUnloadedTenant(tenantDomain);
        SubscriptionDataHolder.getInstance().unregisterTenantSubscriptionStore(tenantDomain);
        WebhooksDataHolder.getInstance().unregisterTenantSubscriptionStore(tenantDomain);
    }

    @Override
    public void terminatingConfigurationContext(ConfigurationContext configCtx) {

        cleanDeployment(configCtx.getAxisConfiguration().getRepository().getPath());
    }

    class AsyncAPIDeployment implements Runnable {

        private String tenantDomain;

        public AsyncAPIDeployment(String tenantDomain) {

            this.tenantDomain = tenantDomain;
        }

        @Override
        public void run() {

            try {
                deployArtifactsInGateway(tenantDomain);
            } catch (ArtifactSynchronizerException e) {
                log.error("Error in Deploying APIs to gateway", e);
            }
        }
    }
}