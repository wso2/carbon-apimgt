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
package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.ArtifactResourceMetaData;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * This interface mainly used to have the common methods to publisher and store
 */
public interface APIManager {
    /**
     * Returns a list of all existing APIs by all providers. The API objects returned by this
     * method may be partially initialized (due to performance reasons). Each API instance
     * is guaranteed to have the API name, version, provider name, context, status and icon URL.
     * All other fields may not be initialized. Therefore, the objects returned by this method
     * must not be used to access any metadata item related to an API, other than the ones listed
     * above. For that purpose a fully initialized API object instance should be acquired by
     * calling the getAPI(String) method.
     *
     * @return a List of API objects (partially initialized), possibly empty
     * @throws APIManagementException on error
     */
    List<API> getAllAPIs() throws APIManagementException;
    /**
     * Returns details of an API
     *
     * @param uuid UUID of the API's registry artifact
     * @return An API object related to the given artifact id or null
     * @throws APIManagementException if failed get API from String
     */
    API getAPIbyUUID(String uuid) throws APIManagementException;

    /**
     * Checks the Availability of given String
     *@api
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API availability
     */
    boolean isAPIAvailable(API api) throws APIManagementException;

    /**
     * Checks whether the given API context is already registered in the system
     *
     * @param context A String representing an API context
     * @return true if the context already exists and false otherwise
     * @throws APIManagementException if failed to check the context availability
     */
    boolean isContextExist(String context) throws APIManagementException;

    /**
     * Checks whether the given API name is already registered in the system
     *
     * @param apiName A String representing an API name
     * @return true if the api name already exists and false otherwise
     * @throws APIManagementException if failed to check the context availability
     */
    boolean isApiNameExist(String apiName) throws APIManagementException;

    /**
     * Returns a set of API versions for the given provider and API name
     *
     * @param providerName name of the provider (common)
     * @param apiName      name of the api
     * @return Set of version strings (possibly empty)
     * @throws APIManagementException if failed to get version for api
     */
    Set<String> getAPIVersions(String providerName, String apiName) throws APIManagementException;

    /**
     * Returns the swagger v2.0 definition as a string
     *
     * @param api id of the String
     * @return swagger string
     * @throws APIManagementException
     */
    String getSwagger20Definition(String api) throws APIManagementException;

    /**
     * Returns a paginated list of documentation attached to a particular API
     *
     * @param apiId UUID of API
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @return {@link List<ArtifactResourceMetaData>} Document meta data list
     * @throws APIManagementException if it failed to fetch Documentations
     */
    List<ArtifactResourceMetaData> getAllDocumentation(String apiId, int offset, int limit)
                                                                        throws APIManagementException;

    /**
     * Get a summary of documentation by doc Id
     *
     * @param docId Document ID
     * @return {@link ArtifactResourceMetaData} Documentation meta data
     * @throws APIManagementException if it failed to fetch Documentation
     */
    ArtifactResourceMetaData getDocumentationSummary(String docId) throws APIManagementException;

    /**
     * This method used to get the content of a documentation
     *
     * @param docId Document ID
     * @return {@link InputStream} Input stream for document content
     * @throws APIManagementException if the requested documentation content is not available
     */
    InputStream getDocumentationContent(String docId) throws APIManagementException;

    /**
     * Returns the corresponding application given the uuid
     * @param uuid uuid of the Application
     * @return it will return Application corresponds to the uuid provided.
     * @throws APIManagementException
     */
    Application getApplicationByUUID(String uuid) throws APIManagementException;

}
