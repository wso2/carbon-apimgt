package org.wso2.carbon.apimgt.core.impl;
/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import feign.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.auth.DCRMServiceStub;
import org.wso2.carbon.apimgt.core.auth.OAuth2ServiceStubs;
import org.wso2.carbon.apimgt.core.auth.ScopeRegistration;
import org.wso2.carbon.apimgt.core.configuration.models.KeyMgtConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

/**
 * This class holds the key manager implementation for light weight key manager
 */
public class WSO2ISKeyManagerImpl extends DefaultKeyManagerImpl {
    private static final Logger log = LoggerFactory.getLogger(WSO2ISKeyManagerImpl.class);
    private static final String SUPER_TENANT_SUFFIX = "@carbon.super";

    public WSO2ISKeyManagerImpl(KeyMgtConfigurations keyManagerConfigs) throws APIManagementException {
    }

    public WSO2ISKeyManagerImpl() throws APIManagementException {
        super();
    }

    public WSO2ISKeyManagerImpl(DCRMServiceStub dcrmServiceStub, OAuth2ServiceStubs oAuth2ServiceStubs,
                                ScopeRegistration scopeRegistration,
                                KeyMgtConfigurations
                                        keyManagerConfigs) throws APIManagementException {
        super(dcrmServiceStub, oAuth2ServiceStubs, scopeRegistration);
    }

    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws KeyManagementException {
        AccessTokenInfo accessTokenInfo = super.getTokenMetaData(accessToken);
        if (StringUtils.isNotEmpty(accessTokenInfo.getEndUserName())) {
            accessTokenInfo.setEndUserName(accessTokenInfo.getEndUserName().replace(SUPER_TENANT_SUFFIX, ""));
        }
        return accessTokenInfo;
    }
    //TODO: Remove after revoke endpoint implementation done in key manager.
    @Override
    public void revokeAccessToken(String accessToken, String clientId, String clientSecret)
            throws KeyManagementException {
        log.debug("Revoking access token");
        Response response;
        try {
            response = oAuth2ServiceStubs.getRevokeServiceStub().revokeAccessToken(accessToken, clientId,
                    clientSecret);
        } catch (APIManagementException e) {
            throw new KeyManagementException("Error occurred while revoking current access token", e,
                    ExceptionCodes.ACCESS_TOKEN_REVOKE_FAILED);
        }
        if (response == null) {
            throw new KeyManagementException("Error occurred while revoking current access token. " +
                    "Response is null", ExceptionCodes.ACCESS_TOKEN_REVOKE_FAILED);
        }
        if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
            if (log.isDebugEnabled()) {
                log.debug("Successfully revoked access token: " + accessToken);
            }
        } else {
            throw new KeyManagementException("Token revocation failed. HTTP error code: " + response.status()
                    + " Error Response Body: " + response.body().toString(),
                    ExceptionCodes.ACCESS_TOKEN_REVOKE_FAILED);
        }
    }

}
