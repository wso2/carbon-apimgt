package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.UsedByMigrationClient;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;

/**
 * RemoteUserStroeManager Admin service client.
 */
public class RemoteUserManagerClient {
	private static final RemoteUserManagerClient INSTANCE = new RemoteUserManagerClient();
    private RemoteUserManagerClient() {

    }

	public static RemoteUserManagerClient getInstance() {

		return INSTANCE;
	}

	/**
     * Return userlist based on a claim
     *
     * @param claim      - The claim
     * @param claimValue - The Claim Value
     * @return - A user list
     * @throws APIManagementException
     */
    @UsedByMigrationClient
    public String[] getUserList(String claim, String claimValue) throws APIManagementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            UserRealm tenantUserRealm =
                    (UserRealm) ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            UserStoreManager userStoreManager = tenantUserRealm.getUserStoreManager();
            return userStoreManager.getUserList(claim, claimValue, null);
        } catch (Exception e) {
            throw new APIManagementException("Error when retrieving user list", e);
        }

    }
}
