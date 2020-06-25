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

import com.google.common.io.ByteStreams;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBRetriever implements ArtifactRetriever {

    private static final Log log = LogFactory.getLog(DBRetriever.class);
    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public void init() throws ArtifactSynchronizerException {
        //not required
    }

    @Override
    public String retrieveArtifact(String APIId, String gatewayLabel, String gatewayInstruction)
            throws ArtifactSynchronizerException {

        String gatewayRuntimeArtifacts;
        try {
            ByteArrayInputStream byteStream =
                    apiMgtDAO.getGatewayPublishedAPIArtifacts(APIId, gatewayLabel, gatewayInstruction);
            byte[] bytes = ByteStreams.toByteArray(byteStream);
            gatewayRuntimeArtifacts = new String(bytes);
            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved Artifact of " + APIId);
            }
        } catch (APIManagementException | IOException e) {
            throw new ArtifactSynchronizerException("Error retrieving Artifact belongs to  " + APIId + " from DB", e);
        }
        return gatewayRuntimeArtifacts;
    }

    @Override
    public  Map <String, String> retrieveAttributes(String apiName, String version, String tenantDomain)
            throws ArtifactSynchronizerException {

        try {
            String baseURL = APIConstants.HTTPS_PROTOCOL_URL_PREFIX +
                    System.getProperty(APIConstants.KEYMANAGER_HOSTNAME) + ":" +
                    System.getProperty(APIConstants.KEYMANAGER_PORT) + APIConstants.INTERNAL_WEB_APP_EP;
            String endcodedVersion= URLEncoder.encode(version, APIConstants.DigestAuthConstants.CHARSET);
            String path = APIConstants.GatewayArtifactSynchronizer.SYNAPSE_ARTIFACTS + "?apiName=" + apiName +
                    "&version=" + endcodedVersion+ "&tenantDomain="+ tenantDomain;
            String endpoint = baseURL + path;
            HttpResponse httpResponse = invokeService(endpoint);
            String responseString;
            if (httpResponse.getEntity() != null ) {
                responseString = EntityUtils.toString(httpResponse.getEntity(),
                        APIConstants.DigestAuthConstants.CHARSET);
            } else {
                throw new ArtifactSynchronizerException("HTTP response is empty");
            }
            Map <String, String> apiAttribute = new HashMap<>();
            JSONObject artifactObject = new JSONObject(responseString);
            String apiId = (String)artifactObject.get("apiId");
            String label = (String)artifactObject.get("label");
            apiAttribute.put("apiId", apiId);
            apiAttribute.put("label", label);
            return apiAttribute;
        } catch (IOException e) {
            String msg = "Error while executing the http client";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        }
    }

    private HttpResponse invokeService(String endpoint) throws IOException {
        HttpGet method = new HttpGet(endpoint);
        URL synapseGetURL = new URL(endpoint);
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
        byte[] credentials = Base64.encodeBase64((username + ":" + password).getBytes
                (APIConstants.DigestAuthConstants.CHARSET));
        int synapsePort = synapseGetURL .getPort();
        String synapseProtocol = synapseGetURL .getProtocol();
        method.setHeader("Authorization", "Basic " + new String(credentials,
                APIConstants.DigestAuthConstants.CHARSET));
        HttpClient httpClient = APIUtil.getHttpClient(synapsePort, synapseProtocol);
        return  APIUtil.executeHTTPRequest(method, httpClient);
    }

    @Override
    public List<String> retrieveAllArtifacts(String label) throws ArtifactSynchronizerException {
        List<String> gatewayRuntimeArtifactsArray = new ArrayList<>();
        try {
            List<ByteArrayInputStream> baip = apiMgtDAO.getAllGatewayPublishedAPIArtifacts(label);
            for (ByteArrayInputStream byteStream :baip){
                byte[] bytes = ByteStreams.toByteArray(byteStream);
                String  gatewayRuntimeArtifacts = new String(bytes);
                gatewayRuntimeArtifactsArray.add(gatewayRuntimeArtifacts);
            }
            if (log.isDebugEnabled()){
                log.debug("Successfully retrieved Artifacts from DB");
            }
        } catch (APIManagementException | IOException e) {
            throw new ArtifactSynchronizerException("Error retrieving Artifact from DB", e);
        }
        return gatewayRuntimeArtifactsArray;
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
