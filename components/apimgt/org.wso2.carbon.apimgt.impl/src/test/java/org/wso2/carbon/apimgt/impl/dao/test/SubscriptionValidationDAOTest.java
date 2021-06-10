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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.subscription.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
    public static ApiMgtDAO apiMgtDAO;
    private static String KM_UUID = UUID.randomUUID().toString();
    private static String APP1_UUID = UUID.randomUUID().toString();
    private static String APP2_UUID = UUID.randomUUID().toString();
    private static int APPLICATION1_ID;
    private static int APPLICATION2_ID;
    private static String CONSUMER_KEY1 = "xbpK4gvIey2EkyVm5fDKSfKQ59Qa";
    private static String CONSUMER_KEY2 = "gYBKWtmq323neB4kTIbtY8324hca";
    private static String TENANT_DOMAIN = "carbon.super";
    private static String KEY_TYPE = "PRODUCTION";
    private static String APPLICATION1_NAME = "Application1";
    private static String APPLICATION2_NAME = "Application2";
    private static String APP_OWNER = "subscriptionValidationUser";

    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance()
                .setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl(config));
        APIMgtDBUtil.initialize();
        apiMgtDAO = ApiMgtDAO.getInstance();
        subscriptionValidationDAO = new SubscriptionValidationDAO();
        IdentityTenantUtil.setRealmService(new TestRealmService());
        String identityConfigPath = System.getProperty("IdentityConfigurationPath");
        IdentityConfigParser.getInstance(identityConfigPath);
        OAuthServerConfiguration oAuthServerConfiguration = OAuthServerConfiguration.getInstance();
        ServiceReferenceHolder.getInstance().setOauthServerConfiguration(oAuthServerConfiguration);
        //Add subscriber
        Subscriber subscriber = new Subscriber(APP_OWNER);
        subscriber.setTenantId(-1234);
        subscriber.setSubscribedDate(new Date());
        apiMgtDAO.addSubscriber(subscriber, null);
        //Add Application1
        Application app1 = new Application(APPLICATION1_NAME, subscriber);
        app1.setUUID(APP1_UUID);
        APPLICATION1_ID = apiMgtDAO.addApplication(app1, APP_OWNER);
        //Add Application2
        Application app2 = new Application(APPLICATION2_NAME, subscriber);
        app2.setUUID(APP2_UUID);
        APPLICATION2_ID = apiMgtDAO.addApplication(app2, APP_OWNER);
        //Add application key mapping for Application1
        apiMgtDAO.createApplicationKeyTypeMappingForManualClients(KEY_TYPE, APPLICATION1_ID, CONSUMER_KEY1,
                APIConstants.KeyManager.DEFAULT_KEY_MANAGER, APP1_UUID);
        //Add application key mapping for Application2
        apiMgtDAO.createApplicationKeyTypeMappingForManualClients(KEY_TYPE, APPLICATION2_ID, CONSUMER_KEY2,
                KM_UUID, APP2_UUID);
        //Add Key Manager
        KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
        keyManagerConfigurationDTO.setName(APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
        keyManagerConfigurationDTO.setUuid(KM_UUID);
        keyManagerConfigurationDTO.setDescription(APIConstants.KeyManager.DEFAULT_KEY_MANAGER_DESCRIPTION);
        keyManagerConfigurationDTO.setTenantDomain(TENANT_DOMAIN);
        keyManagerConfigurationDTO.setType(APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE);
        apiMgtDAO.addKeyManagerConfiguration(keyManagerConfigurationDTO);
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

    private void deleteSubscriber(String subscriber) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String query = "DELETE FROM AM_SUBSCRIBER WHERE USER_ID = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, subscriber);
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
        //Delete subscriber
        deleteSubscriber(APP_OWNER);
        //Delete the key manager entry from the database
        deleteKeyManager(KM_UUID, TENANT_DOMAIN);
    }
}
