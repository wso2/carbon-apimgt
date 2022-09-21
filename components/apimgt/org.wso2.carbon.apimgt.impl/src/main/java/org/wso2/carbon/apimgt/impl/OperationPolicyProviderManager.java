package org.wso2.carbon.apimgt.impl;


import org.wso2.carbon.apimgt.api.OperationPolicyProvider;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.wso2.carbon.apimgt.impl.APIConstants.OperationPolicyConstants;


public class OperationPolicyProviderManager {

    private static OperationPolicyProvider operationPolicyProvider = null;

    public static OperationPolicyProvider getPolicyProviderInstance() {

        Map<String, String> configs = APIManagerConfiguration.getOperationalPolicyProperties();

        if (operationPolicyProvider == null) {
            ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();
            if (serviceReferenceHolder.getPolicyProvider() != null) {
                if (Objects.equals(configs.get(OperationPolicyConstants.CONFIG_KEY_PROVIDER),
                        OperationPolicyConstants.CONFIG_VALUE_BCENTRAL)) {
                    operationPolicyProvider = serviceReferenceHolder.getPolicyProvider();
                } else {
                    operationPolicyProvider = new OperationPolicyProviderImpl();
                }
            } else {
                operationPolicyProvider = new OperationPolicyProviderImpl();
            }
        }

        return operationPolicyProvider;
    }
}
