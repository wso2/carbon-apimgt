package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;

public class ApplicationImportExportManager {
    private static final Log log = LogFactory.getLog(ApplicationImportExportManager.class);
    private APIConsumer apiConsumer;

    public ApplicationImportExportManager(APIConsumer apiConsumer) {
        this.apiConsumer = apiConsumer;
    }

    /**
     * Retrieve all the details of an Application for a given search query.
     *
     * @param query    searchQuery
     * @return {@link Application} instance
     * @throws APIManagementException if an error occurs while retrieving Application details
     */
    public Application getApplicationDetails(String query) throws
            APIManagementException {
        Application application = null;
        if (query == null || query.isEmpty()) {
            return application;
        } else {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            application = apiMgtDAO.getApplicationByUUID(query);
        }
        return application;
    }
}
