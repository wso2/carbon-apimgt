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

import java.time.Instant;
import java.util.HashMap;

/**
 * Returns a specific document
 */

public final class DocumentInfo {

    private DocumentInfo(Builder builder) {
        sourceType = builder.sourceType;
        sourceURL = builder.sourceURL;
        otherType = builder.otherType;
        id = builder.id;
        summary = builder.summary;
        name = builder.name;
        type = builder.type;
        visibility = builder.visibility;
        fileName = builder.fileName;
        permission = builder.permission;
        permissionMap = builder.permissionMap;
        createdTime = builder.createdTime;
        lastUpdatedTime = builder.lastUpdatedTime;
        createdBy = builder.createdBy;
        updatedBy = builder.updatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DocumentInfo that = (DocumentInfo) o;

        return getId().equals(that.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public String getOtherType() {
        return otherType;
    }

    public String getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public String getName() {
        return name;
    }

    public DocType getType() {
        return type;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPermission() {
        return permission;
    }

    public HashMap getPermissionMap() {
        return permissionMap;

    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     *Document Types
     */
    public enum DocType {
        HOWTO("How To"),
        SAMPLES("Samples"),
        PUBLIC_FORUM("Public Forum"),
        SUPPORT_FORUM("Support Forum"),
        API_MESSAGE_FORMAT("API Message Format"),
        SWAGGER_DOC("Swagger API Definition"),
        OTHER("Other");

        private String type;

        DocType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    /**
     *Document Source Types
     */
    public enum SourceType {
        FILE("FILE"),
        INLINE("INLINE"),
        URL("URL"),
        OTHER("OTHER");

        private final String type;

        SourceType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * Gets or Sets visibility
     */
    public enum Visibility {
        OWNER_ONLY("OWNER_ONLY"),

        PRIVATE("PRIVATE"),

        API_LEVEL("API_LEVEL");

        private String value;

        Visibility(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public String getValue() {
            return value;
        }
    }

    private final SourceType sourceType;
    private final String sourceURL;
    private final String otherType;
    private final String id;
    private final String summary;
    private final String name;
    private final String permission;
    private final DocType type;
    private final Visibility visibility;
    private final String fileName;
    private final HashMap permissionMap;
    private final Instant createdTime;
    private final Instant lastUpdatedTime;
    private final String createdBy;
    private final String updatedBy;

    /**
     * {@code DocumentInfo} builder static inner class.
     */
    public static final class Builder {
        private HashMap permissionMap;
        private SourceType sourceType;
        private String sourceURL;
        private String otherType;
        private String id;
        private String summary;
        private String name;
        private DocType type;
        private Visibility visibility;
        private String permission;
        private  String fileName;
        private Instant createdTime;
        private Instant lastUpdatedTime;
        private String createdBy;
        private String updatedBy;

        public SourceType getSourceType() {
            return sourceType;
        }

        public String getSourceURL() {
            return sourceURL;
        }

        public String getOtherType() {
            return otherType;
        }

        public String getId() {
            return id;
        }

        public String getSummary() {
            return summary;
        }

        public String getName() {
            return name;
        }

        public DocType getType() {
            return type;
        }

        public Visibility getVisibility() {
            return visibility;
        }

        public String getFileName() {
            return fileName;
        }

        public Builder() {
        }

        public Builder(DocumentInfo copy) {
            this.sourceType = copy.sourceType;
            this.sourceURL = copy.sourceURL;
            this.otherType = copy.otherType;
            this.id = copy.id;
            this.summary = copy.summary;
            this.name = copy.name;
            this.type = copy.type;
            this.visibility = copy.visibility;
            this.fileName = copy.fileName;
            this.permission = copy.permission;
            this.permissionMap = copy.permissionMap;
        }

        /**
         * Sets the {@code sourceType} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param sourceType the {@code sourceType} to set
         * @return a reference to this Builder
         */
        public Builder sourceType(SourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        /**
         * Sets the {@code sourceURL} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param sourceURL the {@code sourceURL} to set
         * @return a reference to this Builder
         */
        public Builder sourceURL(String sourceURL) {
            this.sourceURL = sourceURL;
            return this;
        }

        /**
         * Sets the {@code otherType} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param otherType the {@code otherType} to set
         * @return a reference to this Builder
         */
        public Builder otherType(String otherType) {
            this.otherType = otherType;
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
         * Sets the {@code summary} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param summary the {@code summary} to set
         * @return a reference to this Builder
         */
        public Builder summary(String summary) {
            this.summary = summary;
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
         * Sets the {@code type} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param type the {@code type} to set
         * @return a reference to this Builder
         */
        public Builder type(DocType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the {@code visibility} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param visibility the {@code visibility} to set
         * @return a reference to this Builder
         */
        public Builder visibility(Visibility visibility) {
            this.visibility = visibility;
            return this;
        }

        /**
         * Sets the {@code permission} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param permission the {@code permission} to set
         * @return a reference to this Builder
         */
        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        /**
         * Sets the {@code permission} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param permissionMap the {@code permission} to set
         * @return a reference to this Builder
         */
        public Builder permissionMap(HashMap permissionMap) {
            this.permissionMap = permissionMap;
            return this;
        }

        /**
         * Sets the {@code fileName} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param fileName the {@code fileName} to set
         * @return a reference to this Builder
         */
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        /**
         * Sets the {@code createdTime} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param createdTime the {@code createdTime} to set
         * @return a reference to this Builder
         */
        public Builder createdTime(Instant createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        /**
         * Sets the {@code lastUpdatedTime} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param lastUpdatedTime the {@code lastUpdatedTime} to set
         * @return a reference to this Builder
         */
        public Builder lastUpdatedTime(Instant lastUpdatedTime) {
            this.lastUpdatedTime = lastUpdatedTime;
            return this;
        }

        /**
         * Sets the {@code createdBy} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param createdBy the {@code createdBy} to set
         * @return a reference to this Builder
         */
        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * Sets the {@code updatedBy} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param updatedBy the {@code updatedBy} to set
         * @return a reference to this Builder
         */
        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        /**
         * Returns a {@code DocumentInfo} built from the parameters previously set.
         *
         * @return a {@code DocumentInfo} built with parameters of this {@code DocumentInfo.Builder}
         */
        public DocumentInfo build() {
            return new DocumentInfo(this);
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }
    }
}
