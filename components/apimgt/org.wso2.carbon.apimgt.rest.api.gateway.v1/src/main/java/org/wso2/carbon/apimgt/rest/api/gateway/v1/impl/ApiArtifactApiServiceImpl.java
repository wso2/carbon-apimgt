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

package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.gateway.utils.EndpointAdminServiceProxy;
import org.wso2.carbon.apimgt.gateway.utils.LocalEntryServiceProxy;
import org.wso2.carbon.apimgt.gateway.utils.SequenceAdminServiceProxy;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.ApiArtifactApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.endpoint.EndpointAdminException;


import javax.ws.rs.core.Response;

public class ApiArtifactApiServiceImpl implements ApiArtifactApiService {

    private static final Log log = LogFactory.getLog(ApiArtifactApiServiceImpl.class);

    public Response apiArtifactGet(String apiName, String label, String apiId, MessageContext messageContext) {

        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        GatewayAPIDTO gatewayAPIDTO = inMemoryApiDeployer.getAPIArtifact(apiId, label);
        String definition = null;
        JSONObject responseObj = new JSONObject();

        if (gatewayAPIDTO != null) {
            try {
                if (gatewayAPIDTO.getEndpointEntriesToBeAdd() != null ) {
                    JSONArray endPointArray = new JSONArray();
                    EndpointAdminServiceProxy endpointAdminServiceProxy = new EndpointAdminServiceProxy
                            (gatewayAPIDTO.getTenantDomain());
                    for (GatewayContentDTO gatewayEndpoint : gatewayAPIDTO.getEndpointEntriesToBeAdd()) {
                        if (endpointAdminServiceProxy.isEndpointExist(gatewayEndpoint.getName())) {
                            endPointArray.put(endpointAdminServiceProxy.getEndpoints(gatewayEndpoint.getName()));
                        } else {
                            log.error(gatewayEndpoint.getName() + " was not deployed in the gateway");
                        }
                    }
                    responseObj.put("Endpoints", endPointArray);
                }

                if (gatewayAPIDTO.getLocalEntriesToBeAdd() != null) {
                    JSONArray localEntryArray = new JSONArray();
                    LocalEntryServiceProxy localEntryServiceProxy = new
                            LocalEntryServiceProxy(gatewayAPIDTO.getTenantDomain());
                    for (GatewayContentDTO localEntry : gatewayAPIDTO.getLocalEntriesToBeAdd()) {
                        if (localEntryServiceProxy.isEntryExists(localEntry.getName())) {
                            localEntryArray.put(localEntryServiceProxy.getEntry(localEntry.getName()));
                        } else {
                            log.error(localEntry.getName() + " was not deployed in the gateway");
                        }
                    }
                    responseObj.put("Local Entries", localEntryArray);
                }

                if (gatewayAPIDTO.getSequenceToBeAdd() != null ) {
                    JSONArray sequencesArray = new JSONArray();
                    SequenceAdminServiceProxy sequenceAdminServiceProxy =
                            new SequenceAdminServiceProxy(gatewayAPIDTO.getTenantDomain());
                    for (GatewayContentDTO sequence : gatewayAPIDTO.getSequenceToBeAdd()) {
                        if(sequenceAdminServiceProxy.isExistingSequence(sequence.getName())) {
                            sequencesArray.put(sequenceAdminServiceProxy.getSequence(sequence.getName()));
                        } else {
                            log.error(sequence.getName() + " was not deployed in the gateway");
                        }
                    }
                    responseObj.put("Sequences", sequencesArray);
                }
            } catch (EndpointAdminException e) {
                log.error("Error in fetching deployed Endpoints from Synapse Configuration." , e);
            } catch (AxisFault axisFault) {
                log.error("Error in fetching deployed artifacts from Synapse Configuration." , axisFault);
            }

            responseObj.put("Definition", definition);
            String responseStringObj = String.valueOf(responseObj);
            return Response.ok().entity(responseStringObj).build();
        } else {
            responseObj.put("Message", "Error");
            String responseStringObj = String.valueOf(responseObj);
            return Response.serverError().entity(responseStringObj).build();
        }
    }
}
