/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIDetails;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manager class for generic API Import and Export handling
 */
public class ApiImportExportManager {

    private static final Logger log = LoggerFactory.getLogger(ApiImportExportManager.class);

    APIPublisher apiPublisher;

    public ApiImportExportManager(APIPublisher apiPublisher) {
        this.apiPublisher = apiPublisher;
    }

    /**
     * Retrieves all API details of the APIs for the given search query. API details consist of:
     *      1. API {@link org.wso2.carbon.apimgt.core.models.API}
     *      2. Document Info {@link org.wso2.carbon.apimgt.core.models.DocumentInfo}
     *      3. Document Content {@link org.wso2.carbon.apimgt.core.models.DocumentContent}
     *      4. Swagger Definition
     *      5. Gateway Definition
     *      6. Thumbnail content
     *
     * @param limit number of max results
     * @param offset starting location when returning a limited set of results
     * @param query searchQuery
     * @return {@link APIDetails} instance
     * @throws APIManagementException if an error occurs while retrieving API details
     */
    public Set<APIDetails> getAPIDetails(Integer limit, Integer offset, String query) throws APIManagementException {

        Set<APIDetails> apiDetailSet = new HashSet<>();
        // search for APIs
        List<API> apis = apiPublisher.searchAPIs(limit, offset, query);
        if (apis == null || apis.isEmpty()) {
            // no APIs found, return
            return apiDetailSet;
        }

        // iterate and collect all information
        for (API api : apis) {
            api = apiPublisher.getAPIbyUUID(api.getId());
            // get endpoints at API Level
            Map<String, Endpoint> endpoints = api.getEndpoint();
            if (endpoints.isEmpty()) {
                log.error("No Endpoints found for api: " + api.getName() + ", version: " + api.getVersion());
                // skip this API
                // continue;
            }
            Set<Endpoint> endpointSet = new HashSet<>();
            for (Map.Entry<String, Endpoint> endpointEntry : endpoints.entrySet()) {
                if (APIMgtConstants.GLOBAL_ENDPOINT.equals(endpointEntry.getValue().getApplicableLevel())) {
                    Endpoint endpoint = new Endpoint.Builder(apiPublisher.getEndpoint(endpointEntry.getValue().getId
                            ())).id("").build();
                    endpoints.replace(endpointEntry.getKey(),endpoint);
                    endpointSet.add(endpoint);
                }
            }
            // get Endpoints at Resource Level
            Map<String,UriTemplate> uriTemplateMap = api.getUriTemplates();
            uriTemplateMap.forEach((k, v) -> {
                UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder(v);
                Map<String,Endpoint> resourceEndpoints = uriTemplateBuilder.getEndpoint();
                resourceEndpoints.forEach((type, value) -> {
                    Endpoint endpoint = null;
                    if (APIMgtConstants.GLOBAL_ENDPOINT.equals(value.getApplicableLevel())) {
                        try {
                            endpoint = new Endpoint.Builder(apiPublisher.getEndpoint(value.getId())).id("")
                                    .build();
                            endpointSet.add(endpoint);
                        } catch (APIManagementException e) {
                            log.error("Error in getting endpoints for Resource: " + v.getTemplateId(), e);
                        }
                    }else{
                        endpoint = new Endpoint.Builder(value).id("").build();
                    }
                    resourceEndpoints.replace(type,endpoint);
                });
                uriTemplateMap.replace(k, uriTemplateBuilder.endpoint(resourceEndpoints).build());
            });
            api = new API.APIBuilder(api).endpoint(endpoints).uriTemplates(uriTemplateMap).build();
            // get swagger definition
            String swaggerDefinition;
            try {
                swaggerDefinition = apiPublisher.getApiSwaggerDefinition(api.getId());
            } catch (APIManagementException e) {
                log.error("Error in getting Swagger configuration for api: " + api.getName() + ", version: " +
                        api.getVersion(), e);
                // skip this API
                continue;
            }

            // get gateway configuration
            String gatewayConfig;
            try {
                gatewayConfig = apiPublisher.getApiGatewayConfig(api.getId());
            } catch (APIManagementException e) {
                log.error("Error in getting gateway configuration for api: " + api.getName() + ", version: " +
                        api.getVersion(), e);
                // skip this API
                continue;
            }

            // get doc information
            List<DocumentInfo> documentInfo = null;
            try {
                documentInfo = apiPublisher.getAllDocumentation(api.getId(), 0, Integer.MAX_VALUE);
            } catch (APIManagementException e) {
                log.error("Error in getting documentation content for api: " + api.getName() + ", version: " +
                        api.getVersion(), e);
                // no need to skip the API as docs don't affect API functionality
            }
            Set<DocumentContent> documentContents = new HashSet<>();
            if (documentInfo != null && !documentInfo.isEmpty()) {
                // iterate and collect document content
                for (DocumentInfo aDocumentInfo : documentInfo) {
                    try {
                        documentContents.add(apiPublisher.getDocumentationContent(aDocumentInfo.getId()));
                    } catch (APIManagementException e) {
                        log.error("Error in getting documentation content for api: " + api.getName() +
                                ", version: " + api.getVersion() + ", doc id: " + aDocumentInfo.getId(), e);
                        // no need to skip the API as docs don't affect API functionality
                    }
                }
            }

            // get thumbnail
            InputStream thumbnailStream = null;
            try {
                thumbnailStream = apiPublisher.getThumbnailImage(api.getId());
            } catch (APIManagementException e) {
                log.error("Error in getting thumbnail for api: " + api.getName() + ", version: " + api.getVersion(), e);
                // no need to skip the API as thumbnail don't affect API functionality
            }

            // search operation returns a summary of APIs, need to get all details of APIs
            APIDetails apiDetails = new APIDetails(api, swaggerDefinition);
            apiDetails.setGatewayConfiguration(gatewayConfig);
            apiDetails.setEndpoints(endpointSet);

            if (documentInfo != null && !documentInfo.isEmpty()) {
                apiDetails.addDocumentInformation(documentInfo);
            }
            if (!documentContents.isEmpty()) {
                apiDetails.addDocumentContents(documentContents);
            }
            if (thumbnailStream != null) {
                apiDetails.setThumbnailStream(thumbnailStream);
            }
            apiDetailSet.add(apiDetails);
        }

        return apiDetailSet;
    }

    /**
     * Adds the API details
     *
     * @param apiDetails {@link APIDetails} instance
     * @throws APIManagementException if an error occurs while adding API details
     */
    public void addAPIDetails(APIDetails apiDetails) throws APIManagementException {

        // update everything
        String swaggerDefinition = apiDetails.getSwaggerDefinition();
        String gatewayConfig = apiDetails.getGatewayConfiguration();
        Map<String, Endpoint> endpointTypeToIdMap = apiDetails.getApi().getEndpoint();
        Map<String,UriTemplate> uriTemplateMap = apiDetails.getApi().getUriTemplates();
        // endpoints
        for (Endpoint endpoint : apiDetails.getEndpoints()) {
            try {
                Endpoint existingEndpoint = apiPublisher.getEndpointByName(endpoint.getName());
                String endpointId;
                if (existingEndpoint == null) {
                    // no endpoint by that name, add it
                    endpointId = apiPublisher.addEndpoint(endpoint);

                } else {
                    endpointId = existingEndpoint.getId();
                    if (log.isDebugEnabled()) {
                        log.debug("Endpoint with id " + endpoint.getId() + " already exists, not adding again");
                    }
                    // endpoint with same name exists, add to endpointTypeToIdMap
                    //     endpointTypeToIdMap.put(endpoint.getType(), existingEndpoint.getId());
                }
                endpointTypeToIdMap.forEach((String k, Endpoint v) ->{
                    if (endpoint.getName().equals(v.getName())){
                        Endpoint replacedEndpoint = new Endpoint.Builder(v).id(endpointId).build();
                        endpointTypeToIdMap.replace(k,replacedEndpoint);
                    }
                });
                uriTemplateMap.forEach(((String templateId, UriTemplate uriTemplate) ->{
                    UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder(uriTemplate);
                    Map<String,Endpoint> uriEndpointMap = uriTemplateBuilder.getEndpoint();
                    uriEndpointMap.forEach((String type, Endpoint endpoint1) -> {
                        if (endpoint.getName().equals(endpoint1.getName())){
                            Endpoint replacedEndpoint = new Endpoint.Builder(endpoint1).id(endpointId).build();
                            uriEndpointMap.replace(type,replacedEndpoint);
                        }
                    });
                    uriTemplateMap.replace(templateId,uriTemplateBuilder.endpoint(uriEndpointMap).build());
                } ));

            } catch (APIManagementException e) {
                // skip adding this API; log and continue
                log.error("Error while adding the endpoint with id: " + endpoint.getId() + ", type: " + endpoint
                        .getType() + " for API: " + apiDetails.getApi().getName() + ", version: " + apiDetails.getApi
                        ().getVersion());
            }
        }

        API.APIBuilder apiBuilder = new API.APIBuilder(apiDetails.getApi());
        apiPublisher.addAPI(apiBuilder.apiDefinition(swaggerDefinition).gatewayConfig(gatewayConfig).
                endpoint(endpointTypeToIdMap).uriTemplates(uriTemplateMap));

        // docs
        try {
            Set<DocumentInfo> documentInfo = apiDetails.getAllDocumentInformation();
            for (DocumentInfo aDocInfo : documentInfo) {
                apiPublisher.addDocumentationInfo(apiDetails.getApi().getId(), aDocInfo);
            }
            for (DocumentContent aDocContent : apiDetails.getDocumentContents()) {
                // add documentation
                if (aDocContent.getDocumentInfo().getSourceType().equals(DocumentInfo.SourceType.FILE)) {
                    apiPublisher.uploadDocumentationFile(aDocContent.getDocumentInfo().getId(),
                            aDocContent.getFileContent(), 
                            URLConnection.guessContentTypeFromStream(aDocContent.getFileContent()));
                } else if (aDocContent.getDocumentInfo().getSourceType().equals(DocumentInfo.SourceType.INLINE)) {
                    apiPublisher.addDocumentationContent(aDocContent.getDocumentInfo().getId(),
                            aDocContent.getInlineContent());
                }
            }

        } catch (APIManagementException e) {
            // no need to throw, log and continue
            log.error("Error while adding Document details for API: " + apiDetails.getApi().getName() + ", version: " +
                    apiDetails.getApi().getVersion(), e);
        } catch (IOException e) {
            // no need to throw, log and continue
            log.error("Error while retrieving content type of the File documentation of API : " 
                    + apiDetails.getApi().getName() + ", version: " + apiDetails.getApi().getVersion(), e);
        }

        // add thumbnail
        try {
            apiPublisher.saveThumbnailImage(apiDetails.getApi().getId(), apiDetails.getThumbnailStream(), "thumbnail");
        } catch (APIManagementException e) {
            // no need to throw, log and continue
            log.error("Error while adding thumbnail for API: " + apiDetails.getApi().getName() + ", version: "
                    + apiDetails.getApi().getVersion(), e);
        }
    }

    /**
     * Updates the API details
     *
     * @param apiDetails {@link APIDetails} instance
     * @throws APIManagementException if an error occurs while updating API details
     */
    void updateAPIDetails(APIDetails apiDetails) throws APIManagementException {

        // update everything
        String swaggerDefinition = apiDetails.getSwaggerDefinition();
        String gatewayConfig = apiDetails.getGatewayConfiguration();
        Map<String, Endpoint> endpointTypeToIdMap = new HashMap<>();

        // endpoints
        for (Endpoint endpoint : apiDetails.getEndpoints()) {
            try {
                apiPublisher.updateEndpoint(endpoint);
                endpointTypeToIdMap.put(endpoint.getType(), endpoint);

            } catch (APIManagementException e) {
                // skip updating this API, log and continue
                log.error("Error while updating the endpoint with id: " + endpoint.getId() + ", type: " + endpoint
                        .getType() + " for API: " + apiDetails.getApi().getName() + ", version: " + apiDetails.getApi
                        ().getVersion());
            }
        }


        API.APIBuilder apiBuilder = new API.APIBuilder(apiDetails.getApi());
        apiPublisher.updateAPI(apiBuilder.apiDefinition(swaggerDefinition).gatewayConfig(gatewayConfig).
                endpoint(endpointTypeToIdMap));

        // docs
        try {
            Set<DocumentInfo> documentInfo = apiDetails.getAllDocumentInformation();
            for (DocumentInfo aDocInfo : documentInfo) {
                apiPublisher.updateDocumentation(aDocInfo.getId(), aDocInfo);
            }
            Collection<DocumentContent> docContents = apiDetails.getDocumentContents();
            for (DocumentContent docContent : docContents) {
                // update documentation
                if (docContent.getDocumentInfo().getSourceType().equals(DocumentInfo.SourceType.FILE)) {
                    apiPublisher
                            .uploadDocumentationFile(docContent.getDocumentInfo().getId(), docContent.getFileContent(),
                                    URLConnection.guessContentTypeFromStream(docContent.getFileContent()));
                } else if (docContent.getDocumentInfo().getSourceType().equals(DocumentInfo.SourceType.INLINE)) {
                    apiPublisher.addDocumentationContent(docContent.getDocumentInfo().getId(),
                            docContent.getInlineContent());
                }
            }

        } catch (APIManagementException e) {
            // no need to throw, log and continue
            log.error("Error while adding Document details for API: " + apiDetails.getApi().getName() + ", version: " +
                    apiDetails.getApi().getVersion(), e);
        } catch (IOException e) {
            // no need to throw, log and continue
            log.error("Error while retrieving content type of the File documentation of API : "
                    + apiDetails.getApi().getName() + ", version: " + apiDetails.getApi().getVersion(), e);
        }

        // update thumbnail
        try {
            apiPublisher.saveThumbnailImage(apiDetails.getApi().getId(), apiDetails.getThumbnailStream(), "thumbnail");
        } catch (APIManagementException e) {
            // no need to throw, log and continue
            log.error("Error while updating thumbnail for API: " + apiDetails.getApi().getName() + ", version: " +
                    apiDetails.getApi().getVersion(), e);
        }
    }
}
