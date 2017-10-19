package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtEntityImportExportException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.FileApplication;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;

import java.io.File;

/**
 * Manager class for API Application Import and Export handling
 */
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
            throw new APIMgtEntityImportExportException(errorMsg, e);
        }

        Application exportApplication = application;
        String applicationExportDirectory = "exported-application";

        try {
            //create directory per application
            APIFileUtils.createDirectory(applicationExportDirectory);

            //export application details
            APIFileUtils.exportApplicationDetailsToFileSystem(new FileApplication(exportApplication),
                    exportDirectoryName);

        } catch (APIMgtDAOException e) {

            log.error("Error in exporting API: " + exportApplication.getName() + ", version: " + application
                    .getId(), e);
            // cleanup the created directory
            try {
                APIFileUtils.deleteDirectory(path);
            } catch (APIMgtDAOException e1) {
                log.warn("Unable to remove directory " + path);
            }
        }

        // Check if no application is exported
        try {
            if (APIFileUtils.getDirectoryList(applicationArtifactBaseDirectoryPath).isEmpty()) {
                // cleanup the archive root directory
                APIFileUtils.deleteDirectory(path);
                String errorMsg = "No Applications exported";
                throw new APIMgtEntityImportExportException(errorMsg, ExceptionCodes.APPLICATION_EXPORT_ERROR);
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to find API definitions at: " + applicationArtifactBaseDirectoryPath;
            log.error(errorMsg, e);
            throw new APIMgtEntityImportExportException(errorMsg, ExceptionCodes.APPLICATION_IMPORT_ERROR);
        }

        return applicationArtifactBaseDirectoryPath;
    }

    //create a zip archive of the export directory
    public String createArchiveFromExportedAppArtifacts(String sourceDirectory, String archiveLocation,
                                                        String archiveName) throws APIMgtEntityImportExportException {

        try {
            APIFileUtils.archiveDirectory(sourceDirectory, archiveLocation, archiveName);

        } catch (APIMgtDAOException e) {
            // cleanup the archive root directory
            try {
                APIFileUtils.deleteDirectory(path);
            } catch (APIMgtDAOException e1) {
                log.warn("Unable to remove directory " + path);
            }
            String errorMsg = "Error while archiving directory " + sourceDirectory;
            throw new APIMgtEntityImportExportException(errorMsg, e, ExceptionCodes.APPLICATION_EXPORT_ERROR);
        }

        return archiveLocation + File.separator + archiveName + ".zip";
    }
}
