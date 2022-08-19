package org.wso2.carbon.apimgt.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.SystemConfigurationsDAO;
import org.wso2.carbon.apimgt.impl.dto.ExternalAPIStoresConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtilTest;
import java.io.File;
import java.io.IOException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIMConfigServiceImpl.class, SystemConfigurationsDAO.class})
public class APIMConfigServiceImplTestCase {

    @Test
    public void testGetSelfSighupConfigWhenSelfSignUpIsEnabled() throws APIManagementException, IOException {
        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(SystemConfigurationsDAO.class);
        SystemConfigurationsDAO systemConfigurationsDAO = Mockito.mock(SystemConfigurationsDAO.class);
        PowerMockito.when(SystemConfigurationsDAO.getInstance()).thenReturn(systemConfigurationsDAO);

        File siteConfFile = new File(
                Thread.currentThread().getContextClassLoader().getResource("tenant-conf.json").getFile());
        String tenantConfValue = FileUtils.readFileToString(siteConfFile);
        JsonObject tenantConfig = (JsonObject) new JsonParser().parse(tenantConfValue);
        Mockito.when(systemConfigurationsDAO.getSystemConfig(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(String.valueOf(tenantConfig));

        APIMConfigServiceImpl configServiceImpl = new APIMConfigServiceImpl();
        UserRegistrationConfigDTO userRegistrationConfigDTO = configServiceImpl.getSelfSighupConfig("carbon.super");
        Assert.assertEquals(1, userRegistrationConfigDTO.getRoles().size());
        Assert.assertEquals("Internal/subscriber", userRegistrationConfigDTO.getRoles().get(0));
    }

    @Test
    public void testGetSelfSighupConfigWhenManySelfSignUpRolesIsPresent() throws APIManagementException, IOException {
        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(SystemConfigurationsDAO.class);
        SystemConfigurationsDAO systemConfigurationsDAO = Mockito.mock(SystemConfigurationsDAO.class);
        PowerMockito.when(SystemConfigurationsDAO.getInstance()).thenReturn(systemConfigurationsDAO);

        File siteConfFile = new File(
                Thread.currentThread().getContextClassLoader().getResource("tenant-conf.json").getFile());
        String tenantConfValue = FileUtils.readFileToString(siteConfFile);
        JsonObject tenantConfig = (JsonObject) new JsonParser().parse(tenantConfValue);
        JsonObject SelfSignUpJsonObject = (JsonObject) tenantConfig.get("SelfSignUp");
        JsonArray SignUpRolesJsonArray = (JsonArray) SelfSignUpJsonObject.get("SignUpRoles");
        SignUpRolesJsonArray.add("testRole");
        Mockito.when(systemConfigurationsDAO.getSystemConfig(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(String.valueOf(tenantConfig));

        APIMConfigServiceImpl configServiceImpl = new APIMConfigServiceImpl();
        UserRegistrationConfigDTO userRegistrationConfigDTO = configServiceImpl.getSelfSighupConfig("coltrain.com");
        Assert.assertEquals(2, userRegistrationConfigDTO.getRoles().size());
        Assert.assertEquals("Internal/subscriber", userRegistrationConfigDTO.getRoles().get(0));
        Assert.assertEquals("testRole", userRegistrationConfigDTO.getRoles().get(1));
    }

    @Test
    public void testGetSelfSighupConfigWhenSelfSignUpIsDisabled() throws APIManagementException, IOException {
        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(SystemConfigurationsDAO.class);
        SystemConfigurationsDAO systemConfigurationsDAO = Mockito.mock(SystemConfigurationsDAO.class);
        PowerMockito.when(SystemConfigurationsDAO.getInstance()).thenReturn(systemConfigurationsDAO);

        File siteConfFile = new File(
                Thread.currentThread().getContextClassLoader().getResource("tenant-conf.json").getFile());
        String tenantConfValue = FileUtils.readFileToString(siteConfFile);
        JsonObject tenantConfig = (JsonObject) new JsonParser().parse(tenantConfValue);
        tenantConfig.remove("SelfSignUp");
        Mockito.when(systemConfigurationsDAO.getSystemConfig(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(String.valueOf(tenantConfig));

        APIMConfigServiceImpl configServiceImpl = new APIMConfigServiceImpl();
        UserRegistrationConfigDTO userRegistrationConfigDTO = configServiceImpl.getSelfSighupConfig("coltrain.com");
        Assert.assertNull(userRegistrationConfigDTO);
    }

    @Test
    public void testGetExternalStoreConfigWhenExternalStoresAreDisabled() throws APIManagementException, IOException {

        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(SystemConfigurationsDAO.class);
        SystemConfigurationsDAO systemConfigurationsDAO = Mockito.mock(SystemConfigurationsDAO.class);
        PowerMockito.when(SystemConfigurationsDAO.getInstance()).thenReturn(systemConfigurationsDAO);

        File siteConfFile = new File(
                Thread.currentThread().getContextClassLoader().getResource("tenant-conf.json").getFile());
        String tenantConfValue = FileUtils.readFileToString(siteConfFile);
        JsonObject tenantConfig = (JsonObject) new JsonParser().parse(tenantConfValue);
        Mockito.when(systemConfigurationsDAO.getSystemConfig(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(String.valueOf(tenantConfig));

        APIMConfigServiceImpl configServiceImpl = new APIMConfigServiceImpl();
        ExternalAPIStoresConfigDTO externalAPIStoresConfigDTO = configServiceImpl.getExternalStoreConfig(
                "carbon.super");
        Assert.assertNull(externalAPIStoresConfigDTO);
    }

    @Test
    public void testGetExternalStoreConfigWhenExternalStoresAreEnabled() throws APIManagementException, IOException {

        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(SystemConfigurationsDAO.class);
        SystemConfigurationsDAO systemConfigurationsDAO = Mockito.mock(SystemConfigurationsDAO.class);
        PowerMockito.when(SystemConfigurationsDAO.getInstance()).thenReturn(systemConfigurationsDAO);

        JsonObject externalAPIStore1 = new JsonObject();
        externalAPIStore1.addProperty("id", "DeveloperPortal1");
        externalAPIStore1.addProperty("type", "wso2");
        externalAPIStore1.addProperty("className", "org.wso2.carbon.apimgt.impl.publishers.WSO2APIPublisher");
        externalAPIStore1.addProperty("DisplayName", "DeveloperPortal1");
        externalAPIStore1.addProperty("Endpoint", "http://localhost:9444/devportal1");
        externalAPIStore1.addProperty("Username", "admin");
        externalAPIStore1.addProperty("Password", "admin");

        JsonObject externalAPIStore2 = new JsonObject();
        externalAPIStore2.addProperty("id", "DeveloperPortal2");
        externalAPIStore2.addProperty("type", "wso2");
        externalAPIStore2.addProperty("className", "org.wso2.carbon.apimgt.impl.publishers.WSO2APIPublisher");
        externalAPIStore2.addProperty("DisplayName", "DeveloperPortal2");
        externalAPIStore2.addProperty("Endpoint", "http://localhost:9444/devportal2");
        externalAPIStore2.addProperty("Username", "admin");
        externalAPIStore2.addProperty("Password", "admin");

        JsonArray externalAPIStoreJsonArray = new JsonArray();
        externalAPIStoreJsonArray.add(externalAPIStore1);
        externalAPIStoreJsonArray.add(externalAPIStore2);

        JsonObject externalAPIStores = new JsonObject();
        externalAPIStores.addProperty("StoreURL", "http://localhost:9443/devportal");
        externalAPIStores.add("ExternalAPIStore", externalAPIStoreJsonArray);

        File siteConfFile = new File(
                Thread.currentThread().getContextClassLoader().getResource("tenant-conf.json").getFile());
        String tenantConfValue = FileUtils.readFileToString(siteConfFile);
        JsonObject tenantConfig = (JsonObject) new JsonParser().parse(tenantConfValue);
        tenantConfig.add("ExternalAPIStores", externalAPIStores);
        Mockito.when(systemConfigurationsDAO.getSystemConfig(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(String.valueOf(tenantConfig));

        APIMConfigServiceImpl configServiceImpl = new APIMConfigServiceImpl();
        ExternalAPIStoresConfigDTO externalAPIStoresConfigDTO = configServiceImpl.getExternalStoreConfig(
                "carbon.super");
        Assert.assertEquals("http://localhost:9443/devportal", externalAPIStoresConfigDTO.getStoreURL());
        Assert.assertEquals(2, externalAPIStoresConfigDTO.getExternalAPIStoresList().size());

        ExternalAPIStoresConfigDTO.ExternalAPIStore externalStore1 = externalAPIStoresConfigDTO.getExternalAPIStoresList()
                .get(0);
        Assert.assertEquals("DeveloperPortal1", externalStore1.getId());
        Assert.assertEquals("wso2", externalStore1.getType());
        Assert.assertEquals("org.wso2.carbon.apimgt.impl.publishers.WSO2APIPublisher", externalStore1.getClassName());
        Assert.assertEquals("DeveloperPortal1", externalStore1.getDisplayName());
        Assert.assertEquals("http://localhost:9444/devportal1", externalStore1.getEndpoint());
        Assert.assertEquals("admin", externalStore1.getUsername());
        Assert.assertEquals("admin", externalStore1.getPassword());

        ExternalAPIStoresConfigDTO.ExternalAPIStore externalStore2 = externalAPIStoresConfigDTO.getExternalAPIStoresList()
                .get(1);
        Assert.assertEquals("DeveloperPortal2", externalStore2.getId());
        Assert.assertEquals("wso2", externalStore2.getType());
        Assert.assertEquals("org.wso2.carbon.apimgt.impl.publishers.WSO2APIPublisher", externalStore2.getClassName());
        Assert.assertEquals("DeveloperPortal2", externalStore2.getDisplayName());
        Assert.assertEquals("http://localhost:9444/devportal2", externalStore2.getEndpoint());
        Assert.assertEquals("admin", externalStore2.getUsername());
        Assert.assertEquals("admin", externalStore2.getPassword());
    }
}
