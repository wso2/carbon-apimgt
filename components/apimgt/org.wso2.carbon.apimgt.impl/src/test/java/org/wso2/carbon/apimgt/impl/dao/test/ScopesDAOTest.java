/*
 *  Copyright (c) 2026, WSO2 LLC (http://www.wso2.com)
 *
 *  WSO2 LLC licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.impl.dao.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.UUID;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

/**
 * Tests {@link ScopesDAO#deleteScopeIfUnused(String, int)}, which removes a local scope after a revision restore only
 * when no API resource of any kind uses it any more and it is not a shared scope.
 */
@RunWith(PowerMockRunner.class)
public class ScopesDAOTest {

    private static final int TENANT_ID = -1234;
    private static final int OTHER_TENANT_ID = 1;
    // API ids that do not collide with the sample data or other DAO tests sharing the same H2 database
    private static final int API_ID = 910001;
    private static final int OTHER_API_ID = 910002;

    private ScopesDAO scopesDAO;

    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (config));
        APIMgtDBUtil.initialize();
        scopesDAO = ScopesDAO.getInstance();
    }

    @Test
    public void testUnusedLocalScopeIsDeleted() throws Exception {
        String scopeName = newScope(TENANT_ID);

        Assert.assertTrue(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
        Assert.assertFalse(scopesDAO.isScopeExist(scopeName, TENANT_ID));
        Assert.assertEquals("Role bindings must be removed with the scope", 0, countBindings(scopeName));
    }

    @Test
    public void testScopeUsedByWorkingCopyIsKept() throws Exception {
        String scopeName = newScope(TENANT_ID);
        attach(scopeName, API_ID, null, TENANT_ID);

        Assert.assertFalse(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
        Assert.assertTrue(scopesDAO.isScopeExist(scopeName, TENANT_ID));
        Assert.assertEquals(1, countBindings(scopeName));
    }

    @Test
    public void testScopeUsedOnlyByRevisionIsKept() throws Exception {
        String scopeName = newScope(TENANT_ID);
        attach(scopeName, API_ID, UUID.randomUUID().toString(), TENANT_ID);

        Assert.assertFalse(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
        Assert.assertTrue(scopesDAO.isScopeExist(scopeName, TENANT_ID));
    }

    @Test
    public void testScopeUsedByAnotherApiVersionIsKept() throws Exception {
        String scopeName = newScope(TENANT_ID);
        attach(scopeName, OTHER_API_ID, null, TENANT_ID);

        Assert.assertFalse(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
        Assert.assertTrue(scopesDAO.isScopeExist(scopeName, TENANT_ID));
    }

    @Test
    public void testScopeUsedByApiProductIsKept() throws Exception {
        String scopeName = newScope(TENANT_ID);
        // An API Product keeps its own copy of the API resource, with the product id as the revision reference
        attach(scopeName, API_ID, String.valueOf(OTHER_API_ID), TENANT_ID);

        Assert.assertFalse(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
        Assert.assertTrue(scopesDAO.isScopeExist(scopeName, TENANT_ID));
    }

    @Test
    public void testUnusedSharedScopeIsKept() throws Exception {
        String scopeName = newScope(TENANT_ID);
        markShared(scopeName, TENANT_ID);

        Assert.assertFalse(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
        Assert.assertTrue(scopesDAO.isScopeExist(scopeName, TENANT_ID));
    }

    @Test
    public void testOnlyTheGivenTenantIsAffected() throws Exception {
        String scopeName = newScope(TENANT_ID);
        addScope(scopeName, OTHER_TENANT_ID);
        attach(scopeName, OTHER_API_ID, null, OTHER_TENANT_ID);

        // Unused in TENANT_ID (the use in the other tenant does not count), so it is removed there only
        Assert.assertTrue(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
        Assert.assertFalse(scopesDAO.isScopeExist(scopeName, TENANT_ID));
        Assert.assertTrue(scopesDAO.isScopeExist(scopeName, OTHER_TENANT_ID));
    }

    @Test
    public void testSharedScopeInAnotherTenantDoesNotProtectLocalScope() throws Exception {
        String scopeName = newScope(TENANT_ID);
        addScope(scopeName, OTHER_TENANT_ID);
        markShared(scopeName, OTHER_TENANT_ID);

        Assert.assertTrue(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
        Assert.assertFalse(scopesDAO.isScopeExist(scopeName, TENANT_ID));
        Assert.assertTrue(scopesDAO.isScopeExist(scopeName, OTHER_TENANT_ID));
    }

    @Test
    public void testMissingScopeReturnsFalse() throws Exception {
        String scopeName = "missing_" + UUID.randomUUID();

        Assert.assertFalse(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
    }

    @Test
    public void testRepeatedDeleteIsHarmless() throws Exception {
        String scopeName = newScope(TENANT_ID);

        Assert.assertTrue(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
        Assert.assertFalse(scopesDAO.deleteScopeIfUnused(scopeName, TENANT_ID));
    }

    private String newScope(int tenantId) throws Exception {
        String scopeName = "scope_" + UUID.randomUUID();
        addScope(scopeName, tenantId);
        return scopeName;
    }

    private void addScope(String scopeName, int tenantId) throws Exception {
        Scope scope = new Scope();
        scope.setKey(scopeName);
        scope.setName(scopeName);
        scope.setDescription(scopeName);
        scope.setRoles("admin");
        scopesDAO.addScopes(Collections.singleton(scope), tenantId);
        Assert.assertTrue(scopesDAO.isScopeExist(scopeName, tenantId));
    }

    private void attach(String scopeName, int apiId, String revisionUuid, int tenantId) throws SQLException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement urlMapping = connection.prepareStatement(
                     "INSERT INTO AM_API_URL_MAPPING (API_ID, HTTP_METHOD, AUTH_SCHEME, URL_PATTERN, REVISION_UUID) "
                             + "VALUES (?, 'GET', 'Any', '/menu', ?)", Statement.RETURN_GENERATED_KEYS)) {
            urlMapping.setInt(1, apiId);
            urlMapping.setString(2, revisionUuid);
            urlMapping.executeUpdate();
            int urlMappingId;
            try (ResultSet keys = urlMapping.getGeneratedKeys()) {
                Assert.assertTrue(keys.next());
                urlMappingId = keys.getInt(1);
            }
            try (PreparedStatement scopeMapping = connection.prepareStatement(
                    "INSERT INTO AM_API_RESOURCE_SCOPE_MAPPING (SCOPE_NAME, URL_MAPPING_ID, TENANT_ID) "
                            + "VALUES (?, ?, ?)")) {
                scopeMapping.setString(1, scopeName);
                scopeMapping.setInt(2, urlMappingId);
                scopeMapping.setInt(3, tenantId);
                scopeMapping.executeUpdate();
            }
        }
    }

    private void markShared(String scopeName, int tenantId) throws SQLException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO AM_SHARED_SCOPE (NAME, UUID, TENANT_ID) VALUES (?, ?, ?)")) {
            statement.setString(1, scopeName);
            statement.setString(2, UUID.randomUUID().toString());
            statement.setInt(3, tenantId);
            statement.executeUpdate();
        }
    }

    private int countBindings(String scopeName) throws SQLException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM AM_SCOPE_BINDING B JOIN AM_SCOPE S ON B.SCOPE_ID = S.SCOPE_ID "
                             + "WHERE S.NAME = ? AND S.TENANT_ID = ?")) {
            statement.setString(1, scopeName);
            statement.setInt(2, TENANT_ID);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private static void initializeDatabase(String configFilePath) {

        InputStream in;
        try {
            in = FileUtils.openInputStream(new File(configFilePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            String dataSource = builder.getDocumentElement().getFirstChildWithName(new QName("DataSourceName")).
                    getText();
            OMElement databaseElement = builder.getDocumentElement().getFirstChildWithName(new QName("Database"));
            String databaseURL = databaseElement.getFirstChildWithName(new QName("URL")).getText();
            String databaseUser = databaseElement.getFirstChildWithName(new QName("Username")).getText();
            String databasePass = databaseElement.getFirstChildWithName(new QName("Password")).getText();
            String databaseDriver = databaseElement.getFirstChildWithName(new QName("Driver")).getText();

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(databaseDriver);
            basicDataSource.setUrl(databaseURL);
            basicDataSource.setUsername(databaseUser);
            basicDataSource.setPassword(databasePass);

            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES,
                    "org.apache.naming");
            try {
                InitialContext.doLookup("java:/comp/env/jdbc/WSO2AM_DB");
            } catch (NamingException e) {
                InitialContext ic = new InitialContext();
                ic.createSubcontext("java:");
                ic.createSubcontext("java:/comp");
                ic.createSubcontext("java:/comp/env");
                ic.createSubcontext("java:/comp/env/jdbc");

                ic.bind("java:/comp/env/jdbc/WSO2AM_DB", basicDataSource);
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
