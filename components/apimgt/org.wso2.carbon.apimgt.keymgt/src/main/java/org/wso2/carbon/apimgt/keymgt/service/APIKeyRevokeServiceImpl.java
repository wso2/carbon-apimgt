package org.wso2.carbon.apimgt.keymgt.service;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.events.RevocationRequestPublisher;

public class APIKeyRevokeServiceImpl implements APIKeyRevokeService {
    private RevocationRequestPublisher revocationRequestPublisher = null;
    private ApiMgtDAO dao = null;
    private static APIKeyRevokeService apiKeyRevokeService;

    private APIKeyRevokeServiceImpl() {
        revocationRequestPublisher = RevocationRequestPublisher.getInstance();
        dao = ApiMgtDAO.getInstance();
    }

    public static synchronized APIKeyRevokeService getInstance() {
        if (apiKeyRevokeService == null) {
            apiKeyRevokeService = new APIKeyRevokeServiceImpl();
        }
        return apiKeyRevokeService;
    }

    public void revokeAPIKey(String token, long expiryTime, int tenantId)
                                                                    throws APIManagementException {
        if (APIUtil.isValidJWT(token)) {
            String splitToken[] = token.split("\\.");
            dao.addRevokedJWTSignature(splitToken[2], APIConstants.API_KEY_TYPE, expiryTime, tenantId);
            revocationRequestPublisher.publishRevocationEvents(token, expiryTime, null);
        }
    }

}
