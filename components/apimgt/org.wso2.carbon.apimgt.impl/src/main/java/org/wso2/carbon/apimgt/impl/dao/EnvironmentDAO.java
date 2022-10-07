package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;

public interface EnvironmentDAO {

    /**
     * Returns the Environment for the uuid in the tenant domain.
     *
     * @param organization the organization to look environment
     * @param uuid         UUID of the environment
     * @return Gateway environment with given UUID
     */
    Environment getEnvironment(String organization, String uuid) throws APIManagementException;

}
