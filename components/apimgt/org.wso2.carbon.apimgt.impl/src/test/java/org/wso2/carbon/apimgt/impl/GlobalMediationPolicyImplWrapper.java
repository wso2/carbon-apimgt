package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.registry.core.Registry;

public class GlobalMediationPolicyImplWrapper extends GlobalMediationPolicyImpl {
    public GlobalMediationPolicyImplWrapper(Registry registry) {
        this.registry = registry;
    }
}
