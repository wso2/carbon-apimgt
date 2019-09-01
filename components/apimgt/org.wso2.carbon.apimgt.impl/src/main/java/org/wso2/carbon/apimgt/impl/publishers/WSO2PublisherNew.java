/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.publishers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIPublisher;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportManager;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class handles all the Publisher functionality when publishing an API to an external WSO2 Store.
 * When publishing an API to an external store, this uses APIImportExportManager to export the API with advertise_only
 * property true and uses the Import API Admin REST API in external store to import and publish the API. To remove
 * an API from external store, this class uses the API DELETE Publisher REST API of external store.
 */
public class WSO2PublisherNew implements APIPublisher {

    private static final Log log = LogFactory.getLog(WSO2PublisherNew.class);

    /**
     * Publish an API to external Store.
     *
     * @param api   API
     * @param store Store
     * @return published/not
     */
    @Override
    public boolean publishToStore(API api, APIStore store) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Publishing API: " + api.getId().getApiName() + " version: " + api.getId().getVersion()
                    + " to external store: " + store.getName());
        }
        //Export API as an advertised only API as a zipped file
        File file = exportAPIArchive(api);
        //Call the Admin REST API of external Store to publish the exported advertised only API
        HttpResponse response = importAPIToExternalStore(file, store, Boolean.FALSE);
        FileUtils.deleteQuietly(file);
        return evaluateImportAPIResponse(response);
    }

    /**
     * Exports API as an advertised API using APIImportExportManager.
     *
     * @param api API artifact to import
     * @return API archive
     * @throws APIManagementException If an error occurs while exporting API.
     */
    private File exportAPIArchive(API api) throws APIManagementException {

        File file;
        String tenantDomain = null;
        int tenantId;
        APIImportExportManager apiImportExportManager;

        try {
            apiImportExportManager = new APIImportExportManager(getLoggedInUserProvider(), getLoggedInUsername());
            //Set additional API properties to identify it as an advertised only API
            api.setAdvertiseOnly(true);
            //Change owner to original provider as the provider will be overriding after importing
            api.setApiOwner(api.getId().getProviderName());
            //Set Store visibility to public and publisher access control to ALL
            api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
            api.setAccessControl(APIConstants.NO_ACCESS_CONTROL);
            //set redirect URL for the original Store
            tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(
                    api.getId().getProviderName()));
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            api.setRedirectURL(getExternalStoreRedirectURL(tenantId));
            //Export API as an archive file and set it as a multipart entity in the request
            file = apiImportExportManager.exportAPIArchive(api, Boolean.TRUE, ExportFormat.JSON);
            if (log.isDebugEnabled()) {
                log.debug("API successfully exported to file: " + file.getName());
            }
        } catch (APIImportExportException e) {
            String errorMessage = "Error while exporting API: " + api.getId().getApiName() + " version: "
                    + api.getId().getVersion();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        } catch (UserStoreException e) {
            String errorMessage = "Error while getting tenantId for tenant domain: " + tenantDomain;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        }
        return file;
    }

    /**
     * Imports an API to external Store by calling Admin import API REST service. If overwrite is true, then the API
     * which is already published in external store will be updated.
     *
     * @param apiArchive API zipped file
     * @param store      external store to import
     * @param overwrite  whether to import or update APII
     * @return HTTP Response whether import is successful or not
     * @throws APIManagementException If an error occurs while importing API.
     */
    private HttpResponse importAPIToExternalStore(File apiArchive, APIStore store, Boolean overwrite)
            throws APIManagementException {

        MultipartEntityBuilder multipartEntityBuilder;
        String storeEndpoint = null;
        HttpClient httpclient;
        URIBuilder uriBuilder;
        HttpPost httppost;
        HttpResponse httpResponse;

        try {
            multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addPart(APIConstants.RestApiConstants.IMPORT_API_ARCHIVE_FILE,
                    new FileBody(apiArchive));
            //Get Admin REST import API endpoint from given store endpoint
            storeEndpoint = getAdminRESTURLFromStoreURL(store.getEndpoint())
                    + APIConstants.RestApiConstants.REST_API_ADMIN_IMPORT_API_RESOURCE;
            httpclient = getHttpClient(storeEndpoint);
            uriBuilder = new URIBuilder(storeEndpoint);
            //Add preserveProvider query parameter false
            uriBuilder.addParameter(APIConstants.RestApiConstants.IMPORT_API_PRESERVE_PROVIDER, Boolean.FALSE.toString());
            uriBuilder.addParameter(APIConstants.RestApiConstants.IMPORT_API_OVERWRITE, overwrite.toString());

            httppost = new HttpPost(uriBuilder.build());
            httppost.setEntity(multipartEntityBuilder.build());

            //Set Authorization Header of external store admin
            httppost.setHeader(HttpHeaders.AUTHORIZATION, getBasicAuthorizationHeader(store));
            if (log.isDebugEnabled()) {
                log.debug("Invoking Admin REST API of external store: " + storeEndpoint + " to import API archive: "
                        + apiArchive.getName());
            }
            //Call import API of external store
            httpResponse = httpclient.execute(httppost);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while building URI for store endpoint: " + storeEndpoint;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Error while importing to external Store: " + storeEndpoint;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        }
        return httpResponse;
    }

    /**
     * Get Admin REST API URL from given Store Endpoint URL assuming that given Store endpoint URL is similar to
     * "http://localhost:9763/store.
     *
     * @param storeEndpoint Store endpoint URL with "/store" context
     * @return Admin REST API URL of external store
     */
    private String getAdminRESTURLFromStoreURL(String storeEndpoint) {
        //Get Admin REST import API endpoint from given store endpoint
        if (storeEndpoint.contains(APIConstants.RestApiConstants.STORE_CONTEXT)) {
            storeEndpoint = storeEndpoint.split(APIConstants.RestApiConstants.STORE_CONTEXT)[0]
                    + APIConstants.RestApiConstants.REST_API_ADMIN_CONTEXT_FULL_0;
        }
        return storeEndpoint;
    }

    /**
     * Get Publisher REST API URL from given Store Endpoint URL assuming that given Store endpoint URL is similar to
     * "http://localhost:9763/store.
     *
     * @param storeEndpoint Store endpoint URL with "/store" context
     * @return Publisher REST API URL of external store
     */
    private String getPublisherRESTURLFromStoreURL(String storeEndpoint) {
        if (storeEndpoint.contains(APIConstants.RestApiConstants.STORE_CONTEXT)) {
            storeEndpoint = storeEndpoint.split(APIConstants.RestApiConstants.STORE_CONTEXT)[0]
                    + APIConstants.RestApiConstants.REST_API_PUBLISHER_CONTEXT_FULL_1;
        }
        return storeEndpoint;
    }

    /**
     * Get Basic Authorization header for external store admin credentials.
     *
     * @param store External Store config
     * @return Base64 encoded Basic Authorization header
     */
    private String getBasicAuthorizationHeader(APIStore store) {
        //Set Authorization Header of external store admin
        byte[] encodedAuth = Base64
                .encodeBase64((store.getUsername() + ":" + store.getPassword()).getBytes(StandardCharsets.ISO_8859_1));
        return APIConstants.AUTHORIZATION_HEADER_BASIC + StringUtils.SPACE + new String(encodedAuth);
    }

    /**
     * Check whether successful response is received for API Import service call.
     *
     * @param response HTTP response received for API Import request
     * @return Successful or not
     * @throws APIManagementException If an error occurs while checking the response
     */
    private Boolean evaluateImportAPIResponse(HttpResponse response) throws APIManagementException {

        HttpEntity entity;
        String responseString;
        try {
            //If API is imported successfully, return true
            if (evaluateResponseStatus(response)) {
                entity = response.getEntity();
                responseString = EntityUtils.toString(entity);
                //release all resources held by the responseHttpEntity
                EntityUtils.consume(entity);
                if (StringUtils.containsIgnoreCase(responseString, APIConstants.RestApiConstants.IMPORT_API_SUCCESS)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Import API service call received successful response: " + responseString);
                    }
                    return true;
                }
                log.error("Import API service call received unsuccessful response: " + responseString);
            }
        } catch (IOException e) {
            String errorMessage = "Error while evaluating HTTP response";
            throw new APIManagementException(errorMessage, e);
        }
        return false;
    }

    /**
     * Check whether HTTP Response code is 200 OK.
     *
     * @param response HTTP Response
     * @return whether status code matches or not.
     */
    private Boolean evaluateResponseStatus(HttpResponse response) {

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            return true;
        }
        log.error("API service call failed with unsuccessful status:" + statusCode);
        return false;
    }

    /**
     * The method to update an already published API in external Store.
     *
     * @param api   API
     * @param store Store
     * @return updated/not
     */
    @Override
    public boolean updateToStore(API api, APIStore store) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Updating API: " + api.getId().getApiName() + " version: " + api.getId().getVersion()
                    + " to external store: " + store.getName());
        }
        //Export API as an advertised only API as a zipped file
        File file = exportAPIArchive(api);
        //Call the Admin REST API of external Store to publish the exported advertised only API
        HttpResponse response = importAPIToExternalStore(file, store, Boolean.TRUE);
        FileUtils.deleteQuietly(file);
        return evaluateImportAPIResponse(response);
    }

    /**
     * The method to publish API to external Store.
     *
     * @param apiId APIIdentifier
     * @param store Store
     * @return deleted/not
     */
    @Override
    public boolean deleteFromStore(APIIdentifier apiId, APIStore store) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Delete API: " + apiId.getApiName() + " version: " + apiId.getVersion()
                    + " from external store: " + store.getName());
        }
        String apiUUID = getAPIUUID(store, apiId);
        if (apiUUID != null) {
            //Get Publisher REST endpoint from given store endpoint
            String storeEndpoint = getPublisherRESTURLFromStoreURL(store.getEndpoint())
                    + APIConstants.RestApiConstants.REST_API_PUB_RESOURCE_PATH_APIS + "/" + apiUUID;
            //Delete API
            try {
                HttpClient httpclient = getHttpClient(storeEndpoint);
                HttpDelete httpDelete = new HttpDelete(storeEndpoint);
                //Set Authorization Header of external store admin
                httpDelete.setHeader(HttpHeaders.AUTHORIZATION, getBasicAuthorizationHeader(store));
                //Call import API of external store
                HttpResponse httpResponse = httpclient.execute(httpDelete);
                if (evaluateResponseStatus(httpResponse)) {
                    return true;
                }
            } catch (IOException e) {
                String errorMessage = "Error while deleting API UUID: " + apiUUID + " from external store: "
                        + store.getName();
                log.error(errorMessage, e);
                throw new APIManagementException(errorMessage, e);
            }
        } else {
            log.error("API: " + apiId.getApiName() + " version: " + apiId.getVersion()
                    + " does not exist in external store: " + store.getName());
        }
        return false;
    }

    /**
     * The method to publish API to external Store.
     *
     * @param api   API
     * @param store Store
     * @return deleted/not
     */
    @Override
    public boolean isAPIAvailable(API api, APIStore store) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Check if API: " + api.getId().getApiName() + " version: " + api.getId().getVersion()
                    + " available in external store: " + store.getName());
        }
        return getAPIUUID(store, api.getId()) != null;
    }

    /**
     * Get API published to external Store by calling search API REST service in external store's publisher component.
     * If API exists, a non empty UUID will be returned.
     *
     * @param store         External Store
     * @param apiIdentifier API ID of the API to retrieve
     * @return UUID of the published API
     * @throws APIManagementException If an error occurs while searching the API in external store.
     */
    private String getAPIUUID(APIStore store, APIIdentifier apiIdentifier) throws APIManagementException {

        String apiUUID = null;
        //Get Publisher REST endpoint from given store endpoint
        String storeEndpoint = getPublisherRESTURLFromStoreURL(store.getEndpoint())
                + APIConstants.RestApiConstants.REST_API_PUB_RESOURCE_PATH_APIS;
        try {
            HttpClient httpclient = getHttpClient(storeEndpoint);
            URIBuilder uriBuilder = new URIBuilder(storeEndpoint);
            String searchQuery = APIConstants.RestApiConstants.PUB_SEARCH_API_QUERY_PARAMS_NAME
                    + apiIdentifier.getApiName() + StringUtils.SPACE
                    + APIConstants.RestApiConstants.PUB_SEARCH_API_QUERY_PARAMS_VERSION + apiIdentifier.getVersion();
            uriBuilder.addParameter(APIConstants.RestApiConstants.REST_API_PUB_SEARCH_API_QUERY, searchQuery);
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            //Set Authorization Header of external store admin
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, getBasicAuthorizationHeader(store));

            //Call Publisher REST API of external store
            HttpResponse httpResponse = httpclient.execute(httpGet);
            if (evaluateResponseStatus(httpResponse)) {
                HttpEntity entity = httpResponse.getEntity();
                String responseString = EntityUtils.toString(entity);
                //release all resources held by the responseHttpEntity
                EntityUtils.consume(entity);
                JSONParser parser = new JSONParser();
                JSONObject responseJson = (JSONObject) parser.parse(responseString);
                if ((long) responseJson.get(APIConstants.RestApiConstants.PUB_API_LIST_RESPONSE_PARAMS_COUNT) == 1) {
                    JSONArray apiList =
                            (JSONArray) responseJson.get(APIConstants.RestApiConstants.PUB_API_LIST_RESPONSE_PARAMS_LIST);
                    JSONObject apiJson = (JSONObject) apiList.get(0);
                    apiUUID = (String) apiJson.get(APIConstants.RestApiConstants.PUB_API_RESPONSE_PARAMS_ID);
                    if (log.isDebugEnabled()) {
                        log.debug("API: " + apiIdentifier.getApiName() + " version: " + apiIdentifier.getVersion()
                                + " exists in external store: " + store.getName() + " with UUID: " + apiUUID);
                    }
                } else {
                    log.error("Duplicate APIs exists in external store for API name:" + apiIdentifier.getApiName()
                            + " version: " + apiIdentifier.getVersion());
                }
            }
        } catch (ParseException e) {
            String errorMessage = "Error while reading API response from external store for API: "
                    + apiIdentifier.getApiName() + " version:" + apiIdentifier.getVersion();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Error while getting API UUID from external store for API: "
                    + apiIdentifier.getApiName() + " version:" + apiIdentifier.getVersion();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while building URI for store endpoint: " + storeEndpoint;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        }
        return apiUUID;
    }

    /**
     * Get APIProvider instance for the logged in user.
     *
     * @return APIProvider instance
     * @throws APIManagementException If na error occurs while getting APIProvider instance
     */
    private static APIProvider getLoggedInUserProvider() throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIProvider(getLoggedInUsername());
    }

    /**
     * Get logged in username.
     *
     * @return username
     */
    private static String getLoggedInUsername() {
        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    /**
     * Get redirect URL for external stores. (This is the URL displayed in the published APIs in external Store)
     *
     * @param tenantId tenant ID
     * @return Redirect URL
     * @throws APIManagementException If an error occurs while getting redirect URL.
     */
    private String getExternalStoreRedirectURL(int tenantId) throws APIManagementException {
        UserRegistry registry;
        String redirectURL;
        redirectURL = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration()
                .getFirstProperty(APIConstants.EXTERNAL_API_STORES + "." + APIConstants.EXTERNAL_API_STORES_STORE_URL);

        if (redirectURL != null) {
            return redirectURL;
        }
        try {
            registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.EXTERNAL_API_STORES_LOCATION)) {
                Resource resource = registry.get(APIConstants.EXTERNAL_API_STORES_LOCATION);
                String content = new String((byte[]) resource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                OMElement storeURL = element.getFirstChildWithName(new QName(APIConstants.EXTERNAL_API_STORES_STORE_URL));
                if (storeURL != null) {
                    redirectURL = storeURL.getText();
                } else {
                    String msg = "Store URL element is missing in External APIStores configuration";
                    log.error(msg);
                    throw new APIManagementException(msg);
                }
            }
            return redirectURL;
        } catch (RegistryException e) {
            String msg = "Error while retrieving External Stores Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Malformed XML found in the External Stores Configuration resource";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }

    }

    /**
     * Get HTTP Client for service endpoint port and protocol.
     *
     * @param storeEndpoint service endpoint URL Eg: http://localhost:9763/api/am/admin/v1.0/import/api
     *                      Eg: http://localhost:9763/api/am/publisher/v0.14
     * @return HTTP Client
     * @throws APIManagementException If an error occurs due to malformed URL.
     */
    private HttpClient getHttpClient(String storeEndpoint) throws APIManagementException {
        try {
            URL storeURL = new URL(storeEndpoint);
            int externalStorePort = storeURL.getPort();
            String externalStoreProtocol = storeURL.getProtocol();
            return APIUtil.getHttpClient(externalStorePort, externalStoreProtocol);
        } catch (MalformedURLException e) {
            throw new APIManagementException("Error while initializing HttpClient due to malformed URL", e);
        }
    }
}
