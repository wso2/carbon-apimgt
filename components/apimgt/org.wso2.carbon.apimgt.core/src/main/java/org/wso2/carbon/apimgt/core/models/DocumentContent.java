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

import java.io.InputStream;

/**
 * Contains the document Content
 */
public final class DocumentContent {
    private final DocumentInfo documentInfo;
    private final String inlineContent;
    private final InputStream fileContent;

    @Override
    public boolean equals(Object o) {
        if (this == o)  {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DocumentContent content = (DocumentContent) o;

        return getDocumentInfo() != null ? getDocumentInfo().equals(content.getDocumentInfo()) :
                content.getDocumentInfo() == null;

    }

    @Override
    public int hashCode() {
        return getDocumentInfo() != null ? getDocumentInfo().hashCode() : 0;
    }

    private DocumentContent(Builder builder) {
        this.documentInfo = builder.documentInfo;
        this.inlineContent = builder.inlineContent;
        this.fileContent = builder.filecontent;
    }

    public static Builder newDocumentContent() {
        return new Builder();
    }

    /**
     * Builder class for DocumentContent
     */
    public static final class Builder {
        private DocumentInfo documentInfo;
        private String inlineContent;
        private InputStream filecontent;

        public Builder() {
        }

        public DocumentContent build() {
            return new DocumentContent(this);
        }

        public Builder documentInfo(DocumentInfo documentInfo) {
            this.documentInfo = documentInfo;
            return this;
        }

        public Builder inlineContent(String inlineContent) {
            this.inlineContent = inlineContent;
            return this;
        }

        public Builder fileContent(InputStream fileContent) {
            this.filecontent = fileContent;
            return this;
        }
    }

    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }

    public String getInlineContent() {
        return inlineContent;
    }

    public InputStream getFileContent() {
        return fileContent;
    }
}
