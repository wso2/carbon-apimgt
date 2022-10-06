package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.user.mgt.internal.UserManagerHolder;
import org.wso2.carbon.context.CarbonContext;

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
    public String[] getUserList(String claim, String claimValue) throws APIManagementException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return UserManagerHolder.getUserManager().getUserList(tenantId, claim, claimValue, null);
        } catch (Exception e) {
            throw new APIManagementException("Error when retrieving user list", e);
        }

    }
}
