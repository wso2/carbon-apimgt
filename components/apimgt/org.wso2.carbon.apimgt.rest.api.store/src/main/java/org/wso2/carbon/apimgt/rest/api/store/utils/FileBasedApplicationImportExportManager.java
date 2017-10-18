package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtEntityImportExportException;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;

import java.io.File;

public class FileBasedApplicationImportExportManager extends ApplicationImportExportManager {

    private static final Logger log = LoggerFactory.getLogger(FileBasedApplicationImportExportManager.class);
    //private static final String IMPORTED_APPLICATIONS_DIRECTORY_NAME = "imported-applications";
    private String path;

    public FileBasedApplicationImportExportManager(APIStore apiStore, String path) {
        super(apiStore);
        this.path = path;
    }

    public String exportApplication(Application application, String exportDirectoryName) throws
    APIMgtEntityImportExportException {
        String applicationArtifactBaseDirectoryPath = path + File.separator + exportDirectoryName;
        try {
            APIFileUtils.createDirectory(applicationArtifactBaseDirectoryPath);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to create the directory for export Application at :"
                    + applicationArtifactBaseDirectoryPath;
            throw new APIMgtEntityImportExportException(errorMsg,e);
        }

        Application exportApplication = application;
        String applicationExportDirectory = "exported-application";

        try {
            APIFileUtils.createDirectory(applicationExportDirectory);
        } catch (APIMgtDAOException e) {
            e.printStackTrace();
        }


    }
}
