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
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.gateway.utils.APIMgtGoogleAnalyticsUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataBridgeDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsData;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This is a handler which is actually embedded to the netty pipeline which does operations such as
 * authentication and throttling for the websocket handshake and subsequent websocket frames.
 */
public class WebsocketInboundHandler extends ChannelInboundHandlerAdapter {
	private static final Log log = LogFactory.getLog(WebsocketInboundHandler.class);
	private static volatile ThrottleDataPublisher throttleDataPublisher = null;
	private String tenantDomain;
	private static APIMgtUsageDataPublisher usageDataPublisher;
	private String uri;
	private String version;
	private APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();
	private io.netty.handler.codec.http.HttpHeaders headers = new DefaultHttpHeaders();

	public WebsocketInboundHandler() {

		if (throttleDataPublisher == null) {
			// The publisher initializes in the first request only
			synchronized (this) {
				throttleDataPublisher = new ThrottleDataPublisher();
			}
		}

		if (usageDataPublisher == null) {
			usageDataPublisher = new APIMgtUsageDataBridgeDataPublisher();
			usageDataPublisher.init();
		}
	}

	/**
	 * extract the version from the request uri
	 *
	 * @param url
	 * @return version String
	 */
	private String getVersionFromUrl(final String url) {
		return url.replaceFirst(".*/([^/?]+).*", "$1");
	}

	private String getContextFromUrl(String url) {
		int lastIndex = url.lastIndexOf('/');
		return url.substring(0, lastIndex);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		//check if the request is a handshake
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest req = (FullHttpRequest) msg;
			uri = req.getUri();
			if (req.getUri().contains("/t/"))  {
				tenantDomain = MultitenantUtils.getTenantDomainFromUrl(req.getUri());
			} else {
				tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
			}

			String useragent = req.headers().get(HttpHeaders.USER_AGENT);
			String authorization = req.headers().get(HttpHeaders.AUTHORIZATION);

			// '-' is used for empty values to avoid possible errors in DAS side.
			// Required headers are stored one by one as validateOAuthHeader()
			// removes some of the headers from the request
			useragent = useragent != null ? useragent : "-";
			headers.add(HttpHeaders.AUTHORIZATION, authorization);
			headers.add(HttpHeaders.USER_AGENT, useragent);

			if (validateOAuthHeader(req)) {
				if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
					// carbon-mediation only support websocket invocation from super tenant APIs.
					// This is a workaround to mimic the invocation came from super tenant.
					req.setUri(req.getUri().replaceFirst("/", "-"));
					String modifiedUri = uri.replaceFirst("/t/", "-t/");
					req.setUri(modifiedUri);
					msg = req;
				} else {
					req.setUri(uri); // Setting Endpoint appended URI
				}
				ctx.fireChannelRead(msg);

				// publish google analytics data
				GoogleAnalyticsData.DataBuilder gaData = new GoogleAnalyticsData.DataBuilder(null, null, null, null)
						.setDocumentPath(uri)
						.setDocumentHostName(DataPublisherUtil.getHostAddress())
						.setSessionControl("end")
						.setCacheBuster(APIMgtGoogleAnalyticsUtils.getCacheBusterId())
						.setIPOverride(ctx.channel().remoteAddress().toString());
				APIMgtGoogleAnalyticsUtils gaUtils = new APIMgtGoogleAnalyticsUtils();
				gaUtils.init(tenantDomain);
				gaUtils.publishGATrackingData(gaData, req.headers().get(HttpHeaders.USER_AGENT), authorization);
			} else {
				ctx.writeAndFlush(new TextWebSocketFrame(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE));
				throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
				                               APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
			}
		} else if (msg instanceof WebSocketFrame) {
			boolean isThrottledOut = doThrottle(ctx, (WebSocketFrame) msg);
			String clientIp = ctx.channel().remoteAddress().toString();

			if (isThrottledOut) {
				ctx.fireChannelRead(msg);
			} else {
				ctx.writeAndFlush(new TextWebSocketFrame("Websocket frame throttled out"));
			}

			// publish analytics events if analytics is enabled
			if (APIUtil.isAnalyticsEnabled()) {
				publishRequestEvent(infoDTO, clientIp, isThrottledOut);
			}
		}
	}

	/**
	 * Authenticate request
	 *
	 * @param req Full Http Request
	 * @return true if the access token is valid
	 */
	private boolean validateOAuthHeader(FullHttpRequest req)
			throws APIManagementException, APISecurityException {
		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
			version = getVersionFromUrl(uri);
			APIKeyValidationInfoDTO info;
			if (!req.headers().contains(HttpHeaders.AUTHORIZATION)) {
				log.error("No Authorization Header Present");
				return false;
			}
			String[] auth = req.headers().get(HttpHeaders.AUTHORIZATION).split(" ");
			if (APIConstants.CONSUMER_KEY_SEGMENT.equals(auth[0])) {
				String cacheKey;
				String apiKey = auth[1];
				if (WebsocketUtil.isRemoveOAuthHeadersFromOutMessage()) {
					req.headers().remove(HttpHeaders.AUTHORIZATION);
				}
				//If the key have already been validated
				if (WebsocketUtil.isGatewayTokenCacheEnabled()) {
					cacheKey = WebsocketUtil.getAccessTokenCacheKey(apiKey, uri);
					info = WebsocketUtil.validateCache(apiKey, cacheKey);
					if (info != null) {
						infoDTO = info;
						return info.isAuthorized();
					}
				}
				String keyValidatorClientType = APISecurityUtils.getKeyValidatorClientType();
				if (APIConstants.API_KEY_VALIDATOR_WS_CLIENT.equals(keyValidatorClientType)) {
					info = new WebsocketWSClient().getAPIKeyData(uri, version, apiKey);
				} else if (APIConstants.API_KEY_VALIDATOR_THRIFT_CLIENT.equals(keyValidatorClientType)) {
					info = new WebsocketThriftClient().getAPIKeyData(uri, version, apiKey);
				} else {
					return false;
				}
				if (info == null || !info.isAuthorized()) {
					return false;
				}
				if (info.getApiName() != null && info.getApiName().contains("*")) {
					String[] str = info.getApiName().split("\\*");
					version = str[1];
					uri += "/" + str[1];
					info.setApiName(str[0]);
				}
				if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(info.getType())) {
					uri = "/_PRODUCTION_" + uri;
				} else if (APIConstants.API_KEY_TYPE_SANDBOX.equals(info.getType())) {
					uri = "/_SANDBOX_" + uri;
				}
				if (WebsocketUtil.isGatewayTokenCacheEnabled()) {
					cacheKey = WebsocketUtil.getAccessTokenCacheKey(apiKey, uri);
					WebsocketUtil.putCache(info, apiKey, cacheKey);
				}
				infoDTO = info;
				return true;
			} else {
				return false;
			}
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

	/**
	 * Checks if the request is throttled
	 *
	 * @param ctx ChannelHandlerContext
	 * @return false if throttled
	 * @throws APIManagementException
	 */
	public boolean doThrottle(ChannelHandlerContext ctx, WebSocketFrame msg)
			throws APIManagementException {

		String applicationLevelTier = infoDTO.getApplicationTier();
		String apiLevelTier = infoDTO.getApiTier();
		String subscriptionLevelTier = infoDTO.getTier();
		String resourceLevelTier = apiLevelTier;
		String authorizedUser;
		if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
				.equalsIgnoreCase(infoDTO.getSubscriberTenantDomain())) {
			authorizedUser = infoDTO.getSubscriber() + "@" + infoDTO.getSubscriberTenantDomain();
		} else {
			authorizedUser = infoDTO.getSubscriber();
		}
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
		jsonObMap.put(APIThrottleConstants.MESSAGE_SIZE, msg.content().capacity());
		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext()
					.setTenantDomain(tenantDomain, true);
			boolean isThrottled = WebsocketUtil
					.isThrottled(resourceLevelThrottleKey, subscriptionLevelThrottleKey,
					             applicationLevelThrottleKey);
			if (isThrottled) {
				return false;
			}
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
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
	 * Publish reuqest event to analytics server
	 *
	 * @param infoDTO API and Application data
	 * @param clientIp client's IP Address
	 * @param isThrottledOut request is throttled out or not
	 */
	private void publishRequestEvent(APIKeyValidationInfoDTO infoDTO, String clientIp, boolean isThrottledOut) {
		long requestTime = System.currentTimeMillis();
		String useragent = headers.get(HttpHeaders.USER_AGENT);

		try {
			Application app = ApiMgtDAO.getInstance().getApplicationById(Integer.parseInt(infoDTO.getApplicationId()));
			String appOwner = app.getSubscriber().getName();

			RequestPublisherDTO requestPublisherDTO = new RequestPublisherDTO();
			requestPublisherDTO.setApi(infoDTO.getApiName());
			requestPublisherDTO.setApiPublisher(infoDTO.getApiPublisher());
			requestPublisherDTO.setApiVersion(infoDTO.getApiName() + ':' + version);
			requestPublisherDTO.setApplicationId(infoDTO.getApplicationId());
			requestPublisherDTO.setApplicationName(infoDTO.getApplicationName());
			requestPublisherDTO.setApplicationOwner(appOwner);
			requestPublisherDTO.setClientIp(clientIp);
			requestPublisherDTO.setConsumerKey(infoDTO.getConsumerKey());
			requestPublisherDTO.setContext(getContextFromUrl(uri));
			requestPublisherDTO.setContinuedOnThrottleOut(isThrottledOut);
			requestPublisherDTO.setHostName(DataPublisherUtil.getHostAddress());
			requestPublisherDTO.setMethod("-");
			requestPublisherDTO.setRequestTime(requestTime);
			requestPublisherDTO.setResourcePath("-");
			requestPublisherDTO.setResourceTemplate("-");
			requestPublisherDTO.setUserAgent(useragent);
			requestPublisherDTO.setUsername(infoDTO.getEndUserName());
			requestPublisherDTO.setTenantDomain(tenantDomain);
			requestPublisherDTO.setTier(infoDTO.getTier());
			requestPublisherDTO.setVersion(version);

			usageDataPublisher.publishEvent(requestPublisherDTO);
		} catch (Exception e) {
			// flow should not break if event publishing failed
			log.error("Cannot publish event. " + e.getMessage(), e);
		}
	}
}
