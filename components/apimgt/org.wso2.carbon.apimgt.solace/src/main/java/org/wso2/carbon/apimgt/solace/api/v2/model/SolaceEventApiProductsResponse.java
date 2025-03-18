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
 * Model class for Solace Event API Products Response.
 */
public class SolaceEventApiProductsResponse {

    @SerializedName("data")
    private List<EventApiProduct> data;

    public List<EventApiProduct> getData() {
        return data;
    }

    public void setData(List<EventApiProduct> data) {
        this.data = data;
    }

    public static class EventApiProduct {
        @SerializedName("createdTime")
        private long createdTime;

        @SerializedName("updatedTime")
        private long updatedTime;

        @SerializedName("createdBy")
        private String createdBy;

        @SerializedName("changedBy")
        private String changedBy;

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("applicationDomainId")
        private String applicationDomainId;

        @SerializedName("applicationDomainName")
        private String applicationDomainName;

        @SerializedName("approvalType")
        private String approvalType;

        @SerializedName("brokerType")
        private String brokerType;

        @SerializedName("description")
        private String description;

        @SerializedName("version")
        private String version;

        @SerializedName("displayName")
        private String displayName;

        @SerializedName("state")
        private String state;

        @SerializedName("plans")
        private List<Plan> plans;

        @SerializedName("solaceMessagingServices")
        private List<SolaceMessagingService> solaceMessagingServices;

        @SerializedName("customAttributes")
        private List<Object> customAttributes;

        @SerializedName("attributes")
        private List<Object> attributes;

        @SerializedName("apis")
        private List<Api> apis;

        @SerializedName("apiParameters")
        private List<Object> apiParameters;

        public long getCreatedTime() {
            return createdTime;
        }

        public long getUpdatedTime() {
            return updatedTime;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public String getChangedBy() {
            return changedBy;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getApplicationDomainId() {
            return applicationDomainId;
        }

        public String getApplicationDomainName() {
            return applicationDomainName;
        }

        public String getApprovalType() {
            return approvalType;
        }

        public String getBrokerType() {
            return brokerType;
        }

        public String getDescription() {
            return description;
        }

        public String getVersion() {
            return version;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getState() {
            return state;
        }

        public List<Plan> getPlans() {
            return plans;
        }

        public List<SolaceMessagingService> getSolaceMessagingServices() {
            return solaceMessagingServices;
        }

        public List<Object> getCustomAttributes() {
            return customAttributes;
        }

        public List<Object> getAttributes() {
            return attributes;
        }

        public List<Api> getApis() {
            return apis;
        }

        public List<Object> getApiParameters() {
            return apiParameters;
        }

        public static class Plan {
            @SerializedName("id")
            private String id;

            @SerializedName("name")
            private String name;

            @SerializedName("solaceClassOfServicePolicy")
            private SolaceClassOfServicePolicy solaceClassOfServicePolicy;

            public String getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public SolaceClassOfServicePolicy getSolaceClassOfServicePolicy() {
                return solaceClassOfServicePolicy;
            }

            public static class SolaceClassOfServicePolicy {
                @SerializedName("id")
                private String id;

                @SerializedName("messageDeliveryMode")
                private String messageDeliveryMode;

                public String getId() {
                    return id;
                }

                public String getMessageDeliveryMode() {
                    return messageDeliveryMode;
                }
            }
        }

        public static class SolaceMessagingService {
            @SerializedName("id")
            private String id;

            @SerializedName("messagingServiceId")
            private String messagingServiceId;

            @SerializedName("messagingServiceName")
            private String messagingServiceName;

            @SerializedName("supportedProtocols")
            private List<String> supportedProtocols;

            @SerializedName("environmentName")
            private String environmentName;

            @SerializedName("eventMeshName")
            private String eventMeshName;

            public String getId() {
                return id;
            }

            public String getMessagingServiceId() {
                return messagingServiceId;
            }

            public String getMessagingServiceName() {
                return messagingServiceName;
            }

            public List<String> getSupportedProtocols() {
                return supportedProtocols;
            }

            public String getEnvironmentName() {
                return environmentName;
            }

            public String getEventMeshName() {
                return eventMeshName;
            }
        }

        public static class Api {
            @SerializedName("id")
            private String id;

            @SerializedName("version")
            private String version;

            @SerializedName("description")
            private String description;

            @SerializedName("displayName")
            private String displayName;

            @SerializedName("name")
            private String name;

            @SerializedName("customAttributes")
            private List<Object> customAttributes;

            public String getId() {
                return id;
            }

            public String getVersion() {
                return version;
            }

            public String getDescription() {
                return description;
            }

            public String getDisplayName() {
                return displayName;
            }

            public String getName() {
                return name;
            }

            public List<Object> getCustomAttributes() {
                return customAttributes;
            }
        }
    }
}
