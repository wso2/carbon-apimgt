package org.wso2.carbon.apimgt.impl.dao.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.GatewayArtifactsMgtDBUtil;
import org.wso2.carbon.base.MultitenantConstants;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RunWith(PowerMockRunner.class)
public class GatewayArtifactsMgtDAOTest {
    public static GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO;
    String apiUUID = "1236233";
    String apiName = "testAddGatewayPublishedAPIDetails";
    String version = "1.0.0";
    String label = "Production and Sandbox";
    ByteArrayInputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());
    private static final long WAIT_TIME = 3000;

    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (config));
        ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties().
                setArtifactSynchronizerDataSource("java:/comp/env/jdbc/WSO2AM_DB");
        GatewayArtifactsMgtDBUtil.initialize();
        gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();
    }


    private static void initializeDatabase(String configFilePath)
            throws XMLStreamException, IOException, NamingException {

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
            throw new XMLStreamException("Unexpected error in processing XML");
        } catch (IOException e) {
            throw new IOException("Error in processing the APIManagerDBConfiguration file ");
        } catch (NamingException e) {
            throw new NamingException("Error in database Username and Password");
        }
    }

    @Test
    public void testAddValidateGatewayPublishedAPIDetails() throws APIManagementException {
        boolean result = gatewayArtifactsMgtDAO.addGatewayPublishedAPIDetails(apiUUID, apiName, version,
                String.valueOf(MultitenantConstants.SUPER_TENANT_ID));
        Assert.assertTrue(result);
    }

    @Test
    public void testAddValidateGatewayPublishedAPIArtifacts() throws APIManagementException {
        boolean result = gatewayArtifactsMgtDAO.addGatewayPublishedAPIArtifacts(apiUUID, label , anyInputStream,
                1, APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH,
                SQLConstants.ADD_GW_API_ARTIFACT);
        Assert.assertTrue(result);
    }

    @Test
    public void testGetGatewayPublishedAPIArtifacts() throws APIManagementException, InterruptedException {
        Thread.sleep(WAIT_TIME);
        String gatewayPublishedAPIArtifacts = gatewayArtifactsMgtDAO.getGatewayPublishedAPIArtifacts(apiUUID, label,
                        APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
        Assert.assertNotNull(gatewayPublishedAPIArtifacts);
    }

    @Test
    public void testGetAllGatewayPublishedAPIArtifacts() throws APIManagementException, InterruptedException {
        Thread.sleep(WAIT_TIME);
        List<String> gatewayRuntimeArtifactsArray = gatewayArtifactsMgtDAO.getAllGatewayPublishedAPIArtifacts(label);
        Assert.assertTrue(gatewayRuntimeArtifactsArray.size() > 0);
    }

    @Test
    public void testIsAPIPublishedInAnyGateway() throws APIManagementException, InterruptedException {
        Thread.sleep(WAIT_TIME);
        boolean isApiPublished = gatewayArtifactsMgtDAO.isAPIPublishedInAnyGateway(apiUUID);
        Assert.assertTrue(isApiPublished);
    }

    @Test
    public void testIsAPIArtifactExists() throws APIManagementException, InterruptedException {
        Thread.sleep(WAIT_TIME);
        boolean isApiArtifactsExists = gatewayArtifactsMgtDAO.isAPIArtifactExists(apiUUID, label);
        Assert.assertTrue(isApiArtifactsExists);
    }

    @Test
    public void testIsAPIDetailExists() throws APIManagementException, InterruptedException {
        Thread.sleep(WAIT_TIME);
        boolean isApiDetailsExists = gatewayArtifactsMgtDAO.isAPIDetailsExists(apiUUID);
        Assert.assertTrue(isApiDetailsExists);
    }
}
