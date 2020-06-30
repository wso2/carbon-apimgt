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


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DBRetriever implements ArtifactRetriever {

    private static final Log log = LogFactory.getLog(DBRetriever.class);
    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
    protected EventHubConfigurationDto eventHubConfigurationDto = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfigurationService().getAPIManagerConfiguration().getEventHubConfigurationDto();
    private String baseURL = eventHubConfigurationDto.getServiceUrl();

    @Override
    public void init() throws ArtifactSynchronizerException {
        //not required
    }

    @Override
    public String retrieveArtifact(String APIId, String gatewayLabel, String gatewayInstruction)
            throws ArtifactSynchronizerException {
        CloseableHttpResponse httpResponse = null;
        try {
            String baseURL = eventHubConfigurationDto.getServiceUrl();
            String endcodedgatewayLabel= URLEncoder.encode(gatewayLabel, APIConstants.DigestAuthConstants.CHARSET);
            String path = APIConstants.GatewayArtifactSynchronizer.SYNAPSE_ARTIFACTS + "?apiId=" + APIId +
                    "&gatewayInstruction=" + gatewayInstruction + "&gatewayLabel="+ endcodedgatewayLabel;
            String endpoint = baseURL + path;
            httpResponse = invokeService(endpoint);
            String gatewayRuntimeArtifact = null;
            if (httpResponse.getEntity() != null ) {
                gatewayRuntimeArtifact = EntityUtils.toString(httpResponse.getEntity(),
                        APIConstants.DigestAuthConstants.CHARSET);
                httpResponse.close();
            } else {
                throw new ArtifactSynchronizerException("HTTP response is empty");
            }
            return gatewayRuntimeArtifact;
        } catch (IOException e) {
            String msg = "Error while executing the http client";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        }
    }

    @Override
    public List<String> retrieveAllArtifacts(String label) throws ArtifactSynchronizerException {
        List<String> gatewayRuntimeArtifactsArray = new ArrayList<>();
        CloseableHttpResponse httpResponse = null;
        try {
            String endcodedgatewayLabel= URLEncoder.encode(label, APIConstants.DigestAuthConstants.CHARSET);
            String path = APIConstants.GatewayArtifactSynchronizer.GATEAY_SYNAPSE_ARTIFACTS
                    + "?gatewayLabel="+ endcodedgatewayLabel;
            String endpoint = baseURL + path;
            httpResponse = invokeService(endpoint);
            String responseString;
            if (httpResponse.getEntity() != null ) {
                responseString = EntityUtils.toString(httpResponse.getEntity(),
                        APIConstants.DigestAuthConstants.CHARSET);
                httpResponse.close();
            } else {
                throw new ArtifactSynchronizerException("HTTP response is empty");
            }
            JSONObject artifactObject = new JSONObject(responseString);
            JSONArray jArray = (JSONArray)artifactObject.get("list");
            if (jArray != null) {
                for (int i = 0; i < jArray.length(); i++) {
                    gatewayRuntimeArtifactsArray.add(jArray.get(i).toString());
                }
            }
            return gatewayRuntimeArtifactsArray;
        } catch (IOException e) {
            String msg = "Error while executing the http client";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        }
    }

    private CloseableHttpResponse invokeService(String endpoint) throws IOException {
        HttpGet method = new HttpGet(endpoint);
        URL url = new URL(endpoint);
        String username = eventHubConfigurationDto.getUsername();
        String password = eventHubConfigurationDto.getPassword();
        byte[] credentials = Base64.encodeBase64((username + APIConstants.DELEM_COLON + password).
                getBytes(APIConstants.DigestAuthConstants.CHARSET));
        int port = url.getPort();
        String protocol = url.getProtocol();
        method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT , APIConstants.AUTHORIZATION_BASIC
                + new String(credentials, APIConstants.DigestAuthConstants.CHARSET));
        HttpClient httpClient = APIUtil.getHttpClient(port, protocol);
        return  APIUtil.executeHTTPRequest(method, httpClient);
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
