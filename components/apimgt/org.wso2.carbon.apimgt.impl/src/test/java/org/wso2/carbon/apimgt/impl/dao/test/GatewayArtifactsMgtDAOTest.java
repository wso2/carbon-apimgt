package org.wso2.carbon.apimgt.impl.dao.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.Notifier;
import org.wso2.carbon.apimgt.impl.notifier.SubscriptionsNotifier;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.GatewayArtifactsMgtDBUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {KeyManagerHolder.class})
public class GatewayArtifactsMgtDAOTest {
    public static GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO;
    private API api;
    private String apiName;
    private String version;
    private APIIdentifier apiId;
    private String label;
    private String apiUUID;
    private String gatewayruntimeArtifacts;


    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl(config));
        List<Notifier> notifierList = new ArrayList<>();
        SubscriptionsNotifier subscriptionsNotifier = new SubscriptionsNotifier();
        notifierList.add(subscriptionsNotifier);
        ServiceReferenceHolder.getInstance().getNotifiersMap().put(subscriptionsNotifier.getType(), notifierList);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties().
                setArtifactSynchronizerDataSource("java:/comp/env/jdbc/WSO2AM_DB");
        GatewayArtifactsMgtDBUtil.initialize();
        gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();
        IdentityTenantUtil.setRealmService(new TestRealmService());
        String identityConfigPath = System.getProperty("IdentityConfigurationPath");
        IdentityConfigParser.getInstance(identityConfigPath);
        OAuthServerConfiguration oAuthServerConfiguration = OAuthServerConfiguration.getInstance();
        ServiceReferenceHolder.getInstance().setOauthServerConfiguration(oAuthServerConfiguration);

        apiName = "testAddGatewayPublishedAPIDetails";
        version = "1.0.0";
        apiId = new APIIdentifier("testGateway", apiName, version);
        api = new API(apiId);
        label = "Production and Sandbox";
        UUID uuid = UUID.randomUUID();
        apiUUID = uuid.toString();

//        gatewayruntimeArtifacts = {"name":"PizzaShackAPI","version":"1.0.0","provider":"admin","tenantDomain":"carbon.super","apiId":"a7f58a46-2fa4-4d4e-b244-57dd6c03a5ae","override":true,"sequencesToBeRemove":["admin--PizzaShackAPI:v1.0.0--In","admin--PizzaShackAPI:v1.0.0--Fault","admin--PizzaShackAPI:v1.0.0--Out"],"localEntriesToBeRemove":["a7f58a46-2fa4-4d4e-b244-57dd6c03a5ae"],"endpointEntriesToBeRemove":["PizzaShackAPI--v1.0.0_APIsandboxEndpoint","PizzaShackAPI--v1.0.0_APIproductionEndpoint"]}
//        byte[] gatewayRuntimeArtifactsAsBytes =  gatewayruntimeArtifacts.getBytes();
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gatewayRuntimeArtifactsAsBytes);
//
//        {"endpointEntriesToBeRemove":["PizzaShackAPI--v1.0.0_APIsandboxEndpoint","PizzaShackAPI--v1.0.0_APIproductionEndpoint"],"provider":"admin","sequencesToBeRemove":["admin--PizzaShackAPI:v1.0.0--In","admin--PizzaShackAPI:v1.0.0--Fault","admin--PizzaShackAPI:v1.0.0--Out"],"name":"PizzaShackAPI","override":true,"localEntriesToBeRemove":["a7f58a46-2fa4-4d4e-b244-57dd6c03a5ae"],"version":"1.0.0","tenantDomain":"carbon.super","apiId":"a7f58a46-2fa4-4d4e-b244-57dd6c03a5ae"}

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

    @Test
    public void testAddGatewayPublishedAPIDetails() throws APIManagementException {
        boolean result = gatewayArtifactsMgtDAO.addGatewayPublishedAPIDetails(apiUUID, apiName, version,
                String.valueOf(MultitenantConstants.SUPER_TENANT_ID));
        Assert.assertTrue(result);
    }

    @Test
    public void testAddGatewayPublishedAPIArtifacts() throws APIManagementException {
        ByteArrayInputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());
        boolean result = gatewayArtifactsMgtDAO.addGatewayPublishedAPIArtifacts(apiUUID,
                label , anyInputStream, 1,
                APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH, SQLConstants.ADD_GW_API_ARTIFACT);
        Assert.assertTrue(result);
    }

    @Test
    public void testGetGatewayPublishedAPIArtifacts() throws APIManagementException {
        String gatewayPublishedAPIArtifacts =
                gatewayArtifactsMgtDAO.getGatewayPublishedAPIArtifacts(apiUUID, label,
                        APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
        Assert.assertNotNull(gatewayPublishedAPIArtifacts);
    }

    @Test
    public void testGetAllGatewayPublishedAPIArtifacts() throws APIManagementException {
        List<String> gatewayRuntimeArtifactsArray = gatewayArtifactsMgtDAO.getAllGatewayPublishedAPIArtifacts(label);
        Assert.assertTrue(gatewayRuntimeArtifactsArray.size() > 1);
    }

    @Test
    public void testIsAPIPublishedInAnyGateway() throws APIManagementException {
        boolean isApiPublished = gatewayArtifactsMgtDAO.isAPIPublishedInAnyGateway(apiUUID);
        Assert.assertTrue(isApiPublished);
    }

    @Test
    public void testIsAPIDetailsExists() throws APIManagementException {
        boolean isApiPublished = gatewayArtifactsMgtDAO.isAPIDetailsExists(apiUUID);
        Assert.assertTrue(isApiPublished);
    }

    @Test
    public void testIsAPIArtifactExists() throws APIManagementException {
        boolean isApiPublished = gatewayArtifactsMgtDAO.isAPIArtifactExists(apiUUID, label);
        Assert.assertTrue(isApiPublished);
    }

    @Test
    public void testGetGatewayAPIId() throws APIManagementException {
        String apiUUID =
                gatewayArtifactsMgtDAO.getGatewayPublishedAPIArtifacts(apiName, version,
                        String.valueOf(MultitenantConstants.SUPER_TENANT_ID));
        Assert.assertNotNull(apiUUID);
    }
}
