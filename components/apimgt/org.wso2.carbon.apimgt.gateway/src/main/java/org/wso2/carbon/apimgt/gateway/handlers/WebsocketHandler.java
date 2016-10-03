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

//import org.wso2.carbon.inbound.endpoint.osgi.service.ServiceReferenceHolder;

public class WebsocketHandler extends ChannelInboundHandlerAdapter {
	private static String tenantDomain;
	private static int port;
	private static volatile ThrottleDataPublisher throttleDataPublisher = null;
	private static Logger log = LoggerFactory.getLogger(WebsocketHandler.class);
	private static ServiceReferenceHolder serviceReferenceHolder;
	API api;
	private String uri;
	private APIConsumer apiConsumer;
	private Subscriber apiSubscriber;
	private APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();

	public WebsocketHandler() {
		System.out.println("executed hand");
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
				System.out.println("http new");
				System.out
						.println(((FullHttpRequest) msg).headers().get(HttpHeaders.AUTHORIZATION));
				System.out.println(((FullHttpRequest) msg).getUri());
				PrivilegedCarbonContext.endTenantFlow();
				ctx.fireChannelRead(msg);
			} else
				throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
				                               APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
		} else if (msg instanceof WebSocketFrame) {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext()
			                       .setTenantDomain(tenantDomain, true);
			System.out.println("web new");
			WebSocketFrame mess = (WebSocketFrame) msg;
			TextWebSocketFrame tess = (TextWebSocketFrame) mess;
			System.out.println(tess.text());
			if (throttle2(ctx)) {
				ctx.fireChannelRead(msg);
			}
		}
	}

	private boolean oauthHandler(FullHttpRequest req) throws APIManagementException {
		PrivilegedCarbonContext.startTenantFlow();
		PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
		api = getAPI();
		AuthUtil util = new AuthUtil();

		AccessTokenInfo tokenInfo = null;
		APIKeyValidationInfoDTO info;
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
						return validateSubscriptionDetails(api.getContext(),
						                                   api.getId().getVersion(),
						                                   info.getConsumerKey());
					} else
						return false;
				}
			}
			try {
				tokenInfo = KeyManagerHolder.getKeyManagerInstance().getTokenMetaData(apikey);
				if (tokenInfo == null) {
					return false;
				}
			} catch (APIManagementException e) {

				e.printStackTrace();
			}
			if (util.isGatewayTokenCacheEnabled()) {
				info = new APIKeyValidationInfoDTO();
				info.setAuthorized(tokenInfo.isTokenValid());
				info.setEndUserName(tokenInfo.getEndUserName());
				info.setConsumerKey(tokenInfo.getConsumerKey());
				info.setIssuedTime(tokenInfo.getIssuedTime());
				info.setValidityPeriod(tokenInfo.getValidityPeriod());
				util.putCache(info, apikey, apikey);
			}
			//apiSubscriber = apiConsumer.getSubscriberById(apikey);

			if (tokenInfo.isTokenValid()) {

				return validateSubscriptionDetails(api.getContext(), api.getId().getVersion(),
				                                   tokenInfo.getConsumerKey());

			} else {
				return false;
			}

		} else {
			return false;
		}
	}

	private boolean throttle2(ChannelHandlerContext ctx) throws APIManagementException {

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
		String apiContext = api.getContext();
		String apiVersion = api.getId().getVersion();
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
		boolean isTrottled = util.service(resourceLevelThrottleKey, subscriptionLevelThrottleKey,
		                                  applicationLevelThrottleKey);
		if (isTrottled) {
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

	//---------------------------------Subscription----------------------------------------------

	public boolean validateSubscriptionDetails(String context, String version, String consumerKey)
			throws APIManagementException {
		boolean defaultVersionInvoked = false;
		String apiTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(context);
		if (apiTenantDomain == null) {
			apiTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
		}
		int apiOwnerTenantId = APIUtil.getTenantIdFromTenantDomain(apiTenantDomain);
		//Check if the api version has been prefixed with _default_
		if (version != null && version.startsWith(APIConstants.DEFAULT_VERSION_PREFIX)) {
			defaultVersionInvoked = true;
			//Remove the prefix from the version.
			version = version.split(APIConstants.DEFAULT_VERSION_PREFIX)[1];
		}
		String sql;
		boolean isAdvancedThrottleEnabled = APIUtil.isAdvanceThrottlingEnabled();
		if (!isAdvancedThrottleEnabled) {
			if (defaultVersionInvoked) {
				sql = SQLConstants.VALIDATE_SUBSCRIPTION_KEY_DEFAULT_SQL;
			} else {
				sql = SQLConstants.VALIDATE_SUBSCRIPTION_KEY_VERSION_SQL;
			}
		} else {
			if (defaultVersionInvoked) {
				sql = SQLConstants.ADVANCED_VALIDATE_SUBSCRIPTION_KEY_DEFAULT_SQL;
			} else {
				sql = SQLConstants.ADVANCED_VALIDATE_SUBSCRIPTION_KEY_VERSION_SQL;
			}
		}

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = APIMgtDBUtil.getConnection();
			conn.setAutoCommit(true);
			ps = conn.prepareStatement(sql);
			ps.setString(1, context);
			ps.setString(2, consumerKey);
			if (isAdvancedThrottleEnabled) {
				ps.setInt(3, apiOwnerTenantId);
				if (!defaultVersionInvoked) {
					ps.setString(4, version);
				}
			} else {
				if (!defaultVersionInvoked) {
					ps.setString(3, version);
				}
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				String subscriptionStatus = rs.getString("SUB_STATUS");
				String type = rs.getString("KEY_TYPE");
				if (APIConstants.SubscriptionStatus.BLOCKED.equals(subscriptionStatus)) {
					infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
					infoDTO.setAuthorized(false);
					return false;
				} else if (APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus) ||
				           APIConstants.SubscriptionStatus.REJECTED.equals(subscriptionStatus)) {
					infoDTO.setValidationStatus(
							APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
					infoDTO.setAuthorized(false);
					return false;
				} else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED
						           .equals(subscriptionStatus) && !APIConstants.API_KEY_TYPE_SANDBOX.equals(type)) {
					infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
					infoDTO.setType(type);
					infoDTO.setAuthorized(false);
					return false;
				}

				String apiProvider = rs.getString("API_PROVIDER");
				String subTier = rs.getString("TIER_ID");
				String appTier = rs.getString("APPLICATION_TIER");
				infoDTO.setTier(subTier);
				infoDTO.setSubscriber(rs.getString("USER_ID"));
				infoDTO.setApplicationId(rs.getString("APPLICATION_ID"));
				infoDTO.setApiName(rs.getString("API_NAME"));
				infoDTO.setApiPublisher(apiProvider);
				infoDTO.setApplicationName(rs.getString("NAME"));
				infoDTO.setApplicationTier(appTier);
				infoDTO.setType(type);

				//Advanced Level Throttling Related Properties
				if (APIUtil.isAdvanceThrottlingEnabled()) {
					String apiTier = rs.getString("API_TIER");
					String subscriberUserId = rs.getString("USER_ID");
					String subscriberTenant = MultitenantUtils.getTenantDomain(subscriberUserId);
					int apiId = rs.getInt("API_ID");
					int subscriberTenantId = APIUtil.getTenantId(subscriberUserId);
					int apiTenantId = APIUtil.getTenantId(apiProvider);
					boolean isContentAware =
							isAnyPolicyContentAware(conn, apiTier, appTier, subTier,
							                        subscriberTenantId, apiTenantId, apiId);
					infoDTO.setContentAware(isContentAware);

					int spikeArrest = 0;
					String apiLevelThrottlingKey = "api_level_throttling_key";
					if (rs.getInt("RATE_LIMIT_COUNT") > 0) {
						spikeArrest = rs.getInt("RATE_LIMIT_COUNT");
					}

					String spikeArrestUnit = null;
					if (rs.getString("RATE_LIMIT_TIME_UNIT") != null) {
						spikeArrestUnit = rs.getString("RATE_LIMIT_TIME_UNIT");
					}
					boolean stopOnQuotaReach = rs.getBoolean("STOP_ON_QUOTA_REACH");
					List<String> list = new ArrayList<String>();
					list.add(apiLevelThrottlingKey);
					infoDTO.setSpikeArrestLimit(spikeArrest);
					infoDTO.setSpikeArrestUnit(spikeArrestUnit);
					infoDTO.setStopOnQuotaReach(stopOnQuotaReach);
					infoDTO.setSubscriberTenantDomain(subscriberTenant);
					if (apiTier != null && apiTier.trim().length() > 0) {
						infoDTO.setApiTier(apiTier);
					}
					//We also need to set throttling data list associated with given API. This need to have policy id and
					// condition id list for all throttling tiers associated with this API.
					infoDTO.setThrottlingDataList(list);
				}
				return true;
			}
			infoDTO.setAuthorized(false);
			infoDTO.setValidationStatus(
					APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
		} catch (SQLException e) {
			handleException("Exception occurred while validating Subscription.", e);
		} finally {
			try {
				conn.setAutoCommit(false);
			} catch (SQLException e) {

			}
			APIMgtDBUtil.closeAllConnections(ps, conn, rs);
		}
		return false;
	}

	private boolean isAnyPolicyContentAware(Connection conn, String apiPolicy, String appPolicy,
	                                        String subPolicy, int subscriptionTenantId,
	                                        int appTenantId, int apiId)
			throws APIManagementException {
		boolean isAnyContentAware = false;
		// only check if using CEP based throttling.
		ResultSet resultSet = null;
		PreparedStatement ps = null;
		String sqlQuery = SQLConstants.ThrottleSQLConstants.IS_ANY_POLICY_CONTENT_AWARE_SQL;

		try {
			String dbProdName = conn.getMetaData().getDatabaseProductName();
            /*if("oracle".equalsIgnoreCase(dbProdName.toLowerCase()) || conn.getMetaData().getDriverName().toLowerCase().contains("oracle")){
				sqlQuery = sqlQuery.replaceAll("\\+", "union all");
				sqlQuery = sqlQuery.replaceFirst("select", "select sum(c) from ");
			}else if(dbProdName.toLowerCase().contains("microsoft") && dbProdName.toLowerCase().contains("sql")){
				sqlQuery = sqlQuery.replaceAll("\\+", "union all");
				sqlQuery = sqlQuery.replaceFirst("select", "select sum(c) from ");
				sqlQuery = sqlQuery + " x";
            }*/

			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, apiPolicy);
			ps.setInt(2, subscriptionTenantId);
			ps.setString(3, apiPolicy);
			ps.setInt(4, subscriptionTenantId);
			ps.setInt(5, apiId);
			ps.setInt(6, subscriptionTenantId);
			ps.setInt(7, apiId);
			ps.setInt(8, subscriptionTenantId);
			ps.setString(9, subPolicy);
			ps.setInt(10, subscriptionTenantId);
			ps.setString(11, appPolicy);
			ps.setInt(12, appTenantId);
			resultSet = ps.executeQuery();
			// We only expect one result if all are not content aware.
			if (resultSet == null) {
				throw new APIManagementException(" Result set Null");
			}
			int count = 0;
			if (resultSet.next()) {
				count = resultSet.getInt(1);
				if (count > 0) {
					isAnyContentAware = true;
				}
			}
		} catch (SQLException e) {
			handleException("Failed to get content awareness of the policies ", e);
		} finally {
			APIMgtDBUtil.closeAllConnections(ps, null, resultSet);
		}
		return isAnyContentAware;
	}

	private void handleException(String description, Exception e) {
		log.error(description, e);
	}

	private API getAPI() throws APIManagementException {
		apiConsumer = APIManagerFactory.getInstance().getAPIConsumer();
		//ApiMgtDAO.getInstance().validateSubscriptionDetails();
		List<API> list = apiConsumer.getAllAPIs();
		for (API api : list) {
			if (api.getContext().equalsIgnoreCase(uri)) {
				return api;
			}
		}
		return null;
	}
}
