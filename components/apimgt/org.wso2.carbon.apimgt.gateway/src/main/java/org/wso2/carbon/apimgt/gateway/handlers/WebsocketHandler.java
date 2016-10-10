/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.axiom.util.UIDGenerator;
import org.apache.http.HttpHeaders;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class WebsocketHandler extends ChannelInboundHandlerAdapter {
	private static String tenantDomain;
	private static int port;
	private static volatile ThrottleDataPublisher throttleDataPublisher = null;
	private static Logger log = LoggerFactory.getLogger(WebsocketHandler.class);
	private String uri;
	private String version;
	private APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();

	public WebsocketHandler() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof FullHttpRequest) {
			FullHttpRequest req = (FullHttpRequest) msg;
			uri = req.getUri();
			port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
			tenantDomain = MultitenantUtils.getTenantDomainFromUrl(req.getUri());
			if (tenantDomain.equals(req.getUri())) {
				tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
			}
			if (oauthHandler(req)) {
				PrivilegedCarbonContext.endTenantFlow();
				ctx.fireChannelRead(msg);
			} else
				throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
				                               APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
		} else if (msg instanceof WebSocketFrame) {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext()
			                       .setTenantDomain(tenantDomain, true);
			if (doThrottle(ctx)) {
				ctx.fireChannelRead(msg);
			}
		}
	}

	/**
	 * Authenticate request
	 *
	 * @param req Full Http Request
	 * @return true if the access token is valid
	 * @throws APIManagementException
	 */
	private boolean oauthHandler(FullHttpRequest req)
			throws APIManagementException, APISecurityException {

		PrivilegedCarbonContext.startTenantFlow();
		PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
		AuthUtil util = new AuthUtil();
		version = getversionFromUrl(uri);
		APIKeyValidationInfoDTO info;
		if (!req.headers().contains(HttpHeaders.AUTHORIZATION)){
			log.error("No Authorization Header Present");
			return false;
		}
		String[] auth = req.headers().get(HttpHeaders.AUTHORIZATION).split(" ");
		if (auth[0].equals(APIConstants.CONSUMER_KEY_SEGMENT)) {
			String apikey = auth[1];
			if (util.isRemoveOAuthHeadersFromOutMessage()) {
				req.headers().remove(HttpHeaders.AUTHORIZATION);
			}
			//If the key have already been validated
			if (util.isGatewayTokenCacheEnabled()) {
				info = util.validateCache(apikey, apikey);
				if (info != null) {
					if (info.isAuthorized()) {
						return true;
					}else {
						return false;
					}
				}
			}
			String keyValidatorClientType = APISecurityUtils.getKeyValidatorClientType();
			if (APIConstants.API_KEY_VALIDATOR_WS_CLIENT.equals(keyValidatorClientType)) {
				info = new auth().getAPIKeyData(uri, version, apikey);
			} else if (APIConstants.API_KEY_VALIDATOR_THRIFT_CLIENT.equals(keyValidatorClientType)) {
				info = new thrift().getAPIKeyData(uri, version, apikey);
			}else{
				return false;
			}
			if(!info.isAuthorized() || info == null){
				return false;
			}
			if (util.isGatewayTokenCacheEnabled()) {
				util.putCache(info, apikey, apikey);
			}
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * Checks if the request is throttled
	 *
	 * @param ctx ChannelHandlerContext
	 * @return false if throttled
	 * @throws APIManagementException
	 */
	private boolean doThrottle(ChannelHandlerContext ctx) throws APIManagementException {

		AuthUtil util = new AuthUtil();
		if (throttleDataPublisher == null) {
			// The publisher initializes in the first request only
			synchronized (this) {
				throttleDataPublisher = new ThrottleDataPublisher();
			}
		}

		String applicationLevelTier = infoDTO.getApplicationTier();
		String apiLevelTier = infoDTO.getApiTier();
		String subscriptionLevelTier = infoDTO.getTier();
		String resourceLevelTier = apiLevelTier;
		String authorizedUser = infoDTO.getSubscriber() + "@" + infoDTO.getSubscriberTenantDomain();
		String apiName = infoDTO.getApiName();
		String apiContext = uri;
		String apiVersion = version;
		String appTenant = infoDTO.getSubscriberTenantDomain();
		String apiTenant = tenantDomain;
		String appId = infoDTO.getApplicationId();
		String applicationLevelThrottleKey = appId + ":" + authorizedUser;
		String apiLevelThrottleKey = apiContext + ":" + apiVersion;
		String resourceLevelThrottleKey = apiLevelThrottleKey;
		String subscriptionLevelThrottleKey = appId + ":" + apiContext + ":" + apiVersion;
		String messageId = UIDGenerator.generateURNString();
		String remoteIP = ctx.channel().remoteAddress().toString();
		remoteIP = remoteIP.substring(1, remoteIP.indexOf(":"));
		JSONObject jsonObMap = new JSONObject();
		if (remoteIP != null && remoteIP.length() > 0) {
			jsonObMap.put(APIThrottleConstants.IP, APIUtil.ipToLong(remoteIP));
		}
		boolean isThrottled = util.isThrottled(resourceLevelThrottleKey, subscriptionLevelThrottleKey,
		                                  applicationLevelThrottleKey);
		if (isThrottled) {
			ctx.writeAndFlush(new TextWebSocketFrame("Websocket frame throttled out"));
			return false;
		}
		PrivilegedCarbonContext.endTenantFlow();
		Object[] objects =
				new Object[] { messageId, applicationLevelThrottleKey, applicationLevelTier,
				               apiLevelThrottleKey, apiLevelTier, subscriptionLevelThrottleKey,
				               subscriptionLevelTier, resourceLevelThrottleKey, resourceLevelTier,
				               authorizedUser, apiContext, apiVersion, appTenant, apiTenant, appId,
				               apiName, jsonObMap.toString() };
		org.wso2.carbon.databridge.commons.Event event =
				new org.wso2.carbon.databridge.commons.Event(
						"org.wso2.throttle.request.stream:1.0.0", System.currentTimeMillis(), null,
						null, objects);
		throttleDataPublisher.getDataPublisher().tryPublish(event);
		return true;
	}

	/**
	 * extract the version from the request uri
	 * @param url
	 * @return version String
	 */
	public static String getversionFromUrl(final String url){
		// return url.replaceFirst("[^?]*/(.*?)(?:\\?.*)","$1);" <-- incorrect
		return url.replaceFirst(".*/([^/?]+).*", "$1");
	}

}
