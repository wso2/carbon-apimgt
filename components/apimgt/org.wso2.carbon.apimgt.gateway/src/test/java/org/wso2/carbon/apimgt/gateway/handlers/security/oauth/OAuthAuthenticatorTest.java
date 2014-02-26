/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.oauth;

import junit.framework.TestCase;
import org.wso2.carbon.apimgt.gateway.handlers.security.TestAPIKeyValidator;
import org.wso2.carbon.apimgt.gateway.TestUtils;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

public class OAuthAuthenticatorTest  {  //Removed extends TestCase
    /*
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(
                new APIManagerConfigurationServiceImpl(new APIManagerConfiguration()));
    }

    public void testSimpleAuthentication() throws Exception {
        TestAPIKeyValidator keyValidator = new TestAPIKeyValidator();        
        APIKeyValidationInfoDTO goldUser = new APIKeyValidationInfoDTO();
        goldUser.setAuthorized(true);
        goldUser.setTier("Gold");
        APIKeyValidationInfoDTO silverUser = new APIKeyValidationInfoDTO();
        silverUser.setAuthorized(true);
        silverUser.setTier("Silver");        
        keyValidator.addUserInfo("/foo", "1.0.0", "abcde12345", goldUser);
        keyValidator.addUserInfo("/foo", "1.0.0", "pqrst67890", silverUser);
        
        TestAuthenticator authenticator = new TestAuthenticator(keyValidator);
        assertTrue(authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "abcde12345")));
        assertTrue(authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "pqrst67890")));

        try {
            authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "klmno09876"));
            fail("No exception thrown on auth failure");
        } catch (APISecurityException e) {
           // assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
           // System.out.println("Expected error received: " + e.getMessage());
        }

        try {
            authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0"));
            fail("No exception thrown on auth failure");
        } catch (APISecurityException e) {
            assertEquals(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS, e.getErrorCode());
            System.out.println("Expected error received: " + e.getMessage());
        }
    }

    public void testAdvancedAuthentication1() throws Exception {
        TestAPIKeyValidator fooKeyValidator = new TestAPIKeyValidator();
        TestAPIKeyValidator barKeyValidator = new TestAPIKeyValidator();
        APIKeyValidationInfoDTO goldUser = new APIKeyValidationInfoDTO();
        goldUser.setAuthorized(true);
        goldUser.setTier("Gold");
        APIKeyValidationInfoDTO silverUser = new APIKeyValidationInfoDTO();
        silverUser.setAuthorized(true);
        silverUser.setTier("Silver");
        fooKeyValidator.addUserInfo("/foo", "1.0.0", "abcde12345", goldUser);
        fooKeyValidator.addUserInfo("/bar", "1.0.0", "pqrst67890", silverUser);
        barKeyValidator.addUserInfo("/foo", "1.0.0", "abcde12345", goldUser);
        barKeyValidator.addUserInfo("/bar", "1.0.0", "pqrst67890", silverUser);

        TestAuthenticator fooAuthenticator = new TestAuthenticator(fooKeyValidator);
        TestAuthenticator barAuthenticator = new TestAuthenticator(barKeyValidator);

        assertTrue(fooAuthenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "abcde12345")));
        try {
            fooAuthenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "pqrst67890"));
        } catch (APISecurityException e) {
            //assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }

        assertTrue(barAuthenticator.authenticate(TestUtils.getMessageContext("/bar", "1.0.0", "pqrst67890")));
        try {
            barAuthenticator.authenticate(TestUtils.getMessageContext("/bar", "1.0.0", "abcde12345"));
        } catch (APISecurityException e) {
           // assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }
        try {
            barAuthenticator.authenticate(TestUtils.getMessageContext("/bar", "1.5.0", "pqrst67890"));
        } catch (APISecurityException e) {
           // assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }
    } */

}
