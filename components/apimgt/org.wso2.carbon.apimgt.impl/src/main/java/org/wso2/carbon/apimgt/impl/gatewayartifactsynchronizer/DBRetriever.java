/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBRetriever implements ArtifactRetriever {

    private static final Log log = LogFactory.getLog(DBRetriever.class);
    protected EventHubConfigurationDto eventHubConfigurationDto = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfigurationService().getAPIManagerConfiguration().getEventHubConfigurationDto();
    protected GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
            ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                    .getGatewayArtifactSynchronizerProperties();
    private String baseURL = eventHubConfigurationDto.getServiceUrl() + APIConstants.INTERNAL_WEB_APP_EP;

    @Override
    public void init() throws ArtifactSynchronizerException {
        //not required
    }

    @Override
    public String retrieveArtifact(String apiId, String gatewayLabel)
            throws ArtifactSynchronizerException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (gatewayArtifactSynchronizerProperties.hasEventWaitingTime()) {
            try {
                Thread.sleep(gatewayArtifactSynchronizerProperties.getEventWaitingTime());
            } catch (InterruptedException e) {
                log.error("Error occurred while waiting to retrieve artifacts from event hub");
            }
        }
        try {
            String encodedGatewayLabel = URLEncoder.encode(gatewayLabel, APIConstants.DigestAuthConstants.CHARSET);
            encodedGatewayLabel = encodedGatewayLabel.replace("\\+", "%20");
            String path = APIConstants.GatewayArtifactSynchronizer.GATEAY_SYNAPSE_ARTIFACTS + "?apiId=" + apiId +
                    "&gatewayLabel=" + encodedGatewayLabel + "&type=Synapse";
            String endpoint = baseURL + path;
            try (CloseableHttpResponse httpResponse = invokeService(endpoint, tenantDomain)) {
                JSONArray jsonArray = retrieveArtifact(httpResponse);
                if (jsonArray != null && jsonArray.length() > 0) {
                    return jsonArray.getString(0);
                }
            }
        } catch (IOException e) {
            String msg = "Error while executing the http client";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        }
        return null;
    }

    private JSONArray retrieveArtifact(CloseableHttpResponse httpResponse)
            throws IOException, ArtifactSynchronizerException {

        String gatewayRuntimeArtifact;
        if (httpResponse.getStatusLine().getStatusCode() == 404) {
            log.info("No artifacts available to deploy");
            return new JSONArray();
        } else if (httpResponse.getStatusLine().getStatusCode() == 200) {
            if (httpResponse.getEntity() != null) {
                gatewayRuntimeArtifact = EntityUtils.toString(httpResponse.getEntity(),
                        APIConstants.DigestAuthConstants.CHARSET);
                JSONObject artifactObject = new JSONObject(gatewayRuntimeArtifact);
                return (JSONArray) artifactObject.get("list");
            } else {
                throw new ArtifactSynchronizerException("HTTP response is empty");
            }
        } else {
            String errorMessage = EntityUtils.toString(httpResponse.getEntity(),
                    APIConstants.DigestAuthConstants.CHARSET);
            throw new ArtifactSynchronizerException(errorMessage + "Event-Hub status code is : "
                    + httpResponse.getStatusLine().getStatusCode());
        }
    }

    @Override
    public List<String> retrieveAllArtifacts(String label, String tenantDomain) throws ArtifactSynchronizerException {
        List<String> gatewayRuntimeArtifactsArray = new ArrayList<>();
        try {
            String endcodedgatewayLabel = URLEncoder.encode(label, APIConstants.DigestAuthConstants.CHARSET);
            String path = APIConstants.GatewayArtifactSynchronizer.GATEAY_SYNAPSE_ARTIFACTS
                    + "?gatewayLabel=" + endcodedgatewayLabel + "&type=Synapse";
            String endpoint = baseURL + path;
            try (CloseableHttpResponse httpResponse = invokeService(endpoint,tenantDomain)) {
                JSONArray jsonArray = retrieveArtifact(httpResponse);
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        gatewayRuntimeArtifactsArray.add(jsonArray.getString(i));
                    }
                }

            }
            return gatewayRuntimeArtifactsArray;
        } catch (IOException e) {
            String msg = "Error while executing the http client";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        } catch (ArtifactSynchronizerException e) {
            String msg = "Error while retrieving artifacts";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e, ExceptionCodes.ARTIFACT_SYNC_HTTP_REQUEST_FAILED);
        }
    }

    @Override
    public Map<String, String> retrieveAttributes(String apiName, String version, String tenantDomain)
            throws ArtifactSynchronizerException {

        CloseableHttpResponse httpResponse = null;
        try {
            String endcodedVersion = URLEncoder.encode(version, APIConstants.DigestAuthConstants.CHARSET);
            String path = APIConstants.GatewayArtifactSynchronizer.SYNAPSE_ATTRIBUTES + "?apiName=" + apiName +
                    "&tenantDomain=" + tenantDomain + "&version=" + endcodedVersion;
            String endpoint = baseURL + path;
            httpResponse = invokeService(endpoint, tenantDomain);
            String responseString;
            if (httpResponse.getEntity() != null) {
                responseString = EntityUtils.toString(httpResponse.getEntity(),
                        APIConstants.DigestAuthConstants.CHARSET);
                httpResponse.close();
            } else {
                throw new ArtifactSynchronizerException("HTTP response is empty");
            }
            Map<String, String> apiAttribute = new HashMap<>();

            JSONObject artifactObject = new JSONObject(responseString);
            String label = null;
            String apiId = null;
            try {
                apiId = (String) artifactObject.get(APIConstants.GatewayArtifactSynchronizer.API_ID);
                String labelsStr = artifactObject.get(APIConstants.GatewayArtifactSynchronizer.LABELS).toString();

                Set<String> labelsSet = new Gson().fromJson(labelsStr, new TypeToken<HashSet<String>>() {
                }.getType());
                Set<String> gatewaySubscribedLabel = gatewayArtifactSynchronizerProperties.getGatewayLabels();
                if (!labelsSet.isEmpty() || !gatewaySubscribedLabel.isEmpty()) {
                    labelsSet.retainAll(gatewaySubscribedLabel);
                    if (!labelsSet.isEmpty()) {
                        label = labelsSet.iterator().next();
                    }
                }
            } catch (ClassCastException e) {
                log.error("Unexpected response received from the storage." + e.getMessage());
            }

            apiAttribute.put(APIConstants.GatewayArtifactSynchronizer.API_ID, apiId);
            apiAttribute.put(APIConstants.GatewayArtifactSynchronizer.LABEL, label);
            return apiAttribute;
        } catch (IOException e) {
            String msg = "Error while executing the http client";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        }
    }

    private CloseableHttpResponse invokeService(String endpoint,String tenantDomain) throws IOException,
            ArtifactSynchronizerException {

        HttpGet method = new HttpGet(endpoint);
        URL url = new URL(endpoint);
        String username = eventHubConfigurationDto.getUsername();
        String password = eventHubConfigurationDto.getPassword();
        byte[] credentials = Base64.encodeBase64((username + APIConstants.DELEM_COLON + password).
                getBytes(APIConstants.DigestAuthConstants.CHARSET));
        int port = url.getPort();
        String protocol = url.getProtocol();
        method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC
                + new String(credentials, APIConstants.DigestAuthConstants.CHARSET));
        if (tenantDomain != null) {
            method.setHeader(APIConstants.HEADER_TENANT, tenantDomain);
        }

        HttpClient httpClient = APIUtil.getHttpClient(port, protocol);
        try {
            return APIUtil.executeHTTPRequestWithRetries(method, httpClient);
        } catch (APIManagementException e) {
            throw new ArtifactSynchronizerException(e);
        }
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public String getName() {

        return APIConstants.GatewayArtifactSynchronizer.DB_RETRIEVER_NAME;
    }
}
