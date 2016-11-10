/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.wso2.carbon.apimgt.lifecycle.manager.core.ManagedLifecycle;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;

import java.util.Date;
import java.util.List;

/**
 * Representation of an API object. Only immutable instances of this class can be created via the provided inner static
 * {@code APIBuilder} class which implements the builder pattern as outlined in "Effective Java 2nd Edition
 * by Joshua Bloch(Item 2)"
 */

public final class API {
    private API(APIBuilder builder) {
        id = builder.id;
        provider = builder.provider;
        name = builder.name;
        version = builder.version;
        context = builder.context;
        description = builder.description;
        lifeCycleStatus = builder.lifeCycleStatus;
        lifecycleInstanceId = builder.lifecycleInstanceId;
        apiDefinition = builder.apiDefinition;
        wsdlUri = builder.wsdlUri;
        isResponseCachingEnabled = builder.isResponseCachingEnabled;
        cacheTimeout = builder.cacheTimeout;
        isDefaultVersion = builder.isDefaultVersion;
        apiPolicy = builder.apiPolicy;
        transport = builder.transport;
        tags = builder.tags;
        policies = builder.policies;
        visibility = builder.visibility;
        visibleRoles = builder.visibleRoles;
        endpoints = builder.endpoints;
        gatewayEnvironments = builder.gatewayEnvironments;
        businessInformation = builder.businessInformation;
        corsConfiguration = builder.corsConfiguration;
        createdTime = builder.createdTime;
        createdBy = builder.createdBy;
        lastUpdatedTime = builder.lastUpdatedTime;
    }

    public String getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getContext() {
        return context;
    }

    public String getDescription() {
        return description;
    }

    public String getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public String getLifecycleInstanceId() {
        return lifecycleInstanceId;
    }

    public String getApiDefinition() {
        return apiDefinition;
    }

    public String getWsdlUri() {
        return wsdlUri;
    }

    public boolean isResponseCachingEnabled() {
        return isResponseCachingEnabled;
    }

    public int getCacheTimeout() {
        return cacheTimeout;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public String getApiPolicy() {
        return apiPolicy;
    }

    public List<String> getTransport() {
        return transport;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getPolicies() {
        return policies;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public List<String> getVisibleRoles() {
        return visibleRoles;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public List<Environment> getGatewayEnvironments() {
        return gatewayEnvironments;
    }

    public BusinessInformation getBusinessInformation() {
        return businessInformation;
    }

    public CorsConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }

    public Date getCreatedTime() {
        return new Date(createdTime.getTime());
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getLastUpdatedTime() {
        return new Date(lastUpdatedTime.getTime());
    }

    /**
     * Visibility options
     */
    public enum Visibility {
        PUBLIC,  PRIVATE,  RESTRICTED,  CONTROLLED,
    }

    private final String id;
    private final String provider;
    private final String name;
    private final String version;
    private final String context;
    private final String description;
    private final String lifeCycleStatus;
    private final String lifecycleInstanceId;
    private final String apiDefinition;
    private final String wsdlUri;
    private final boolean isResponseCachingEnabled;
    private final int cacheTimeout;
    private final boolean isDefaultVersion;
    private final String apiPolicy;
    private final List<String> transport;
    private final List<String> tags;
    private final List<String> policies;
    private final Visibility visibility;
    private final List<String> visibleRoles;
    private final List<Endpoint> endpoints;
    private final List<Environment> gatewayEnvironments;
    private final BusinessInformation businessInformation;
    private final CorsConfiguration corsConfiguration;
    private final Date createdTime;
    private final String createdBy;
    private final Date lastUpdatedTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        API that = (API) o;
        return (name.equals(that.name) && provider.equals(that.provider) && version.equals(that.version));
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + provider.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    /**
     * {@code API} builder static inner class.
     */
    @SuppressFBWarnings("CD_CIRCULAR_DEPENDENCY")
    public static final class APIBuilder implements ManagedLifecycle{
        private String id;
        private String provider;
        private String name;

        public String getId() {
            return id;
        }

        public String getProvider() {
            return provider;
        }

        public String getVersion() {
            return version;
        }

        public String getContext() {
            return context;
        }

        public String getDescription() {
            return description;
        }

        public String getLifeCycleStatus() {
            return lifeCycleStatus;
        }

        public String getLifecycleInstanceId() {
            return lifecycleInstanceId;
        }

        public String getApiDefinition() {
            return apiDefinition;
        }

        public String getWsdlUri() {
            return wsdlUri;
        }

        public boolean isResponseCachingEnabled() {
            return isResponseCachingEnabled;
        }

        public int getCacheTimeout() {
            return cacheTimeout;
        }

        public boolean isDefaultVersion() {
            return isDefaultVersion;
        }

        public String getApiPolicy() {
            return apiPolicy;
        }

        public List<String> getTransport() {
            return transport;
        }

        public List<String> getTags() {
            return tags;
        }

        public List<String> getPolicies() {
            return policies;
        }

        public Visibility getVisibility() {
            return visibility;
        }

        public List<String> getVisibleRoles() {
            return visibleRoles;
        }

        public List<Endpoint> getEndpoints() {
            return endpoints;
        }

        public List<Environment> getGatewayEnvironments() {
            return gatewayEnvironments;
        }

        public BusinessInformation getBusinessInformation() {
            return businessInformation;
        }

        private String version;
        private String context;
        private String description;
        private String lifeCycleStatus;
        private String lifecycleInstanceId;
        private String apiDefinition;
        private String wsdlUri;
        private boolean isResponseCachingEnabled;
        private int cacheTimeout;
        private boolean isDefaultVersion;
        private String apiPolicy;
        private List<String> transport;
        private List<String> tags;
        private List<String> policies;
        private Visibility visibility;
        private List<String> visibleRoles;
        private List<Endpoint> endpoints;
        private List<Environment> gatewayEnvironments;
        private BusinessInformation businessInformation;
        private CorsConfiguration corsConfiguration;
        private Date createdTime;
        private String createdBy;
        private Date lastUpdatedTime;

        public APIBuilder(String provider, String name, String version) {
            this.provider = provider;
            this.name = name;
            this.version = version;
        }
        public APIBuilder(API copy) {
            this.id = copy.id;
            this.provider = copy.provider;
            this.name = copy.name;
            this.version = copy.version;
            this.context = copy.context;
            this.description = copy.description;
            this.lifeCycleStatus = copy.lifeCycleStatus;
            this.lifecycleInstanceId = copy.lifecycleInstanceId;
            this.apiDefinition = copy.apiDefinition;
            this.wsdlUri = copy.wsdlUri;
            this.isResponseCachingEnabled = copy.isResponseCachingEnabled;
            this.cacheTimeout = copy.cacheTimeout;
            this.isDefaultVersion = copy.isDefaultVersion;
            this.apiPolicy = copy.apiPolicy;
            this.transport = copy.transport;
            this.tags = copy.tags;
            this.policies = copy.policies;
            this.visibility = copy.visibility;
            this.visibleRoles = copy.visibleRoles;
            this.endpoints = copy.endpoints;
            this.gatewayEnvironments = copy.gatewayEnvironments;
            this.businessInformation = copy.businessInformation;
            this.corsConfiguration = copy.corsConfiguration;
            this.createdTime = copy.createdTime;
            this.createdBy = copy.createdBy;
            this.lastUpdatedTime = copy.lastUpdatedTime;
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
         * Sets the {@code provider} and returns a reference to this APIBuilder so that the methods can be chained together.
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
         * Sets the {@code version} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param version the {@code version} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the {@code context} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param context the {@code context} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder context(String context) {
            this.context = context;
            return this;
        }

        /**
         * Sets the {@code description} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param description the {@code description} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the {@code lifeCycleStatus} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param lifeCycleStatus the {@code lifeCycleStatus} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder lifeCycleStatus(String lifeCycleStatus) {
            this.lifeCycleStatus = lifeCycleStatus;
            return this;
        }

        /**
         * Sets the {@code lifeCycleInstanceID} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param lifecycleInstanceId the {@code lifeCycleInstanceID} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder lifecycleInstanceId(String lifecycleInstanceId) {
            this.lifecycleInstanceId = lifecycleInstanceId;
            return this;
        }

        /**
         * Sets the {@code apiDefinition} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param apiDefinition the {@code apiDefinition} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder apiDefinition(String apiDefinition) {
            this.apiDefinition = apiDefinition;
            return this;
        }

        /**
         * Sets the {@code wsdlUri} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param wsdlUri the {@code wsdlUri} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder wsdlUri(String wsdlUri) {
            this.wsdlUri = wsdlUri;
            return this;
        }

        /**
         * Sets the {@code isResponseCachingEnabled} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param isResponseCachingEnabled the {@code isResponseCachingEnabled} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder isResponseCachingEnabled(boolean isResponseCachingEnabled) {
            this.isResponseCachingEnabled = isResponseCachingEnabled;
            return this;
        }

        /**
         * Sets the {@code cacheTimeout} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param cacheTimeout the {@code cacheTimeout} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder cacheTimeout(int cacheTimeout) {
            this.cacheTimeout = cacheTimeout;
            return this;
        }

        /**
         * Sets the {@code isDefaultVersion} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param isDefaultVersion the {@code isDefaultVersion} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder isDefaultVersion(boolean isDefaultVersion) {
            this.isDefaultVersion = isDefaultVersion;
            return this;
        }

        /**
         * Sets the {@code apiPolicy} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param apiPolicy the {@code apiPolicy} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder apiPolicy(String apiPolicy) {
            this.apiPolicy = apiPolicy;
            return this;
        }

        /**
         * Sets the {@code transport} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param transport the {@code transport} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder transport(List<String> transport) {
            this.transport = transport;
            return this;
        }

        /**
         * Sets the {@code tags} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param tags the {@code tags} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        /**
         * Sets the {@code policies} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param policies the {@code policies} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder policies(List<String> policies) {
            this.policies = policies;
            return this;
        }

        /**
         * Sets the {@code visibility} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param visibility the {@code visibility} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder visibility(Visibility visibility) {
            this.visibility = visibility;
            return this;
        }

        /**
         * Sets the {@code visibleRoles} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param visibleRoles the {@code visibleRoles} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder visibleRoles(List<String> visibleRoles) {
            this.visibleRoles = visibleRoles;
            return this;
        }

        /**
         * Sets the {@code endpoints} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param endpoints the {@code endpoints} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder endpoints(List<Endpoint> endpoints) {
            this.endpoints = endpoints;
            return this;
        }

        /**
         * Sets the {@code gatewayEnvironments} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param gatewayEnvironments the {@code gatewayEnvironments} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder gatewayEnvironments(List<Environment> gatewayEnvironments) {
            this.gatewayEnvironments = gatewayEnvironments;
            return this;
        }

        /**
         * Sets the {@code businessInformation} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param businessInformation the {@code businessInformation} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder businessInformation(BusinessInformation businessInformation) {
            this.businessInformation = businessInformation;
            return this;
        }

        /**
         * Sets the {@code corsConfiguration} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param corsConfiguration the {@code corsConfiguration} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder corsConfiguration(CorsConfiguration corsConfiguration) {
            this.corsConfiguration = corsConfiguration;
            return this;
        }

        /**
         * Sets the {@code createdTime} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param createdTime the {@code createdTime} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder createdTime(Date createdTime) {
            this.createdTime = new Date(createdTime.getTime());
            return this;
        }

        /**
         * Sets the {@code createdBy} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param createdBy the {@code createdBy} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * Sets the {@code lastUpdatedTime} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param lastUpdatedTime the {@code lastUpdatedTime} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder lastUpdatedTime(Date lastUpdatedTime) {
            this.lastUpdatedTime = new Date(lastUpdatedTime.getTime());
            return this;
        }

        /**
         * Returns a {@code API} built from the parameters previously set.
         *
         * @return a {@code API} built with parameters of this {@code API.APIBuilder}
         */
        public API build() {
            return new API(this);
        }

        /**
         * This method should be implemented to create association between object which implementing Managed
         * Lifecycle and
         * the Lifecycle framework. This method should implement logic which saves the returned uuid in the external
         * party (API, APP etc). So both parties will have lifecycle uuid saved in their side which will cater the
         * purpose of mapping.
         *
         * @param lifecycleState Lifecycle state object.
         */
        @Override
        public void associateLifecycle(LifecycleState lifecycleState) throws LifecycleException {
            lifecycleInstanceId = lifecycleState.getLifecycleId();
            lifeCycleStatus = lifecycleState.getState();
        }

        /**
         * @param lcName Name of the lifecycle to be removed.
         *               This method should be implemented to remove the lifecycle data from the object which
         *               implements this interface.
         *               Persisted lifecycle state id (say stored in database) should be removed by implementing this
         *               method.
         */
        @Override
        public void dissociateLifecycle(String lcName) throws LifecycleException {
        }

        /**
         * @param lifecycleState Lifecycle state object.
         *                       This method should be implemented to update the lifecycle state after state change
         *                       operation and check list
         *                       item operation
         */
        @Override
        public void setLifecycleStateInfo(LifecycleState lifecycleState) throws LifecycleException {
            lifeCycleStatus = lifecycleState.getState();
        }

        public String getName() {
            return name;
        }
    }

}
