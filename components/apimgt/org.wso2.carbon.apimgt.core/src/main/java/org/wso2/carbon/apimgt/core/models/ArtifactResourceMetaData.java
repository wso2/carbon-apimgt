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

/**
 * Meta data of a generic resource associated with an artifact such as an API(For example: images, documents,
 * text). Only immutable instances of this class can be created via the provided inner static {@code Builder} class
 * which implements the builder pattern as outlined in "Effective Java 2nd Edition by Joshua Bloch(Item 2)"
 */

public final class ArtifactResourceMetaData {
    private final String id;
    private final String name;
    private final String description;
    private final ResourceCategory category;
    private final String dataType;
    private final ResourceVisibility visibility;


    private ArtifactResourceMetaData(Builder builder) {
        id = builder.id;
        name = builder.name;
        description = builder.description;
        category = builder.category;
        dataType = builder.dataType;
        visibility = builder.visibility;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ResourceCategory getCategory() {
        return category;
    }

    public String getDataType() {
        return dataType;
    }

    public ResourceVisibility getVisibility() {
        return visibility;
    }

    /**
     * {@code ArtifactResourceMetaData} builder static inner class.
     */
    public static final class Builder {
        private String id;
        private String name;
        private String description;
        private ResourceCategory category;
        private String dataType;
        private ResourceVisibility visibility;

        public Builder() {
        }

        public Builder(ArtifactResourceMetaData copy) {
            this.id = copy.id;
            this.name = copy.name;
            this.description = copy.description;
            this.category = copy.category;
            this.dataType = copy.dataType;
            this.visibility = copy.visibility;
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
         * Sets the {@code name} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param name the {@code name} to set
         * @return a reference to this Builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the {@code description} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param description the {@code description} to set
         * @return a reference to this Builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the {@code category} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param category the {@code category} to set
         * @return a reference to this Builder
         */
        public Builder category(ResourceCategory category) {
            this.category = category;
            return this;
        }

        /**
         * Sets the {@code dataType} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param dataType the {@code dataType} to set
         * @return a reference to this Builder
         */
        public Builder dataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        /**
         * Sets the {@code visibility} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param visibility the {@code visibility} to set
         * @return a reference to this Builder
         */
        public Builder visibility(ResourceVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        /**
         * Returns a {@code ArtifactResourceMetaData} built from the parameters previously set.
         *
         * @return a {@code ArtifactResourceMetaData} built with parameters
         * of this {@code ArtifactResourceMetaData.Builder}
         */
        public ArtifactResourceMetaData build() {
            return new ArtifactResourceMetaData(this);
        }
    }
}
