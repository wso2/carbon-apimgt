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

package org.wso2.carbon.apimgt.impl.notification;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.impl.notification.util.NotificationExecutorWrapper;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.Registry;

import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class})
public class NotificationExecutorTest {
    private Registry registry;
    private NotificationExecutor notificationExecutor;
    private final String TENANT_DOMAIN = "carbon.super";
    private final int TENANT_ID = -1234;
    private final String validNotifierConf = "{\"NotificationsEnabled\":\"true\",\n" + "  \"Notifications\":[{\n"
            + "    \"Type\":\"new_api_version\",\n" + "    \"Notifiers\" :[{\n"
            + "      \"Class\":\"org.wso2.carbon.apimgt.impl.notification.NewAPIVersionEmailNotifier\",\n"
            + "      \"ClaimsRetrieverImplClass\":\"org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever\",\n"
            + "      \"Title\": \"New Version Released\",\n"
            + "      \"Template\": \"<html></html>\""
            + "    }]\n" + "  }\n" + "  ]}";
    private final String invalidNotifierConf = "{\"NotificationsEnabled\":\"true\",\n" + "  \"Notifications\":[{\n"
            + "    \"Type\":\"new_api_version\",\n" + "    \"Notifiers\" :[{\n"
            + "      \"Class\":\"\",\n"
            + "      \"ClaimsRetrieverImplClass\":\"org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever\",\n"
            + "      \"Title\": \"New Version Released\",\n"
            + "      \"Template\": \"<html></html>\""
            + "    }]\n" + "  }\n" + "  ]}";


    @Before
    public void setup() {
        registry = Mockito.mock(Registry.class);
        notificationExecutor = new NotificationExecutorWrapper(registry);
        PowerMockito.mockStatic(APIUtil.class);
    }


    @Test
    public void testShouldSendNotificationWhenConfigurationExists() throws Exception {
        NotificationDTO notificationDTO = new NotificationDTO(new Properties(), NotifierConstants
                .NOTIFICATION_TYPE_NEW_VERSION);
        notificationDTO.setTenantDomain(TENANT_DOMAIN);
        notificationDTO.setTenantID(TENANT_ID);
        Notifier notifier = Mockito.mock(Notifier.class);
        Mockito.doNothing().when(notifier).run();
        PowerMockito.when(APIUtil.class, "getTenantConfig", TENANT_DOMAIN).thenReturn(new JSONParser().parse(validNotifierConf));
        PowerMockito.when(APIUtil.class,"getClassInstance","org.wso2.carbon.apimgt.impl.notification" +
                ".NewAPIVersionEmailNotifier").thenReturn(notifier);
        try {
            notificationExecutor.sendAsyncNotifications(notificationDTO);
        } catch (NotificationException e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testShouldNotThrowExceptionWhenConfigurationNotFound() throws Exception {
        NotificationDTO notificationDTO = new NotificationDTO(new Properties(), NotifierConstants
                .NOTIFICATION_TYPE_NEW_VERSION);
        notificationDTO.setTenantID(TENANT_ID);
        notificationDTO.setTenantDomain(TENANT_DOMAIN);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NotifierConstants.Notifications_KEY,new JSONArray());
        PowerMockito.when(APIUtil.class, "getTenantConfig", TENANT_DOMAIN).thenReturn(jsonObject);
        try {
            notificationExecutor.sendAsyncNotifications(notificationDTO);
        } catch (NotificationException e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testShouldNotThrowExceptionWhenNotificationTypeNotRegistered() throws Exception {
        NotificationDTO notificationDTO = new NotificationDTO(new Properties(), "new_subscription");
        notificationDTO.setTenantDomain(TENANT_DOMAIN);
        notificationDTO.setTenantID(TENANT_ID);
        Notifier notifier = Mockito.mock(Notifier.class);

        Mockito.doNothing().when(notifier).run();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NotifierConstants.Notifications_KEY,new JSONArray());
        PowerMockito.when(APIUtil.class, "getTenantConfig", TENANT_DOMAIN).thenReturn(jsonObject);
        try {
            notificationExecutor.sendAsyncNotifications(notificationDTO);
        } catch (NotificationException e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testShouldNotThrowExceptionWhenNotifierNotDefined() throws Exception {
        NotificationDTO notificationDTO = new NotificationDTO(new Properties(), NotifierConstants
                .NOTIFICATION_TYPE_NEW_VERSION);
        notificationDTO.setTenantDomain(TENANT_DOMAIN);
        notificationDTO.setTenantID(TENANT_ID);
        Notifier notifier = Mockito.mock(Notifier.class);

        Mockito.doNothing().when(notifier).run();
        PowerMockito.when(APIUtil.class, "getTenantConfig", TENANT_DOMAIN).thenReturn(new JSONParser().parse(invalidNotifierConf));

        try {
            notificationExecutor.sendAsyncNotifications(notificationDTO);
        } catch (NotificationException e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test(expected = NotificationException.class)
    public void testShouldThrowExceptionsWhenRegistryErrorOccurs() throws Exception {
        NotificationDTO notificationDTO = new NotificationDTO(new Properties(), NotifierConstants
                .NOTIFICATION_TYPE_NEW_VERSION);
        notificationDTO.setTenantDomain(TENANT_DOMAIN);
        notificationDTO.setTenantID(TENANT_ID);
        Notifier notifier = Mockito.mock(Notifier.class);

        Mockito.doNothing().when(notifier).run();
        PowerMockito.when(APIUtil.class, "getTenantConfig", TENANT_DOMAIN).thenThrow(APIManagementException.class);

        notificationExecutor.sendAsyncNotifications(notificationDTO);
        Assert.fail("Should throw an exception");
    }

}
