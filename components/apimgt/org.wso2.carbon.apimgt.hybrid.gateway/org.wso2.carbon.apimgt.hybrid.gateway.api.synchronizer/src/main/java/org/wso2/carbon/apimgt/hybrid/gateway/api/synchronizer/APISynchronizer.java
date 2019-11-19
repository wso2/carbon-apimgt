/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.APIDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.APIListDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.APIListPaginationDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.LabelDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.MediationDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.MediationInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.MediationListDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.SequenceDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.exceptions.APISynchronizationException;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.internal.ServiceDataHolder;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.util.APIMappingUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.util.APISynchronizationConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.common.OnPremiseGatewayInitListener;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.AccessTokenDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.OAuthApplicationInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.HttpRequestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.MicroGatewayCommonUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.TokenUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Class for synchronizing APIs upon initial server startup
 */
public class APISynchronizer implements OnPremiseGatewayInitListener {
    private static final Log log = LogFactory.getLog(APISynchronizer.class);
    private String apiViewUrl = APISynchronizationConstants.EMPTY_STRING;
    private String mediationPolicyUrl = APISynchronizationConstants.EMPTY_STRING;
    /** Label configured for this gateway (if configured) */
    private String label;

    @Override
    public void completedInitialization() {
        try {
            synchronizeApis(null);
        } catch (APISynchronizationException e) {
            log.error("API Synchronization failed.", e);
        }
    }

    /**
     * Method to initialize on premise gateway properties
     */
    private void initializeOnPremiseGatewayProperties() throws APISynchronizationException {
        try {
            String apiPublisherUrl = ConfigManager.getConfigManager()
                    .getProperty(APISynchronizationConstants.API_PUBLISHER_URL_PROPERTY);
            String apiVersion = ConfigManager.getConfigManager()
                    .getProperty(APISynchronizationConstants.API_VERSION_PROPERTY);
            if (apiVersion == null) {
                apiVersion = APISynchronizationConstants.API_DEFAULT_VERSION;
                if (log.isDebugEnabled()) {
                    log.debug("Using default API version: " + apiVersion);
                }
            } else if (APISynchronizationConstants.CLOUD_API.equals(apiVersion)) {
                apiVersion = APISynchronizationConstants.EMPTY_STRING;
                if (log.isDebugEnabled()) {
                    log.debug("Cloud API doesn't have a version. Therefore, removing the version.");
                }
            }
            if (apiPublisherUrl == null) {
                apiPublisherUrl = APISynchronizationConstants.DEFAULT_API_PUBLISHER_URL;
                if (log.isDebugEnabled()) {
                    log.debug("Using default API publisher URL." + apiPublisherUrl);
                }
            }
            //Remove '//' which is created in cloud case.
            apiViewUrl = apiPublisherUrl + APISynchronizationConstants.API_VIEW_PATH
                    .replace(APISynchronizationConstants.API_VERSION_PARAM, apiVersion)
                    .replace("//", APISynchronizationConstants.URL_PATH_SEPARATOR);
            mediationPolicyUrl = apiPublisherUrl + APISynchronizationConstants.API_VIEW_GLOBAL_MEDIATION_POLICY_PATH
                    .replace(APISynchronizationConstants.API_VERSION_PARAM, apiVersion)
                    .replace("//", APISynchronizationConstants.URL_PATH_SEPARATOR);

            label = ConfigManager.getConfigManager().getProperty(OnPremiseGatewayConstants.GATEWAY_LABEL_PROPERTY_KEY);
            if (StringUtils.isNotBlank(label) && log.isDebugEnabled()) {
                log.debug("Configured label for the gateway is: " + label);
            }
        } catch (OnPremiseGatewayException e) {
            throw new APISynchronizationException(
                    "An error occurred while retrieving micro gateway configuration.", e);
        }
    }

    /**
     * Method to synchronize APIs
     *
     * @param updatedApiIds identifiers of updated APIs
     */
    public void synchronizeApis(JSONArray updatedApiIds)
            throws APISynchronizationException {
        try {
            log.info("Started synchronizing APIs.");
            loadTenant();
            initializeOnPremiseGatewayProperties();
            // Registering API client and obtaining an access token to invoke the publisher REST API
            AccessTokenDTO accessTokenDTO = getAccessToken();

            List<APIDTO> apiInfo;
            if (updatedApiIds != null) {
                // Retrieve updated API details
                apiInfo = getDetailsOfUpdatedAPIs(updatedApiIds, accessTokenDTO);
            } else {
                // Retrieve API details
                apiInfo = getDetailsOfAllAPIs(accessTokenDTO);
            }

            // Create APIs
            for (APIDTO apidto : apiInfo) {
                try {
                    // Create custom sequences
                    createCustomSequences(apidto, accessTokenDTO);
                    // Overriding context information to remove path /t/providerDomain/ since it will be appended at the
                    // time of creating an API
                    String providerDomain =
                            MultitenantUtils.getTenantDomain(apidto.getProvider());
                    String removeStr = "/t/" + providerDomain;
                    String contextReturned = apidto.getContext();
                    String context = contextReturned.replaceAll(removeStr, APISynchronizationConstants.EMPTY_STRING);
                    apidto.setContext(context);
                    APIMappingUtil.apisUpdate(apidto);
                } catch (APISynchronizationException e) {
                    log.error("Failed to create API " + apidto.getId());
                }
            }
            log.info("API synchronization completed.");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Method to generate an access token to invoke publisher REST API
     *
     * @return AccessTokenDTO
     */
    private AccessTokenDTO getAccessToken() throws APISynchronizationException {
        if (log.isDebugEnabled()) {
            log.debug("Registering client with dynamic client registration endpoint.");
        }
        try {
            // Registering API client and obtaining a response with consumer key and consumer secret values.
            OAuthApplicationInfoDTO oAuthDto = TokenUtil.registerClient();

            // Generating an access token to invoke publisher REST API
            String combinedScopes = APISynchronizationConstants.API_VIEW_SCOPE + " "
                    + APISynchronizationConstants.API_MEDIATION_POLICY_VIEW_SCOPE;
            if (log.isDebugEnabled()) {
                log.debug("Generating an access token with scope(s): " + combinedScopes);
            }

            return TokenUtil.generateAccessToken(oAuthDto.getClientId(),
                    oAuthDto.getClientSecret().toCharArray(), combinedScopes);
        } catch (OnPremiseGatewayException e) {
            throw new APISynchronizationException("Failed to generate an access token.", e);
        }
    }

    /**
     * Method to update APIs
     */
    public void updateApis() throws APISynchronizationException {
        JSONArray updatedApiIds = getIdentifiersOfUpdatedAPIs();
        if (updatedApiIds.size() != 0) {
            synchronizeApis(updatedApiIds);
        }
    }

    /**
     * Method to obtain complete details of all APIs
     *
     * @param accessTokenDTO access token DTO
     * @return A list of APIDTO objects
     */
    private List<APIDTO> getDetailsOfAllAPIs(AccessTokenDTO accessTokenDTO) throws APISynchronizationException {
        APIListDTO summarizedApiDTOList = getAPIList(accessTokenDTO, 0);
        List<APIInfoDTO> apiInfoList = summarizedApiDTOList.getList();
        APIListPaginationDTO pagination = summarizedApiDTOList.getPagination();

        // If APIs count exceeds configured pagination limit(500), below logic will iteratively call 'getAPIList()'
        // with new offset value and get all the remaining APIs.
        while (pagination != null && pagination.getOffset() + pagination.getLimit() < pagination.getTotal()) {
            int newOffset = pagination.getOffset() + pagination.getLimit();
            if (log.isDebugEnabled()) {
                log.debug("Retrieving paginated APIs from offset value: " + newOffset + " to: " +
                          (newOffset + pagination.getLimit()));
            }
            summarizedApiDTOList = getAPIList(accessTokenDTO, newOffset);
            pagination = summarizedApiDTOList.getPagination();
            apiInfoList.addAll(summarizedApiDTOList.getList());
        }

        List<APIDTO> apiDTOList = new ArrayList<>();
        for (APIInfoDTO apiInfoObj : apiInfoList) {
            String id = apiInfoObj.getId();
            String detailedAPIViewUrl = apiViewUrl + APISynchronizationConstants.URL_PATH_SEPARATOR + id;
            try {
                APIDTO apiDTO = getAPI(id, detailedAPIViewUrl, accessTokenDTO);
                apiDTOList.add(apiDTO);
            } catch (APISynchronizationException e) {
                log.error("An error occurred while retrieving details of API: " + id, e);
            }
        }
        return apiDTOList;
    }

    /**
     * Method to obtain complete details of an API
     *
     * @param id                 API ID
     * @param detailedAPIViewUrl url
     * @param accessTokenDTO     access token DTO
     * @return An APIDTO object
     */
    private APIDTO getAPI(String id, String detailedAPIViewUrl, AccessTokenDTO accessTokenDTO)
            throws APISynchronizationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving details of API " + id + " using publisher REST API.");
            }

            String apiPublisherUrl = ConfigManager.getConfigManager()
                    .getProperty(OnPremiseGatewayConstants.API_PUBLISHER_URL_PROPERTY_KEY);
            if (apiPublisherUrl == null) {
                apiPublisherUrl = OnPremiseGatewayConstants.DEFAULT_API_PUBLISHER_URL;
                if (log.isDebugEnabled()) {
                    log.debug("Using default API publisher URL: " + apiPublisherUrl);
                }
            }
            URL apiPublisherUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiPublisherUrl);
            HttpClient httpClient = APIUtil.getHttpClient(apiPublisherUrlValue.getPort(), apiPublisherUrlValue
                    .getProtocol());
            HttpGet httpGet = new HttpGet(detailedAPIViewUrl);
            String authHeaderValue = OnPremiseGatewayConstants.AUTHORIZATION_BEARER + accessTokenDTO.getAccessToken();
            httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);
            String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpGet,
                    OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
            if (log.isDebugEnabled()) {
                log.debug("Received response from GET API Details : " + response);
            }
            InputStream is = new ByteArrayInputStream(response.getBytes(
                    Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(is, APIDTO.class);
        } catch (OnPremiseGatewayException e) {
            throw new APISynchronizationException(e);
        } catch (IOException e) {
            throw new APISynchronizationException("An error occurred while de-serializing APIDTO object " +
                    "from input stream.", e);
        }
    }

    /**
     * Method to retrieve a list of all APIs
     *
     * @param accessTokenDTO Access token DTO
     * @param offset         Pagination offset for listing APIs
     * @return APIListDTO APIListDTO
     */
    private APIListDTO getAPIList(AccessTokenDTO accessTokenDTO, int offset)
            throws APISynchronizationException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving details of all APIs using publisher REST API.");
        }

        String apiPublisherUrl = null;
        try {
            apiPublisherUrl = ConfigManager.getConfigManager()
                    .getProperty(OnPremiseGatewayConstants.API_PUBLISHER_URL_PROPERTY_KEY);
        } catch (OnPremiseGatewayException e) {
            throw new APISynchronizationException(e);
        }
        if (apiPublisherUrl == null) {
            apiPublisherUrl = OnPremiseGatewayConstants.DEFAULT_API_PUBLISHER_URL;
            if (log.isDebugEnabled()) {
                log.debug("Using default API publisher URL: " + apiPublisherUrl);
            }
        }
        HttpClient httpClient = null;
        try {
            URL apiPublisherUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiPublisherUrl);
            httpClient = APIUtil.getHttpClient(apiPublisherUrlValue.getPort(), apiPublisherUrlValue.getProtocol());
        } catch (OnPremiseGatewayException e) {
            throw new APISynchronizationException("Error while retrieving Http client." ,e);
        }

        // Setting offset limit to 0 and pagination limit to 500 at the beginning
        String apiViewUrl = this.apiViewUrl + APISynchronizationConstants.QUESTION_MARK +
                            APISynchronizationConstants.OFFSET_PREFIX + String.valueOf(offset) +
                            APISynchronizationConstants.AMPERSAND +
                            APISynchronizationConstants.PAGINATION_LIMIT_PREFIX +
                            APISynchronizationConstants.PAGINATION_LIMIT;
        try {
            // Check whether a label is configured, if true set label as a query param to the URL
            if (StringUtils.isNotBlank(label)) {
                apiViewUrl = apiViewUrl + APISynchronizationConstants.AMPERSAND +
                             APISynchronizationConstants.API_SEARCH_LABEL_QUERY_PREFIX +
                             URLEncoder.encode(label, APISynchronizationConstants.CHARSET_UTF8);
                if (log.isDebugEnabled()) {
                    log.debug("API GET URL after adding label property value: " + apiViewUrl);
                }
            }
        } catch (UnsupportedEncodingException e) {
            // If an error occurred during URL encoding, throw the error to break synchronization for that label.
            throw new APISynchronizationException("An error occurred when encoding the URL with label: " + label, e);
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Sending request to GET details of APIs for the URL: " + apiViewUrl);
            }
            HttpGet httpGet = new HttpGet(apiViewUrl);
            String authHeaderValue = OnPremiseGatewayConstants.AUTHORIZATION_BEARER + accessTokenDTO.getAccessToken();
            httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);
            String response = HttpRequestUtil
                    .executeHTTPMethodWithRetry(httpClient, httpGet, OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
            if (log.isDebugEnabled()) {
                log.debug("Received response from GET details of all APIs : " + response);
            }
            InputStream is = new ByteArrayInputStream(response.getBytes(
                    Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, APIListDTO.class);
        } catch (OnPremiseGatewayException e) {
            throw new APISynchronizationException("An error occurred while retrieving details of all APIs " +
                    "using publisher REST API.", e);
        } catch (IOException e) {
            throw new APISynchronizationException("An error occurred while de-serializing APIListDTO object " +
                    "from input stream.", e);
        }
    }

    /**
     * Method to retrieve identifiers of updated APIs
     *
     * @return A JSON array with identifiers of updated APIs
     */
    private JSONArray getIdentifiersOfUpdatedAPIs() throws APISynchronizationException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving identifiers of updated APIs.");
        }
        try {
            APIManagerConfiguration config = ServiceDataHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
            char [] password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD).toCharArray();
            String updatedAPIViewUrl =
                    ConfigManager.getConfigManager()
                            .getProperty(APISynchronizationConstants.DEFAULT_API_UPDATE_URL_PROPERTY);
            if (updatedAPIViewUrl == null) {
                updatedAPIViewUrl = APISynchronizationConstants.DEFAULT_API_UPDATE_SERVICE_URL;
            }
            URL updatedAPIViewUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(updatedAPIViewUrl);
            HttpClient httpClient = APIUtil.getHttpClient(updatedAPIViewUrlValue.getPort(), updatedAPIViewUrlValue
                    .getProtocol());

            HttpGet httpGet = new HttpGet(updatedAPIViewUrl);
            String authHeaderValue = TokenUtil.getBasicAuthHeaderValue(username, password);
            MicroGatewayCommonUtil.cleanPasswordCharArray(password);
            httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);
            String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpGet,
                    OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
            if (log.isDebugEnabled()) {
                log.debug("Received response from GET updated API identifiers : " + response);
            }
            JSONParser parser = new JSONParser();
            JSONObject detailedAPIObj = (JSONObject) parser.parse(response);
            return (JSONArray) detailedAPIObj.get("API_IDs");
        } catch (OnPremiseGatewayException e) {
            throw new APISynchronizationException("An error occurred while retrieving identifiers of updated APIs.", e);
        } catch (ParseException e) {
            throw new APISynchronizationException("An error occurred while parsing the response to GET" +
                    " updated API identifiers.", e);
        }
    }

    /**
     * Method to obtain complete details of all updated APIs
     *
     * @param updatedApiIds  identifiers of updated APIs
     * @param accessTokenDTO access token DTO
     * @return A List of APIDTO objects
     */
    private List<APIDTO> getDetailsOfUpdatedAPIs(JSONArray updatedApiIds, AccessTokenDTO accessTokenDTO)
            throws APISynchronizationException {
        List<APIDTO> apiDtoList = new ArrayList<>();
        for (Object updatedApiId : updatedApiIds) {
            String id = updatedApiId.toString();
            String detailedAPIViewUrl = apiViewUrl + APISynchronizationConstants.URL_PATH_SEPARATOR + id;
            try {
                APIDTO apiDTO = getAPI(id, detailedAPIViewUrl, accessTokenDTO);
                // If a label is configured, only the APIs with the given label should be
                // synced out of the APIs that have been updated.
                if (StringUtils.isNotBlank(label)) {
                    for (LabelDTO labelDTO : apiDTO.getLabels()) {
                        if (label.equals(labelDTO.getName())) {
                            apiDtoList.add(apiDTO);
                            break;
                        }
                    }
                } else {
                    apiDtoList.add(apiDTO);
                }
            } catch (APISynchronizationException e) {
                log.error("An error occurred while retrieving details of API: " + id, e);
            }
        }
        return apiDtoList;
    }

    /**
     * Method to deploy custom sequences of an API
     *
     * @param api            APIDTO object
     * @param accessTokenDTO access token DTO
     */
    private void createCustomSequences(APIDTO api, AccessTokenDTO accessTokenDTO)
            throws APISynchronizationException {
        List<SequenceDTO> sequences = api.getSequences();
        if (sequences.size() > 0) {
            String apiId = api.getId();
            String mediationPolicyViewUrl = apiViewUrl + APISynchronizationConstants.URL_PATH_SEPARATOR
                    + apiId + APISynchronizationConstants.API_VIEW_MEDIATION_POLICY_PATH;

            Map<String, String> policies = new HashMap<>();
            try {
                String apiPublisherUrl = ConfigManager.getConfigManager()
                        .getProperty(OnPremiseGatewayConstants.API_PUBLISHER_URL_PROPERTY_KEY);
                if (apiPublisherUrl == null) {
                    apiPublisherUrl = OnPremiseGatewayConstants.DEFAULT_API_PUBLISHER_URL;
                    if (log.isDebugEnabled()) {
                        log.debug("Using default API publisher URL: " + apiPublisherUrl);
                    }
                }
                URL apiPublisherUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiPublisherUrl);
                HttpClient httpClient = APIUtil.getHttpClient(apiPublisherUrlValue.getPort(), apiPublisherUrlValue
                        .getProtocol());
                HttpGet httpGet = new HttpGet(mediationPolicyViewUrl);
                String authHeaderValue = OnPremiseGatewayConstants.AUTHORIZATION_BEARER +
                        accessTokenDTO.getAccessToken();
                httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);

                // Retrieve all API specific mediation policies from publisher REST API
                String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpGet,
                        OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);

                InputStream is = new ByteArrayInputStream(response.getBytes(
                        Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
                ObjectMapper mapper = new ObjectMapper();

                MediationListDTO mediationListDTO = mapper.readValue(is, MediationListDTO.class);
                List<MediationInfoDTO> mediationInfoList = mediationListDTO.getList();
                for (MediationInfoDTO mediationInfoObj : mediationInfoList) {
                    policies.put(mediationInfoObj.getName(), mediationInfoObj.getId());
                }

                // Sequences ids in API details present in api objects are different to those in policies map.
                // Hence using the policies map to identify the ids of required sequence using sequence names.
                for (SequenceDTO apiSequence : sequences) {
                    String name = apiSequence.getName();
                    String sequenceId = policies.get(name);
                    if (StringUtils.isBlank(sequenceId)) {
                        // If a matching sequence is not found among API specific mediation policies, look for a
                        // matching sequence among global API policies.
                        Map<String, String> globalPolicies = getGlobalLevelMediationPolicies(accessTokenDTO);
                        if (globalPolicies.size() > 0) {
                            sequenceId = globalPolicies.get(name);
                            if (StringUtils.isBlank(sequenceId)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("No matching policies were found for " +
                                            "custom sequence " + name + " of the API " + apiId);
                                }
                                continue;
                            }
                        }
                    }
                    // Deploying sequence
                    deploySequence(api, apiId, sequenceId, accessTokenDTO);
                }
            } catch (OnPremiseGatewayException e) {
                throw new APISynchronizationException("An error occurred while retrieving a summary of all " +
                        "API details of API " + apiId + " using publisher REST API.", e);
            } catch (IOException e) {
                throw new APISynchronizationException("An error occurred while de-serializing MediationListDTO " +
                        "object of API " + apiId + " from input stream.", e);
            } catch (APISynchronizationException e) {
                throw new APISynchronizationException("Error while deploying custom sequences of API " + apiId);
            }
        }
    }

    /**
     * Method to retrieve all the global level mediation policies
     *
     * @param accessTokenDTO Access token DTO
     * @return a Map of sequence-id and sequence-name pairs of mediation policies
     */
    private Map<String, String> getGlobalLevelMediationPolicies(AccessTokenDTO accessTokenDTO)
            throws APISynchronizationException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving global level mediation policies.");
        }
        Map<String, String> globalMediationSeq = new HashMap<>();
        try {
            String apiPublisherUrl = ConfigManager.getConfigManager()
                    .getProperty(OnPremiseGatewayConstants.API_PUBLISHER_URL_PROPERTY_KEY);
            if (apiPublisherUrl == null) {
                apiPublisherUrl = OnPremiseGatewayConstants.DEFAULT_API_PUBLISHER_URL;
                if (log.isDebugEnabled()) {
                    log.debug("Using default API publisher URL: " + apiPublisherUrl);
                }
            }
            URL apiPublisherUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiPublisherUrl);
            HttpClient httpClient = APIUtil.getHttpClient(apiPublisherUrlValue.getPort(), apiPublisherUrlValue
                    .getProtocol());
            HttpGet httpGet = new HttpGet(mediationPolicyUrl);
            String authHeaderValue = OnPremiseGatewayConstants.AUTHORIZATION_BEARER +
                    accessTokenDTO.getAccessToken();
            httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);

            String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpGet,
                    OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
            if (log.isDebugEnabled()) {
                log.debug("Received response from GET global level mediation policies : " + response);
            }

            InputStream is = new ByteArrayInputStream(response.getBytes(
                    Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
            ObjectMapper mapper = new ObjectMapper();

            MediationListDTO mediationListDTO = mapper.readValue(is, MediationListDTO.class);
            List<MediationInfoDTO> mediationInfoList = mediationListDTO.getList();
            for (MediationInfoDTO mediationInfoObj : mediationInfoList) {
                globalMediationSeq.put(mediationInfoObj.getName(), mediationInfoObj.getId());
            }
        } catch (OnPremiseGatewayException e) {
            throw new APISynchronizationException("An error occurred while retrieving global API sequences.", e);
        } catch (IOException e) {
            throw new APISynchronizationException("An error occurred while de-serializing MediationListDTO object " +
                    "from input stream.", e);
        }
        return globalMediationSeq;
    }

    /**
     * Method to deploy sequences of an API
     *
     * @param api            APIDTO object
     * @param seqId          id of the sequence to be recreated
     * @param accessTokenDTO access token DTO
     */
    private void deploySequence(APIDTO api, String apiId, String seqId, AccessTokenDTO accessTokenDTO)
            throws APISynchronizationException {
        String uri = apiViewUrl + APISynchronizationConstants.URL_PATH_SEPARATOR + apiId
                + APISynchronizationConstants.API_VIEW_MEDIATION_POLICY_PATH
                + APISynchronizationConstants.URL_PATH_SEPARATOR + seqId;
        try {
            String apiPublisherUrl = ConfigManager.getConfigManager()
                    .getProperty(OnPremiseGatewayConstants.API_PUBLISHER_URL_PROPERTY_KEY);
            if (apiPublisherUrl == null) {
                apiPublisherUrl = OnPremiseGatewayConstants.DEFAULT_API_PUBLISHER_URL;
                if (log.isDebugEnabled()) {
                    log.debug("Using default API publisher URL: " + apiPublisherUrl);
                }
            }
            URL apiPublisherUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiPublisherUrl);
            HttpClient httpClient = APIUtil.getHttpClient(apiPublisherUrlValue.getPort(), apiPublisherUrlValue
                    .getProtocol());
            HttpGet httpGet = new HttpGet(uri);
            String authHeaderValue = OnPremiseGatewayConstants.AUTHORIZATION_BEARER +
                    accessTokenDTO.getAccessToken();
            httpGet.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);

            // Retrieve all API specific mediation policies from publisher REST API
            String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpGet,
                    OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
            if (log.isDebugEnabled()) {
                log.debug("Received response from GET api sequence: " + seqId);
            }

            InputStream is = new ByteArrayInputStream(response.getBytes(
                    Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
            ObjectMapper mapper = new ObjectMapper();

            MediationDTO mediationDTO = mapper.readValue(is, MediationDTO.class);
            writeSequenceToFile(mediationDTO, api);
        } catch (OnPremiseGatewayException | IOException | APIManagementException e) {
            throw new APISynchronizationException("An error occurred while deploying custom sequences of API " + apiId,
                    e);
        }
    }

    /**
     * Method to write a custom sequence of an API to file
     *
     * @param sequenceInfo MediationDTO object
     * @param api          APIDTO object
     */
    private void writeSequenceToFile(MediationDTO sequenceInfo, APIDTO api) throws APIManagementException,
            APISynchronizationException {
        String seqFileName = APISynchronizationConstants.EMPTY_STRING;
        String tenantDomain = APISynchronizationConstants.EMPTY_STRING;
        String sequenceName = APISynchronizationConstants.SEQUENCE_NAME;
        try {
            String type = sequenceInfo.getType().name();
            String xmlStr = sequenceInfo.getConfig();
            String name = api.getName();

            // Sequences should be named according to the naming convention provider--apiName_version--sequenceType.xml
            // Provider should be set as Admin
            APIManagerConfiguration config = ServiceDataHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String provider = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);

            provider = provider.replaceAll("@", "-AT-");
            String version = api.getVersion();

            if ("in".equals(type)) {
                type = "In";
            } else if ("out".equals(type)) {
                type = "Out";
            }
            String seqElementName = provider + "--" + name + ":" + "v" + version + "--" + type;
            String seqName = provider + "--" + name + "_v" + version + "--" + type;
            seqFileName = seqName + ".xml";
            if (log.isDebugEnabled()) {
                log.debug("Starting to deploy sequence: " + seqName);
            }
            OMElement element = AXIOMUtil.stringToOM(xmlStr);

            String originalSeqName = element.getAttributeValue(new QName(sequenceName));
            String newXML = xmlStr.replace(originalSeqName, seqElementName);

            APIManagerConfiguration apimConfig = ServiceDataHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String username = apimConfig.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
            tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = ServiceDataHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            String path = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId + File.separator +
                    "synapse-configs" + File.separator + "default" + File.separator + "sequences" + File.separator;

            File dir = new File(path);
            File file = new File(dir, seqFileName);
            FileUtils.writeStringToFile(file, newXML);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed sequence: " + seqName);
            }
        } catch (UserStoreException e) {
            throw new APISynchronizationException("An error occurred while obtaining tenant identifier of " +
                    "tenant domain " + tenantDomain, e);
        } catch (XMLStreamException e) {
            throw new APISynchronizationException("There was an error in reading XML Stream of the file " + seqFileName,
                    e);
        } catch (FileNotFoundException e) {
            throw new APISynchronizationException("The file " + seqFileName + " could not be located.", e);
        } catch (IOException e) {
            throw new APISynchronizationException("An error occurred while reading the file " + seqFileName);
        }
    }

    /**
     * Method to convert a Document to string
     *
     * @param doc Document to be converted
     * @return string representation of the given document
     */
    private String convertDocumentToString(Document doc) throws APISynchronizationException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            throw new APISynchronizationException("An error occurred while transforming document to string.", e);
        }
    }

    /**
     * Method to convert a string to a Document
     *
     * @param xmlStr String to be converted to a Document
     * @return Document representation of the given string
     */
    private Document convertStringToDocument(String xmlStr) throws APISynchronizationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlStr)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new APISynchronizationException("An error occurred while transforming string to document.", e);
        }
    }

    /**
     * Method to load the configurations of a tenant
     */
    private void loadTenant() {
        APIManagerConfiguration config = ServiceDataHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            ConfigurationContext context = ServiceDataHolder.getInstance().getConfigurationContextService()
                    .getServerConfigContext();
            TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, context);
            if (log.isDebugEnabled()) {
                log.debug("Tenant was loaded into Carbon Context. Tenant : " + tenantDomain
                        + ", Username : " + username);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Skipping loading super tenant space since execution is currently in super tenant flow.");
            }
        }
    }
}
