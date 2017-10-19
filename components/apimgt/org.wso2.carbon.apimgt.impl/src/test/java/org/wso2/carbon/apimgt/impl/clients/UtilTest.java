/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.clients;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.core.util.KeyStoreManager;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ KeyStoreManager.class })
public class UtilTest {
    private PrivateKey privateKey;

    public UtilTest() throws NoSuchAlgorithmException {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        keygen.initialize(1024);
        privateKey = keygen.generateKeyPair().getPrivate();
    }

    @Test
    public void testShouldSetAuthHeaders() throws Exception {
        Options options = new Options();
        PowerMockito.mockStatic(KeyStoreManager.class);
        KeyStoreManager keyStoreManager = Mockito.mock(KeyStoreManager.class);
        ServiceClient serviceClient = Mockito.mock(ServiceClient.class);

        PowerMockito.when(KeyStoreManager.getInstance(Mockito.anyInt())).thenReturn(keyStoreManager);
        Mockito.when(serviceClient.getOptions()).thenReturn(options);
        Mockito.when(keyStoreManager.getDefaultPrimaryCertificate()).thenReturn(null);
        Mockito.when(keyStoreManager.getDefaultPrivateKey()).thenReturn(privateKey);

        Util.setAuthHeaders(serviceClient, "admin");
    }

    @Test(expected = Exception.class)
    public void testShouldThrowEceptionWhenRetrievingCertificateFails() throws Exception {
        PowerMockito.mockStatic(KeyStoreManager.class);
        KeyStoreManager keyStoreManager = Mockito.mock(KeyStoreManager.class);

        PowerMockito.when(KeyStoreManager.getInstance(Mockito.anyInt())).thenReturn(keyStoreManager);
        Mockito.when(keyStoreManager.getDefaultPrimaryCertificate()).thenThrow(Exception.class);
        Mockito.when(keyStoreManager.getDefaultPrivateKey()).thenReturn(privateKey);

        Util.getAuthHeader("admin");
    }

    @Test(expected = Exception.class)
    public void testShouldThrowEceptionWhenSigningFails() throws Exception {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        keygen.initialize(512);
        PrivateKey pvtKey = keygen.generateKeyPair().getPrivate();
        PowerMockito.mockStatic(KeyStoreManager.class);
        KeyStoreManager keyStoreManager = Mockito.mock(KeyStoreManager.class);

        PowerMockito.when(KeyStoreManager.getInstance(Mockito.anyInt())).thenReturn(keyStoreManager);
        Mockito.when(keyStoreManager.getDefaultPrimaryCertificate()).thenReturn(null);
        Mockito.when(keyStoreManager.getDefaultPrivateKey()).thenReturn(pvtKey);

        Util.getAuthHeader("admin");
    }
}
