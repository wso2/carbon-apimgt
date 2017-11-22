package org.wso2.carbon.apimgt.rest.api.store.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtEntityImportExportException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manager class for File System based Application Import and Export handling
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
     * Export a given Application to a file system as zip archive.
     * The export root location is given by {@link FileBasedApplicationImportExportManager#path}/exported-application.
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
            Files.createDirectories(Paths.get(applicationArtifactBaseDirectoryPath));
        } catch (IOException e) {
            String errorMsg = "Unable to create the directory for export Application at :"
                    + applicationArtifactBaseDirectoryPath;
            throw new APIMgtEntityImportExportException(errorMsg, e);
        }

        Application exportApplication = application;
        String applicationExportDirectory = applicationArtifactBaseDirectoryPath + File.separator + exportApplication.getName();

        try {
            //create directory per application
            Files.createDirectories(Paths.get(applicationExportDirectory));

            //export application details
            FileBasedApplicationImportExportManager.exportApplicationDetailsToFileSystem(exportApplication,
                    applicationExportDirectory);

        } catch (IOException e) {

            log.error("Error in exporting Application: " + exportApplication.getName() + ", appId: " + application
                    .getId(), e);
            // cleanup the created directory
            try {
                FileUtils.deleteDirectory(new File(path));
            } catch (IOException e1) {
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
        } catch (APIManagementException e) {
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

        } catch (APIManagementException e) {
            // cleanup the archive root directory
            try {
                FileUtils.deleteDirectory(new File(path));
            } catch (IOException e1) {
                log.warn("Unable to remove directory " + path);
            }
            String errorMsg = "Error while archiving directory " + sourceDirectory;
            throw new APIMgtEntityImportExportException(errorMsg, e, ExceptionCodes.APPLICATION_EXPORT_ERROR);
        }

        return archiveLocation + File.separator + archiveName + ".zip";
    }

    //import and create applications
    public Application importApplications(InputStream uploadedAppArchiveInputStream)
            throws APIMgtEntityImportExportException {

        String appArchiveLocation = path + File.separator + IMPORTED_APPLICATIONS_DIRECTORY_NAME + ".zip";
        String archiveExtractLocation = null;

        try {
            archiveExtractLocation = extractUploadedArchiveApplication(uploadedAppArchiveInputStream,
                    IMPORTED_APPLICATIONS_DIRECTORY_NAME,
                    appArchiveLocation, path);
        } catch (APIManagementException e) {
            String errorMsg = "Error in accessing uploaded Application archive" + appArchiveLocation;
            log.error(errorMsg, e);
            throw new APIMgtEntityImportExportException(errorMsg, e, ExceptionCodes.APPLICATION_IMPORT_ERROR);
        }

        Application applicationDetails = parseApplicationFile(archiveExtractLocation);
        return applicationDetails;

    }

    public Application parseApplicationFile(String applicationDetailsFilePath)
            throws APIMgtEntityImportExportException {
        String applicationDetailsString;
        try {
            applicationDetailsString = new String(Files.readAllBytes(Paths.get(applicationDetailsFilePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            String errorMsg = "Unable to read application details from file at " + applicationDetailsFilePath;
            throw new APIMgtEntityImportExportException(errorMsg, e);
        }

        //convert to bean
        Gson gson = new GsonBuilder().create();
        //returns an application object from a json string
        Application applicationDetails = gson.fromJson(applicationDetailsString, Application.class);

        return applicationDetails;

    }

    /**
     * write the given Application details to file system
     *
     * @param application    {@link Application} object to be exported
     * @param exportLocation file system location to write the Application Details
     * @throws IOException if an error occurs while writing the Application Details
     */
    public static void exportApplicationDetailsToFileSystem(Application application, String exportLocation)
            throws IOException {
        String applicationFileLocation = exportLocation + File.separator + application.getName() +
                APIMgtConstants.APIFileUtilConstants.JSON_EXTENSION;

        Gson gson = new Gson();

        try (FileOutputStream fileOutputStream = new FileOutputStream(applicationFileLocation);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,
                     StandardCharsets.UTF_8)) {
            gson.toJson(application, outputStreamWriter);}
        if (log.isDebugEnabled()) {
            log.debug("Successfully saved Application details for application: " + application.getName());
        }
    }
    /**
     * Extracts the Application to the file system by reading the incoming {@link InputStream} object
     * uploadedApplicationArchiveInputStream
     *
     * @param uploadedAppArchiveInputStream Incoming {@link InputStream}
     * @param importedDirectoryName         directory to extract the archive
     * @param appArchiveLocation            full path of the archive location
     * @param extractLocation               full path to the location to which the archive will be written
     * @return location to which Applications were extracted
     * @throws APIManagementException if an error occurs while extracting the archive
     */
    public static String extractUploadedArchiveApplication(InputStream uploadedAppArchiveInputStream,
                                                           String importedDirectoryName,
                                                           String appArchiveLocation, String extractLocation)
            throws APIManagementException {
        String archiveExtractLocation;
        String archiveName;
        String extractedFilePath = null;

            // create api import directory structure
            APIFileUtils.createDirectory(extractLocation);
            // create archive
            APIFileUtils.createArchiveFromInputStream(uploadedAppArchiveInputStream, appArchiveLocation);
            // extract the archive
            archiveExtractLocation = extractLocation + File.separator + importedDirectoryName;
            archiveName = APIFileUtils.extractArchive(appArchiveLocation, archiveExtractLocation);
            extractedFilePath = archiveExtractLocation + File.separator + archiveName + File.separator +
                    archiveName + ".json";

        return extractedFilePath;
    }



}

