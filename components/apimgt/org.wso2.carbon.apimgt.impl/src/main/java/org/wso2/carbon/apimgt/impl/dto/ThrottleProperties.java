/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ThrottleProperties {
    private boolean enabledSubscriptionLevelSpikeArrest;
    private DataPublisher dataPublisher;
    private GlobalEngineWSConnection globalEngineWSConnection;
    private DataPublisherPool dataPublisherPool;
    private DataPublisherThreadPool dataPublisherThreadPool;
    private JMSConnectionProperties jmsConnectionProperties;
    private boolean enableUnlimitedTier;
    private String throttleDataSourceName;

    public boolean isEnablePolicyDeployment() {

        return enablePolicyDeployment;
    }

    public void setEnablePolicyDeployment(boolean enablePolicyDeployment) {

        this.enablePolicyDeployment = enablePolicyDeployment;
    }

    private boolean enablePolicyDeployment;
    private PolicyDeployer policyDeployer;
    private BlockCondition blockCondition;
    private boolean enableHeaderConditions = false;
    private boolean enableJwtConditions = false;
    private boolean enableQueryParamConditions =false;
    private String[] skipRedeployingPolicies = new String[]{};
    private Map<String, Long> defaultThrottleTierLimits = new HashMap<String, Long>();
    private TrafficManager trafficManager;
    public boolean isEnabledSubscriptionLevelSpikeArrest() {
        return enabledSubscriptionLevelSpikeArrest;
    }

    public void setEnabledSubscriptionLevelSpikeArrest(boolean enabledSubscriptionLevelSpikeArrest) {
        this.enabledSubscriptionLevelSpikeArrest = enabledSubscriptionLevelSpikeArrest;
    }

    public DataPublisherThreadPool getDataPublisherThreadPool() {
        return dataPublisherThreadPool;
    }

    public void setDataPublisherThreadPool(DataPublisherThreadPool dataPublisherThreadPool) {
        this.dataPublisherThreadPool = dataPublisherThreadPool;
    }

    public BlockCondition getBlockCondition() {
        return blockCondition;
    }

    public void setBlockCondition(BlockCondition blockCondition) {
        this.blockCondition = blockCondition;
    }

    public PolicyDeployer getPolicyDeployer() {
        return policyDeployer;
    }

    public void setPolicyDeployer(PolicyDeployer policyDeployer) {
        this.policyDeployer = policyDeployer;
    }

    public boolean isEnableUnlimitedTier() {
        return enableUnlimitedTier;
    }

    public void setEnableUnlimitedTier(boolean enableUnlimitedTier) {
        this.enableUnlimitedTier = enableUnlimitedTier;
    }

    public String getThrottleDataSourceName() {
        return throttleDataSourceName;
    }

    public void setThrottleDataSourceName(String throttleDataSourceName) {
        this.throttleDataSourceName = throttleDataSourceName;
    }

    public DataPublisher getDataPublisher() {
        return dataPublisher;
    }

    public void setDataPublisher(DataPublisher dataPublisher) {
        this.dataPublisher = dataPublisher;
    }

    public GlobalEngineWSConnection getGlobalEngineWSConnection() {
        return globalEngineWSConnection;
    }

    public void setGlobalEngineWSConnection(GlobalEngineWSConnection globalEngineWSConnection) {
        this.globalEngineWSConnection = globalEngineWSConnection;
    }

    public DataPublisherPool getDataPublisherPool() {
        return dataPublisherPool;
    }

    public void setDataPublisherPool(DataPublisherPool dataPublisherPool) {
        this.dataPublisherPool = dataPublisherPool;
    }

    public JMSConnectionProperties getJmsConnectionProperties() {
        return jmsConnectionProperties;
    }


    public void setJmsConnectionProperties(JMSConnectionProperties jmsConnectionProperties) {
        this.jmsConnectionProperties = jmsConnectionProperties;
    }

    public boolean isEnableHeaderConditions() {
        return enableHeaderConditions;
    }

    public void setEnableHeaderConditions(boolean enableHeaderConditions) {
        this.enableHeaderConditions = enableHeaderConditions;
    }

    public boolean isEnableJwtConditions() {
        return enableJwtConditions;
    }

    public void setEnableJwtConditions(boolean enableJwtConditions) {
        this.enableJwtConditions = enableJwtConditions;
    }

    public boolean isEnableQueryParamConditions() {
        return enableQueryParamConditions;
    }

    public void setEnableQueryParamConditions(boolean enableQueryParamConditions) {
        this.enableQueryParamConditions = enableQueryParamConditions;
    }

    public void setTrafficManager(TrafficManager trafficManager) {
        this.trafficManager = trafficManager;
    }

    public TrafficManager getTrafficManager() {
        return trafficManager;
    }

    public Map<String, Long> getDefaultThrottleTierLimits() {
        return defaultThrottleTierLimits;
    }

    public void setDefaultThrottleTierLimits(Map<String, Long> defaultThrottleTierLimits) {
        this.defaultThrottleTierLimits = defaultThrottleTierLimits;
    }

    public static class DataPublisher {
        private String type = "Binary";
        private String receiverUrlGroup = "tcp://localhost:9611";
        private String authUrlGroup = "ssl://localhost:9711";
        private String username = "admin";
        private String password = "admin";
        private boolean enabled = false;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getReceiverUrlGroup() {
            return receiverUrlGroup;
        }

        public void setReceiverUrlGroup(String receiverUrlGroup) {
            this.receiverUrlGroup = receiverUrlGroup;
        }

        public String getAuthUrlGroup() {
            return authUrlGroup;
        }

        public void setAuthUrlGroup(String authUrlGroup) {
            this.authUrlGroup = authUrlGroup;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public static class TrafficManager {
        private String type = "Binary";
        private String receiverUrlGroup = "tcp://localhost:9611";
        private String authUrlGroup = "ssl://localhost:9711";
        private String username = "admin";
        private String password = "admin";

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getReceiverUrlGroup() {
            return receiverUrlGroup;
        }

        public void setReceiverUrlGroup(String receiverUrlGroup) {
            this.receiverUrlGroup = receiverUrlGroup;
        }

        public String getAuthUrlGroup() {
            return authUrlGroup;
        }

        public void setAuthUrlGroup(String authUrlGroup) {
            this.authUrlGroup = authUrlGroup;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }


    public static class DataPublisherPool {
        private int maxIdle = 1000;
        private int initIdleCapacity = 200;

        public int getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getInitIdleCapacity() {
            return initIdleCapacity;
        }

        public void setInitIdleCapacity(int initIdleCapacity) {
            this.initIdleCapacity = initIdleCapacity;
        }
    }

    public static class GlobalEngineWSConnection {
        private String dataSource;
        private boolean enabled = false;
        private String serviceUrl;
        private String username;
        private String password;
        private long initialDelay = 60000;

        public long getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
        }

        public String getDataSource() {
            return dataSource;
        }

        public void setDataSource(String dataSource) {
            this.dataSource = dataSource;
        }

        public String getServiceUrl() {
            return serviceUrl;
        }

        public void setServiceUrl(String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class JMSConnectionProperties {
        private boolean enabled = false;
        private String serviceUrl;
        private String username;
        private String password;
        private long initialDelay = 60000;
        private Properties jmsConnectionProperties;
        private JMSTaskManagerProperties jmsTaskManagerProperties;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
        }

        public String getServiceUrl() {
            return serviceUrl;
        }

        public void setServiceUrl(String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Properties getJmsConnectionProperties() {
            return jmsConnectionProperties;
        }

        public void setJmsConnectionProperties(Properties jmsConnectionProperties) {
            this.jmsConnectionProperties = jmsConnectionProperties;
        }

        public JMSTaskManagerProperties getJmsTaskManagerProperties() {
            return jmsTaskManagerProperties;
        }

        public void setJmsTaskManagerProperties(JMSTaskManagerProperties jmsTaskManagerProperties) {
            this.jmsTaskManagerProperties = jmsTaskManagerProperties;
        }

        public static class JMSTaskManagerProperties {
            private int minThreadPoolSize = 20;
            private int maxThreadPoolSize = 100;
            private int keepAliveTimeInMillis = 1000;
            private int jobQueueSize = 10;

            public int getMinThreadPoolSize() {
                return minThreadPoolSize;
            }

            public void setMinThreadPoolSize(int minThreadPoolSize) {
                this.minThreadPoolSize = minThreadPoolSize;
            }

            public int getMaxThreadPoolSize() {
                return maxThreadPoolSize;
            }

            public void setMaxThreadPoolSize(int maxThreadPoolSize) {
                this.maxThreadPoolSize = maxThreadPoolSize;
            }

            public int getKeepAliveTimeInMillis() {
                return keepAliveTimeInMillis;
            }

            public void setKeepAliveTimeInMillis(int keepAliveTimeInMillis) {
                this.keepAliveTimeInMillis = keepAliveTimeInMillis;
            }

            public int getJobQueueSize() {
                return jobQueueSize;
            }

            public void setJobQueueSize(int jobQueueSize) {
                this.jobQueueSize = jobQueueSize;
            }
        }
    }

    public static class PolicyDeployer {
        private boolean enabled = false;
        private String serviceUrl;
        private String username;
        private String password;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getServiceUrl() {
            return serviceUrl;
        }

        public void setServiceUrl(String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class BlockCondition {
        private boolean enabled;
        private String dataSource;
        private String serviceUrl;
        private String username;
        private String password;
        private int corePoolSize = 1;
        // by Default 1 mins
        private long initDelay = 60000;
        // by default per 5 min
        private long period = 300000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getPeriod() {
            return period;
        }

        public void setPeriod(long period) {
            this.period = period;
        }

        public long getInitDelay() {
            return initDelay;
        }

        public void setInitDelay(long initDelay) {
            this.initDelay = initDelay;
        }

        public String getDataSource() {
            return dataSource;
        }

        public void setDataSource(String dataSource) {
            this.dataSource = dataSource;
        }

        public String getServiceUrl() {
            return serviceUrl;
        }

        public void setServiceUrl(String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }
    }

    public static class DataPublisherThreadPool {
        private int corePoolSize = 200;
        private int maximumPoolSize = 500;
        private long keepAliveTime = 100;

        public int getCorePoolSize() {

            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public long getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }
    }

    public String[] getSkipRedeployingPolicies() {
        return skipRedeployingPolicies;
    }

    public void setSkipRedeployingPolicies(String[] skipRedeployingPolicies) {
        this.skipRedeployingPolicies = skipRedeployingPolicies;
    }
}

