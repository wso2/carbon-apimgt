/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.models;

import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Representation of a Composite API object. Only immutable instances of this class can be created via the provided
 * inner static {@code APIBuilder} class which implements the builder pattern as outlined in "Effective Java 2nd Edition
 * by Joshua Bloch(Item 2)"
 */

public class CompositeAPI {

    private final String id;
    private final String provider;
    private final String name;
    private final String version;
    private final String context;
    private final String description;
    private final String lifeCycleStatus;
    private final String lifecycleInstanceId;
    private final String gatewayConfig;
    private final boolean isResponseCachingEnabled;
    private final int cacheTimeout;
    private final boolean isDefaultVersion;
    private final Set<String> transport;
    private final Set<String> labels;
    private final BusinessInformation businessInformation;
    private final CorsConfiguration corsConfiguration;
    private final LocalDateTime createdTime;
    private final String createdBy;
    private final String updatedBy;
    private final LocalDateTime lastUpdatedTime;
    private final LifecycleState lifecycleState;
    private final Map<String, UriTemplate> uriTemplates;
    private final String copiedFromApiId;
    private final String apiDefinition;
    private final HashMap permissionMap;
    private final String apiPermission;
    private final String workflowStatus;

    private CompositeAPI(APIBuilder builder) {
        id = builder.id;
        provider = builder.provider;
        name = builder.name;
        version = builder.version;
        context = builder.context;
        description = builder.description;
        lifeCycleStatus = builder.lifeCycleStatus;
        lifecycleInstanceId = builder.lifecycleInstanceId;
        gatewayConfig = builder.gatewayConfig;
        isResponseCachingEnabled = builder.isResponseCachingEnabled;
        cacheTimeout = builder.cacheTimeout;
        isDefaultVersion = builder.isDefaultVersion;
        transport = builder.transport;
        labels = builder.labels;
        businessInformation = builder.businessInformation;
        corsConfiguration = builder.corsConfiguration;
        createdTime = builder.createdTime;
        createdBy = builder.createdBy;
        updatedBy = builder.updatedBy;
        lastUpdatedTime = builder.lastUpdatedTime;
        lifecycleState = builder.lifecycleState;
        uriTemplates = builder.uriTemplates;
        copiedFromApiId = builder.copiedFromApiId;
        apiDefinition = builder.apiDefinition;
        permissionMap = builder.permissionMap;
        apiPermission = builder.apiPermission;
        workflowStatus = builder.workflowStatus;
    }

    /**
     * {@code CompositeAPI} builder static inner class.
     */
    public static final class APIBuilder {
        private String id;
        private String provider;
        private String name;
        private String version;
        private String context;
        private String description;
        private String lifeCycleStatus;
        private String lifecycleInstanceId;
        private String gatewayConfig;
        private boolean isResponseCachingEnabled;
        private int cacheTimeout;
        private boolean isDefaultVersion;
        private Set<String> transport;
        private Set<String> labels;
        private BusinessInformation businessInformation;
        private CorsConfiguration corsConfiguration;
        private LocalDateTime createdTime;
        private String createdBy;
        private String updatedBy;
        private LocalDateTime lastUpdatedTime;
        private LifecycleState lifecycleState;
        private Map<String, UriTemplate> uriTemplates;
        private String copiedFromApiId;
        private String apiDefinition;
        private HashMap permissionMap;
        private String apiPermission;
        private String workflowStatus;

        public APIBuilder(String provider, String name, String version) {
            this.provider = provider;
            this.name = name;
            this.version = version;
        }

        public APIBuilder(CompositeAPI copy) {
            this.id = copy.id;
            this.provider = copy.provider;
            this.name = copy.name;
            this.version = copy.version;
            this.context = copy.context;
            this.description = copy.description;
            this.lifeCycleStatus = copy.lifeCycleStatus;
            this.lifecycleInstanceId = copy.lifecycleInstanceId;
            this.gatewayConfig = copy.gatewayConfig;
            this.isResponseCachingEnabled = copy.isResponseCachingEnabled;
            this.cacheTimeout = copy.cacheTimeout;
            this.isDefaultVersion = copy.isDefaultVersion;
            this.transport = copy.transport;
            this.labels = copy.labels;
            this.businessInformation = copy.businessInformation;
            this.corsConfiguration = copy.corsConfiguration;
            this.createdTime = copy.createdTime;
            this.createdBy = copy.createdBy;
            this.updatedBy = copy.updatedBy;
            this.lastUpdatedTime = copy.lastUpdatedTime;
            this.lifecycleState = copy.lifecycleState;
            this.uriTemplates = copy.uriTemplates;
            this.copiedFromApiId = copy.copiedFromApiId;
            this.apiDefinition = copy.apiDefinition;
            this.permissionMap = copy.permissionMap;
            this.apiPermission = copy.apiPermission;
            this.workflowStatus = copy.workflowStatus;
        }

        /**
         * Sets the {@code id} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param id the {@code id} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the {@code provider} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param provider the {@code provider} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder provider(String provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Sets the {@code name} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param name the {@code name} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the {@code version} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param version the {@code version} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the {@code context} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param context the {@code context} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder context(String context) {
            this.context = context;
            return this;
        }

        /**
         * Sets the {@code description} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param description the {@code description} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the {@code lifeCycleStatus} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param lifeCycleStatus the {@code lifeCycleStatus} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder lifeCycleStatus(String lifeCycleStatus) {
            this.lifeCycleStatus = lifeCycleStatus;
            return this;
        }

        /**
         * Sets the {@code lifecycleInstanceId} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param lifecycleInstanceId the {@code lifecycleInstanceId} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder lifecycleInstanceId(String lifecycleInstanceId) {
            this.lifecycleInstanceId = lifecycleInstanceId;
            return this;
        }

        /**
         * Sets the {@code gatewayConfig} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param gatewayConfig the {@code gatewayConfig} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder gatewayConfig(String gatewayConfig) {
            this.gatewayConfig = gatewayConfig;
            return this;
        }

        /**
         * Sets the {@code isResponseCachingEnabled} and returns a reference to this APIBuilder so that the methods
         * can be chained together.
         *
         * @param isResponseCachingEnabled the {@code isResponseCachingEnabled} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder isResponseCachingEnabled(boolean isResponseCachingEnabled) {
            this.isResponseCachingEnabled = isResponseCachingEnabled;
            return this;
        }

        /**
         * Sets the {@code cacheTimeout} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param cacheTimeout the {@code cacheTimeout} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder cacheTimeout(int cacheTimeout) {
            this.cacheTimeout = cacheTimeout;
            return this;
        }

        /**
         * Sets the {@code isDefaultVersion} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param isDefaultVersion the {@code isDefaultVersion} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder isDefaultVersion(boolean isDefaultVersion) {
            this.isDefaultVersion = isDefaultVersion;
            return this;
        }

        /**
         * Sets the {@code transport} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param transport the {@code transport} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder transport(Set<String> transport) {
            this.transport = transport;
            return this;
        }

        /**
         * Sets the {@code labels} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param labels the {@code labels} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder labels(Set<String> labels) {
            this.labels = labels;
            return this;
        }

        /**
         * Sets the {@code businessInformation} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param businessInformation the {@code businessInformation} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder businessInformation(BusinessInformation businessInformation) {
            this.businessInformation = businessInformation;
            return this;
        }

        /**
         * Sets the {@code corsConfiguration} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param corsConfiguration the {@code corsConfiguration} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder corsConfiguration(CorsConfiguration corsConfiguration) {
            this.corsConfiguration = corsConfiguration;
            return this;
        }

        /**
         * Sets the {@code createdTime} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param createdTime the {@code createdTime} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        /**
         * Sets the {@code createdBy} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param createdBy the {@code createdBy} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * Sets the {@code updatedBy} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param updatedBy the {@code updatedBy} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        /**
         * Sets the {@code lastUpdatedTime} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param lastUpdatedTime the {@code lastUpdatedTime} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder lastUpdatedTime(LocalDateTime lastUpdatedTime) {
            this.lastUpdatedTime = lastUpdatedTime;
            return this;
        }

        /**
         * Sets the {@code lifecycleState} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param lifecycleState the {@code lifecycleState} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder lifecycleState(LifecycleState lifecycleState) {
            this.lifecycleState = lifecycleState;
            return this;
        }

        /**
         * Sets the {@code uriTemplates} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param uriTemplates the {@code uriTemplates} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder uriTemplates(Map<String, UriTemplate> uriTemplates) {
            this.uriTemplates = uriTemplates;
            return this;
        }

        /**
         * Sets the {@code copiedFromApiId} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param copiedFromApiId the {@code copiedFromApiId} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder copiedFromApiId(String copiedFromApiId) {
            this.copiedFromApiId = copiedFromApiId;
            return this;
        }

        /**
         * Sets the {@code apiDefinition} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param apiDefinition the {@code apiDefinition} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder apiDefinition(String apiDefinition) {
            this.apiDefinition = apiDefinition;
            return this;
        }

        /**
         * Sets the {@code permissionMap} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param permissionMap the {@code permissionMap} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder permissionMap(HashMap permissionMap) {
            this.permissionMap = permissionMap;
            return this;
        }

        /**
         * Sets the {@code apiPermission} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param apiPermission the {@code apiPermission} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder apiPermission(String apiPermission) {
            this.apiPermission = apiPermission;
            return this;
        }

        /**
         * Sets the {@code workflowStatus} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param workflowStatus the {@code workflowStatus} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder workflowStatus(String workflowStatus) {
            this.workflowStatus = workflowStatus;
            return this;
        }

        /**
         * Returns a {@code CompositeAPI} built from the parameters previously set.
         *
         * @return a {@code CompositeAPI} built with parameters of this {@code CompositeAPI.APIBuilder}
         */
        public CompositeAPI build() {
            return new CompositeAPI(this);
        }
    }
}
