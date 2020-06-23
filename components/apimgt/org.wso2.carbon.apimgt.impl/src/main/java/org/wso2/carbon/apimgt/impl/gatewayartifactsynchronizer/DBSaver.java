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
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.h2.engine.Constants.UTF8;

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
        String baseUrl = APIConstants.HTTPS_PROTOCOL_URL_PREFIX + System.getProperty(APIConstants.KEYMANAGER_HOSTNAME) + ":" +
                System.getProperty(APIConstants.KEYMANAGER_PORT) + APIConstants.INTERNAL_WEB_APP_EP;
        String synapsePost = baseUrl + APIConstants.GatewayArtifactSynchronizer.SYNAPSE_ARTIFACTS;
        HttpPost method = new HttpPost(synapsePost);
        URL synapsePostURL = null;
        try {
            synapsePostURL = new URL(synapsePost);
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
            String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
            byte[] credentials = Base64.encodeBase64((username + ":" + password).getBytes
                    (StandardCharsets.UTF_8));
            int keyMgtPort = synapsePostURL.getPort();
            String keyMgtProtocol = synapsePostURL.getProtocol();
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);

//            List<NameValuePair> urlParameters = new ArrayList<>();
//            urlParameters.add(new BasicNameValuePair("gatewayRuntimeArtifacts", gatewayRuntimeArtifacts));
//            urlParameters.add(new BasicNameValuePair("gatewayLabel", gatewayLabel));
//            urlParameters.add(new BasicNameValuePair("gatewayInstruction", gatewayInstruction));
//            method.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));

            String endcodedGatewayArtifacts= URLEncoder.encode(gatewayRuntimeArtifacts,UTF8);
            JSONObject revokeRequestPayload = new JSONObject();
            revokeRequestPayload.put("gatewayRuntimeArtifacts", endcodedGatewayArtifacts);
            revokeRequestPayload.put("gatewayLabel", gatewayLabel);
            revokeRequestPayload.put("gatewayInstruction", gatewayInstruction);
            StringEntity requestEntity = new StringEntity(revokeRequestPayload.toString());
            method.addHeader("content-type", "application/x-www-form-urlencoded");
            method.setEntity(requestEntity);

            HttpResponse httpResponse = null;
            httpResponse = httpClient.execute(method);
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                log.error("Error in storing files to the DB");
                throw new ArtifactSynchronizerException("Error in storing files to the DB");
            }
        } catch (MalformedURLException e) {
            String msg = "Error while constructing key manager URL ";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        } catch (IOException e) {
            String msg = "Error while executing the http client ";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        }
    }

    @Override
    public boolean isAPIPublished(String apiId) {

        try {
            return apiMgtDAO.isAPIPublishedInAnyGateway(apiId);
        } catch (APIManagementException e) {
            log.error("Error checking API with ID " + apiId + " is published in any gateway", e);
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
