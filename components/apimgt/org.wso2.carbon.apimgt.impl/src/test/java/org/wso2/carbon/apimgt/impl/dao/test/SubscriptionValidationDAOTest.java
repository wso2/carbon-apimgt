/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dao.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.subscription.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
public class SubscriptionValidationDAOTest {

    public SubscriptionValidationDAO subscriptionValidationDAO;
    private static String KM_UUID = UUID.randomUUID().toString();
    private static String APP1_UUID = UUID.randomUUID().toString();
    private static String APP2_UUID = UUID.randomUUID().toString();
    private static int APPLICATION1_ID;
    private static int APPLICATION2_ID;
    private static String CONSUMER_KEY1 = "xbpK4gvIey2EkyVm5fDKSfKQ59Qa";
    private static String CONSUMER_KEY2 = "gYBKWtmq323neB4kTIbtY8324hca";
    private static String TENANT_DOMAIN = "carbon.super";
    private static String KEY_TYPE = "PRODUCTION";
    private static String STATE = "COMPLETED";
    private static String APPLICATION1_NAME = "Application1";
    private static String APPLICATION2_NAME = "Application2";
    private static String STATUS = "APPROVED";
    private static String TIER = "Unlimited";
    private static String APP_OWNER = "admin";
    private static String TOKEN_TYPE = "JWT";

    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance()
                .setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl(config));
        APIMgtDBUtil.initialize();
        subscriptionValidationDAO = new SubscriptionValidationDAO();
        IdentityTenantUtil.setRealmService(new TestRealmService());
        String identityConfigPath = System.getProperty("IdentityConfigurationPath");
        IdentityConfigParser.getInstance(identityConfigPath);
        OAuthServerConfiguration oAuthServerConfiguration = OAuthServerConfiguration.getInstance();
        ServiceReferenceHolder.getInstance().setOauthServerConfiguration(oAuthServerConfiguration);
        //Insert Key Manager entry to the database
        insertKeyManagerEntry(KM_UUID, APIConstants.KeyManager.DEFAULT_KEY_MANAGER,
                APIConstants.KeyManager.DEFAULT_KEY_MANAGER_DESCRIPTION,
                APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE, TENANT_DOMAIN);
        //Insert applications
        APPLICATION1_ID = insertApplication(APPLICATION1_NAME, TIER, STATUS, APP_OWNER, APP1_UUID, TOKEN_TYPE);
        APPLICATION2_ID = insertApplication(APPLICATION2_NAME, TIER, STATUS, APP_OWNER, APP2_UUID, TOKEN_TYPE);
        //Insert application key mapping entries
        //Application 1 where the db entry has key manager name
        insertApplicationKeyMappingEntry(APP1_UUID, APPLICATION1_ID, CONSUMER_KEY1, KEY_TYPE, STATE,
                APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
        //Application 2 where the db entry has key manager uuid
        insertApplicationKeyMappingEntry(APP2_UUID, APPLICATION2_ID, CONSUMER_KEY2, KEY_TYPE, STATE, KM_UUID);
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
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
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

    @Test
    public void testGetApplicationKeyMappingWithKmName() {
        ApplicationKeyMapping keyMapping = subscriptionValidationDAO
                .getApplicationKeyMapping(CONSUMER_KEY1, APIConstants.KeyManager.DEFAULT_KEY_MANAGER, TENANT_DOMAIN);
        assertNotNull(keyMapping);
        assertEquals(APPLICATION1_ID, keyMapping.getApplicationId());
        assertEquals(CONSUMER_KEY1, keyMapping.getConsumerKey());
    }

    @Test
    public void testGetApplicationKeyMappingWithKmUUID() {
        ApplicationKeyMapping keyMapping = subscriptionValidationDAO
                .getApplicationKeyMapping(CONSUMER_KEY2, APIConstants.KeyManager.DEFAULT_KEY_MANAGER, TENANT_DOMAIN);
        assertNotNull(keyMapping);
        assertEquals(APPLICATION2_ID, keyMapping.getApplicationId());
        assertEquals(CONSUMER_KEY2, keyMapping.getConsumerKey());
    }

    private int insertApplication(String name, String tier, String status, String owner, String uuid, String tokenType)
            throws SQLException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        int applicationId = 0;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = "INSERT INTO AM_APPLICATION (NAME, APPLICATION_TIER, "
                    + " APPLICATION_STATUS, CREATED_BY, CREATED_TIME, UPDATED_TIME, UUID,"
                    + " TOKEN_TYPE) VALUES (?,?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, name);
            ps.setString(2, tier);
            ps.setString(3, status);
            ps.setString(4, owner);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(5, timestamp);
            ps.setTimestamp(6, timestamp);
            ps.setString(7, uuid);
            ps.setString(8, tokenType);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            while (rs.next()) {
                applicationId = Integer.parseInt(rs.getString(1));
            }
            conn.commit();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return applicationId;
    }

    private void insertApplicationKeyMappingEntry(String uuid, int appId, String consumerKey, String keyType,
            String state, String keyManager) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = "INSERT INTO AM_APPLICATION_KEY_MAPPING (UUID, APPLICATION_ID, CONSUMER_KEY, KEY_TYPE, "
                    + "STATE, KEY_MANAGER) VALUES (?,?,?,?,?,?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, uuid);
            ps.setInt(2, appId);
            ps.setString(3, consumerKey);
            ps.setString(4, keyType);
            ps.setString(5, state);
            ps.setString(6, keyManager);
            ps.executeUpdate();
            conn.commit();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    private void insertKeyManagerEntry(String uuid, String name, String description, String type, String tenantDomain)
            throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = "INSERT INTO AM_KEY_MANAGER (UUID,NAME,DESCRIPTION,TYPE,TENANT_DOMAIN) VALUES (?,?,?,?,?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, uuid);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setString(4, type);
            ps.setString(5, tenantDomain);
            ps.executeUpdate();
            conn.commit();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    private void deleteKeyManager(String uuid, String tenantDomain) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String query = "DELETE FROM AM_KEY_MANAGER WHERE UUID = ? AND TENANT_DOMAIN = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, uuid);
            ps.setString(2, tenantDomain);
            ps.executeUpdate();
            conn.commit();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    private void deleteApplicationKeyMappings(int applicationId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String query = "DELETE FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, applicationId);
            ps.executeUpdate();
            conn.commit();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    private void deleteApplication(int applicationId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String query = "DELETE FROM AM_APPLICATION WHERE APPLICATION_ID = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, applicationId);
            ps.executeUpdate();
            conn.commit();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    @After
    public void deleteArtifactsFromDB() throws Exception {
        //Delete application key mappings
        deleteApplicationKeyMappings(APPLICATION1_ID);
        deleteApplicationKeyMappings(APPLICATION2_ID);
        //Delete applications
        deleteApplication(APPLICATION1_ID);
        deleteApplication(APPLICATION2_ID);
        //Delete the key manager entry from the database
        deleteKeyManager(KM_UUID, TENANT_DOMAIN);
    }
}
