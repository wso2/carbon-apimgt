package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;

/**
 * This class contains the utility methods related to Users and Roles
 */
public class APIRealmUtils {
    /**
     * Returns the claims of a User
     *
     * @param userName The name of the user
     * @return The looked up claims of the user
     * @throws APIManagementException if failed to get user
     */
    public static Map<String, String> getLoggedInUserClaims(String userName) throws APIManagementException {
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        int tenantId = APIUtil.getTenantId(tenantDomain);
        Map<String, String> claimMap = APIUtil.getClaims(userName, tenantId, ClaimsRetriever.DEFAULT_DIALECT_URI);
        return claimMap;
    }
}
