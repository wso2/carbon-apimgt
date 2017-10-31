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

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Context class for holding all necessary API related object references -
 *      1. API {@link org.wso2.carbon.apimgt.core.models.API}
 *      2  Endpoint {@link org.wso2.carbon.apimgt.core.models.Endpoint}
 *      3. Document Info {@link org.wso2.carbon.apimgt.core.models.DocumentInfo}
 *      4. Document Content {@link org.wso2.carbon.apimgt.core.models.DocumentContent}
 *      5. Swagger Definition
 *      6. Gateway Definition
 *      7. Thumbnail content
 */
public class APIDetails {

    private API api;

    private Set<Endpoint> endpoints;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        APIDetails that = (APIDetails) o;

        return getApi().getId().equals(that.getApi().getId());

    }

    @Override
    public int hashCode() {
        return getApi().getId().hashCode();
    }

    private String swaggerDefinition;

    private String gatewayConfiguration;

    private InputStream thumbnailStream;

    private Set<DocumentInfo> documentInformation;

    // mapping from document id to Document Content
    private Map<String, DocumentContent> documentContentMap;

    public APIDetails (API api, String swaggerDefinition) {
        this.api = api;
        this.swaggerDefinition = swaggerDefinition;
        endpoints = new HashSet<>();
        documentInformation = new HashSet<>();
        documentContentMap = new HashMap();
    }

    public API getApi() {
        return api;
    }

    public void addEndpoint (Endpoint endpoint) {
        endpoints.add(endpoint);
    }

    public void setEndpoints (Set<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public Set<Endpoint> getEndpoints () {
        return endpoints;
    }

    public String getSwaggerDefinition() {
        return swaggerDefinition;
    }

    public String getGatewayConfiguration() {
        return gatewayConfiguration;
    }

    public Map<String, DocumentContent> getDocumentContentMap() {
        return documentContentMap;
    }

    public void addDocumentInformation (DocumentInfo documentInfo) {
        this.documentInformation.add(documentInfo);
    }

    public void addDocumentInformation (Collection<DocumentInfo> documentInfo) {
        for (DocumentInfo aDocumentInfo : documentInfo) {
            addDocumentInformation(aDocumentInfo);
        }
    }

    public Set<DocumentInfo> getAllDocumentInformation () {
        return documentInformation;
    }

    public void addDocumentContent (String documentId, DocumentContent content) {
        this.documentContentMap.put(documentId, content);
    }

    public void addDocumentContents (Collection<DocumentContent> contents) {
        for (DocumentContent content : contents) {
            this.documentContentMap.put(content.getDocumentInfo().getId(), content);
        }
    }

    public DocumentContent getDocumentContent (String documentId) {
        return documentContentMap.get(documentId);
    }

    public Collection<DocumentContent> getDocumentContents () {
        return documentContentMap.values();
    }

    public void setGatewayConfiguration(String gatewayConfiguration) {
        this.gatewayConfiguration = gatewayConfiguration;
    }

    public InputStream getThumbnailStream() {
        return thumbnailStream;
    }

    public void setThumbnailStream(InputStream thumbnailStream) {
        this.thumbnailStream = thumbnailStream;
    }
}
