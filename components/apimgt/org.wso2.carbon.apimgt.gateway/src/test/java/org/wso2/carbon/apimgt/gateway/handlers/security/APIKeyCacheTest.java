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

package org.wso2.carbon.apimgt.gateway.handlers.security;

import junit.framework.TestCase;
import org.wso2.carbon.apimgt.gateway.TestUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.oauth.TestAuthenticator;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

public class APIKeyCacheTest {
    /*
    public void testCaching() throws Exception {
        APIKeyCacheFactory.getInstance().reset();
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
        assertTrue(authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "abcde12345")));
        assertTrue(authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "pqrst67890")));
        assertTrue(authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "abcde12345")));
        assertTrue(authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "pqrst67890")));

        assertEquals(2, keyValidator.getCounter());

        try {
            authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "klmno09876"));
            fail("No exception thrown on auth failure");
        } catch (APISecurityException e) {
            assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }

        try {
            authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "klmno09876"));
            fail("No exception thrown on auth failure");
        } catch (APISecurityException e) {
            assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }

        assertEquals(3, keyValidator.getCounter());
    }
    
    public void testLRUBehavior() throws Exception {
        APIKeyCacheFactory.getInstance().reset();
        TestAPIKeyValidator keyValidator = new TestAPIKeyValidator();
        TestAuthenticator authenticator = new TestAuthenticator(keyValidator);

        String testKey = "abcde12345";
        // Send a few requests with the testKey - This will be cached and hence the counter will be 1
        for (int i = 0; i < 10; i++) {
            try {
                authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", testKey));
            } catch (APISecurityException e) {
                assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
            }
        }
        assertEquals(1, keyValidator.getCounter());
        
        // Send requests with random keys - Each request will increment the counter by 1
        // Also the cache will get filled up to the capacity
        for (int i = 1; i < APISecurityConstants.DEFAULT_MAX_INVALID_KEYS; i++) {
            try {
                authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "abcde12345_" + i));
            } catch (APISecurityException e) {
                assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
            }
        }
        assertEquals(APISecurityConstants.DEFAULT_MAX_INVALID_KEYS, keyValidator.getCounter());

        // We send the same set of requests - This time all will be served from the cache
        // Hence the counter will not change
        for (int i = 1; i < APISecurityConstants.DEFAULT_MAX_INVALID_KEYS; i++) {
            try {
                authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "abcde12345_" + i));
            } catch (APISecurityException e) {
                assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
            }
        }
        assertEquals(APISecurityConstants.DEFAULT_MAX_INVALID_KEYS, keyValidator.getCounter());

        // Now send a request with a whole different key - Since the cache is full something
        // needs to be evicted. It should be the testKey entry since that's the least recently
        // used entry as at this point. Because our current key is brand new the counter will
        // increment by 1
        try {
            authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", "pqrst"));
        } catch (APISecurityException e) {
            assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }
        assertEquals(APISecurityConstants.DEFAULT_MAX_INVALID_KEYS + 1, keyValidator.getCounter());

        // Try sending another request with testKey. Since it was evicted, the counter must
        // get incremented
        try {
            authenticator.authenticate(TestUtils.getMessageContext("/foo", "1.0.0", testKey));
        } catch (APISecurityException e) {
            assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }
        assertEquals(APISecurityConstants.DEFAULT_MAX_INVALID_KEYS + 2, keyValidator.getCounter());
    } */

}
