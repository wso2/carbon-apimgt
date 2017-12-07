/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.core.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.NotificationConfigurations;
import org.wso2.carbon.apimgt.core.executors.NotificationExecutor;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.impl.NotifierConstants;
import org.wso2.carbon.apimgt.core.template.dto.NotificationDTO;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


/**
 * Notification TestCase
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIManagerFactory.class)
public class NotificationTestCase {

    @Test
    public void testNotificationExecutor() throws Exception {
        Properties properties = Mockito.mock(Properties.class);


        NotificationDTO notificationDTO = Mockito.mock(NotificationDTO.class);
        Mockito.when(notificationDTO.getTitle()).thenReturn("Title");
        Mockito.when(notificationDTO.getType()).thenReturn("ApiNewVersion");
        Mockito.when(notificationDTO.getMessage()).thenReturn("Message");
        Mockito.when(notificationDTO.getProperties()).thenReturn(properties);


        APIMConfigurations apimConfigurations = Mockito.mock(APIMConfigurations.class);
        NotificationConfigurations notificationConfigurations = Mockito.mock(NotificationConfigurations.class);
        PowerMockito.mockStatic(APIMConfigurations.class);
        PowerMockito.when(apimConfigurations.getNotificationConfigurations()).
                thenReturn(notificationConfigurations);


        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        Set subscriber = new HashSet();
        subscriber.add("User");
        Mockito.when((Set<String>) notificationDTO.
                getProperty(NotifierConstants.SUBSCRIBERS_PER_API)).thenReturn(subscriber);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        PowerMockito.when(apiManagerFactory.getIdentityProvider()).thenReturn(identityProvider);
        PowerMockito.when(identityProvider.getIdOfUser("User")).thenReturn("1111");
        PowerMockito.when(identityProvider.getEmailOfUser("1111")).thenReturn("admin@gmail.com");
        new NotificationExecutor().sendAsyncNotifications(notificationDTO);
    }


}
