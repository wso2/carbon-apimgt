/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dao;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.TestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.MicroGatewayAPIUsageConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.constants.Constants;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dto.UploadedFileInfoDTO;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * UploadedUsageFileInfoDAOTest Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MicroGatewayAPIUsageConstants.class})
public class UploadedUsageFileInfoDAOTest {
    private static final Log log = LogFactory.getLog(UploadedUsageFileInfoDAOTest.class);
    private final String tenantDomain = "ccc2222";
    private final String fileName = "api-usage-data.dat";

    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
        String apimDBConfigPath = System.getProperty(Constants.CARBON_HOME) + File.separator + "amConfig.xml";
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(apimDBConfigPath);
        config.load(apimDBConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (config));
        APIMgtDBUtil.initialize();
        Connection connection = APIMgtDBUtil.getConnection();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/h2/db.sql'");
    }

    @Test
    public void persistFileUpload() throws Exception {
        Connection connection = APIMgtDBUtil.getConnection();
        String deleteFilesQuery = "DELETE FROM AM_USAGE_UPLOADED_FILES";
        Statement st = connection.createStatement();
        st.executeUpdate(deleteFilesQuery);

        PowerMockito.mockStatic(MicroGatewayAPIUsageConstants.class);
        UploadedFileInfoDTO uploadedFileInfoDTO = new UploadedFileInfoDTO(tenantDomain,
                "api-usage-data.dat.1517296920006.gz", 1213232);
        InputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());
        UploadedUsageFileInfoDAO.persistFileUpload(uploadedFileInfoDTO, anyInputStream);
        String getUploadedFilesQuery = "SELECT * FROM AM_USAGE_UPLOADED_FILES WHERE TENANT_DOMAIN = (?)";
        PreparedStatement getResultsStmt = connection.prepareStatement(getUploadedFilesQuery);
        getResultsStmt.setString(1, tenantDomain);
        ResultSet resultSet = getResultsStmt.executeQuery();
        if (resultSet.next()) {
            Assert.assertEquals(resultSet.getString("TENANT_DOMAIN"), tenantDomain);
        }
        Statement st2 = connection.createStatement();
        st2.executeUpdate(deleteFilesQuery);
        connection.close();
    }

    @Test
    public void getNextFilesToProcess() throws Exception {
        Connection connection = APIMgtDBUtil.getConnection();
        String insertFilesQuery = "INSERT INTO AM_USAGE_UPLOADED_FILES " +
                "(TENANT_DOMAIN,FILE_NAME,FILE_TIMESTAMP) VALUES(?,?,?);";
        PreparedStatement statement = connection.prepareStatement(insertFilesQuery);
        statement.setString(1, tenantDomain);
        statement.setString(2, "api-usage-data.dat");
        statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        statement.executeUpdate();
        connection.commit();

        List<UploadedFileInfoDTO> uploadedFileInfoList = UploadedUsageFileInfoDAO.getNextFilesToProcess(3);
        for (UploadedFileInfoDTO fileInfoDTO: uploadedFileInfoList) {
            Assert.assertEquals(fileInfoDTO.getTenantDomain(), tenantDomain);
        }
        String deleteFilesQuery = "DELETE FROM AM_USAGE_UPLOADED_FILES";
        Statement st = connection.createStatement();
        st.executeUpdate(deleteFilesQuery);
        connection.close();
    }

    @Test
    public void getFileContent() throws Exception {
        Connection connection = APIMgtDBUtil.getConnection();
        InputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());
        String insertFilesQuery = "INSERT INTO AM_USAGE_UPLOADED_FILES " +
                "(TENANT_DOMAIN,FILE_NAME,FILE_TIMESTAMP,FILE_CONTENT) VALUES(?,?,?,?);";
        PreparedStatement statement = connection.prepareStatement(insertFilesQuery);
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        statement.setString(1, tenantDomain);
        statement.setString(2, fileName);
        statement.setTimestamp(3, timeStamp);
        statement.setBinaryStream(4 , anyInputStream);
        statement.executeUpdate();
        connection.commit();

        UploadedFileInfoDTO uploadedFileInfoDTO = new UploadedFileInfoDTO(tenantDomain, fileName, timeStamp.getTime());
        InputStream returnedInputStream = UploadedUsageFileInfoDAO.getFileContent(uploadedFileInfoDTO);
        Assert.assertNotNull(returnedInputStream);

        String deleteFilesQuery = "DELETE FROM AM_USAGE_UPLOADED_FILES";
        Statement st = connection.createStatement();
        st.executeUpdate(deleteFilesQuery);
        connection.close();
    }


    @Test
    public void updateCompletion() throws Exception {
        Connection connection = APIMgtDBUtil.getConnection();
        String insertQuery = "INSERT INTO AM_USAGE_UPLOADED_FILES " +
                "(TENANT_DOMAIN,FILE_NAME,FILE_TIMESTAMP) VALUES(?,?,?);";
        PreparedStatement st = connection.prepareStatement(insertQuery);
        st.setString(1, "ccc2222");
        st.setString(2, "api-usage-data.dat.1517296920006.gz");
        st.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        st.executeUpdate();

        PowerMockito.mockStatic(MicroGatewayAPIUsageConstants.class);
        UploadedFileInfoDTO uploadedFileInfoDTO = new UploadedFileInfoDTO("ccc2222",
                "api-usage-data.dat.1517296920006.gz", 1213232);
        UploadedUsageFileInfoDAO.updateCompletion(uploadedFileInfoDTO);

        String getUploadedFilesQuery = "SELECT * FROM AM_USAGE_UPLOADED_FILES WHERE TENANT_DOMAIN = (?)";
        PreparedStatement getResultsStmt = connection.prepareStatement(getUploadedFilesQuery);
        getResultsStmt.setString(1, "ccc2222");
        ResultSet resultSet = getResultsStmt.executeQuery();
        if (resultSet.next()) {
            Assert.assertEquals(resultSet.getString("FILE_PROCESSED"), "2");
        }

        String deleteFilesQuery = "DELETE FROM AM_USAGE_UPLOADED_FILES";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteFilesQuery);
        connection.close();
    }

    @Test
    public void deleteProcessedOldFiles() throws Exception {
        Connection connection = APIMgtDBUtil.getConnection();
        String insertQuery = "INSERT INTO AM_USAGE_UPLOADED_FILES " +
                "(TENANT_DOMAIN,FILE_NAME,FILE_TIMESTAMP,FILE_PROCESSED) VALUES(?,?,?,?);";
        PreparedStatement st = connection.prepareStatement(insertQuery);
        st.setString(1, tenantDomain);
        st.setString(2, "api-usage-data.dat.1517296920006.gz");
        st.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        st.setInt(4, 1);
        st.executeUpdate();
        PowerMockito.mockStatic(MicroGatewayAPIUsageConstants.class);
        UploadedUsageFileInfoDAO.deleteProcessedOldFiles(new Timestamp(System.currentTimeMillis()));

        String deleteFilesQuery = "DELETE FROM AM_USAGE_UPLOADED_FILES";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteFilesQuery);
        connection.close();
    }

    private static void initializeDatabase(String configFilePath) {
        InputStream in;
        try {
            in = FileUtils.openInputStream(new File(configFilePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            String dataSource = builder.getDocumentElement().getFirstChildWithName(new QName("DataSourceName")).
                    getText();
            OMElement databaseElement = builder.getDocumentElement()
                    .getFirstChildWithName(new QName("Database"));
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
        } catch (XMLStreamException | IOException | NamingException e) {
            log.error(e);
        }
    }

}
