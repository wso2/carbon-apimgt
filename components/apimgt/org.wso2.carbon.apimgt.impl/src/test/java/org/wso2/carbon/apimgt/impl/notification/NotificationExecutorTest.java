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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.impl.notification.util.NotificationExecutorWrapper;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.Properties;

public class NotificationExecutorTest {
    private Registry registry;
    private Resource resource;
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
    }


    @Test
    public void testShouldSendNotificationWhenConfigurationExists() throws RegistryException {
        NotificationDTO notificationDTO = new NotificationDTO(new Properties(), NotifierConstants
                .NOTIFICATION_TYPE_NEW_VERSION);
        notificationDTO.setTenantDomain(TENANT_DOMAIN);
        notificationDTO.setTenantID(TENANT_ID);
        Notifier notifier = Mockito.mock(Notifier.class);
        resource = Mockito.mock(Resource.class);

        Mockito.doNothing().when(notifier).run();
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(resource.getContent()).thenReturn(validNotifierConf.getBytes());
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource);

        try {
            notificationExecutor.sendAsyncNotifications(notificationDTO);
        } catch (NotificationException e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testShouldNotThrowExceptionWhenConfigurationNotFound() throws RegistryException {
        NotificationDTO notificationDTO = new NotificationDTO(new Properties(), NotifierConstants
                .NOTIFICATION_TYPE_NEW_VERSION);
        notificationDTO.setTenantID(TENANT_ID);

        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(false);

        try {
            notificationExecutor.sendAsyncNotifications(notificationDTO);
        } catch (NotificationException e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testShouldNotThrowExceptionWhenNotificationTypeNotRegistered() throws RegistryException {
        NotificationDTO notificationDTO = new NotificationDTO(new Properties(), "new_subscription");
        notificationDTO.setTenantDomain(TENANT_DOMAIN);
        notificationDTO.setTenantID(TENANT_ID);
        Notifier notifier = Mockito.mock(Notifier.class);
        resource = Mockito.mock(Resource.class);

        Mockito.doNothing().when(notifier).run();
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(resource.getContent()).thenReturn(validNotifierConf.getBytes());
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource);

        try {
            notificationExecutor.sendAsyncNotifications(notificationDTO);
        } catch (NotificationException e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testShouldNotThrowExceptionWhenNotifierNotDefined() throws RegistryException {
        NotificationDTO notificationDTO = new NotificationDTO(new Properties(), NotifierConstants
                .NOTIFICATION_TYPE_NEW_VERSION);
        notificationDTO.setTenantDomain(TENANT_DOMAIN);
        notificationDTO.setTenantID(TENANT_ID);
        Notifier notifier = Mockito.mock(Notifier.class);
        resource = Mockito.mock(Resource.class);

        Mockito.doNothing().when(notifier).run();
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(resource.getContent()).thenReturn(invalidNotifierConf.getBytes());
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource);

        try {
            notificationExecutor.sendAsyncNotifications(notificationDTO);
        } catch (NotificationException e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test(expected = NotificationException.class)
    public void testShouldThrowExceptionsWhenRegistryErrorOccurs() throws RegistryException, NotificationException {
        NotificationDTO notificationDTO = new NotificationDTO(new Properties(), NotifierConstants
                .NOTIFICATION_TYPE_NEW_VERSION);
        notificationDTO.setTenantDomain(TENANT_DOMAIN);
        notificationDTO.setTenantID(TENANT_ID);
        Notifier notifier = Mockito.mock(Notifier.class);
        resource = Mockito.mock(Resource.class);

        Mockito.doNothing().when(notifier).run();
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenThrow(RegistryException.class);
        Mockito.when(resource.getContent()).thenReturn(validNotifierConf.getBytes());
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource);

        notificationExecutor.sendAsyncNotifications(notificationDTO);
        Assert.fail("Should throw an exception");
    }

}
