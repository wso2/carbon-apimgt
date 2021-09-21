/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.notification;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.publishers.RevocationRequestPublisher;
import org.wso2.carbon.apimgt.notification.event.TokenRevocationEvent;

import java.util.Properties;

import static org.mockito.Mockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AbstractKeyManagerEventHandler.class, ApiMgtDAO.class, RevocationRequestPublisher.class })
public class AbstractKeyManagerEventHandlerTest {

    @Test
    public void handleTokenRevocationEventTest() throws Exception {

        TokenRevocationEvent tokenRevocationEvent = new TokenRevocationEvent();
        Assert.assertTrue(StringUtils.isBlank(tokenRevocationEvent.getTokenType()));

        RevocationRequestPublisher revocationRequestPublisher = Mockito.mock(RevocationRequestPublisher.class);
        PowerMockito.mockStatic(RevocationRequestPublisher.class);
        PowerMockito.when(RevocationRequestPublisher.getInstance()).thenReturn(revocationRequestPublisher);
        doNothing().when(revocationRequestPublisher)
                .publishRevocationEvents(Mockito.anyString(), Mockito.anyLong(), Mockito.anyObject());

        Properties properties = Mockito.mock(Properties.class);
        whenNew(Properties.class).withNoArguments().thenReturn(properties);

        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        DefaultKeyManagerEventHandlerImpl defaultKeyManagerEventHandler = new DefaultKeyManagerEventHandlerImpl();
        Boolean result = defaultKeyManagerEventHandler.handleTokenRevocationEvent(tokenRevocationEvent);

        Assert.assertTrue(result);
        Assert.assertEquals("Default", tokenRevocationEvent.getTokenType());
    }
}
