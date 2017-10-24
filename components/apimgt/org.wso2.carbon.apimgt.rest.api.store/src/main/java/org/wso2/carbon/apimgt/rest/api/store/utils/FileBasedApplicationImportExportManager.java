package org.wso2.carbon.apimgt.rest.api.store.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.io.InputStream;

/**
 * Manager class for API Application Import and Export handling
 */
public class FileBasedApplicationImportExportManager extends ApplicationImportExportManager {

    private static final Logger log = LoggerFactory.getLogger(FileBasedApplicationImportExportManager.class);
    private static final String IMPORTED_APPLICATIONS_DIRECTORY_NAME = "imported-applications";
    private String path;


    public FileBasedApplicationImportExportManager(APIStore apiStore, String path) {
        super(apiStore);
        this.path = path;
    }


    /**
     * Export a give set of Applications to a file system as zip archive.
     * The export root location is given by {@link FileBasedApplicationImportExportManager#path}/exported-applications.
     *
     * @param application         Application{@link Application} to be exported
     * @param exportDirectoryName Name of directory to be exported
     * @return path to the exported directory with exported artifacts
     * @throws APIMgtEntityImportExportException
     */
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
        String applicationExportDirectory = applicationArtifactBaseDirectoryPath;

        try {
            //create directory per application
            APIFileUtils.createDirectory(applicationExportDirectory);

            //export application details
            APIFileUtils.exportApplicationDetailsToFileSystem(new FileApplication(exportApplication),
                    applicationExportDirectory);

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
            String errorMsg = "Unable to find Application Details at: " + applicationArtifactBaseDirectoryPath;
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

    //import and create applications
    public Application importAndCreateApplications(InputStream uploadedAppArchiveInputStream)
            throws APIMgtEntityImportExportException {

        String appArchiveLocation = path + File.separator + IMPORTED_APPLICATIONS_DIRECTORY_NAME + ".zip";
        String archiveExtractLocation = null;

        try {
            archiveExtractLocation = APIFileUtils.extractUploadedArchive(uploadedAppArchiveInputStream,
                    IMPORTED_APPLICATIONS_DIRECTORY_NAME,
                    appArchiveLocation, path); /*have to write a separate function extractUploadedArchive*/
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error in accessing uploaded Application archive" + appArchiveLocation;
            log.error(errorMsg, e);
            throw new APIMgtEntityImportExportException(errorMsg, e, ExceptionCodes.APPLICATION_IMPORT_ERROR);
        }


        /*Application applicationDetails = decodeAppInformationFromDirectoryStructure(archiveExtractLocation);*/
        Application applicationDetails = decodeApplicationFile(archiveExtractLocation);
        return applicationDetails;


    }

    public Application decodeApplicationFile(String applicationDetailsFilePath)
            throws APIMgtEntityImportExportException {
        String applicationDetailsString;
        try {
            applicationDetailsString = APIFileUtils.readFileContentAsText(applicationDetailsFilePath);


        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to read application details from file at " + applicationDetailsFilePath;
            throw new APIMgtEntityImportExportException(errorMsg, e);
        }

        //convert to bean
        Gson gson = new GsonBuilder().create();
        return (gson.fromJson(applicationDetailsString, Application.class));/*returns an application object from
                                                                                                        a String*/
    }

    /*private Application decodeAppInformationFromDirectoryStructure(String applicationArtifactBasePath)
            throws APIMgtEntityImportExportException {
        Application appDetails = null;
        Set<String> appDetailsRootDirectoryPaths = null;


        try {
            appDetailsRootDirectoryPaths = APIFileUtils.getDirectoryList(applicationArtifactBasePath);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to find application details at" + applicationArtifactBasePath;
            log.error(errorMsg, e);
            throw new APIMgtEntityImportExportException(errorMsg, e, ExceptionCodes.APPLICATION_IMPORT_ERROR);
        }
        if(appDetailsRootDirectoryPaths.isEmpty()){
            try {
                APIFileUtils.deleteDirectory(path);
            } catch (APIMgtDAOException e) {
                log.error("Unable remove directory at " + path);
            }
            String errorMsg = "Unable to find Application details at " + applicationArtifactBasePath;
            throw new APIMgtEntityImportExportException(errorMsg, ExceptionCodes.APPLICATION_IMPORT_ERROR);
        }

        File appDetailsFile = getFileFromPrefix(applicationArtifactBasePath, ""); // prefix is ""
        return appDetails;
    }*/


}

