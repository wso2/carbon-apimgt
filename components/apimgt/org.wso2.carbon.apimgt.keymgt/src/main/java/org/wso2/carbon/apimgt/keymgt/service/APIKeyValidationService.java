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
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.handlers.KeyValidationHandler;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        Timer timer = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "VALIDATE_MAIN"));
        Timer.Context timerContext = timer.start();

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
                    if(headersMap != null) {
                        headersList.add(new Header("activityID", (String) headersMap.get("activityID")));
                    }

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

        Timer timer2 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "VALIDATE_TOKEN"));
        Timer.Context timerContext2 = timer2.start();
        boolean state = keyValidationHandler.validateToken(validationContext);
        timerContext2.stop();
        log.debug("State after calling validateToken ... " + state);

        if (state) {
            Timer timer3 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                    APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "VALIDATE_SUBSCRIPTION"));
            Timer.Context timerContext3 = timer3.start();
            state = keyValidationHandler.validateSubscription(validationContext);
            timerContext3.stop();
        }

        log.debug("State after calling validateSubscription... " + state);

        if (state) {
            Timer timer4 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                    APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "VALIDATE_SCOPES"));
            Timer.Context timerContext4 = timer4.start();
            state = keyValidationHandler.validateScopes(validationContext);
            timerContext4.stop();
        }

        log.debug("State after calling validateScopes... " + state);

        if (state && APIKeyMgtDataHolder.isJwtGenerationEnabled() &&
                validationContext.getValidationInfoDTO().getEndUserName() != null && !validationContext.isCacheHit()) {
            Timer timer5 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                    APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "GENERATE_JWT"));
            Timer.Context timerContext5 = timer5.start();
            keyValidationHandler.generateConsumerToken(validationContext);
            timerContext5.stop();
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

        timerContext.stop();
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
        Timer timer6 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "GET_URI_TEMPLATE"));
        Timer.Context timerContext6 = timer6.start();
        ArrayList<URITemplate> templates = ApiMgtDAO.getInstance().getAllURITemplates(context, version);
        timerContext6.stop();
        return templates;
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

    /**
     * validate access token for websocket handshake
     *
     * @param context context of the API
     * @param version version of the API
     * @param accessToken access token of the request
     * @return api information
     * @throws APIKeyMgtException
     * @throws APIManagementException
     */
    public APIKeyValidationInfoDTO validateKeyforHandshake(String context, String version,
                                                           String accessToken)
            throws APIKeyMgtException, APIManagementException {

        APIKeyValidationInfoDTO info = new APIKeyValidationInfoDTO();
        info.setAuthorized(false);
        TokenValidationContext validationContext = new TokenValidationContext();
        validationContext.setAccessToken(accessToken);
        validationContext.setContext(context);
        validationContext.setValidationInfoDTO(new APIKeyValidationInfoDTO());
        validationContext.setVersion(version);
        validationContext.setRequiredAuthenticationLevel("Any");
        boolean state = keyValidationHandler.validateToken(validationContext);
        if (state) {
            info.setAuthorized(true);
            info.setValidityPeriod(validationContext.getTokenInfo().getValidityPeriod());
            info.setIssuedTime(validationContext.getTokenInfo().getIssuedTime());
            info = validateSubscriptionDetails(info, validationContext.getContext(),
                                               validationContext.getVersion(),
                                               validationContext.getTokenInfo().getConsumerKey());
        }
        return info;
    }

    /**
     * Check for the subscription of the user
     *
     * @param infoDTO
     * @param context
     * @param version
     * @param consumerKey
     * @return APIKeyValidationInfoDTO including data of api and application
     * @throws APIManagementException
     */
    public APIKeyValidationInfoDTO validateSubscriptionDetails(APIKeyValidationInfoDTO infoDTO,
                                                               String context, String version,
                                                               String consumerKey)
            throws APIManagementException {
        boolean defaultVersionInvoked = false;
        String apiTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(context);
        if (apiTenantDomain == null) {
            apiTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        String temp_version = isDefaultVersionInvoked(context);
        if (temp_version != null) {
            defaultVersionInvoked = true;
            version = temp_version;
            context += "/" + temp_version;
        }

        int apiOwnerTenantId = APIUtil.getTenantIdFromTenantDomain(apiTenantDomain);
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
                    return infoDTO;
                } else if (APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus) ||
                           APIConstants.SubscriptionStatus.REJECTED.equals(subscriptionStatus)) {
                    infoDTO.setValidationStatus(
                            APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
                    infoDTO.setAuthorized(false);
                    return infoDTO;
                } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED
                                   .equals(subscriptionStatus) &&
                           !APIConstants.API_KEY_TYPE_SANDBOX.equals(type)) {
                    infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    infoDTO.setType(type);
                    infoDTO.setAuthorized(false);
                    return infoDTO;
                }

                final String API_PROVIDER = rs.getString("API_PROVIDER");
                final String SUB_TIER = rs.getString("TIER_ID");
                final String APP_TIER = rs.getString("APPLICATION_TIER");
                infoDTO.setTier(SUB_TIER);
                infoDTO.setSubscriber(rs.getString("USER_ID"));
                infoDTO.setApplicationId(rs.getString("APPLICATION_ID"));
                infoDTO.setApiName(rs.getString("API_NAME"));
                infoDTO.setApiPublisher(API_PROVIDER);
                infoDTO.setApplicationName(rs.getString("NAME"));
                infoDTO.setApplicationTier(APP_TIER);
                infoDTO.setType(type);

                //this is done to support default websocket apis
                if (defaultVersionInvoked) {
                    infoDTO.setApiName(rs.getString("API_NAME") + "*" + version);
                }

                //Advanced Level Throttling Related Properties
                if (APIUtil.isAdvanceThrottlingEnabled()) {
                    String apiTier = rs.getString("API_TIER");
                    String subscriberUserId = rs.getString("USER_ID");
                    String subscriberTenant = MultitenantUtils.getTenantDomain(subscriberUserId);
                    int apiId = rs.getInt("API_ID");
                    int subscriberTenantId = APIUtil.getTenantId(subscriberUserId);
                    int apiTenantId = APIUtil.getTenantId(API_PROVIDER);
                    //TODO isContentAware
                    boolean isContentAware =
                            isAnyPolicyContentAware(conn, apiTier, APP_TIER, SUB_TIER,
                                                    subscriberTenantId, apiTenantId, apiId);
                    infoDTO.setContentAware(isContentAware);

                    //TODO this must implement as a part of throttling implementation.
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
                return infoDTO;
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
                log.error("Error occurred while fetching data: " + e, e);
            }
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return infoDTO;
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

    private String isDefaultVersionInvoked(String context) throws APIManagementException {

        String apiName = "";
        String apiProvider = "";
        String sql = SQLConstants.GET_API_FOR_CONTEXT_TEMPLATE_SQL;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(true);
            ps = conn.prepareStatement(sql);
            ps.setString(1, context);

            rs = ps.executeQuery();
            if (rs.first()) {
                apiName = rs.getString("API_NAME");
                apiProvider = rs.getString("API_PROVIDER");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                log.error("Error occurred while fetching data: " + e, e);
            }
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }

        if (!(apiName.equalsIgnoreCase("") || apiProvider.equalsIgnoreCase(""))) {
            ApiMgtDAO dao = ApiMgtDAO.getInstance();
            return dao.getDefaultVersion(new APIIdentifier(apiProvider, apiName, ""));
        }
        return null;
    }
}
