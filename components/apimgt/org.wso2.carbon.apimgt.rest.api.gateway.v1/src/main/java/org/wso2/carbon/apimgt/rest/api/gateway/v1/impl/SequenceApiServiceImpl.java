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
import org.wso2.carbon.apimgt.gateway.utils.SequenceAdminServiceProxy;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.core.Response;

public class SequenceApiServiceImpl implements SequenceApiService {

    private static final Log log = LogFactory.getLog(SequenceApiServiceImpl.class);

    public Response sequenceGet(String apiName, String label, String apiId, MessageContext messageContext) {

        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        GatewayAPIDTO gatewayAPIDTO = inMemoryApiDeployer.getAPIArtifact(apiId, label);

        JSONObject responseObj = new JSONObject();
        JSONArray sequencesArray = new JSONArray();
        if (gatewayAPIDTO != null) {
            SequenceAdminServiceProxy sequenceAdminServiceProxy =
                    new SequenceAdminServiceProxy(gatewayAPIDTO.getTenantDomain());
            if (gatewayAPIDTO.getSequenceToBeAdd() != null) {
                for (GatewayContentDTO sequence : gatewayAPIDTO.getSequenceToBeAdd()) {
                    try {
                        if(sequenceAdminServiceProxy.getSequence(sequence.getName()) != null) {
                            sequencesArray.put(sequenceAdminServiceProxy.getSequence(sequence.getName()));
                        }
                    } catch (AxisFault axisFault) {
                        log.error("Error in fetching deployed sequences from Synapse Configuration." , axisFault);
                    }
                }
            }
            responseObj.put("Sequence", sequencesArray);
            String responseStringObj = String.valueOf(responseObj);
            return Response.ok().entity(responseStringObj).build();
        } else {
            responseObj.put("Message", "Error");
            String responseStringObj = String.valueOf(responseObj);
            return Response.serverError().entity(responseStringObj).build();
        }
    }
}
