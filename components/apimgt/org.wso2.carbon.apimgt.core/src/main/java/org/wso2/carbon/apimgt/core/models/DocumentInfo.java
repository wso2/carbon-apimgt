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

    /**
     *Document Types
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

    private SourceType sourceType;
    private String sourceURL;
    private String otherType;
    private String id;
    private String summary;
    private String name;
    private String type;
    private Visibility visibility;


    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOtherType() {
        return otherType;
    }

    public void setOtherType(String otherType) {
        this.otherType = otherType;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}
