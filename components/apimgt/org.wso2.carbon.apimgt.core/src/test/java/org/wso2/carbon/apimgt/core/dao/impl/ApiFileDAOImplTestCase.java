/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.TestUtil;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


public class ApiFileDAOImplTestCase {
    private static final String EDITOR_SAVE_PATH = "editorSavePath";
    private static final String EDITOR_MODE = "editorMode";
    private static final String ADMIN = "admin";
    File tempWorkspace = null;

    @BeforeClass
    public void setup() throws IOException {
        //create a temp folder to save files.
        tempWorkspace = File.createTempFile("editorWorkspace", "");
        tempWorkspace.delete();
        tempWorkspace.mkdir();
        tempWorkspace.deleteOnExit();

        //set system properties
        System.setProperty(EDITOR_MODE, "true");
        System.setProperty(EDITOR_SAVE_PATH, tempWorkspace.getAbsolutePath());
    }

    @AfterClass
    public void unsetSystemProperties() {
        //unset system properties
        System.clearProperty(EDITOR_MODE);
        System.clearProperty(EDITOR_SAVE_PATH);
    }

    @BeforeMethod
    public void cleanTempDirectory() throws IOException {
        // Clean temp directory before each test
        FileUtils.cleanDirectory(tempWorkspace);
    }

    @Test
    public void testAddGetAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        apiDAO.addAPI(api);

        API apiFromDB = apiDAO.getAPI(api.getId());

        Assert.assertNotNull(apiFromDB);
        Assert.assertTrue(api.equals(apiFromDB), TestUtil.printDiff(api, apiFromDB));
    }

    @Test
    public void testAddGetAPIWithApiLevelEndpoint() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPIWithApiLevelEndpoint();
        API api = builder.build();
        apiDAO.addAPI(api);
        API apiFromDB = apiDAO.getAPI(api.getId());

        Assert.assertNotNull(apiFromDB);
        Assert.assertTrue(api.equals(apiFromDB), TestUtil.printDiff(api, apiFromDB));
    }

    @Test
    public void testDeleteAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        apiDAO.addAPI(api);

        apiDAO.deleteAPI(api.getId());

        API deletedAPI = apiDAO.getAPI(api.getId());
        Assert.assertNull(deletedAPI);
    }

    @Test
    public void testUpdateAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        apiDAO.addAPI(api);

        HashMap permissionMap = new HashMap();
        permissionMap.put(APIMgtConstants.Permission.UPDATE, APIMgtConstants.Permission.UPDATE_PERMISSION);
        builder = SampleTestObjectCreator.createAlternativeAPI().permissionMap(permissionMap);
        API substituteAPI = builder.build();

        apiDAO.updateAPI(api.getId(), substituteAPI);
        API apiFromFile = apiDAO.getAPI(api.getId());

        API expectedAPI = SampleTestObjectCreator.copyAPIIgnoringNonEditableFields(api, substituteAPI);

        Assert.assertNotNull(apiFromFile);
        Assert.assertEquals(apiFromFile, expectedAPI, TestUtil.printDiff(apiFromFile, expectedAPI));
    }

    @Test(description = "Get image from API")
    public void testGetImage() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        apiDAO.addAPI(api);
        apiDAO.updateImage(api.getId(), SampleTestObjectCreator.createDefaultThumbnailImage(), "image/jpg", ADMIN);
        InputStream image = apiDAO.getImage(api.getId());
        Assert.assertNotNull(image);
    }
}
