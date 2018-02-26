package org.wso2.carbon.apimgt.micro.gateway.status.checker.internal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.micro.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.apimgt.micro.gateway.status.checker.StatusChecker;
import org.wso2.carbon.apimgt.micro.gateway.status.checker.util.StatusCheckerConstants;

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
