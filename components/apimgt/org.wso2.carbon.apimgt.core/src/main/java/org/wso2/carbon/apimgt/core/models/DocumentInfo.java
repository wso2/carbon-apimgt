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
    private final DocType type;
    private final Visibility visibility;
    private final String fileName;

    /**
     * {@code DocumentInfo} builder static inner class.
     */
    public static final class Builder {
        private SourceType sourceType;
        private String sourceURL;
        private String otherType;
        private String id;
        private String summary;
        private String name;
        private DocType type;
        private Visibility visibility;
        private  String fileName;

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
         * Returns a {@code DocumentInfo} built from the parameters previously set.
         *
         * @return a {@code DocumentInfo} built with parameters of this {@code DocumentInfo.Builder}
         */
        public DocumentInfo build() {
            return new DocumentInfo(this);
        }
    }
}
