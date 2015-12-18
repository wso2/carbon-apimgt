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
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.handlers.KeyValidationHandler;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.*;

/**
 *
 */
public class APIKeyValidationService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(APIKeyValidationService.class);
    private static KeyValidationHandler keyValidationHandler;

    public APIKeyValidationService() {
        try {
            if (keyValidationHandler == null) {

                KeyValidationHandler validationHandler = (KeyValidationHandler) APIUtil.getClassForName
                        (ServiceReferenceHolder.getInstance().
                                getAPIManagerConfigurationService().getAPIManagerConfiguration().
                                getFirstProperty(APIConstants.API_KEY_MANGER_VALIDATIONHANDLER_CLASS_NAME)).newInstance();
                log.info("Initialised KeyValidationHandler instance successfully");
                if (keyValidationHandler == null) {
                    synchronized (this) {
                        keyValidationHandler = validationHandler;
                    }
                }
            }
        } catch (InstantiationException e) {
            log.error("Error while instantiating class" + e.toString());
        } catch (IllegalAccessException e) {
            log.error("Error while accessing class" + e.toString());
        } catch (ClassNotFoundException e) {
            log.error("Error while creating keyManager instance" + e.toString());
        }
    }

    /**
     * Validates the access tokens issued for a particular user to access an API.
     *
     * @param context     Requested context
     * @param accessToken Provided access token
     * @return APIKeyValidationInfoDTO with authorization info and tier info if authorized. If it is not
     * authorized, tier information will be <pre>null</pre>
     * @throws APIKeyMgtException Error occurred when accessing the underlying database or registry.
     */
    public APIKeyValidationInfoDTO validateKey(String context, String version, String accessToken,
                                               String requiredAuthenticationLevel, String clientDomain,
                                               String matchingResource, String httpVerb)
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

        if (log.isDebugEnabled()) {
            String logMsg = "KeyValidation request from gateway: requestTime=" + new Date(System.currentTimeMillis());
            if (activityID != null) {
                logMsg = logMsg + " , transactionId=" + activityID;
            }
            log.debug(logMsg);
        }

        TokenValidationContext validationContext = new TokenValidationContext();
        validationContext.setAccessToken(accessToken);
        validationContext.setClientDomain(clientDomain);
        validationContext.setContext(context);
        validationContext.setHttpVerb(httpVerb);
        validationContext.setMatchingResource(matchingResource);
        validationContext.setRequiredAuthenticationLevel(requiredAuthenticationLevel);
        validationContext.setValidationInfoDTO(new APIKeyValidationInfoDTO());
        validationContext.setVersion(version);

        String cacheKey = APIUtil.getAccessTokenCacheKey(accessToken,
                                                         context, version, matchingResource, httpVerb, requiredAuthenticationLevel);

        validationContext.setCacheKey(cacheKey);

        APIKeyValidationInfoDTO infoDTO = APIKeyMgtUtil.getFromKeyManagerCache(cacheKey);

        if (infoDTO != null) {
            validationContext.setCacheHit(true);
            log.debug("APIKeyValidationInfoDTO fetched from cache. Setting cache hit to true...");
            validationContext.setValidationInfoDTO(infoDTO);
        }

        log.debug("Before calling Validate Token method...");
        boolean state = keyValidationHandler.validateToken(validationContext);
        log.debug("State after calling validateToken ... " + state);

        if (state) {
            state = keyValidationHandler.validateSubscription(validationContext);
        }

        log.debug("State after calling validateSubscription... " + state);

        if (state) {
            state = keyValidationHandler.validateScopes(validationContext);
        }

        log.debug("State after calling validateScopes... " + state);

        if (state && APIKeyMgtDataHolder.isJwtGenerationEnabled() 
                && validationContext.getValidationInfoDTO().getEndUserName() != null) {
            keyValidationHandler.generateConsumerToken(validationContext);
        }
        log.debug("State after calling generateConsumerToken... " + state);

        if (!validationContext.isCacheHit()) {
            APIKeyMgtUtil.writeToKeyManagerCache(cacheKey, validationContext.getValidationInfoDTO());
        }

        if (log.isDebugEnabled() && axis2MessageContext != null) {
            logMessageDetails(axis2MessageContext, validationContext.getValidationInfoDTO());
        }

        if (log.isDebugEnabled()) {
            log.debug("APIKeyValidationInfoDTO before returning : " + validationContext.getValidationInfoDTO());
        }

        return validationContext.getValidationInfoDTO();
    }

    /**
     * Return the URI Templates for an API
     *
     * @param context Requested context
     * @param version API Version
     * @return APIKeyValidationInfoDTO with authorization info and tier info if authorized. If it is not
     * authorized, tier information will be <pre>null</pre>
     * @throws APIKeyMgtException Error occurred when accessing the underlying database or registry.
     */
    public ArrayList<URITemplate> getAllURITemplates(String context, String version)
            throws APIKeyMgtException, APIManagementException {

        return ApiMgtDAO.getAllURITemplates(context, version);

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
