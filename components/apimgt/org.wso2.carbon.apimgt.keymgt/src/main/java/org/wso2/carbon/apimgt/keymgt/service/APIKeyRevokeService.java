package org.wso2.carbon.apimgt.keymgt.service;

import org.wso2.carbon.apimgt.api.APIManagementException;

public interface APIKeyRevokeService {

    void revokeAPIKey(String token, long expiryTime, int tenantId) throws APIManagementException;

}
