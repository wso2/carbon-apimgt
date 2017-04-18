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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.TestUtil;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Endpoint;

import java.io.File;
import java.io.IOException;


public class ApiFileDAOImplIT {
    private static final String EDITOR_SAVE_PATH = "editorSavePath";
    private static final String EDITOR_MODE = "editorMode";

    @BeforeClass
    public void setup() throws IOException {
        //create a temp folder to save files.
        File tempWorkspace = File.createTempFile("editorWorkspace", "");
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

    @Test
    public void testAddGetAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        API apiFromDB = apiDAO.getAPI(api.getId());

        Assert.assertNotNull(apiFromDB);
        Assert.assertTrue(api.equals(apiFromDB), TestUtil.printDiff(api, apiFromDB));
    }

    @Test
    public void testAddGetEndpoint() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        apiDAO.addEndpoint(endpoint);
        Endpoint retrieved = apiDAO.getEndpoint(endpoint.getId());
        Assert.assertEquals(endpoint, retrieved);
    }

    @Test
    public void testAddUpdateGetEndpoint() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        apiDAO.addEndpoint(SampleTestObjectCreator.createMockEndpoint());
        Endpoint updatedEndpoint = SampleTestObjectCreator.createUpdatedEndpoint();
        apiDAO.updateEndpoint(updatedEndpoint);
        Endpoint retrieved = apiDAO.getEndpoint(updatedEndpoint.getId());
        Assert.assertEquals(updatedEndpoint, retrieved);
    }

    @Test
    public void testAddDeleteGetEndpoint() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        apiDAO.addEndpoint(endpoint);
        apiDAO.deleteEndpoint(endpoint.getId());
        Endpoint retrieved = apiDAO.getEndpoint(endpoint.getId());
        Assert.assertNull(retrieved);
    }

    @Test
    public void testDeleteAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        apiDAO.deleteAPI(api.getId());

        API deletedAPI = apiDAO.getAPI(api.getId());
        Assert.assertNull(deletedAPI);
    }
}
