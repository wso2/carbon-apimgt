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

import com.google.common.net.HttpHeaders;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceReferenceHolder.class)
public class OAuthAuthenticatorTest {


    @Test
    public void initOAuthParams() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        OAuthAuthenticator oauthAuthenticator = new OauthAuthenticatorWrapper(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.REMOVE_OAUTH_HEADERS_FROM_MESSAGE))
                .thenReturn("true");
        Mockito.when(apiManagerConfiguration.getJwtConfigurationDto()).thenReturn(new JWTConfigurationDto());
    }


    @Test
    public void authenticate() throws Exception {
    }

    @Test
    public void extractCustomerKeyFromAuthHeader() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        OAuthAuthenticator oauthAuthenticator = new OauthAuthenticatorWrapper(apiManagerConfiguration);

        Map map = new HashMap();
        Assert.assertNull("Assertion failure due to not null", oauthAuthenticator.extractCustomerKeyFromAuthHeader
                (map));
        map.put(HttpHeaders.AUTHORIZATION, "Bearer abcde-fghij");
        Assert.assertNotNull(oauthAuthenticator.extractCustomerKeyFromAuthHeader(map), "Assertion failure due to null");

    }


    @Test
    public void getRequestOrigin() throws Exception {
    }

    @Test
    public void getSecurityHeader() throws Exception {
    }

    @Test
    public void setSecurityHeader() throws Exception {
    }

    @Test
    public void getDefaultAPIHeader() throws Exception {
    }

    @Test
    public void setDefaultAPIHeader() throws Exception {
    }

    @Test
    public void getConsumerKeyHeaderSegment() throws Exception {
    }

    @Test
    public void setConsumerKeyHeaderSegment() throws Exception {
    }

    @Test
    public void getOauthHeaderSplitter() throws Exception {
    }

    @Test
    public void setOauthHeaderSplitter() throws Exception {
    }

    @Test
    public void getConsumerKeySegmentDelimiter() throws Exception {
    }

    @Test
    public void setConsumerKeySegmentDelimiter() throws Exception {
    }

    @Test
    public void getSecurityContextHeader() throws Exception {
    }

    @Test
    public void setSecurityContextHeader() throws Exception {
    }

    @Test
    public void isRemoveOAuthHeadersFromOutMessage() throws Exception {
    }

    @Test
    public void setRemoveOAuthHeadersFromOutMessage() throws Exception {
    }

    @Test
    public void getClientDomainHeader() throws Exception {
    }

    @Test
    public void setClientDomainHeader() throws Exception {
    }

    @Test
    public void isRemoveDefaultAPIHeaderFromOutMessage() throws Exception {
    }

    @Test
    public void setRemoveDefaultAPIHeaderFromOutMessage() throws Exception {
    }

    @Test
    public void setRequestOrigin() throws Exception {
    }

}