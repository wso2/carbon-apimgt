/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.RefreshTokenValidationDataDO;

import java.util.Map;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.net.ssl.*" })
@PrepareForTest({ APIMOAuthEventInterceptor.class})
public class APIMOAuthEventInterceptorTest {

    private static final Log log = LogFactory.getLog(APIMOAuthEventInterceptorTest.class);
    private APIMOAuthEventInterceptor APIMOAuthEventInterceptor;
    private OAuthRevocationRequestDTO revokeRequestDTO;
    private OAuthRevocationResponseDTO revokeResponseDTO;
    private AccessTokenDO accessTokenDO;
    private RefreshTokenValidationDataDO refreshTokenDO;
    private Map<String, Object> params;

    @Before
    public void Init() throws Exception {

        APIMOAuthEventInterceptor = Mockito.mock(APIMOAuthEventInterceptor.class);
        revokeRequestDTO = PowerMockito.mock(OAuthRevocationRequestDTO.class);
        revokeResponseDTO = PowerMockito.mock(OAuthRevocationResponseDTO.class);
        refreshTokenDO = PowerMockito.mock(RefreshTokenValidationDataDO.class);
        accessTokenDO = PowerMockito.mock(AccessTokenDO.class);

        PowerMockito.whenNew(APIMOAuthEventInterceptor.class).withNoArguments().thenReturn(APIMOAuthEventInterceptor);
        PowerMockito.when(APIMOAuthEventInterceptor.isEnabled()).thenReturn(true);
        PowerMockito.doNothing().when(APIMOAuthEventInterceptor)
                .onPostTokenRevocationByClient(revokeRequestDTO, revokeResponseDTO, accessTokenDO, refreshTokenDO,
                        params);
    }

    @Test
    public void testOnPostTokenRevocationByClient() {

        log.info("Running the test case to check the onPostTokenRevocationByClient method");
        try {
            APIMOAuthEventInterceptor = new APIMOAuthEventInterceptor();
            APIMOAuthEventInterceptor
                    .onPostTokenRevocationByClient(revokeRequestDTO, revokeResponseDTO, accessTokenDO, refreshTokenDO,
                            params);
        } catch (Exception e) {
            Assert.fail("Should not throw any exceptions");
        }
        log.info("Finished the test case to check the onPostTokenRevocationByClient method");

    }

    @Test
    public void testIsEnabled() {

        log.info("Running the test case to check the return string of the isEnabled method.");
        APIMOAuthEventInterceptor = new APIMOAuthEventInterceptor();
        Assert.assertTrue("Checking revocation interceptor is enabled", APIMOAuthEventInterceptor.isEnabled());
        log.info("Finished the test case to check the return string of the isEnabled method.");

    }

}