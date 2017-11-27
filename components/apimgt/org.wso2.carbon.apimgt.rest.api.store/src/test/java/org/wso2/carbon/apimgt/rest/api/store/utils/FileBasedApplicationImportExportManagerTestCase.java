package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtEntityImportExportException;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;

import java.io.File;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Application.class, APIFileUtils.class})
public class FileBasedApplicationImportExportManagerTestCase {

    private final static Logger log = LoggerFactory.getLogger(FileBasedApplicationImportExportManagerTestCase.class);
    private static String importExportRootDirectory = System.getProperty("java.io.tmpdir") + File.separator +
            "import-export-test";
    private APIStore apiStore = Mockito.mock(APIStore.class);


    @Test(expected = APIMgtEntityImportExportException.class)
    public void testExportApplicationErrorPath() throws Exception {
        printTestMethodName();
        Application testApp = Mockito.mock(Application.class);
        FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager
                (apiStore, "");
        importExportManager.exportApplication(testApp, "exported-apps");
    }

    @Test(expected = APIMgtEntityImportExportException.class)
    public void testCreateArchiveFromExportedAppArtifactsError() throws Exception {
        printTestMethodName();
        FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager
                (apiStore, importExportRootDirectory);
        importExportManager.createArchiveFromExportedAppArtifacts("testDir", "testLoc", "testName");
    }

    private static void printTestMethodName() {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }

    @AfterClass
    protected void tearDown() {
        try {
            APIFileUtils.deleteDirectory(importExportRootDirectory);
        } catch (APIMgtDAOException e) {
            log.warn("Unable to delete directory " + importExportRootDirectory);
        }
    }

}