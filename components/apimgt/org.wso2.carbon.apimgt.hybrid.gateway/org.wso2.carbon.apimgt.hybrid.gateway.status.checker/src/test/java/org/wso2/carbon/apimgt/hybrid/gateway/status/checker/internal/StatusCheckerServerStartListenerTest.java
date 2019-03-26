/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.hybrid.gateway.status.checker.internal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.status.checker.StatusChecker;
import org.wso2.carbon.apimgt.hybrid.gateway.status.checker.util.StatusCheckerConstants;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StatusCheckerServerStartListener.class, StatusCheckerConstants.class,
                        ScheduledExecutorService.class })
public class StatusCheckerServerStartListenerTest {

    private static final String TOKEN = "00000";
    private static final String PING_URL = "https://test.com/ping";

    @Test
    public void isTokenSet() {
        StatusCheckerServerStartListener statusCheckerServerStartListener = new StatusCheckerServerStartListener();
        Assert.assertEquals(true, statusCheckerServerStartListener.isTokenSet(TOKEN));
        Assert.assertEquals(false, statusCheckerServerStartListener
                                           .isTokenSet(OnPremiseGatewayConstants.UNIQUE_IDENTIFIER_HOLDER));
    }

    @Test(expected = Exception.class)
    public void startStatusCheck() {
        final ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        PowerMockito.mockStatic(ScheduledExecutorService.class);
        Mockito.doCallRealMethod().when(scheduler.scheduleAtFixedRate(new StatusChecker(TOKEN, PING_URL), 0,
                                                                      StatusCheckerConstants.PING_INTERVAL,
                                                                      TimeUnit.MINUTES));
    }
}
