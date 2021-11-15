/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.solace.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.AsyncProtocolEndpoint;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.ExternalEnvironment;
import org.wso2.carbon.apimgt.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the Solace environment object parsing tasks
 */
@Component(
        name = "solace.external.environment.component",
        immediate = true,
        service = ExternalEnvironment.class
)
public class SolaceEnvironmentImpl implements ExternalEnvironment {

    private static final Log log = LogFactory.getLog(SolaceEnvironmentImpl.class);

    /**
     * Get endpoint URLs of the Solace environment
     *
     * @return List of protocol endpoint URLs map of Solace
     */
    @Override
    public List<AsyncProtocolEndpoint> getExternalEndpointURLs(Environment environment) {
        SolaceAdminApis solaceAdminApis = new SolaceAdminApis(environment.getServerURL(),
                environment.getUserName(), environment.getPassword(), environment.
                getAdditionalProperties().get(SolaceConstants.SOLACE_ENVIRONMENT_DEV_NAME));
        HttpResponse response = solaceAdminApis.environmentGET(
                environment.getAdditionalProperties()
                        .get(SolaceConstants.SOLACE_ENVIRONMENT_ORGANIZATION), environment.getName());
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String responseString = null;
            try {
                responseString = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                log.error(e.getMessage());
            }

            JSONObject jsonObject = new JSONObject(responseString);
            if (jsonObject.has("messagingProtocols")) {
                JSONArray protocols = jsonObject.getJSONArray("messagingProtocols");
                List<AsyncProtocolEndpoint> asyncProtocolEndpoints = new ArrayList<>();
                for (int i = 0; i < protocols.length(); i++) {
                    JSONObject protocolDetails = protocols.getJSONObject(i);
                    String protocolName = protocolDetails.getJSONObject("protocol").getString("name");
                    String endpointURI = protocolDetails.getString("uri");
                    AsyncProtocolEndpoint asyncProtocolEndpoint = new AsyncProtocolEndpoint();
                    asyncProtocolEndpoint.setProtocol(protocolName);
                    asyncProtocolEndpoint.setProtocolUrl(endpointURI);
                    asyncProtocolEndpoints.add(asyncProtocolEndpoint);
                }
                return asyncProtocolEndpoints;
            }
        }
        return null;
    }

    /**
     * Get provider of the environment as Solace
     *
     * @return String Solace type
     */
    @Override
    public String getType() {
        return SolaceConstants.SOLACE_ENVIRONMENT;
    }
}
