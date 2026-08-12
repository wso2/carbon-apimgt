/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.core.util.CryptoUtil;

import java.lang.reflect.Method;

/**
 * Unit tests for the AWS secret decrypt guard in APIMappingUtil.decryptSecurity.
 *
 * Kept as a standalone JUnit test (no PowerMockRunner) so plain reflection into the private
 * static method works under JDK 17+ / the module system - PowerMock's custom classloader
 * throws InaccessibleObjectException for the same reflection.
 */
public class APIMappingUtilAwsSecretDecryptTest {

    /**
     * Invokes the private static APIMappingUtil.decryptSecurity(...) via reflection and returns the
     * (possibly mutated) production endpoint-security section.
     */
    @SuppressWarnings("unchecked")
    private static JSONObject invokeDecryptSecurity(String storedSecretKey, CryptoUtil cryptoUtil) throws Exception {
        JSONObject production = new JSONObject();
        if (storedSecretKey != null) {
            production.put(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY, storedSecretKey);
        }
        JSONObject endpointSecurity = new JSONObject();
        endpointSecurity.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION, production);

        Method method = APIMappingUtil.class.getDeclaredMethod(
                "decryptSecurity", JSONObject.class, String.class, CryptoUtil.class);
        method.setAccessible(true);
        method.invoke(null, endpointSecurity, APIConstants.ENDPOINT_SECURITY_PRODUCTION, cryptoUtil);
        return (JSONObject) endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION);
    }

    /**
     * Environment-credentials mode does not encrypt the AWS secret on the save path, so a stray
     * non-empty value (e.g. a masked placeholder) must NOT be passed to base64DecodeAndDecrypt on
     * the read path - otherwise it fails with a CryptoException. The guard should leave it untouched.
     */
    @Test
    public void testDecryptSecuritySkipsAwsSecretWhenNotCipherText() throws Exception {
        String plainSecret = "********";
        CryptoUtil cryptoUtil = Mockito.mock(CryptoUtil.class);
        Mockito.when(cryptoUtil.base64DecodeAndIsSelfContainedCipherText(plainSecret)).thenReturn(false);

        JSONObject production = invokeDecryptSecurity(plainSecret, cryptoUtil);

        Assert.assertEquals("Non-cipher AWS secret should be left unchanged", plainSecret,
                production.get(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY));
        // Decryption must never be attempted on a non-cipher value.
        Mockito.verify(cryptoUtil, Mockito.never()).base64DecodeAndDecrypt(Mockito.anyString());
    }

    /**
     * Stored-credentials mode still encrypts the AWS secret, so a genuine cipher text must be
     * decrypted as before (regression guard for the guard added above).
     */
    @Test
    public void testDecryptSecurityDecryptsAwsSecretWhenCipherText() throws Exception {
        String cipherText = "encryptedSecretValue";
        String decrypted = "plainSecretValue";
        CryptoUtil cryptoUtil = Mockito.mock(CryptoUtil.class);
        Mockito.when(cryptoUtil.base64DecodeAndIsSelfContainedCipherText(cipherText)).thenReturn(true);
        Mockito.when(cryptoUtil.base64DecodeAndDecrypt(cipherText)).thenReturn(decrypted.getBytes());

        JSONObject production = invokeDecryptSecurity(cipherText, cryptoUtil);

        Assert.assertEquals("Cipher AWS secret should be decrypted", decrypted,
                production.get(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY));
        Mockito.verify(cryptoUtil).base64DecodeAndDecrypt(cipherText);
    }
}
