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

package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.HttpResponse;

import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * Interface which defines utility methods for a REST client.
 */
public interface RestCallUtil {

    /**
     * To make a RESTful login-request post call to a service.
     *
     * @param uri               URI of the service
     * @param username          Logged in user's username
     * @param password          Password of the user
     * @param acceptContentType MediaType which is expected as a response
     * @return HttpResponse from service
     * @throws APIManagementException In case of any failures, when trying to make a login-request post call
     */
    HttpResponse loginRequest(URI uri, String username, String password, MediaType acceptContentType)
            throws APIManagementException;

    /**
     * To make a RESTful user-fetch-request post call to a service.
     * Specifically used in this project to initiate the communication with app cloud, to get the functions deployed by
     * the user in app cloud.
     *
     * @param uri               URI of the service
     * @param username          Logged in user's username
     * @param userTenantDomain  Tenant domain of the user
     * @param rsaSignedToken    JWT signed with RSA to ensure integrity
     * @param acceptContentType MediaType which is expected as a response
     * @return HttpResponse from service
     * @throws APIManagementException In case of any failures, when trying to make a user-fetch-request post call
     */
    HttpResponse rsaSignedFetchUserRequest(URI uri, String username, String userTenantDomain, String rsaSignedToken,
                                           MediaType acceptContentType) throws APIManagementException;

    /**
     * To capture the cookies attached with response from a service.
     *
     * @param response HttpResponse by the service which contains all header fields
     * @return List of all cookies found
     */
    List<String> captureCookies(HttpResponse response);

    /**
     * Http GET request.
     *
     * @param uri               URI of the service
     * @param acceptContentType MediaType which is expected as a response
     * @param cookies           List of cookies to be added with the request, if any
     * @return HttpResponse from service
     * @throws APIManagementException In case of any failures, when trying to make a GET request
     */
    HttpResponse getRequest(URI uri, MediaType acceptContentType, List<String> cookies)
            throws APIManagementException;

    /**
     * Http POST request.
     *
     * @param uri                URI of the service
     * @param acceptContentType  MediaType which is expected as a response
     * @param cookies            List of cookies to be added with the request, if any
     * @param entity             Entity which needs to be sent to the service with the POST request
     * @param payloadContentType MediaType of the payload added in the request
     * @param headers            additional header key/value map
     * @return HttpResponse from service
     * @throws APIManagementException In case of any failures, when trying to make a POST request
     */
    HttpResponse postRequest(URI uri, MediaType acceptContentType, List<String> cookies, Entity entity,
            MediaType payloadContentType, Map<String, String> headers) throws APIManagementException;

    /**
     * Http PUT request.
     *
     * @param uri                URI of the service
     * @param acceptContentType  MediaType which is expected as a response
     * @param cookies            List of cookies to be added with the request, if any
     * @param entity             Entity which needs to be sent to the service with the POST request
     * @param payloadContentType MediaType of the payload added in the request
     * @return HttpResponse from service
     * @throws APIManagementException In case of any failures, when trying to make a PUT request
     */
    HttpResponse putRequest(URI uri, MediaType acceptContentType, List<String> cookies, Entity entity,
                            MediaType payloadContentType) throws APIManagementException;

    /**
     * Http DELETE request.
     *
     * @param uri               URI of the service
     * @param acceptContentType MediaType which is expected as a response
     * @param cookies           List of cookies to be added with the request, if any
     * @return HttpResponse from service
     * @throws APIManagementException In case of any failures, when trying to make a DELETE request
     */
    HttpResponse deleteRequest(URI uri, MediaType acceptContentType, List<String> cookies)
            throws APIManagementException;
}
