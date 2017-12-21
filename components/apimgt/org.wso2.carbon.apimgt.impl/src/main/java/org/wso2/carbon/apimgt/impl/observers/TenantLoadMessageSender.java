/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.impl.observers;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.message.clustering.TenantLoadMessage;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * Observer class for keeping track of tenant loading/unloading operations
 */
public class TenantLoadMessageSender extends AbstractAxis2ConfigurationContextObserver implements TenantLoadNotifier {

    private static final Log log = LogFactory.getLog(TenantLoadMessageSender.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        if (!isEnabled()) {
            log.debug("Tenant Load Notifications are disabled");
            return;
        }
        notifyTenantLoad();
    }

    @Override
    public void notifyTenantLoad() {
        ClusteringAgent clusteringAgent = getClusteringAgent();
        // if clustering is not enabled, the clusteringAgent should be null
        if (clusteringAgent != null) {
            int tenantId = getTenantId();
            String tenantDomain = getTenantDomain();
            try {
                sendTenantLoadMessage(clusteringAgent, tenantId, tenantDomain, 60);
            } catch (ClusteringFault e) {
                log.error("Could not send TenantLoadMessage for tenant domain: " + tenantDomain + ", tenant " + "id: "
                        + tenantId + ". Several retries failed.", e);
            }
        }
    }

    /**
     * Send the {@link TenantLoadMessage} message
     *
     * @param clusteringAgent {@link ClusteringAgent} object
     * @param tenantId        tenant id
     * @param tenantDomain    tenant domain
     * @param retryCount      retry count if cluster message sending fails
     * @throws ClusteringFault id cluster message sending fails
     */
    void sendTenantLoadMessage(ClusteringAgent clusteringAgent, int tenantId, String tenantDomain, int retryCount)
            throws ClusteringFault {
        // need to re-try if the initial message fails.
        int numberOfRetries = 0;
        ClusteringMessage request = new TenantLoadMessage(tenantId, tenantDomain);
        while (numberOfRetries < retryCount) {
            try {
                clusteringAgent.sendMessage(request, true);
                log.info("Sent [" + request.toString() + "]");
                break;

            } catch (ClusteringFault e) {
                numberOfRetries++;
                if (numberOfRetries < retryCount) {
                    log.warn("Could not send TenantRegistryLoadMessage for tenant " + tenantId
                            + ". Retry will be attempted in 2s. Request: " + request, e);
                } else {
                    // cluster message sending failed, throw
                    throw e;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    /**
     * Retrieves the tenant id from the Thread Local Carbon Context
     *
     * @return tenant id
     */
    int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * Retrieves the tenant domain from the Thread Local Carbon Context
     *
     * @return tenant id
     */
    String getTenantDomain() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    /**
     * Retrieves the Clustering Agent object from the Axis2 Configuration
     *
     * @return {@link ClusteringAgent} object if clustering is enabled, else null
     */
    ClusteringAgent getClusteringAgent() {
        return ServiceReferenceHolder.getContextService().getServerConfigContext().getAxisConfiguration()
                .getClusteringAgent();
    }

    public void terminatingConfigurationContext(ConfigurationContext configContext) {
        // do nothing
    }

    /**
     * Check if the tenant load notifier is enabled
     *
     * @return true if the java system property 'enableTenantLoadNotification'
     * is set to true, else false
     */
    public boolean isEnabled() {
        return Boolean.parseBoolean((System.getProperty(APIConstants.ENABLE_TENANT_LOAD_NOTIFICATION)));
    }
}
