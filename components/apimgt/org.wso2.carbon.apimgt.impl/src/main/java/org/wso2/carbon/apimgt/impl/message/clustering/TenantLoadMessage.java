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

package org.wso2.carbon.apimgt.impl.message.clustering;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.UUID;

/**
 * Cluster message for loading tenant registry
 */
public class TenantLoadMessage extends ClusteringMessage {

    private static final Log log = LogFactory.getLog(TenantLoadMessage.class);
    private int tenantId;
    private String tenantDomain;
    private UUID messageId;

    public TenantLoadMessage(int tenantId, String tenantDomain) {
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.messageId = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TenantLoadMessage that = (TenantLoadMessage) o;

        if (tenantId != that.tenantId) {
            return false;
        }
        if (!tenantDomain.equals(that.tenantDomain)) {
            return false;
        }
        return messageId.equals(that.messageId);
    }

    @Override
    public int hashCode() {
        int result = tenantId;
        result = 31 * result + tenantDomain.hashCode();
        result = 31 * result + messageId.hashCode();
        return result;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if (!isEnabled()) {
            log.debug("Tenant Load Notifications are disabled");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Executing cluster message " + this.toString());
        }
        APIUtil.loadTenantConfig(tenantDomain);
    }

    @Override
    public ClusteringCommand getResponse() {
        return null;
    }

    @Override
    public String toString() {
        return "TenantLoadMessage [tenantId=" + tenantId + ", tenantDomain=" + tenantDomain + ", messageId=" + messageId
                + "]";
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
