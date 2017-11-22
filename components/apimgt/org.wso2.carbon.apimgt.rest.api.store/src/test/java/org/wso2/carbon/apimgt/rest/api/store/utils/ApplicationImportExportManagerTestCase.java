package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.impl.ApplicationDAOImpl;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.workflow.Workflow;


import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DAOFactory.class, Application.class})
public class ApplicationImportExportManagerTestCase {

    private static final Logger log = LoggerFactory.getLogger(ApplicationImportExportManagerTestCase.class);

    APIStore consumer = Mockito.mock(APIStoreImpl.class);
    String USER = "admin";
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetApplicationDetails() throws Exception {
        printTestMethodName();
        String appId = UUID.randomUUID().toString();
        ApplicationImportExportManager applicationImportExportManager = new ApplicationImportExportManager(consumer);
        applicationImportExportManager.getApplicationDetails(appId, USER);
    }

    /*@Test
    public void updateApplication() throws Exception {
        printTestMethodName();
        ApplicationImportExportManager applicationImportExportManager = new ApplicationImportExportManager(consumer);
        //String appId = UUID.randomUUID().toString();
        //Application importedApp = applicationImportExportManager.getApplicationDetails(appId, USER);
        Application importedApp = Mockito.mock(Application.class);
        PowerMockito.mockStatic(DAOFactory.class);
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PowerMockito.when(DAOFactory.getApplicationDAO()).thenReturn(applicationDAO);
        PowerMockito.when(applicationDAO.isApplicationNameExists(Mockito.anyString())).thenReturn(true);
        APIStoreImpl apiStore =Mockito.mock(APIStoreImpl.class);
        WorkflowResponse workflowResponse = Mockito.mock(WorkflowResponse.class);
        Mockito.when(apiStore.updateApplication(Mockito.anyString(), importedApp)).thenReturn(workflowResponse);
        applicationImportExportManager.updateApplication(importedApp, USER);
        //Application importApp = Mockito.mock(Application.class);
        applicationImportExportManager.updateApplication(importedApp, USER);
        //check for a non-existing application
        //Assert.assertFalse(applicationDAO.isApplicationNameExists("ExistingApp"));

    }

    @Test
    public void updateApplicationError() throws Exception {
        printTestMethodName();
        Application application =Mockito.mock(Application.class);

        PowerMockito.mockStatic(DAOFactory.class);
        //ApplicationDAOImpl applicationDAO = Mockito.mock(ApplicationDAOImpl.class);
        //Mockito.when(applicationDAO.isApplicationNameExists(Mockito.anyString())).thenReturn(true);
       // ApplicationImportExportManager applicationImportExportManager = new ApplicationImportExportManager(consumer);
        //applicationImportExportManager.updateApplication(application, USER);
        PowerMockito.when(DAOFactory.getApplicationDAO().isApplicationNameExists(Mockito.anyString())).thenReturn(true);

        //check for a non-existing application
        //Assert.assertFalse(applicationDAO.isApplicationNameExists("ExistingApp"));
    }*/


    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }

}