package org.wso2.carbon.apimgt.keymgt.service;

import org.wso2.carbon.apimgt.api.APIManagementException;

public interface APIKeyRevokeServiceImpl {

    void notifyAndPersistAPIKeyRevoke(String signature, long expiryTime, int tenantId) throws APIManagementException;

}
