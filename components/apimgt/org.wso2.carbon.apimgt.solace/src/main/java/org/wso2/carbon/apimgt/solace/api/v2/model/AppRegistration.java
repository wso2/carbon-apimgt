/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.solace.api.v2.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model class for Solace Application Registration.
 */
public class AppRegistration {

    @SerializedName("registrationId")
    private String registrationId;

    @SerializedName("source")
    private String source;

    @SerializedName("name")
    private String name;

    @SerializedName("sourceOwner")
    private String sourceOwner;

    @SerializedName("applicationDomainId")
    private String applicationDomainId;

    @SerializedName("accessRequests")
    private List<AccessRequest> accessRequests;

    @SerializedName("credentials")
    private List<Credentials> credentials;

    @SerializedName("brokerType")
    private String brokerType;

    // Getters and Setters
    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceOwner() {
        return sourceOwner;
    }

    public void setSourceOwner(String sourceOwner) {
        this.sourceOwner = sourceOwner;
    }

    public String getApplicationDomainId() {
        return applicationDomainId;
    }

    public void setApplicationDomainId(String applicationDomainId) {
        this.applicationDomainId = applicationDomainId;
    }

    public List<AccessRequest> getAccessRequests() {
        return accessRequests;
    }

    public void setAccessRequests(List<AccessRequest> accessRequests) {
        this.accessRequests = accessRequests;
    }

    public List<Credentials> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<Credentials> credentials) {
        this.credentials = credentials;
    }

    public String getBrokerType() {
        return brokerType;
    }

    public void setBrokerType(String brokerType) {
        this.brokerType = brokerType;
    }

    public static class Credentials {
        @SerializedName("secret")
        private Secret secret;

        @SerializedName("issuedAt")
        private long issuedAt;

        @SerializedName("expiresAt")
        private long expiresAt;

        // Getters and Setters
        public Secret getSecret() {
            return secret;
        }

        public void setSecret(Secret secret) {
            this.secret = secret;
        }

        public long getIssuedAt() {
            return issuedAt;
        }

        public void setIssuedAt(long issuedAt) {
            this.issuedAt = issuedAt;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(long expiresAt) {
            this.expiresAt = expiresAt;
        }

        public static class Secret {
            @SerializedName("consumerKey")
            private String consumerKey;

            @SerializedName("consumerSecret")
            private String consumerSecret;

            // Getters and Setters
            public String getConsumerKey() {
                return consumerKey;
            }

            public void setConsumerKey(String consumerKey) {
                this.consumerKey = consumerKey;
            }

            public String getConsumerSecret() {
                return consumerSecret;
            }

            public void setConsumerSecret(String consumerSecret) {
                this.consumerSecret = consumerSecret;
            }
        }
    }

    public static class AccessRequest {
        @SerializedName("accessRequestId")
        private String accessRequestId;

        @SerializedName("eventApiProductId")
        private String eventApiProductId;

        @SerializedName("planId")
        private String planId;

        @SerializedName("registrationId")
        private String registrationId;

        @SerializedName("eventApiProductVersion")
        private String eventApiProductVersion;

        @SerializedName("state")
        private String state;

        @SerializedName("eventApiProductResourceInformation")
        private EventApiProductResourceInformation eventApiProductResourceInformation;

        // Getters and Setters
        public String getAccessRequestId() {
            return accessRequestId;
        }

        public void setAccessRequestId(String accessRequestId) {
            this.accessRequestId = accessRequestId;
        }

        public String getEventApiProductId() {
            return eventApiProductId;
        }

        public void setEventApiProductId(String eventApiProductId) {
            this.eventApiProductId = eventApiProductId;
        }

        public String getPlanId() {
            return planId;
        }

        public void setPlanId(String planId) {
            this.planId = planId;
        }

        public String getRegistrationId() {
            return registrationId;
        }

        public void setRegistrationId(String registrationId) {
            this.registrationId = registrationId;
        }

        public String getEventApiProductVersion() {
            return eventApiProductVersion;
        }

        public void setEventApiProductVersion(String eventApiProductVersion) {
            this.eventApiProductVersion = eventApiProductVersion;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public EventApiProductResourceInformation getEventApiProductResourceInformation() {
            return eventApiProductResourceInformation;
        }

        public void setEventApiProductResourceInformation(
                EventApiProductResourceInformation eventApiProductResourceInformation) {
            this.eventApiProductResourceInformation = eventApiProductResourceInformation;
        }

        public static class EventApiProductResourceInformation {
            @SerializedName("accessType")
            private String accessType;

            @SerializedName("maxMsgSpoolUsage")
            private int maxMsgSpoolUsage;

            @SerializedName("maxTtl")
            private int maxTtl;

            @SerializedName("name")
            private String name;

            // Getters and Setters
            public String getAccessType() {
                return accessType;
            }

            public void setAccessType(String accessType) {
                this.accessType = accessType;
            }

            public int getMaxMsgSpoolUsage() {
                return maxMsgSpoolUsage;
            }

            public void setMaxMsgSpoolUsage(int maxMsgSpoolUsage) {
                this.maxMsgSpoolUsage = maxMsgSpoolUsage;
            }

            public int getMaxTtl() {
                return maxTtl;
            }

            public void setMaxTtl(int maxTtl) {
                this.maxTtl = maxTtl;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
