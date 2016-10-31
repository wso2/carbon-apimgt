/*
 *
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
 *
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.models.API;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


public class DefaultApiDAOImplTest {

    // Reuse same connection in test since we are creating an in memory DB
    DataSource dataSource;
    private static final String sqlFilePath = "src" + File.separator + "main" +
            File.separator + "resources" + File.separator + "h2.sql";

    @org.testng.annotations.BeforeClass
    public void setUp() throws Exception {
        dataSource = new InMemoryDataSource();
        DAOUtil.initialize(dataSource);

        try (Connection connection = DAOUtil.getConnection()) {
            DBScriptRunnerUtil.executeSQLScript(sqlFilePath, connection);
        }
    }


    @org.testng.annotations.AfterClass
    public void tempDBCleanup() throws SQLException, IOException {
        ((InMemoryDataSource)dataSource).resetDB();
    }

    @Test
    public void testAddDeleteGetAPI() throws Exception {
        ApiDAO apiDAO = new DefaultApiDAOImpl();
        API api = new API("admin", "1.0.0", "WeatherAPI");

        API createdAPI = apiDAO.addAPI(api);

        Assert.assertEquals(createdAPI.getProvider(), api.getProvider());
        Assert.assertEquals(createdAPI.getVersion(), api.getVersion());
        Assert.assertEquals(createdAPI.getName(), api.getName());

        API apiFromDB = apiDAO.getAPI(createdAPI.getID());

        Assert.assertNotNull(apiFromDB);
        Assert.assertEquals(apiFromDB.getProvider(), createdAPI.getProvider());
        Assert.assertEquals(apiFromDB.getVersion(), createdAPI.getVersion());
        Assert.assertEquals(apiFromDB.getName(), createdAPI.getName());
        Assert.assertEquals(apiFromDB.getID(), createdAPI.getID());

        apiDAO.deleteAPI(apiFromDB.getID());

        API deletedAPI = apiDAO.getAPI(apiFromDB.getID());
        Assert.assertNull(deletedAPI);
    }

    @Test
    public void testGetAPIsForRoles() throws Exception {

    }

    @Test
    public void testSearchAPIsForRoles() throws Exception {

    }



    @Test
    public void testUpdateAPI() throws Exception {

    }

    @Test
    public void testDeleteAPI() throws Exception {

    }

    @Test
    public void testGetSwaggerDefinition() throws Exception {

    }

    @Test
    public void testUpdateSwaggerDefinition() throws Exception {

    }

    @Test
    public void testGetImage() throws Exception {

    }

    @Test
    public void testUpdateImage() throws Exception {

    }

    @Test
    public void testChangeLifeCylceStatus() throws Exception {

    }

    @Test
    public void testCreateNewAPIVersion() throws Exception {

    }

    @Test
    public void testGetDocumentsInfoList() throws Exception {

    }

    @Test
    public void testGetDocumentInfo() throws Exception {

    }



}