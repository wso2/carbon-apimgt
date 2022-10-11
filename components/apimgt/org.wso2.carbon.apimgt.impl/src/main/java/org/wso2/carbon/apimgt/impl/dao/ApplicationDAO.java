package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;

public interface ApplicationDAO {

    /**
     * returns the SubscribedAPI object which is related to the UUID
     *
     * @param uuid UUID of Application
     * @return {@link SubscribedAPI} Object which contains the subscribed API information.
     * @throws APIManagementException
     */
    SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException;

    /**
     * Retrieve the applications by user/application name
     *
     * @param user
     * @param owner
     * @param tenantId
     * @param limit
     * @param offset
     * @param sortBy
     * @param sortOrder
     * @param appName
     * @return
     * @throws APIManagementException
     */
    Application[] getApplicationsWithPagination(String user, String owner, int tenantId, int limit,
                                                int offset, String sortBy, String sortOrder, String appName)
            throws APIManagementException;

    /**
     * returns application for Organization
     *
     * @param organization Organization Name
     * @return Application List
     * @throws APIManagementException
     */
    Application[] getAllApplicationsOfTenantForMigration(String organization) throws
            APIManagementException;

    /**
     * Get count of the applications for the tenantId.
     *
     * @param tenantId          content to get application count based on tenant_id
     * @param searchOwner       content to search applications based on owners
     * @param searchApplication content to search applications based on application
     * @throws APIManagementException if failed to get application
     */
    int getApplicationsCount(int tenantId, String searchOwner, String searchApplication) throws
            APIManagementException;
}
