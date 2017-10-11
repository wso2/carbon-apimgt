package org.wso2.carbon.apimgt.gateway.handlers.security.keys;

import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;

public class APIKeyValidatorClientPoolTest {
    @Test
    public void testGetKeyValidationClient() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty("APIKeyValidator.ConnectionPool.MaxIdle")).thenReturn
                ("10");
        Mockito.when(apiManagerConfiguration.getFirstProperty("APIKeyValidator.ConnectionPool.InitIdleCapacity"))
                .thenReturn("5");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn
                ("https://localhost:" + 8082 + "/services/");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn
                ("admin");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn
                ("admin");
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (apiManagerConfiguration));
        APIKeyValidatorClientPool apiKeyValidatorClientPool = APIKeyValidatorClientPool.getInstance();
        APIKeyValidatorClient apiKeyValidatorClient = apiKeyValidatorClientPool.get();
        apiKeyValidatorClientPool.release(apiKeyValidatorClient);
        apiKeyValidatorClientPool.cleanup();
    }
}