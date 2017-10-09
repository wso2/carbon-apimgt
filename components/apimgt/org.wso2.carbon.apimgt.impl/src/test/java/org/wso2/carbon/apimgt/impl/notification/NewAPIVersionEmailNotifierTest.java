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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.impl.notification.util.NewAPIVersionEmailNotifierWrapper;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.*;

public class NewAPIVersionEmailNotifierTest {
    private NotificationDTO notificationDTO;
    private NewAPIVersionEmailNotifier notifier;
    private Registry registry;
    private ClaimsRetriever claimsRetriever;
    private final String ADMIN = "admin";
    private final String API_NAME = "test";
    private final int TENANT_ID = -1234;

    @Before
    public void setup() {
        notifier = new NewAPIVersionEmailNotifier();
        registry = Mockito.mock(Registry.class);
        claimsRetriever = Mockito.mock(ClaimsRetriever.class);
        Subscriber subscriber = new Subscriber(ADMIN);
        Set<Subscriber> subscribersOfAPI = new HashSet<Subscriber>();
        subscribersOfAPI.add(subscriber);

        Properties properties = new Properties();
        properties.put(NotifierConstants.API_KEY,
                new APIIdentifier(ADMIN, API_NAME, "1.0.0"));
        properties.put(NotifierConstants.NEW_API_KEY,
                new APIIdentifier(ADMIN, API_NAME, "2.0.0"));
        properties.put(NotifierConstants.TITLE_KEY,
                "New Version");
        properties.put(NotifierConstants.SUBSCRIBERS_PER_API, subscribersOfAPI);
        properties.put(NotifierConstants.CLAIMS_RETRIEVER_IMPL_CLASS,
                "org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever");
        properties.put(NotifierConstants.TEMPLATE_KEY, "<html>$1</html>");
        notificationDTO = new NotificationDTO(properties, NotifierConstants
                .NOTIFICATION_TYPE_NEW_VERSION);
        notificationDTO.setTenantID(TENANT_ID);
    }

    @Test
    public void testShouldNotSendNotificationWhenSubscribersUnavailable() {
        notificationDTO.getProperties().put(NotifierConstants.SUBSCRIBERS_PER_API, new HashSet<Subscriber>());

        try {
            notifier.sendNotifications(notificationDTO);
        } catch (Exception e) {
            Assert.fail("Should not throw any exceptions");
        }
    }

    @Test
    public void testShouldNotSendNotificationWhenEmailsUnavailable() {
        NewAPIVersionEmailNotifier emailNotifier = new NewAPIVersionEmailNotifierWrapper(registry, claimsRetriever) {
            @Override
            protected String getTenantDomain() {
                // intentionally throw null to get an exception for checking if execution stopped before this line
                // if the test is successful this line should not be executed
                throw null;
            }

            @Override
            public Set<String> getNotifierSet(NotificationDTO notificationDTO) throws NotificationException {
                return new HashSet<String>();
            }
        };

        try {
            emailNotifier.sendNotifications(notificationDTO);
        } catch (Exception e) {
            Assert.fail("Should not throw any exceptions");
        }
    }

    @Test
    public void testShouldSendNotificationWhenParametersCorrect() {
        NewAPIVersionEmailNotifier emailNotifier = new NewAPIVersionEmailNotifierWrapper(registry, claimsRetriever);

        try {
            emailNotifier.sendNotifications(notificationDTO);
        } catch (Exception e) {
            Assert.fail("Should not throw any exceptions");
        }
    }

    @Test
    public void testShouldNotThrowExceptionWhenRetrievingNotifiers() throws APIManagementException {
        NewAPIVersionEmailNotifier emailNotifier = new NewAPIVersionEmailNotifierWrapper(registry, claimsRetriever);
        ClaimsRetriever claimsRetriever = Mockito.mock(ClaimsRetriever.class);
        Mockito.doNothing().when(claimsRetriever).init();

        try {
            emailNotifier.getNotifierSet(notificationDTO);
        } catch (NotificationException e) {
            Assert.fail("Should not throw any exceptions");
        }
    }

    @Test
    public void testShouldReturnNotifiersWhenPropertiesValid() throws APIManagementException {
        final ClaimsRetriever claimsRetriever = Mockito.mock(ClaimsRetriever.class);
        final SortedMap<String, String> claims = new TreeMap<String, String>();
        claims.put(NotifierConstants.EMAIL_CLAIM, "admin@wso2.com");
        NewAPIVersionEmailNotifier emailNotifier = new NewAPIVersionEmailNotifier() {
            @Override
            protected ClaimsRetriever getClaimsRetriever(String claimsRetrieverImplClass)
                    throws IllegalAccessException, InstantiationException, ClassNotFoundException {
                return claimsRetriever;
            }
        };
        Mockito.doNothing().when(claimsRetriever).init();
        Mockito.when(claimsRetriever.getClaims(Mockito.anyString())).thenReturn(claims);

        try {
            Assert.assertTrue(emailNotifier.getNotifierSet(notificationDTO).size() > 0);
        } catch (NotificationException e) {
            Assert.fail("Should not throw any exceptions");
        }
    }

    @Test
    public void testShouldLoadMessageTemplateWhenDtoValid() throws RegistryException {
        Resource resource = Mockito.mock(Resource.class);
        NewAPIVersionEmailNotifier emailNotifier = new NewAPIVersionEmailNotifier() {
            @Override
            protected Registry getConfigSystemRegistry(int tenantId) throws RegistryException {
                return registry;
            }
        };
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true).thenReturn(false);
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource);
        Mockito.when(resource.getContent()).thenReturn("<html>$1-$2-$3</html>".getBytes());

        try {
            NotificationDTO notification = emailNotifier.loadMessageTemplate(notificationDTO);
            Assert.assertFalse(!notification.getMessage().contains(API_NAME));

            // should use dto template if resource not found in registry
            notification = emailNotifier.loadMessageTemplate(notificationDTO);
            Assert.assertFalse(!notification.getMessage().contains(API_NAME));
        } catch (NotificationException e) {
            Assert.fail("Should not throw any exceptions");
        }
    }

    @Test
    public void testShouldNotLoadMessageWhenErrorOccurs() throws RegistryException {
        Resource resource = Mockito.mock(Resource.class);
        NewAPIVersionEmailNotifier emailNotifier = new NewAPIVersionEmailNotifier() {
            @Override
            protected Registry getConfigSystemRegistry(int tenantId) throws RegistryException {
                return registry;
            }
        };
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true).thenThrow(RegistryException.class);
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource);
        Mockito.when(resource.getContent()).thenReturn("".getBytes());

        try {
            NotificationDTO notification = emailNotifier.loadMessageTemplate(notificationDTO);
            Assert.assertFalse(!notification.getMessage().equals(""));

            emailNotifier.loadMessageTemplate(notificationDTO);
            Assert.fail("Should fail with an exception");
        } catch (NotificationException e) {
            // exception is expection
            return;
        }
    }
}
