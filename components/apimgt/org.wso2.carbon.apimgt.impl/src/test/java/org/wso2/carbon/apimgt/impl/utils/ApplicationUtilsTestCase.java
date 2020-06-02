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
package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.ApiMgtDAOMockCreator;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KeyManagerHolder.class, ServiceReferenceHolder.class, LogFactory.class, ApiMgtDAO.class})
public class ApplicationUtilsTestCase {

    private KeyManager keyManager = Mockito.mock(KeyManager.class);
    private static ApiMgtDAO apiMgtDAO;
    private static ApiMgtDAOMockCreator apiMgtDAOMockCreator;

    @Before
    public void setup() throws UserStoreException, RegistryException {

        apiMgtDAOMockCreator = new ApiMgtDAOMockCreator(444);
        apiMgtDAO = apiMgtDAOMockCreator.getMock();
    }

    @Test
    public void testCreateTokenRequestWhenAccessTokenIsNull() throws APIManagementException {

        PowerMockito.mockStatic(KeyManagerHolder.class);
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        ApplicationUtils.createAccessTokenRequest(keyManager, oAuthApplicationInfo, null);
        Mockito.verify(keyManager, Mockito.times(1))
                .buildAccessTokenRequestFromOAuthApp(Matchers.any(OAuthApplicationInfo.class),
                        Matchers.any(AccessTokenRequest.class));
    }

    @Test
    public void testCreateTokenRequestWhenAccessTokenNotNull() throws APIManagementException {

        PowerMockito.mockStatic(KeyManagerHolder.class);
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        ApplicationUtils.createAccessTokenRequest(keyManager, oAuthApplicationInfo, accessTokenRequest);
        Mockito.verify(keyManager, Mockito.times(1))
                .buildAccessTokenRequestFromOAuthApp(Matchers.any(OAuthApplicationInfo.class),
                        Matchers.any(AccessTokenRequest.class));
    }

    @Test
    public void testCreateTokenRequestWhenKeyManagerNull() throws APIManagementException {

        PowerMockito.mockStatic(KeyManagerHolder.class);
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        AccessTokenRequest result = ApplicationUtils.createAccessTokenRequest(null, oAuthApplicationInfo,
                accessTokenRequest);
        Assert.assertNull(result);
        Mockito.verify(keyManager, Mockito.times(0))
                .buildAccessTokenRequestFromOAuthApp(Matchers.any(OAuthApplicationInfo.class),
                        Matchers.any(AccessTokenRequest.class));
    }

    @Test
    public void testPopulateTokenRequestWhenAccessTokenIsNull() throws APIManagementException {

        PowerMockito.mockStatic(KeyManagerHolder.class);
        ApplicationUtils.populateTokenRequest(keyManager, "", null);
        Mockito.verify(keyManager, Mockito.times(1))
                .buildAccessTokenRequestFromJSON(Matchers.anyString(),
                        Matchers.any(AccessTokenRequest.class));
    }

    @Test
    public void testPopulateTokenRequestWhenAccessTokenNotNull() throws APIManagementException {

        PowerMockito.mockStatic(KeyManagerHolder.class);
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        ApplicationUtils.populateTokenRequest(keyManager, "", accessTokenRequest);
        Mockito.verify(keyManager, Mockito.times(1))
                .buildAccessTokenRequestFromJSON(Matchers.anyString(),
                        Matchers.any(AccessTokenRequest.class));
    }

    @Test
    public void testPopulateTokenRequestWhenKeyManagerNull() throws APIManagementException {

        PowerMockito.mockStatic(KeyManagerHolder.class);
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        ApplicationUtils.populateTokenRequest(null, "", accessTokenRequest);
        Mockito.verify(keyManager, Mockito.times(0))
                .buildAccessTokenRequestFromJSON(Matchers.anyString(), Matchers.any(AccessTokenRequest.class));
    }

    @Test
    public void testCrateOauthAppRequest() throws APIManagementException {

        PowerMockito.mockStatic(KeyManagerHolder.class);
        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "default")).thenReturn(keyManager);
        OAuthAppRequest oAuthAppRequest = ApplicationUtils
                .createOauthAppRequest("client1", "clientId", "http://foo.com", "subscribe", "details", "DEFAULT",
                        "carbon.super", "default");
        Assert.assertNotNull(oAuthAppRequest);
    }

    @Test
    public void testRetrieveApplication() throws Exception {

        String appName = "NewApp";
        String owner = "DevX";
        String tenant = "wso2.com";
        Subscriber subscriber = new Subscriber(owner);
        Application newApp = new Application(appName, subscriber);
        newApp.setId(3);
        newApp.setTier("Gold");
        newApp.setDescription("This is a new app");
        newApp.setOwner(owner);
        newApp.setGroupId("group1");
        Mockito.when(apiMgtDAO.getApplicationByName(appName, owner, tenant)).thenReturn(newApp);

        Application app = ApplicationUtils.retrieveApplication(appName, owner, tenant);

        Assert.assertEquals(newApp, app);
        Assert.assertEquals(newApp.getDescription(), app.getDescription());
        Assert.assertEquals(newApp.getOwner(), app.getOwner());
        Assert.assertEquals(newApp.getGroupId(), app.getGroupId());
        Assert.assertEquals(newApp.getTier(), app.getTier());
    }
}
