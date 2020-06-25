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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class DBSaver implements ArtifactSaver {

    private static final Log log = LogFactory.getLog(DBSaver.class);
    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public void init() throws ArtifactSynchronizerException {
        //not required
    }

    @Override
    public void saveArtifact(String gatewayRuntimeArtifacts, String gatewayLabel, String gatewayInstruction)
            throws ArtifactSynchronizerException {

        JSONObject artifactObject = new JSONObject(gatewayRuntimeArtifacts);
        String apiId = (String) artifactObject.get("apiId");
        String apiName = (String) artifactObject.get("name");
        String version = (String) artifactObject.get("version");
        String tenantDomain = (String) artifactObject.get("tenantDomain");
        byte[] bytesEncoded =  Base64.encodeBase64(gatewayRuntimeArtifacts.getBytes());
        String bytesEncodedAsString =  new String(bytesEncoded);

        try {
            String baseUrl = APIConstants.HTTPS_PROTOCOL_URL_PREFIX +
                    System.getProperty(APIConstants.KEYMANAGER_HOSTNAME) + ":" +
                    System.getProperty(APIConstants.KEYMANAGER_PORT) + APIConstants.INTERNAL_WEB_APP_EP;
            String synapsePost = baseUrl + APIConstants.GatewayArtifactSynchronizer.SYNAPSE_ARTIFACTS;

            JSONObject revokeRequestPayload = new JSONObject();
            revokeRequestPayload.put("apiId", apiId);
            revokeRequestPayload.put("apiName", apiName);
            revokeRequestPayload.put("version", version);
            revokeRequestPayload.put("tenantDomain", tenantDomain);
            revokeRequestPayload.put("gatewayInstruction", gatewayInstruction);
            revokeRequestPayload.put("bytesEncodedAsString", bytesEncodedAsString);
            revokeRequestPayload.put("gatewayLabel", URLEncoder.encode(gatewayLabel,
                    APIConstants.DigestAuthConstants.CHARSET));
            HttpResponse httpResponse = invokeService(synapsePost,revokeRequestPayload);
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                log.error("Retrieving artifacts is UnSuccessful. Internal Data Service returned HTTP Status " +
                        "code : " + httpResponse.getStatusLine().getStatusCode() );
                throw new ArtifactSynchronizerException("Error while Saving Artifacts to DB");
            }
        } catch (MalformedURLException e) {
            String msg = "Error while constructing Synapse POST URL";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        } catch (IOException e) {
            String msg = "Error while executing the http client";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        }
    }

    private HttpResponse invokeService(String endpoint, JSONObject revokeRequestPayload) throws
            IOException {
        URL synapsePostURL = new URL(endpoint);
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
        byte[] credentials = Base64.encodeBase64((username + ":" + password).getBytes
                (APIConstants.DigestAuthConstants.CHARSET));
        int synapsePort = synapsePostURL.getPort();
        String synapseProtocol = synapsePostURL.getProtocol();
        HttpClient httpClient = APIUtil.getHttpClient(synapsePort, synapseProtocol);

        if (revokeRequestPayload != null) {
             HttpPost method = new HttpPost(endpoint);
             method.setHeader("Authorization", "Basic " + new String(credentials,
                    APIConstants.DigestAuthConstants.CHARSET));
             StringEntity requestEntity = new StringEntity(revokeRequestPayload.toString());
             requestEntity.setContentType(APIConstants.APPLICATION_JSON_MEDIA_TYPE);
             method.setEntity(requestEntity);
             return APIUtil.executeHTTPRequest(method, httpClient);

        } else {
             HttpGet method = new HttpGet(endpoint);
             method.setHeader("Authorization", "Basic " + new String(credentials,
                    APIConstants.DigestAuthConstants.CHARSET));
             return APIUtil.executeHTTPRequest(method, httpClient);
        }
    }

    @Override
    public boolean isAPIPublished(String apiId){
        try {
            String baseURL = APIConstants.HTTPS_PROTOCOL_URL_PREFIX +
                    System.getProperty(APIConstants.KEYMANAGER_HOSTNAME) + ":" +
                    System.getProperty(APIConstants.KEYMANAGER_PORT) + APIConstants.INTERNAL_WEB_APP_EP;
            String path  = APIConstants.GatewayArtifactSynchronizer.IS_API_PUBLISHED + "?apiId=" + apiId;
            String endpoint = baseURL + path;
            HttpResponse httpResponse = invokeService(endpoint,null);
            String responseString = EntityUtils.toString(httpResponse.getEntity(),
                    APIConstants.DigestAuthConstants.CHARSET);
            if (responseString.equals("true")) {
                return true;
            }
            return false;
        } catch (IOException e) {
            String msg = "Error while executing the http client " ;
            log.error(msg, e);
        }
        return false;
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public String getName() {
        return APIConstants.GatewayArtifactSynchronizer.DB_SAVER_NAME;
    }
}
