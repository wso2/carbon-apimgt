/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.API;

/**
 * Test class for APISecurityConstants
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(APISecurityConstants.class)
public class APISecurityConstantsTestCase {

    @Before
    public void setup() {
//        PowerMockito.mockStatic(APISecurityConstants.class);
    }

    @Test
    public void testGetAuthenticationFailureMessage() {
        Assert.assertEquals(APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED));
        Assert.assertEquals(APISecurityConstants.API_AUTH_ACCESS_TOKEN_INACTIVE_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.API_AUTH_ACCESS_TOKEN_INACTIVE));
        Assert.assertEquals(APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.API_AUTH_GENERAL_ERROR));
        Assert.assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS));
        Assert.assertEquals(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS));
        Assert.assertEquals(APISecurityConstants.API_AUTH_INCORRECT_API_RESOURCE_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.API_AUTH_INCORRECT_API_RESOURCE));
        Assert.assertEquals(APISecurityConstants.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE));
        Assert.assertEquals(APISecurityConstants.API_BLOCKED_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.API_BLOCKED));
        Assert.assertEquals(APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.API_AUTH_FORBIDDEN));
        Assert.assertEquals(APISecurityConstants.SUBSCRIPTION_INACTIVE_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.SUBSCRIPTION_INACTIVE));
        Assert.assertEquals(APISecurityConstants.INVALID_SCOPE_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(APISecurityConstants.INVALID_SCOPE));
        Assert.assertEquals(APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, APISecurityConstants
                .getAuthenticationFailureMessage(Mockito.anyInt()));
    }

    @Test
    public void testGetFailureMessageDetailDescription() {
        String incorrectResourceDesc = "No matching resource found in the API for the given request. Check the API " +
                "documentation and add a proper REST resource path to the invocation URL";
        String tokenInactiveDesc = "Access Token Inactive. Generate a new access token and try again";
        String missingCredentialsDesc = "Missing Credentials. Make sure your API invocation call has a header: ";
        String tokenExpiredDesc = "Access Token Expired. Renew the access token and try again";
        String invalidCredentialsDesc = "Invalid Credentials. Make sure you have given the correct access token";

        Assert.assertEquals(incorrectResourceDesc, APISecurityConstants
                .getFailureMessageDetailDescription(APISecurityConstants.API_AUTH_INCORRECT_API_RESOURCE,
                        APISecurityConstants.API_AUTH_INCORRECT_API_RESOURCE_MESSAGE));
        Assert.assertEquals(tokenInactiveDesc, APISecurityConstants
                .getFailureMessageDetailDescription(APISecurityConstants.API_AUTH_ACCESS_TOKEN_INACTIVE,
                        APISecurityConstants.API_AUTH_ACCESS_TOKEN_INACTIVE_MESSAGE));
        Assert.assertEquals(missingCredentialsDesc, APISecurityConstants
                .getFailureMessageDetailDescription(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                        APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE));
        Assert.assertEquals(tokenExpiredDesc, APISecurityConstants
                .getFailureMessageDetailDescription(APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED,
                        APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED_MESSAGE));
        Assert.assertEquals(invalidCredentialsDesc, APISecurityConstants
                .getFailureMessageDetailDescription(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE));
    }

    @Test
    public void testStaticClass() {
        PowerMockito.mockStatic(APISecurityConstants.class);
    }
}
