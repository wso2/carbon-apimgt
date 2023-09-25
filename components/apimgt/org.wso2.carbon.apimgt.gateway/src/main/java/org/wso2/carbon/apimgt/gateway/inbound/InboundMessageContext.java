/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.inbound;

import io.netty.channel.ChannelHandlerContext;
import org.apache.axis2.context.MessageContext;
import org.apache.synapse.api.API;
import org.wso2.carbon.apimgt.api.gateway.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.dto.GraphQLOperationDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.Authentication.Authenticator;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ResourceInfoDTO;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message context to hold information of an intercepted single inbound connection.
 */
public class InboundMessageContext {

    private MessageContext axis2MessageContext;
    private String tenantDomain;
    private String fullRequestPath;
    private String requestPath; // request path without query param section
    private String version;
    private APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();
    private Map<String, String> requestHeaders = new HashMap<>();   //Current request headers
    private List<String> headersToRemove = new ArrayList<>();   //Headers to remove from requestHeaders
    private Map<String, String> headersToAdd = new HashMap<>(); //Headers to add to requestHeaders
    private String token;
    private String apiContext;
    private String apiName;
    private String keyType;
    private API api;
    private String electedRoute;
    private AuthenticationContext authContext;
    private org.wso2.carbon.apimgt.keymgt.model.entity.API electedAPI;
    private SignedJWTInfo signedJWTInfo;
    private Map<String, ResourceInfoDTO> resourcesMap = new HashMap<>(); //elected API resources
    private String userIP;
    private String matchingResource; //invoking API resource
    private ChannelHandlerContext ctx;
    private boolean isJWTToken;
    private Authenticator authenticator;

    //Graphql Subscription specific connection context information
    private GraphQLSchemaDTO graphQLSchemaDTO;
    private Map<String, GraphQLOperationDTO> graphQLMsgIdToVerbInfo = new HashMap<>();

    public void addVerbInfoForGraphQLMsgId(String msgId, GraphQLOperationDTO graphQLOperationDTO) {
        this.graphQLMsgIdToVerbInfo.put(msgId, graphQLOperationDTO);
    }

    public GraphQLOperationDTO getVerbInfoForGraphQLMsgId(String msgId) {
        return this.graphQLMsgIdToVerbInfo.get(msgId);
    }

    public MessageContext getAxis2MessageContext() {
        return axis2MessageContext;
    }

    public void setAxis2MessageContext(MessageContext axis2MessageContext) {
        this.axis2MessageContext = axis2MessageContext;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getFullRequestPath() {
        return fullRequestPath;
    }

    public void setFullRequestPath(String fullRequestPath) {
        this.fullRequestPath = fullRequestPath;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public APIKeyValidationInfoDTO getInfoDTO() {
        return infoDTO;
    }

    public void setInfoDTO(APIKeyValidationInfoDTO infoDTO) {
        this.infoDTO = infoDTO;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public API getApi() {
        return api;
    }

    public void setApi(API api) {
        this.api = api;
    }

    public String getElectedRoute() {
        return electedRoute;
    }

    public void setElectedRoute(String electedRoute) {
        this.electedRoute = electedRoute;
    }

    public AuthenticationContext getAuthContext() {
        return authContext;
    }

    public void setAuthContext(AuthenticationContext authContext) {
        this.authContext = authContext;
    }

    public org.wso2.carbon.apimgt.keymgt.model.entity.API getElectedAPI() {
        return electedAPI;
    }

    public void setElectedAPI(org.wso2.carbon.apimgt.keymgt.model.entity.API electedAPI) {
        this.electedAPI = electedAPI;
    }

    public GraphQLSchemaDTO getGraphQLSchemaDTO() {
        return graphQLSchemaDTO;
    }

    public void setGraphQLSchemaDTO(GraphQLSchemaDTO graphQLSchemaDTO) {
        this.graphQLSchemaDTO = graphQLSchemaDTO;
    }

    public SignedJWTInfo getSignedJWTInfo() {
        return signedJWTInfo;
    }

    public void setSignedJWTInfo(SignedJWTInfo signedJWTInfo) {
        this.signedJWTInfo = signedJWTInfo;
    }

    public String getUserIP() {
        return userIP;
    }

    public void setUserIP(String userIP) {
        this.userIP = userIP;
    }

    public Map<String, ResourceInfoDTO> getResourcesMap() {
        return resourcesMap;
    }

    public List<String> getHeadersToRemove() {
        return headersToRemove;
    }

    public void setHeadersToRemove(List<String> headersToRemove) {
        this.headersToRemove = headersToRemove;
    }

    public Map<String, String> getHeadersToAdd() {
        return headersToAdd;
    }

    public String getMatchingResource() {
        return matchingResource;
    }

    public void setMatchingResource(String matchingResource) {
        this.matchingResource = matchingResource;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public boolean isJWTToken() {
        return isJWTToken;
    }

    public void setJWTToken(boolean JWTToken) {
        isJWTToken = JWTToken;
    }

    public void setAuthenticator (Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public Authenticator getAuthenticator () {
        return this.authenticator;
    }
}
