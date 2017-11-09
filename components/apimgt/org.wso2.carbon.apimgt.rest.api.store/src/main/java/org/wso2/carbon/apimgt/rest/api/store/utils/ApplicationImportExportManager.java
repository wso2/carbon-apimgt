package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Application;

import static org.wso2.carbon.apimgt.core.dao.impl.DAOFactory.getApplicationDAO;

/**
 * Manager class for Applications Import and Export handling
 */
public class ApplicationImportExportManager {

    private static final Logger log = LoggerFactory.getLogger(ApplicationImportExportManager.class);

    private APIStore apiStore;

    public ApplicationImportExportManager(APIStore apiStore) {this.apiStore = apiStore;}


    /**
     * Retrieve all the details of an Application for a given search query.
     *
     * @param query searchQuery
     * @param username logged in user
     * @return {@link Application} instance
     * @throws APIManagementException if an error occurs while retrieving Application details
     */
    public Application getApplicationDetails(String query, String username) throws
    APIManagementException {

        Application application = apiStore.getApplication(query,username);
        if (application == null ){
            log.error("No applications found matching the provided applicationId");
        }
        return application;

    }


    /**
     * Update details of an existing Application when imported
     *
     * @param importedApplication
     * @param username
     * @throws APIManagementException
     */
    public void updateApplication (Application importedApplication, String username)
            throws APIManagementException {
        try {
            if(getApplicationDAO().isApplicationNameExists(importedApplication.getName())){
                Application existingApplication = apiStore.getApplicationByName(importedApplication.getName(),
                        username);
                apiStore.updateApplication(existingApplication.getUuid(), importedApplication);
            }else {
                log.error("No applications found matching the provided application name");
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while updating the application";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
    }

}
