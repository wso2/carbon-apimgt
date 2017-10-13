package org.wso2.carbon.apimgt.impl;

import org.mockito.Mockito;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ConfigurationContextServiceMockCreator {
    private ConfigurationContextService contextService;
    private ConfigurationContextMockCreator contextMockCreator;

    public ConfigurationContextServiceMockCreator() {
        contextService = Mockito.mock(ConfigurationContextService.class);
        contextMockCreator = new ConfigurationContextMockCreator();
        Mockito.when(contextService.getServerConfigContext()).thenReturn(contextMockCreator.getMock());
    }

    public ConfigurationContextService getMock() {
        return contextService;
    }

    public ConfigurationContextMockCreator getContextMockCreator() {
        return contextMockCreator;
    }
}
