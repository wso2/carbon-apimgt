package org.wso2.carbon.apimgt.rest.api.store.utils;


import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.models.Application;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileBasedApplicationImportExportManagerTestCase {

    private final static Logger log = LoggerFactory.getLogger(FileBasedApplicationImportExportManagerTestCase.class);
    private static String importExportRootDirectory = System.getProperty("java.io.tmpdir") + File.separator +
            "import-export-test";
    private APIStore apiStore;
    private static final String USER = "admin";
    //private static String path =


    @BeforeClass
    protected void setUp() throws Exception {
        //log.info("Test directory: " + importExportRootDirectory);
    }

    @AfterClass
    public void tearDown() throws Exception {
    }

    @Test
    public void testImportExportApplication() throws Exception {
        printTestMethodName();
        APIStore apiStore = Mockito.mock(APIStore.class);
        testExportApplication(importExportRootDirectory);
        testImportApplication(importExportRootDirectory);

    }

    @Test
    public void testExportApplication(String exportDir) throws Exception {
       // Files.createDirectories(Path.);
    }

    @Test
    public void testCreateArchiveFromExportedAppArtifacts() throws Exception {
        /*Application application = Mockito.mock(Application.class);
        apiStore = Mockito.mock(APIStore.class);
        FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager
                (apiStore, importExportRootDirectory);
        String sourceDirectory = "exported-Apps";
        String archiveLocation = importExportManager.exportApplication(application, sourceDirectory);
        importExportManager.createArchiveFromExportedAppArtifacts(sourceDirectory, archiveLocation,
                "exportedApp");*/
    }

    @Test
    public void testImportApplication(String impportDir) throws Exception {
    }

    @Test
    public void testParseApplicationFile() throws Exception {
    }

    @Test
    public void testExportApplicationDetailsToFileSystem() throws Exception {
    }

    @Test
    public void testExtractUploadedArchiveApplication() throws Exception {
    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }

}