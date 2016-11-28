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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;
import java.util.concurrent.TimeUnit;

public class WebsocketUtil {
	private static Logger log = LoggerFactory.getLogger(WebsocketUtil.class);
	private static boolean isGatewayKeyCacheInitialized = false;
	private static boolean removeOAuthHeadersFromOutMessage = true;
	private static boolean gatewayTokenCacheEnabled = false;

	static {
		initParams();
		getGatewayKeyCache();
	}

	/**
	 * initialize static parameters of WebsocketUtil class
	 *
	 */
	protected static void initParams() {
		try {
			APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
			String cacheEnabled = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
			if (cacheEnabled != null) {
				gatewayTokenCacheEnabled = Boolean.parseBoolean(cacheEnabled);
			}
			String value = config.getFirstProperty(APIConstants.REMOVE_OAUTH_HEADERS_FROM_MESSAGE);
			if (value != null) {
				removeOAuthHeadersFromOutMessage = Boolean.parseBoolean(value);
			}
		} catch (NullPointerException e) {
			log.error(
					"Did not found valid API Validation Information cache configuration. "
					+ e.getMessage(), e);
		}

	}

	public static boolean isRemoveOAuthHeadersFromOutMessage() {
		return removeOAuthHeadersFromOutMessage;
	}

	/**
	 * validate access token via cache
	 *
	 * @param apiKey access token
	 * @param cacheKey key of second level cache
	 * @return APIKeyValidationInfoDTO
	 */
	public static APIKeyValidationInfoDTO validateCache(String apiKey, String cacheKey) {

		//Get the access token from the first level cache.
		String cachedToken = (String) getGatewayTokenCache().get(apiKey);

		//If the access token exists in the first level cache.
		if (cachedToken != null) {
			APIKeyValidationInfoDTO info =
					(APIKeyValidationInfoDTO) getGatewayKeyCache().get(cacheKey);

			if (info != null) {
				if (APIUtil.isAccessTokenExpired(info)) {
					info.setAuthorized(false);
					// in cache, if token is expired  remove cache entry.
					getGatewayKeyCache().remove(cacheKey);
					//Remove from the first level token cache as well.
					getGatewayTokenCache().remove(apiKey);
				}
				return info;
			}
		}

		return null;
	}

	/**
	 * write to cache
	 *
	 * @param info
	 * @param apiKey
	 * @param cacheKey
	 */
	public static void putCache(APIKeyValidationInfoDTO info, String apiKey, String cacheKey) {

		//Get the tenant domain of the API that is being invoked.
		String tenantDomain =
				PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

		//Add to first level Token Cache.
		getGatewayTokenCache().put(apiKey, tenantDomain);
		//Add to Key Cache.
		getGatewayKeyCache().put(cacheKey, info);

		//If this is NOT a super-tenant API that is being invoked
		if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
			//Add the tenant domain as a reference to the super tenant cache so we know from which tenant cache
			//to remove the entry when the need occurs to clear this particular cache entry.
			try {
				PrivilegedCarbonContext.startTenantFlow();
				PrivilegedCarbonContext.getThreadLocalCarbonContext().
						setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);

				getGatewayTokenCache().put(apiKey, tenantDomain);
			} finally {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}

	}

	protected static Cache getGatewayKeyCache() {
		String apimGWCacheExpiry =
				ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
						getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);
		if (!isGatewayKeyCacheInitialized && apimGWCacheExpiry != null) {
			isGatewayKeyCacheInitialized = true;
			return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
			              .createCacheBuilder(APIConstants.GATEWAY_KEY_CACHE_NAME).
					              setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
					                        new CacheConfiguration.Duration(TimeUnit.SECONDS,
					                                                        Long.parseLong(
							                                                        apimGWCacheExpiry)))
			              .
					              setExpiry(CacheConfiguration.ExpiryType.ACCESSED,
					                        new CacheConfiguration.Duration(TimeUnit.SECONDS,
					                                                        Long.parseLong(
							                                                        apimGWCacheExpiry)))
			              .setStoreByValue(false).build();
		} else {
			return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
			              .getCache(APIConstants.GATEWAY_KEY_CACHE_NAME);
		}
	}

	protected static Cache getGatewayTokenCache() {
		Cache cache = null;
		try {
			javax.cache.CacheManager manager =
					Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER);
			cache = manager.getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME);
		} catch (NullPointerException e) {
			log.error(
					"Did not found valid API Validation Information cache configuration. " +
					e.getMessage(), e);
		}
		return cache;

	}

	public static boolean isGatewayTokenCacheEnabled() {
		return gatewayTokenCacheEnabled;
	}

	/**
	 * check if the request is throttled
	 *
	 * @param resourceLevelThrottleKey
	 * @param subscriptionLevelThrottleKey
	 * @param applicationLevelThrottleKey
	 * @return true if request is throttled out
	 */
	public static boolean isThrottled(String resourceLevelThrottleKey, String subscriptionLevelThrottleKey,
	                           String applicationLevelThrottleKey) {
		boolean isApiLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder()
				                                                              .isAPIThrottled(resourceLevelThrottleKey);
		boolean isSubscriptionLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder()
				                                                              .isThrottled(subscriptionLevelThrottleKey);
		boolean isApplicationLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder()
				                                                              .isThrottled(applicationLevelThrottleKey);
		return (isApiLevelThrottled || isApplicationLevelThrottled || isSubscriptionLevelThrottled);
	}

	public static String getAccessTokenCacheKey(String accessToken, String apiContext) {
		return accessToken + ':' + apiContext;
	}
}
