package org.wso2.carbon.apimgt.rest.api.store.utils;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manager class for File System based Application Import and Export handling
 */
public class FileBasedApplicationImportExportManager extends ApplicationImportExportManager {
    private static final Log log = LogFactory.getLog(FileBasedApplicationImportExportManager.class);
    private static final String IMPORTED_APPLICATIONS_DIRECTORY_NAME = "imported-applications";
    private String path;

    public FileBasedApplicationImportExportManager(APIConsumer apiConsumer, String path) {
        super(apiConsumer);
        this.path = path;
    }

    /**
     * Export a given Application to a file system as zip archive.
     * The export root location is given by {@link FileBasedApplicationImportExportManager#path}/exported-application.
     *
     * @param application         Application{@link Application} to be exported
     * @param exportDirectoryName Name of directory to be exported
     * @return path to the exported directory with exported artifacts
     * @throws APIManagementException
     */
    public String exportApplication(Application application, String exportDirectoryName) throws
            APIManagementException {

        String applicationArtifactBaseDirectoryPath = path + File.separator + exportDirectoryName;
        try {
            Files.createDirectories(Paths.get(applicationArtifactBaseDirectoryPath));
        } catch (IOException e) {
            String errorMsg = "Unable to create the directory for export Application at :"
                    + applicationArtifactBaseDirectoryPath;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
        Application exportApplication = application;
        String applicationExportDirectory = applicationArtifactBaseDirectoryPath + File.separator +
                exportApplication.getName();
        try {
            //create directory per application
            Files.createDirectories(Paths.get(applicationExportDirectory));
            //export application details
            exportApplicationDetailsToFileSystem(exportApplication,
                    applicationExportDirectory);
        } catch (IOException e) {
            log.error("Error while exporting Application: " + exportApplication.getName(), e);
        }
        // Check if no application is exported
        try {
            if (getDirectoryList(applicationArtifactBaseDirectoryPath).isEmpty()) {
                // cleanup the archive root directory
                FileUtils.deleteDirectory(new File(path));
            }
        } catch (IOException e) {
            String errorMsg = "Unable to find Application Details at: " + applicationArtifactBaseDirectoryPath;
            throw new APIManagementException(errorMsg);
        }
        return applicationArtifactBaseDirectoryPath;
    }

    /**
     * Creates an archive of the contained application details.
     *
     * @param sourceDirectory Directory which contains source file
     * @param archiveLocation Directory to generate the zip archive
     * @param archiveName     Name of the zip archive
     * @return path to the created archive file
     * @throws APIManagementException
     */
    public String createArchiveFromExportedAppArtifacts(String sourceDirectory, String archiveLocation,
                                                        String archiveName) throws APIManagementException {
        String archivedFilePath = null;
        try {
            archiveDirectory(sourceDirectory, archiveLocation, archiveName);
        } catch (IOException e) {
            // cleanup the archive root directory
            try {
                FileUtils.deleteDirectory(new File(path));
            } catch (IOException e1) {
                log.warn("Unable to remove directory " + path);
            }
            String errorMsg = "Error while archiving directory " + sourceDirectory;
            throw new APIManagementException(errorMsg);
        }
        archivedFilePath = archiveLocation + File.separator + archiveName + ".zip";
        return archivedFilePath;
    }

    /**
     * write the given Application details to file system
     *
     * @param application    {@link Application} object to be exported
     * @param exportLocation file system location to write the Application Details
     * @throws IOException if an error occurs while writing the Application Details
     */
    private static void exportApplicationDetailsToFileSystem(Application application, String exportLocation)
            throws IOException {
        String applicationFileLocation = exportLocation + File.separator + application.getName() +
                ".json";
        Gson gson = new Gson();
        try (FileOutputStream fileOutputStream = new FileOutputStream(applicationFileLocation);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,
                     StandardCharsets.UTF_8)) {
            gson.toJson(application, outputStreamWriter);
        }
    }

    /**
     * Creates a zip archive from of a directory
     *
     * @param sourceDirectory directory to create zip archive from
     * @param archiveLocation path to the archive location, excluding archive name
     * @param archiveName     name of the archive to create
     * @throws IOException if an error occurs while creating the archive
     */
    public static void archiveDirectory(String sourceDirectory, String archiveLocation, String archiveName)
            throws IOException {

        File directoryToZip = new File(sourceDirectory);

        List<File> fileList = new ArrayList<>();
        getAllFiles(directoryToZip, fileList);
        writeArchiveFile(directoryToZip, fileList, archiveLocation, archiveName);
        if (log.isDebugEnabled()) {
            log.debug("Archived API generated successfully" + archiveName);
        }
    }

    /**
     * Queries all files under a directory recursively
     *
     * @param sourceDirectory full path to the root directory
     * @param fileList        list containing the files
     */
    private static void getAllFiles(File sourceDirectory, List<File> fileList) {
        File[] files = sourceDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                fileList.add(file);
                if (file.isDirectory()) {
                    getAllFiles(file, fileList);
                }
            }
        }
    }

    private static void writeArchiveFile(File directoryToZip, List<File> fileList, String archiveLocation,
                                         String archiveName) throws IOException {

        try (FileOutputStream fileOutputStream = new FileOutputStream(archiveLocation + File.separator + archiveName
                + ".zip");
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            for (File file : fileList) {
                if (!file.isDirectory()) {
                    addToArchive(directoryToZip, file, zipOutputStream);
                }
            }
        }
    }

    private static void addToArchive(File directoryToZip, File file, ZipOutputStream zipOutputStream)
            throws IOException {
        // Add a file to archive
        try (FileInputStream fileInputStream = new FileInputStream(file)) {

            // Get relative path from archive directory to the specific file
            String zipFilePath = file.getCanonicalPath()
                    .substring(directoryToZip.getCanonicalPath().length() + 1, file.getCanonicalPath().length());
            if (File.separatorChar != '/') {
                zipFilePath = zipFilePath.replace(File.separatorChar, '/');
            }
            ZipEntry zipEntry = new ZipEntry(zipFilePath);
            zipOutputStream.putNextEntry(zipEntry);

            IOUtils.copy(fileInputStream, zipOutputStream);
            zipOutputStream.closeEntry();
        }
    }

    /**
     * Queries the list of directories available under a root directory path
     *
     * @param path full path of the root directory
     * @return Set of directory path under the root directory given by path
     * @throws IOException if an error occurs while listing directories
     */
    public static Set<String> getDirectoryList(String path) throws IOException {
        Set<String> directoryNames = new HashSet<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(path))) {
            for (Path directoryPath : directoryStream) {
                directoryNames.add(directoryPath.toString());
            }
        }
        return directoryNames;
    }
}
