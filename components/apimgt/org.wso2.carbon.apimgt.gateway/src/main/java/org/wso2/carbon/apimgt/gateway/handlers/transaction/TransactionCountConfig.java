package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

public class TransactionCountConfig {

    private static APIManagerConfiguration apiManagerConfiguration;
    public TransactionCountConfig() {
        apiManagerConfiguration = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
    }


}
