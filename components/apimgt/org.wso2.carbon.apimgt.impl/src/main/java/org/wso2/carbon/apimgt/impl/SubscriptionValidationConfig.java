/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl;

public class SubscriptionValidationConfig {

    private boolean enabled;
    private String serviceURL;
    private String username;
    private String password;

    private SubscriptionValidationConfig() {

    }

    public String getServiceURL() {

        return serviceURL;
    }

    public String getUsername() {

        return username;
    }

    public String getPassword() {

        return password;
    }

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public static class Builder {

        private boolean enabled;
        private String serviceURL;
        private String username;
        private String password;

        public Builder serviceURL(String serviceURL) {

            this.serviceURL = serviceURL;
            return this;
        }

        public Builder username(String username) {

            this.username = username;
            return this;
        }

        public Builder password(String password) {

            this.password = password;
            return this;
        }

        public Builder(boolean enabled) {

            this.enabled = enabled;
        }

        public SubscriptionValidationConfig build() {

            SubscriptionValidationConfig subscriptionValidationConfig = new SubscriptionValidationConfig();
            subscriptionValidationConfig.serviceURL = serviceURL;
            subscriptionValidationConfig.username = username;
            subscriptionValidationConfig.password = password;
            subscriptionValidationConfig.enabled = enabled;
            return subscriptionValidationConfig;
        }
    }
}
