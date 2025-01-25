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

import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.ApiUtils;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.constants.HealthCheckConstants;
import org.wso2.carbon.apimgt.common.gateway.constants.JWTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketConstants;

import java.util.TreeMap;

/**
 * Default API Handler to handle Default Version.
 */
public class DefaultAPIHandler extends AbstractSynapseHandler {
    private static final Log log = LogFactory.getLog(DefaultAPIHandler.class);
    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        if (messageContext.getPropertyKeySet().contains(InboundWebsocketConstants.WEBSOCKET_SUBSCRIBER_PATH)) {
            return true;
        }
        log.info("-----------------------------");
        String u;
        Object obj = messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        if (obj != null) {
            u = (String) obj;
            log.info("++++++ obj + " + u);
        }
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        String u2 = (String) msgCtx.getProperty(Constants.Configuration.TRANSPORT_IN_URL);
        log.info("+++++++ u2 bef " +  u2);
        if (u2 == null) {
            u2 = (String) messageContext.getProperty(NhttpConstants.SERVICE_PREFIX) + ": SERVICE_PREFIX";
            log.info("+++++++ u2 in " +  u2);
        }
        
        log.info("-----------------------------");
        String path = ApiUtils.getFullRequestPath(messageContext);
        String tenantDomain = GatewayUtils.getTenantDomain();

        // Handle JWKS API calls
        boolean isJWKSEndpoint = false;
        if (APIConstants.SUPER_TENANT_DOMAIN.equalsIgnoreCase(tenantDomain)) {
            if (path.equals(JWTConstants.GATEWAY_JWKS_API_CONTEXT)) {
                isJWKSEndpoint = true;
            }
        } else {
            if (path.equals(APIConstants.TENANT_PREFIX + tenantDomain + JWTConstants.GATEWAY_JWKS_API_CONTEXT)) {
                isJWKSEndpoint = true;
            }
        }

        boolean isJWKSApiEnabled =  ServiceReferenceHolder
                .getInstance().getAPIManagerConfiguration().getJwtConfigurationDto().isJWKSApiEnabled();

        if (isJWKSEndpoint && isJWKSApiEnabled) {
            try {
                InMemoryAPIDeployer.deployJWKSSynapseAPI(tenantDomain);
            } catch(APIManagementException e){
                log.error("Error while deploying JWKS API for tenant domain :" + tenantDomain, e);
            }
            return true;
        }

        if (GatewayUtils.checkForFileBasedApiContexts(path, tenantDomain)) {
            return true;
        }

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        TreeMap<String, API> selectedAPIS = Utils.getSelectedAPIList(path, tenantDomain);

        if (selectedAPIS.size() > 0) {
            Object transportInUrl = axis2MessageContext.getProperty(APIConstants.TRANSPORT_URL_IN);
            String selectedPath = selectedAPIS.firstKey();
            API selectedAPI = selectedAPIS.get(selectedPath);
            if (selectedAPI != null) {
                messageContext.setProperty(APIMgtGatewayConstants.API_OBJECT, selectedAPI);
                if (GatewayUtils.isOnDemandLoading()) {
                    if (!selectedAPI.isDeployed()) {
                        synchronized ("LoadAPI_".concat(selectedAPI.getContext()).intern()) {
                            if(!selectedAPI.isDeployed()) {
                                InMemoryAPIDeployer inMemoryAPIDeployer = new InMemoryAPIDeployer();
                                try {
                                    inMemoryAPIDeployer.deployAPI(selectedAPI.getUuid());
                                } catch (ArtifactSynchronizerException e) {
                                    log.error("Error while retrieve and deploy artifact for API : " + selectedAPI.getApiId(), e);
                                    return false;
                                }
                            }
                        }
                    }
                }
                if (transportInUrl instanceof String && StringUtils.isNotEmpty((String) transportInUrl)) {
                    String updatedTransportInUrl = ((String) transportInUrl).replaceFirst(selectedPath,
                            selectedAPI.getContext());
                    axis2MessageContext.setProperty(APIConstants.TRANSPORT_URL_IN, updatedTransportInUrl);
                }
                messageContext.getPropertyKeySet().remove(RESTConstants.REST_FULL_REQUEST_PATH);
            }
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
