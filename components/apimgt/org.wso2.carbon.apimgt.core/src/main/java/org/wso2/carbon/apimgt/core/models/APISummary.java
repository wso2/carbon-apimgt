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


import org.wso2.carbon.apimgt.lifecycle.manager.core.ManagedLifecycle;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation an API object that contains a limited number of details. Only immutable instances of this class
 * can be created via the provided inner static {@code Builder} class which implements the builder pattern
 * as outlined in "Effective Java 2nd Edition by Joshua Bloch(Item 2)"
 */

public final class APISummary {
    private final String provider;
    private final String version;
    private final String description;
    private final String name;
    private final String context;
    private final String id;
    private final String status;
    private final String lifecycleInstanceId;

    private APISummary(Builder builder) {
        provider = builder.provider;
        version = builder.version;
        description = builder.description;
        name = builder.name;
        context = builder.context;
        id = builder.id;
        status = builder.status;
        lifecycleInstanceId = builder.lifecycleInstanceId;
    }

    public String getLifecycleInstanceId() {
        return lifecycleInstanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        APISummary that = (APISummary) o;
        return (name.equals(that.name) && provider.equals(that.provider) && version.equals(that.version));
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + provider.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    public String getProvider() {
        return provider;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getContext() {
        return context;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    /**
     * {@code APISummary} builder static inner class.
     */
    public static final class Builder implements ManagedLifecycle{
        private String provider;
        private String version;
        private String description;
        private String name;
        private String context;
        private String id;
        private String status;
        private String lifecycleInstanceId ;
        public Builder(String provider, String name, String version) {
            this.provider = provider;
            this.name = name;
            this.version = version;
        }

        /**
         * Sets the {@code description} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param description the {@code description} to set
         * @return a reference to this Builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the {@code context} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param context the {@code context} to set
         * @return a reference to this Builder
         */
        public Builder context(String context) {
            this.context = context;
            return this;
        }

        /**
         * Sets the {@code id} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param id the {@code id} to set
         * @return a reference to this Builder
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the {@code status} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param status the {@code status} to set
         * @return a reference to this Builder
         */
        public Builder status(String status) {
            this.status = status;
            return this;
        }

        /**
         * Returns a {@code APISummary} built from the parameters previously set.
         *
         * @return a {@code APISummary} built with parameters of this {@code APISummary.Builder}
         */
        public APISummary build() {
            return new APISummary(this);
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
            status = lifecycleState.getState();
        }

        public String getLifecycleInstanceId() {
            return lifecycleInstanceId;
        }
    }
}
