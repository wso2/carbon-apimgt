package org.wso2.carbon.apimgt.impl.notifier.events;

import org.wso2.carbon.apimgt.impl.APIConstants;

public class PolicyResetEvent extends Event {
    protected APIConstants.PolicyType policyType;
    protected boolean isResourceLevel = false;

    public APIConstants.PolicyType getPolicyType() {
        return policyType;
    }

    public boolean getIsResourceLevel() {return isResourceLevel;};
}
