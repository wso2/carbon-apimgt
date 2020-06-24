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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
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
    public static final int retrievalTimeoutInSeconds = 15;
    public static final int retrievalRetries = 2;

    @Override
    public void init() throws ArtifactSynchronizerException {
        //not required
    }

    @Override
    public String retrieveArtifact(String APIId, String gatewayLabel, String gatewayInstruction)
            throws ArtifactSynchronizerException {
        try {
            String baseURL = APIConstants.HTTPS_PROTOCOL_URL_PREFIX +
                    System.getProperty(APIConstants.KEYMANAGER_HOSTNAME) + ":" +
                    System.getProperty(APIConstants.KEYMANAGER_PORT) + APIConstants.INTERNAL_WEB_APP_EP;
            String endcodedgatewayLabel= URLEncoder.encode(gatewayLabel, APIConstants.DigestAuthConstants.CHARSET);
            String path  = APIConstants.GatewayArtifactSynchronizer.SYNAPSE_ARTIFACTS + "?apiId=" + APIId +
                    "&gatewayInstruction=" + gatewayInstruction +"&gatewayLabel="+ endcodedgatewayLabel;
            String endpoint = baseURL + path;
            HttpResponse httpResponse = invokeService(endpoint);
            return EntityUtils.toString(httpResponse.getEntity(), APIConstants.DigestAuthConstants.CHARSET);
        } catch (IOException e) {
            String msg = "Error while executing the http client " ;
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        }
    }

    @Override
    public List<String> retrieveAllArtifacts(String label) throws ArtifactSynchronizerException {
        List<String> gatewayRuntimeArtifactsArray = new ArrayList<>();
        try {
            String baseURL = APIConstants.HTTPS_PROTOCOL_URL_PREFIX + System.getProperty(APIConstants.KEYMANAGER_HOSTNAME) + ":" +
                    System.getProperty(APIConstants.KEYMANAGER_PORT) + APIConstants.INTERNAL_WEB_APP_EP;
            String endcodedgatewayLabel= URLEncoder.encode(label, APIConstants.DigestAuthConstants.CHARSET);
            String path  = APIConstants.GatewayArtifactSynchronizer.GATEAY_SYNAPSE_ARTIFACTS
                    + "?gatewayLabel="+ endcodedgatewayLabel;
            String endpoint = baseURL + path;
            HttpResponse httpResponse = invokeService(endpoint);
            String responseString = EntityUtils.toString(httpResponse.getEntity(), APIConstants.DigestAuthConstants.CHARSET);
            JSONObject artifactObject = new JSONObject(responseString);
            JSONArray jArray = (JSONArray)artifactObject.get("list");
            if (jArray != null) {
                for (int i = 0; i < jArray.length(); i++) {
                    gatewayRuntimeArtifactsArray.add(jArray.get(i).toString());
                }
            }
            return gatewayRuntimeArtifactsArray;
        } catch (IOException e) {
            String msg = "Error while executing the http client " ;
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        }
    }

    private HttpResponse invokeService(String endpoint) throws IOException, ArtifactSynchronizerException {
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
        HttpResponse httpResponse = null;
        int retryCount = 0;
        boolean retry = false;
        do {
            try {
                httpResponse = httpClient.execute(method);
                retry = false;
            } catch (IOException ex) {
                retryCount++;
                if (retryCount < retrievalRetries) {
                    retry = true;
                    log.warn("Failed retrieving " + endpoint + " from remote endpoint: " + ex.getMessage()
                            + ". Retrying after " + retrievalTimeoutInSeconds +
                            " seconds.");
                    try {
                        Thread.sleep(retrievalTimeoutInSeconds * 1000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                } else {
                    throw ex;
                }
            }
        } while (retry);
        if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
            log.error("Retrieving artifacts is unsuccessful");
            throw new ArtifactSynchronizerException("Error while retrieving artifacts");
        }
        return httpResponse;
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
