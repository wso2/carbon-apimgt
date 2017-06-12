/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.TestUtil;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

public class ApplicationDAOImplIT extends DAOIntegrationTestBase {

    @Test
    public void testAddAndGetApplication() throws Exception {

        //add new app
        Application app = TestUtil.addTestApplication();
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        //get added app
        Application appFromDB = applicationDAO.getApplication(app.getId());
        Assert.assertNotNull(appFromDB);
        //compare
        Assert.assertEquals(appFromDB, app, TestUtil.printDiff(appFromDB, app));
        validateAppTimestamps(appFromDB, app);
    }

    @Test
    public void testUpdateApplication() throws Exception {

        //add new app
        Application currentApp = TestUtil.addTestApplication();
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        Application newApp = SampleTestObjectCreator.createAlternativeApplication();
        newApp.setId(currentApp.getId());
        newApp.setCreatedTime(currentApp.getCreatedTime());
        //update app
        applicationDAO.updateApplication(currentApp.getId(), newApp);
        //get app
        Application appFromDB = applicationDAO.getApplication(newApp.getId());
        Assert.assertNotNull(appFromDB);
        //compare
        Assert.assertEquals(appFromDB, newApp, TestUtil.printDiff(appFromDB, newApp));
        validateAppTimestamps(appFromDB, newApp);
    }

    @Test
    public void testDeleteApplication() throws Exception {

        // add app
        Application app = TestUtil.addTestApplication();
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        //delete app
        applicationDAO.deleteApplication(app.getId());

        try {
            applicationDAO.getApplication(app.getId());
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(), "Application is not available in the system.");
        }
    }

    @Test
    public void testIsApplicationNameExists() throws Exception {

        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        //check for a non-existing application
        Assert.assertFalse(applicationDAO.isApplicationNameExists("ExistingApp"));
        //add new app
        Application app = TestUtil.addTestApplication();
        //check for the existing application
        Assert.assertTrue(applicationDAO.isApplicationNameExists(app.getName()));
    }

    @Test
    public void testGetApplicationByName() throws Exception {

        // add app
        Application app = TestUtil.addTestApplication();
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        //get app by name
        Application appFromDB = applicationDAO.getApplicationByName(app.getName(), app.getCreatedUser());
        Assert.assertNotNull(appFromDB);
        //compare
        Assert.assertEquals(appFromDB, app, TestUtil.printDiff(appFromDB, app));
        validateAppTimestamps(appFromDB, app);
    }

    @Test
    public void testAddApplicationWithPermissions() throws Exception {

        //add new app with permissions
        Application app = TestUtil.addTestApplicationWithPermissions();
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        //get added app
        Application appFromDB = applicationDAO.getApplication(app.getId());
        Assert.assertNotNull(appFromDB);
        //compare
        Assert.assertEquals(appFromDB, app, TestUtil.printDiff(appFromDB, app));
        validateAppTimestamps(appFromDB, app);
    }

    @Test
    public void testUpdateApplicationWithPermissions() throws Exception {

        //add new app
        Application currentApp = TestUtil.addTestApplication();
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        //create new app with permissions
        HashMap permissionMap = new HashMap();
        permissionMap.put(APIMgtConstants.Permission.UPDATE, APIMgtConstants.Permission.UPDATE_PERMISSION);
        Application newApp = SampleTestObjectCreator.createAlternativeApplication();
        newApp.setId(currentApp.getId());
        newApp.setCreatedTime(currentApp.getCreatedTime());
        newApp.setPermissionMap(permissionMap);
        //update app
        applicationDAO.updateApplication(currentApp.getId(), newApp);
        //get app
        Application appFromDB = applicationDAO.getApplication(newApp.getId());
        Assert.assertNotNull(appFromDB);
        //compare
        Assert.assertEquals(appFromDB, newApp, TestUtil.printDiff(appFromDB, newApp));
        validateAppTimestamps(appFromDB, newApp);
    }

    @Test
    public void testUpdateApplicationState() throws Exception {

        //add new app
        Application app = TestUtil.addTestApplication();
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        //update app
        applicationDAO.updateApplicationState(app.getId(), APIMgtConstants.ApplicationStatus.APPLICATION_APPROVED);
        //get app
        Application appFromDB = applicationDAO.getApplication(app.getId());
        Assert.assertNotNull(appFromDB);
        //check whether the status has updated
        Assert.assertEquals(appFromDB.getStatus(), APIMgtConstants.ApplicationStatus.APPLICATION_APPROVED);
        //compare
        Assert.assertNotEquals(appFromDB, app, TestUtil.printDiff(appFromDB, app));
        validateAppTimestamps(appFromDB, app);
    }

    @Test
    public void testGetAllApplications() throws Exception {

        //add 4 apps
        String username = "admin";
        Application app1 = TestUtil.addCustomApplication("App1", username);
        Application app2 = TestUtil.addCustomApplication("App2", username);
        Application app3 = TestUtil.addCustomApplication("App3", username);
        Application app4 = TestUtil.addCustomApplication("App4", username);
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        //get added apps
        List<Application> appsFromDB = applicationDAO.getApplications(username);
        Assert.assertNotNull(appsFromDB);
        Assert.assertEquals(appsFromDB.size(), 4);
        for (Application application : appsFromDB) {
            Assert.assertNotNull(application);
            if (application.getName().equals(app1.getName())) {
                Assert.assertEquals(application, app1, TestUtil.printDiff(application, app1));
                validateAppTimestamps(application, app1);
            } else if (application.getName().equals(app2.getName())) {
                Assert.assertEquals(application, app2, TestUtil.printDiff(application, app2));
                validateAppTimestamps(application, app2);
            } else if (application.getName().equals(app3.getName())) {
                Assert.assertEquals(application, app3, TestUtil.printDiff(application, app3));
                validateAppTimestamps(application, app3);
            } else if (application.getName().equals(app4.getName())) {
                Assert.assertEquals(application, app4, TestUtil.printDiff(application, app4));
                validateAppTimestamps(application, app4);
            } else {
                Assert.fail("Invalid Application returned.");
            }
        }
    }

    @Test
    public void testGetAllApplicationsForValidation() throws Exception {

        //add 4 apps
        String username = "admin";
        Application app1 = TestUtil.addCustomApplication("App1", username);
        Application app2 = TestUtil.addCustomApplication("App2", username);
        Application app3 = TestUtil.addCustomApplication("App3", username);
        Application app4 = TestUtil.addCustomApplication("App4", username);
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //get added apps
        List<Application> appsFromDB = applicationDAO.getAllApplications();
        Assert.assertNotNull(appsFromDB);
        Assert.assertEquals(appsFromDB.size(), 4);
        for (Application application : appsFromDB) {
            Assert.assertNotNull(application);
            if (application.getName().equals(app1.getName())) {
                validateApp(application, app1, policyDAO);
            } else if (application.getName().equals(app2.getName())) {
                validateApp(application, app2, policyDAO);
            } else if (application.getName().equals(app3.getName())) {
                validateApp(application, app3, policyDAO);
            } else if (application.getName().equals(app4.getName())) {
                validateApp(application, app4, policyDAO);
            } else {
                Assert.fail("Invalid Application returned.");
            }
        }
    }

    @Test
    public void testFingerprintAfterUpdatingApplication() throws Exception {
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();

        //add new app
        Application currentApp = TestUtil.addTestApplication();
        String fingerprintBeforeUpdate = ETagUtils
                .generateETag(applicationDAO.getLastUpdatedTimeOfApplication(currentApp.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);
        Thread.sleep(1);

        Application newApp = SampleTestObjectCreator.createAlternativeApplication();
        newApp.setId(currentApp.getId());
        newApp.setCreatedTime(currentApp.getCreatedTime());
        //update app
        applicationDAO.updateApplication(currentApp.getId(), newApp);
        String fingerprintAfterUpdate = ETagUtils
                .generateETag(applicationDAO.getLastUpdatedTimeOfApplication(currentApp.getId()));
        Assert.assertNotNull(fingerprintAfterUpdate);

        //compare
        Assert.assertNotEquals(fingerprintBeforeUpdate, fingerprintAfterUpdate);
    }

    @Test
    public void testGetApplicationsForUser() throws Exception {

    }

    @Test
    public void testSearchApplicationsForUser() throws Exception {

    }

    @Test
    public void testAddAndGetApplicationKeys() throws Exception {
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        //add test app
        Application app = TestUtil.addTestApplicationWithPermissions();
        String appId = app.getId();
        String prodConsumerKey = "prod-xxx";
        String sandConsumerKey = "sand-yyy";

        //add prod key
        applicationDAO.addApplicationKeys(appId, KeyManagerConstants.OAUTH_CLIENT_PRODUCTION, prodConsumerKey);
        //get by key type
        OAuthApplicationInfo keysFromDB = applicationDAO.getApplicationKeys(appId,
                KeyManagerConstants.OAUTH_CLIENT_PRODUCTION);
        Assert.assertEquals(keysFromDB.getClientId(), prodConsumerKey);

        //add sand key
        applicationDAO.addApplicationKeys(appId, KeyManagerConstants.OAUTH_CLIENT_SANDBOX, sandConsumerKey);
        //get all keys
        List<OAuthApplicationInfo> allKeysFromDB = applicationDAO.getApplicationKeys(appId);
        Assert.assertEquals(allKeysFromDB.size(), 2, "Wrong number of keys are returned.");

        int i = 0; //this should stay 0 at the end
        for (OAuthApplicationInfo oAuthApplicationInfo : allKeysFromDB) {
            switch (oAuthApplicationInfo.getKeyType()) {
                case KeyManagerConstants.OAUTH_CLIENT_PRODUCTION:
                    Assert.assertEquals(oAuthApplicationInfo.getClientId(), prodConsumerKey);
                    i++;
                    break;
                case KeyManagerConstants.OAUTH_CLIENT_SANDBOX:
                    Assert.assertEquals(oAuthApplicationInfo.getClientId(), sandConsumerKey);
                    i--;
                    break;
                default:
                    Assert.fail("Invalid key type.");
                    break;
            }
        }
        Assert.assertEquals(i, 0, "Received key counts of each type is not 1");
    }

    private void validateAppTimestamps(Application appFromDB, Application expectedApp) {
        Assert.assertTrue(Duration.between(expectedApp.getCreatedTime(), appFromDB.getCreatedTime()).toMillis() < 1000L,
                "Application created time is not the same!");
        Assert.assertTrue(Duration.between(expectedApp.getUpdatedTime(), appFromDB.getUpdatedTime()).toMillis() < 1000L,
                "Application updated time is not the same!");
    }

    private void validateApp(Application appFromDB, Application expectedApp, PolicyDAO policyDAO) throws
            APIMgtDAOException, IllegalAccessException {
        Assert.assertEquals(appFromDB.getName(), expectedApp.getName(), TestUtil.printDiff(appFromDB.getName(),
                expectedApp.getName()));
        Assert.assertEquals(appFromDB.getStatus(), expectedApp.getStatus(), TestUtil.printDiff(appFromDB.getStatus(),
                expectedApp.getStatus()));
        Assert.assertEquals(appFromDB.getCreatedUser(), expectedApp.getCreatedUser(), TestUtil.printDiff(appFromDB
                .getCreatedUser(), expectedApp.getCreatedUser()));
        Assert.assertEquals(appFromDB.getId(), expectedApp.getId(), TestUtil.printDiff(appFromDB.getId(), expectedApp
                .getId()));
        Assert.assertEquals(policyDAO.getApplicationPolicyByUuid(appFromDB.getPolicy().getUuid()).getPolicyName(),
                expectedApp
                .getPolicy().getPolicyName(), TestUtil.printDiff(policyDAO.getApplicationPolicyByUuid(appFromDB
                        .getPolicy().getUuid())
                .getPolicyName(), expectedApp.getPolicy().getPolicyName()));
    }
}
