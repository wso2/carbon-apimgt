/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.service;


import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.core.AbstractAdmin;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class APIKeyValidationService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(APIKeyValidationService.class);

    /**
     * Validates the access tokens issued for a particular user to access an API.
     *
     * @param context     Requested context
     * @param accessToken Provided access token
     * @return APIKeyValidationInfoDTO with authorization info and tier info if authorized. If it is not
     *         authorized, tier information will be <pre>null</pre>
     * @throws APIKeyMgtException Error occurred when accessing the underlying database or registry.
     */
    public APIKeyValidationInfoDTO validateKey(String context, String version, String accessToken,String requiredAuthenticationLevel, String clientDomain)
            throws APIKeyMgtException, APIManagementException {
        MessageContext axis2MessageContext = MessageContext.getCurrentMessageContext();
        Map headersMap = null;
        String activityID = null;
        try {
            if (axis2MessageContext != null) {
                MessageContext responseMessageContext = axis2MessageContext.getOperationContext().
                        getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (log.isDebugEnabled()) {
                    List headersList = new ArrayList();
                    Object headers = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                    if (headers != null && headers instanceof Map) {
                        headersMap = (Map) headers;
                        activityID = (String) headersMap.get("activityID");
                    }
                    headersList.add(new Header("activityID", (String) headersMap.get("activityID")));
                    responseMessageContext.setProperty(HTTPConstants.HTTP_HEADERS, headersList);
                }
            }
        } catch (AxisFault axisFault) {
            throw new APIKeyMgtException("Error while building response messageContext: " + axisFault.getLocalizedMessage());
        }

        String logMsg = "KeyValidation request from gateway: requestTime=" + new Date(System.currentTimeMillis());
        if (log.isDebugEnabled()) {
            if (activityID != null) {
                logMsg = logMsg + " , transactionId=" + activityID;
            }
            log.debug(logMsg);
        }

        Cache cache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.KEY_CACHE_NAME);
        String cacheKey = accessToken + ":" + context + ":" + version+":"+requiredAuthenticationLevel;
        APIKeyValidationInfoDTO info;
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        Boolean keyCacheEnabledGateway = APIKeyMgtDataHolder.getKeyCacheEnabledKeyMgt();

        //If gateway key cache enabled only we retrieve key validation info or JWT token form cache
        if (keyCacheEnabledGateway) {
            info = (APIKeyValidationInfoDTO) cache.get(cacheKey);
            //If key validation information is not null then only we proceed with cached object
            if (info != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found cached access token for : " + cacheKey + " .Checking for expiration time.");
                }
                
                if (info.isAuthorized()) {
                    //return if client domain is not-authorized
                    checkClientDomainAuthorized(info, clientDomain);
                }

                 //check if token has expired
                boolean tokenExpired = APIKeyMgtUtil.hasAccessTokenExpired(info);
                if (!tokenExpired) {
                    //If key validation information is authorized then only we have to check for JWT token
                    //If key validation information is authorized and JWT cache disabled then only we use
                    //cached api key validation information and generate new JWT token
                    if (!APIKeyMgtDataHolder.getJWTCacheEnabledKeyMgt() && info.isAuthorized()) {
                        String JWTString;
                        if (info.getUserType().equalsIgnoreCase(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION)) {
                            JWTString = apiMgtDAO.createJWTTokenString(context, version, info.getSubscriber(),
                                    apiMgtDAO.getApplicationNameFromId(Integer.parseInt(info.getApplicationId())),
                                    info.getTier(), "null");
                        } else {
                            JWTString = apiMgtDAO.createJWTTokenString(context, version, info.getSubscriber(),
                                    apiMgtDAO.getApplicationNameFromId(Integer.parseInt(info.getApplicationId())),
                                    info.getTier(), info.getEndUserName());
                        }
                        info.setEndUserToken(JWTString);
                    }
                    if (log.isDebugEnabled() && axis2MessageContext != null) {
                        logMessageDetails(axis2MessageContext, info);
                    }
                    return info;
                } else {
                    log.info("Token " + cacheKey + " expired.");
                }
            }
        }
        //If validation info is not cached creates fresh api key validation information object and returns it
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = apiMgtDAO.validateKey(context, version, accessToken,requiredAuthenticationLevel);
        
        if (apiKeyValidationInfoDTO.isAuthorized()) {
        	//return if client domain is not-authorized
        	checkClientDomainAuthorized(apiKeyValidationInfoDTO, clientDomain);
        }
        
        //If key validation information is not null and key validation enabled at keyMgt we put validation
        //information into cache
        if (apiKeyValidationInfoDTO != null) {
            cache.put(cacheKey, apiKeyValidationInfoDTO);
        }

        if (log.isDebugEnabled() && axis2MessageContext != null) {
            logMessageDetails(axis2MessageContext, apiKeyValidationInfoDTO);
        }
        return apiKeyValidationInfoDTO;
    }

    /**
     * Return the URI Templates for an API
     *
     * @param context Requested context
     * @param version API Version
     * @return APIKeyValidationInfoDTO with authorization info and tier info if authorized. If it is not
     *         authorized, tier information will be <pre>null</pre>
     * @throws APIKeyMgtException Error occurred when accessing the underlying database or registry.
     */
    public ArrayList<URITemplate> getAllURITemplates(String context, String version)
            throws APIKeyMgtException, APIManagementException {

        return ApiMgtDAO.getAllURITemplates(context, version);

    }
    
    private void checkClientDomainAuthorized (APIKeyValidationInfoDTO apiKeyValidationInfoDTO, String clientDomain) 
    		throws APIManagementException {
    	if (clientDomain != null) {
    		clientDomain = clientDomain.trim();
    	}
    	List<String> authorizedDomains = apiKeyValidationInfoDTO.getAuthorizedDomains();
    	if (!(authorizedDomains.contains("ALL") || authorizedDomains.contains(clientDomain))) {
    		log.error("Unauthorized client domain :" + clientDomain +
    				". Only \"" + authorizedDomains + "\" domains are authorized to access the API.");
    		throw new APIManagementException("Unauthorized client domain :" + clientDomain +
    				". Only \"" + authorizedDomains + "\" domains are authorized to access the API.");
    	}
    	
    }

    private void logMessageDetails(MessageContext messageContext, APIKeyValidationInfoDTO apiKeyValidationInfoDTO) {
        String applicationName = apiKeyValidationInfoDTO.getApplicationName();
        String endUserName = apiKeyValidationInfoDTO.getEndUserName();
        String consumerKey = apiKeyValidationInfoDTO.getConsumerKey();
        Boolean isAuthorize = apiKeyValidationInfoDTO.isAuthorized();
        //Do not change this log format since its using by some external apps
        String logMessage = "";
        if (applicationName != null) {
            logMessage = " , appName=" + applicationName;
        }
        if (endUserName != null) {
            logMessage = logMessage + " , userName=" + endUserName;
        }
        Map headers = (Map) messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String logID = (String) headers.get("activityID");
        if (logID != null) {
            logMessage = logMessage + " , transactionId=" + logID;
        }
        if (consumerKey != null) {
            logMessage = logMessage + " , consumerKey=" + consumerKey;
        }
        logMessage = logMessage + " , isAuthorized=" + isAuthorize;
        logMessage = logMessage + " , responseTime=" + new Date(System.currentTimeMillis());

        log.debug("OAuth token response from keyManager to gateway: " + logMessage);
    }
}
