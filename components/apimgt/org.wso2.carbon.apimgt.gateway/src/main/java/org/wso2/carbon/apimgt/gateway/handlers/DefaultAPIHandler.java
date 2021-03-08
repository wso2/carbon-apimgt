/*
 *Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.ApiUtils;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketConstants;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Default API Handler to handle Default Version.
 */
public class DefaultAPIHandler extends AbstractSynapseHandler {
    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        if (messageContext.getPropertyKeySet().contains(InboundWebsocketConstants.WEBSOCKET_SUBSCRIBER_PATH)) {
            return true;
        }
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String path = ApiUtils.getFullRequestPath(messageContext);
        TreeMap<String, API> selectedAPIS = Utils.getSelectedAPIList(path, GatewayUtils.getTenantDomain());
        if (selectedAPIS.size() > 0) {
            Object transportInUrl = axis2MessageContext.getProperty(APIConstants.TRANSPORT_URL_IN);
            String selectedPath = selectedAPIS.firstKey();
            API selectedAPI = selectedAPIS.get(selectedPath);
            if (transportInUrl instanceof String && StringUtils.isNotEmpty((String) transportInUrl)) {
                String updatedTransportInUrl = ((String) transportInUrl).replaceFirst(selectedPath,
                        selectedAPI.getContext());
                axis2MessageContext.setProperty(APIConstants.TRANSPORT_URL_IN, updatedTransportInUrl);
            }
            messageContext.getPropertyKeySet().remove(RESTConstants.REST_FULL_REQUEST_PATH);
        }
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        return true;
    }
}
