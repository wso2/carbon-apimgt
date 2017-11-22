/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.factory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstantOracle;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstantPostgreSQL;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstantsDB2;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstantsH2MySQL;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstantsMSSQL;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIMgtDBUtil.class)
public class SQLConstantmanagerFactoryTestCase {

    private Connection connection;
    DatabaseMetaData databaseMetaData;

    @Before
    public void setup() throws Exception{
        PowerMockito.mockStatic(APIMgtDBUtil.class);
        connection = Mockito.mock(Connection.class);
        databaseMetaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(APIMgtDBUtil.getConnection()).thenReturn(connection);
        Mockito.when(databaseMetaData.getDatabaseProductName()).thenReturn("");
    }

    @Test
    public void testInitializeSQLConstantManagerMySQL() throws Exception {
        Mockito.when(databaseMetaData.getDriverName()).thenReturn("MySQL");
        SQLConstantManagerFactory.initializeSQLConstantManager();
        PowerMockito.verifyStatic(APIMgtDBUtil.class);
        APIMgtDBUtil.closeAllConnections(null, connection, null);
    }

    @Test
    public void testInitializeSQLConstantManagerDB2() throws Exception {
        Mockito.when(databaseMetaData.getDriverName()).thenReturn("");
        Mockito.when(databaseMetaData.getDatabaseProductName()).thenReturn("DB2");
        SQLConstantManagerFactory.initializeSQLConstantManager();
        PowerMockito.verifyStatic(APIMgtDBUtil.class);
        APIMgtDBUtil.closeAllConnections(null, connection, null);
    }

    @Test
    public void testInitializeSQLConstantManagerMSSQL() throws Exception {
        Mockito.when(databaseMetaData.getDriverName()).thenReturn("MS SQL");
        SQLConstantManagerFactory.initializeSQLConstantManager();
        PowerMockito.verifyStatic(APIMgtDBUtil.class);
        APIMgtDBUtil.closeAllConnections(null, connection, null);
    }

    @Test
    public void testInitializeSQLConstantManagerMicrosoft() throws Exception {
        Mockito.when(databaseMetaData.getDriverName()).thenReturn("Microsoft");
        SQLConstantManagerFactory.initializeSQLConstantManager();
        PowerMockito.verifyStatic(APIMgtDBUtil.class);
        APIMgtDBUtil.closeAllConnections(null, connection, null);
    }

    @Test
    public void testInitializeSQLConstantManagerPostgreSQL() throws Exception {
        Mockito.when(databaseMetaData.getDriverName()).thenReturn("PostgreSQL");
        SQLConstantManagerFactory.initializeSQLConstantManager();
        PowerMockito.verifyStatic(APIMgtDBUtil.class);
        APIMgtDBUtil.closeAllConnections(null, connection, null);
    }

    @Test
    public void testInitializeSQLConstantManagerOracle() throws Exception {
        Mockito.when(databaseMetaData.getDriverName()).thenReturn("Oracle");
        SQLConstantManagerFactory.initializeSQLConstantManager();
        PowerMockito.verifyStatic(APIMgtDBUtil.class);
        APIMgtDBUtil.closeAllConnections(null, connection, null);
    }

    @Test(expected = APIManagementException.class)
    public void testInitializeSQLConstantManagerNone() throws Exception {
        Mockito.when(databaseMetaData.getDriverName()).thenReturn("");
        SQLConstantManagerFactory.initializeSQLConstantManager();
    }

    @Test(expected = APIManagementException.class)
    public void testInitializeSQLConstantManagerSQLException() throws Exception {
        Mockito.doThrow(SQLException.class).when(connection).getMetaData();
        SQLConstantManagerFactory.initializeSQLConstantManager();
    }

    @Test
    public void testGetSQLStringH2MySQL() throws Exception{
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "dbType", "h2mysql");
        SQLConstantsH2MySQL sqlConstantsH2MySQL = new SQLConstantsH2MySQL();
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "sqlConstantsH2MySQL", sqlConstantsH2MySQL);
        String result = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_CASESENSITVE");
        Assert.assertEquals(result, SQLConstantsH2MySQL.GET_APPLICATIONS_PREFIX_CASESENSITVE);
    }

    @Test
    public void testGetSQLStringH2MSSQL() throws Exception{
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "dbType", "mssql");
        SQLConstantsMSSQL sqlConstantsMSSQL = new SQLConstantsMSSQL();
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "sqlConstantsMSSQL", sqlConstantsMSSQL);
        String result = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_CASESENSITVE");
        Assert.assertEquals(result, SQLConstantsMSSQL.GET_APPLICATIONS_PREFIX_CASESENSITVE);
    }

    @Test
    public void testGetSQLStringDB2() throws Exception{
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "dbType", "db2");
        SQLConstantsDB2 sqlConstantsDB2 = new SQLConstantsDB2();
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "sqlConstantsDB2", sqlConstantsDB2);
        String result = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_CASESENSITVE");
        Assert.assertEquals(result, SQLConstantsDB2.GET_APPLICATIONS_PREFIX_CASESENSITVE);
    }

    @Test
    public void testGetSQLStringPostgre() throws Exception{
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "dbType", "postgre");
        SQLConstantPostgreSQL sqlConstantPostgreSQL = new SQLConstantPostgreSQL();
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "sqlConstantPostgreSQL", sqlConstantPostgreSQL);
        String result = SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_CASESENSITVE");
        Assert.assertEquals(result, SQLConstantPostgreSQL.GET_APPLICATIONS_PREFIX_CASESENSITVE);
    }

    @Test
    public void testGetSQLStringOrcale() throws Exception{
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "dbType", "oracle");
        SQLConstantOracle sqlConstantOracle = new SQLConstantOracle();
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "sqlConstantOracle", sqlConstantOracle);
        String result= SQLConstantManagerFactory.getSQlString("GET_APPLICATIONS_PREFIX_CASESENSITVE");
        Assert.assertEquals(result, SQLConstantOracle.GET_APPLICATIONS_PREFIX_CASESENSITVE);
    }

    @Test(expected = APIManagementException.class)
    public void testGetSQLStringNoSucFieldException() throws Exception{
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "dbType", "oracle");
        SQLConstantOracle sqlConstantOracle = new SQLConstantOracle();
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "sqlConstantOracle", sqlConstantOracle);
        SQLConstantManagerFactory.getSQlString("");
    }

    @Test(expected = APIManagementException.class)
    public void testGetSQLStringNoDBTypeFound() throws Exception{
        Whitebox.setInternalState(SQLConstantManagerFactory.class, "dbType", "");
        SQLConstantManagerFactory.getSQlString("");
    }

}
