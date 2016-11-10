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


public class ApiDAOImplIT {
    private DataSource dataSource;
    private static final String sqlFilePath = "src" + File.separator + "main" +
            File.separator + "resources" + File.separator + "h2.sql";

    @org.testng.annotations.BeforeMethod
    public void setUp() throws Exception {
        dataSource = new InMemoryDataSource();
        DAOUtil.initialize(dataSource);

        try (Connection connection = DAOUtil.getConnection()) {
            DBScriptRunnerUtil.executeSQLScript(sqlFilePath, connection);
        }
    }


    @org.testng.annotations.AfterMethod
    public void tempDBCleanup() throws SQLException, IOException {
        ((InMemoryDataSource) dataSource).resetDB();
    }

    @Test
    public void testAddGetAPI() throws Exception {
        ApiDAO apiDAO = new ApiDAOImpl(new H2MySQLStatements());
        API api = SampleAPICreator.createDefaultAPI();

        apiDAO.addAPI(api);

        API apiFromDB = apiDAO.getAPI(api.getId());

        Assert.assertNotNull(apiFromDB);
        validateAPIs(apiFromDB, api);
    }


    @Test
    public void testDeleteAPI() throws Exception {
        ApiDAO apiDAO = new ApiDAOImpl(new H2MySQLStatements());
        API api = SampleAPICreator.createDefaultAPI();

        apiDAO.addAPI(api);

        apiDAO.deleteAPI(api.getId());

        API deletedAPI = apiDAO.getAPI(api.getId());
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

    private void validateAPIs(API actualAPI, API expectedAPI) {
        Assert.assertEquals(actualAPI.getProvider(), expectedAPI.getProvider());
        Assert.assertEquals(actualAPI.getVersion(), expectedAPI.getVersion());
        Assert.assertEquals(actualAPI.getName(), expectedAPI.getName());
        Assert.assertEquals(actualAPI.getDescription(), expectedAPI.getDescription());
        Assert.assertEquals(actualAPI.getContext(), expectedAPI.getContext());
        Assert.assertEquals(actualAPI.getId(), expectedAPI.getId());
        Assert.assertEquals(actualAPI.getLifeCycleStatus(), expectedAPI.getLifeCycleStatus());
        Assert.assertEquals(actualAPI.getLifecycleInstanceId(), expectedAPI.getLifecycleInstanceId());
        Assert.assertEquals(actualAPI.getApiDefinition(), expectedAPI.getApiDefinition());
        Assert.assertEquals(actualAPI.getWsdlUri(), expectedAPI.getWsdlUri());
        Assert.assertEquals(actualAPI.isResponseCachingEnabled(), expectedAPI.isResponseCachingEnabled());
        Assert.assertEquals(actualAPI.getCacheTimeout(), expectedAPI.getCacheTimeout());
        Assert.assertEquals(actualAPI.isDefaultVersion(), expectedAPI.isDefaultVersion());
        //Assert.assertEquals(actualAPI.getApiPolicy(), expectedAPI.getApiPolicy());
        Assert.assertEquals(actualAPI.getTransport(), expectedAPI.getTransport());
        Assert.assertEquals(actualAPI.getTags(), expectedAPI.getTags());
        //Assert.assertEquals(actualAPI.getPolicies(), expectedAPI.getPolicies());
        Assert.assertEquals(actualAPI.getVisibility(), expectedAPI.getVisibility());
        Assert.assertEquals(actualAPI.getVisibleRoles(), expectedAPI.getVisibleRoles());
        //Assert.assertEquals(actualAPI.getEndpoints(), expectedAPI.getEndpoints());
        //Assert.assertEquals(actualAPI.getGatewayEnvironments(), expectedAPI.getGatewayEnvironments());
        Assert.assertEquals(actualAPI.getBusinessInformation(), expectedAPI.getBusinessInformation());
        Assert.assertEquals(actualAPI.getCorsConfiguration(), expectedAPI.getCorsConfiguration());
        Assert.assertEquals(actualAPI.getCreatedTime(), expectedAPI.getCreatedTime());
        Assert.assertEquals(actualAPI.getCreatedBy(), expectedAPI.getCreatedBy());
        Assert.assertEquals(actualAPI.getLastUpdatedTime(), expectedAPI.getLastUpdatedTime());
    }

}
