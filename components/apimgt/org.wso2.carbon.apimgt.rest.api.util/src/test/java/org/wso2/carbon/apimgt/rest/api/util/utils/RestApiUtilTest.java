package org.wso2.carbon.apimgt.rest.api.util.utils;

import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.context.CarbonContext;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class, CarbonContext.class})
public class RestApiUtilTest {

    @Test
    public void testGetLoggedInUsername() {
        System.setProperty(CARBON_HOME, "");

        String defaultUsername = "default@user.com";

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        Mockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        Mockito.when(carbonContext.getUsername()).thenReturn(defaultUsername);

        String loggedInUsername = RestApiCommonUtil.getLoggedInUsername();

        Assert.assertEquals(defaultUsername, loggedInUsername);
    }
}
