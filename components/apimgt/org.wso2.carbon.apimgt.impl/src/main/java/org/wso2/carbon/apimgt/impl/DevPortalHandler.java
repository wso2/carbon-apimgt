/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.io.InputStream;

/**
 * This interface used to handle different types of developer portals' interactions with APIM.
 */
public interface DevPortalHandler {

    /**
     * Publishes API metadata to the Developer Portal.
     *
     * @param organization The organization to which the API belongs.
     * @param api          The API object containing metadata details.
     * @return A reference ID (Devportal side) of API.
     * @throws APIManagementException If an error occurs while publishing metadata.
     */
    String publishAPIMetadata(String organization, API api) throws APIManagementException;

    /**
     * Updates API metadata in the Developer Portal.
     *
     * @param organization The organization to which the API belongs.
     * @param api          The API object containing updated metadata.
     * @param refId        The reference ID of the API to be updated.
     * @throws APIManagementException If an error occurs while updating metadata.
     */
    void updateAPIMetadata(String organization, API api, String refId) throws APIManagementException;

    /**
     * Un-publishes API metadata from the Developer Portal.
     *
     * @param organization The organization to which the API belongs.
     * @param api          The API object whose metadata is to be removed.
     * @param refId        The reference ID of the API to be unpublished.
     * @throws APIManagementException If an error occurs while un-publishing metadata.
     */
    void unpublishAPIMetadata(String organization, API api, String refId) throws APIManagementException;

    /**
     * Publishes API content (e.g., documentation, descriptions) to the Developer Portal.
     *
     * @param organization The organization to which the API belongs.
     * @param refId        The reference ID of the API.
     * @param content      The InputStream containing the content to be published.
     * @param apiName      The name of the API.
     * @throws APIManagementException If an error occurs while publishing content.
     */
    void publishAPIContent(String organization, String refId, InputStream content, String apiName) throws APIManagementException;

    /**
     * Un-publishes API content from the Developer Portal.
     *
     * @param organization The organization to which the API belongs.
     * @param refId        The reference ID of the API whose content is to be removed.
     * @throws APIManagementException If an error occurs while un-publishing content.
     */
    void unpublishAPIContent(String organization, String refId) throws APIManagementException;

    /**
     * Publishes organization theme to the Developer Portal.
     *
     * @param organization The organization for which content is being published.
     * @param content      The InputStream containing the content to be published.
     * @throws APIManagementException If an error occurs while publishing organization content.
     */
    void publishOrgContent(String organization, InputStream content) throws APIManagementException;

    /**
     * Un-publishes organization-level content from the Developer Portal.
     *
     * @param organization The organization whose content is to be removed.
     * @throws APIManagementException If an error occurs while un-publishing organization content.
     */
    void unpublishOrgContent(String organization) throws APIManagementException;
}
