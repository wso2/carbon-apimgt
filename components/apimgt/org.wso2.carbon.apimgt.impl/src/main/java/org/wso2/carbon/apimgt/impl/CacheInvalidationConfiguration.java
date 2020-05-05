/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CacheInvalidationConfiguration {

    private boolean enabled = false;
    private String domain = "default";
    private String stream ;
    private String receiverUrlGroup = "tcp://localhost:9611";
    private String authUrlGroup = "ssl://localhost:9711";
    private String username = "admin";
    private String password = "admin";
    private String cacheInValidationTopic = "globalCacheInvalidation";
    private List<String> excludedCaches = new ArrayList<>();
    private Properties jmsConnectionParameters = new Properties();

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public String getDomain() {

        return domain;
    }

    public void setDomain(String domain) {

        this.domain = domain;
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

    public String getCacheInValidationTopic() {

        return cacheInValidationTopic;
    }

    public void setCacheInValidationTopic(String cacheInValidationTopic) {

        this.cacheInValidationTopic = cacheInValidationTopic;
    }

    public List<String> getExcludedCaches() {

        return excludedCaches;
    }

    public void addExcludedCaches(String excludedCache) {
        this.excludedCaches.add(excludedCache);
    }

    public Properties getJmsConnectionParameters() {

        return jmsConnectionParameters;
    }

    public void setJmsConnectionParameters(Properties jmsConnectionParameters) {

        this.jmsConnectionParameters = jmsConnectionParameters;
    }

    public String getStream() {

        return stream;
    }

    public void setStream(String stream) {

        this.stream = stream;
    }
}
