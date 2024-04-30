/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Notification;
import org.wso2.carbon.apimgt.api.model.NotificationList;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.portalNotifications.*;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.base.MultitenantConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class PortalNotificationDAOTest {
    public static PortalNotificationDAO portalNotificationDAO;

    private static final String organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance()
                .setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl(config));
        APIMgtDBUtil.initialize();
        portalNotificationDAO = PortalNotificationDAO.getInstance();
    }

    private static void initializeDatabase(String configFilePath)
            throws IOException, XMLStreamException, NamingException {

        InputStream in;
        in = FileUtils.openInputStream(new File(configFilePath));
        StAXOMBuilder builder = new StAXOMBuilder(in);
        String dataSource = builder.getDocumentElement().getFirstChildWithName(new QName("DataSourceName")).getText();
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
    }

    @Test
    public void testAddNotificationSuccess() throws APIManagementException {
        PortalNotificationMetaData metaData = getPortalNotificationMetaData();
        PortalNotificationDTO portalNotificationDTO = getPortalNotificationDTO(metaData);

        boolean result = portalNotificationDAO.addNotification(portalNotificationDTO);
        Assert.assertTrue(result);
    }

    @NotNull
    private static PortalNotificationDTO getPortalNotificationDTO(PortalNotificationMetaData metaData) {
        PortalNotificationEndUserDTO endUser1 = new PortalNotificationEndUserDTO();
        endUser1.setDestinationUser("Kevin");
        endUser1.setOrganization(organization);
        endUser1.setPortalToDisplay("publisher");
        List<PortalNotificationEndUserDTO> endUsersList = new ArrayList<>();
        endUsersList.add(endUser1);

        PortalNotificationDTO portalNotificationDTO = new PortalNotificationDTO();
        portalNotificationDTO.setNotificationType(PortalNotificationType.API_STATE_CHANGE);
        portalNotificationDTO.setCreatedTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        portalNotificationDTO.setNotificationMetadata(metaData);
        portalNotificationDTO.setEndUsers(endUsersList);
        return portalNotificationDTO;
    }

    @NotNull
    private static PortalNotificationMetaData getPortalNotificationMetaData() {
        PortalNotificationMetaData metaData = new PortalNotificationMetaData();
        metaData.setApi("Sample_API");
        metaData.setApiVersion("1.0");
        metaData.setApiContext("/sample");
        metaData.setApplicationName(null);
        metaData.setRequestedTier(null);
        metaData.setRevisionId(null);
        metaData.setComment("Sample comment");
        metaData.setAction("Publish");
        return metaData;
    }

    @Test
    public void testGetNotificationsSuccess() throws APIManagementException {
        NotificationList notificationList = portalNotificationDAO.getNotifications("helani", organization,
                "publisher", "desc", 0, 10);
        Assert.assertNotNull(notificationList);
    }

    @Test
    public void testMarkNotificationAsReadByIdSuccess() throws Exception {
        Notification notification = portalNotificationDAO.markNotificationAsReadById("helani", organization,
                "1e2736ab-7882-4184-a1ba-6d3c07271b69", "publisher");
        assertEquals(true, notification.getIsRead());
    }

    @Test
    public void testMarkAllNotificationsAsReadSuccess() throws Exception {
        NotificationList notificationList = portalNotificationDAO.markAllNotificationsAsRead("helani",
                organization, "publisher");
        assertEquals(true, notificationList.getList().get(0).getIsRead());
    }

    @Test
    public void testDeleteNotificationByIdSuccess() throws Exception {
        boolean result = portalNotificationDAO.deleteNotificationById("helani", organization,
                "7dc674f1-2b4c-4826-8e95-fa9b2628b816", "publisher");
        Assert.assertTrue(result);
    }

    @Test
    public void testDeleteAllNotificationsSuccess() throws Exception {
        boolean result = portalNotificationDAO.deleteAllNotifications("Kevin", organization,
                "publisher");
        Assert.assertTrue(result);
    }

    @Test
    public void testGetAPIUUIDUsingNameContextVersionSuccess() throws Exception {
        String apiUUID = portalNotificationDAO.getAPIUUIDUsingNameContextVersion("testAPI1",
                "/sample/api", "1.0.0", organization);
        assertEquals("821b9824-eeca-4173-9f56-3dc6d46bd6eb", apiUUID);
    }

    @Test
    public void testGetUnreadNotificationCountSuccess() throws Exception {
        Connection connection = APIMgtDBUtil.getConnection();
        int unreadCount = portalNotificationDAO.getUnreadNotificationCount("Nisha", organization,
                "devportal", connection);
        assertEquals(1, unreadCount);

    }

    @Test
    public void testAddNotificationFailure() {
        PortalNotificationMetaData metaData = getPortalNotificationMetaData();
        PortalNotificationEndUserDTO endUser1 = new PortalNotificationEndUserDTO();
        endUser1.setDestinationUser("Kevin");
        endUser1.setOrganization(organization);
        List<PortalNotificationEndUserDTO> endUsersList = new ArrayList<>();
        endUsersList.add(endUser1);

        PortalNotificationDTO portalNotificationDTO = new PortalNotificationDTO();
        portalNotificationDTO.setNotificationType(PortalNotificationType.API_STATE_CHANGE);
        portalNotificationDTO.setCreatedTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        portalNotificationDTO.setNotificationMetadata(metaData);
        portalNotificationDTO.setEndUsers(endUsersList);

        Exception exception = assertThrows(APIManagementException.class, () -> {
            portalNotificationDAO.addNotification(portalNotificationDTO);
        });

        String expectedMessage = "Error while adding notification";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testGetNotificationsFailure() throws APIManagementException {
        NotificationList notificationList = portalNotificationDAO.getNotifications("Rush", organization,
                "publisher", "desc", 0, 10);
        assertEquals(0, notificationList.getList().size());
    }

    @Test
    public void testMarkNotificationAsReadByIdFailure() throws Exception {
        Notification notification = portalNotificationDAO.markNotificationAsReadById("Rush", organization,
                "1e2736ab-7882-4184-a1ba-6d3c07271b69", "publisher");
        assertNull(notification);
    }

    @Test
    public void testMarkAllNotificationsAsReadFailure() throws Exception {
        NotificationList notificationList = portalNotificationDAO.markAllNotificationsAsRead("Rush",
                organization, "publisher");
        assertNull(notificationList);
    }

    @Test
    public void testDeleteNotificationByIdFailure() throws Exception {
        boolean result = portalNotificationDAO.deleteNotificationById("Rush", organization,
                "7dc674f1-2b4c-4826-8e95-fa9b2628b816", "publisher");
        Assert.assertFalse(result);
    }

    @Test
    public void testDeleteAllNotificationsFailure() throws Exception {
        boolean result = portalNotificationDAO.deleteAllNotifications("Rush", organization,
                "publisher");
        Assert.assertFalse(result);
    }

    @Test
    public void testGetAPIUUIDUsingNameContextVersionFailure() throws Exception {
        String apiUUID = portalNotificationDAO.getAPIUUIDUsingNameContextVersion("MyApi", "/api",
                "1.0.0", organization);
        assertNull(apiUUID);
    }

    @Test
    public void testGetUnreadNotificationCountFailure() throws Exception {
        Connection connection = APIMgtDBUtil.getConnection();
        int unreadCount = portalNotificationDAO.getUnreadNotificationCount("Rush", organization,
                "publisher", connection);
        assertEquals(0, unreadCount);
    }

}
