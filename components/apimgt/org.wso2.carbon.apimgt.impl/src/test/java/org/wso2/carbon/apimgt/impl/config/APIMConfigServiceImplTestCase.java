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
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowConfigDTO;
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

    /**
     * Test the population proper WorkflowConfigDTO when config json is given as input
     *
     * @throws IOException
     * @throws APIManagementException
     */
    @Test
    public void testGetWorkflowConfigWhenConfigured() throws IOException, APIManagementException {
        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(SystemConfigurationsDAO.class);
        SystemConfigurationsDAO systemConfigurationsDAO = Mockito.mock(SystemConfigurationsDAO.class);
        PowerMockito.when(SystemConfigurationsDAO.getInstance()).thenReturn(systemConfigurationsDAO);

        File siteConfFile = new File(
                Thread.currentThread().getContextClassLoader().getResource("tenant-conf.json").getFile());
        String tenantConfValue = FileUtils.readFileToString(siteConfFile);
        JsonObject tenantConfig = JsonParser.parseString(tenantConfValue).getAsJsonObject();
        Mockito.when(systemConfigurationsDAO.getSystemConfig(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(String.valueOf(tenantConfig));

        APIMConfigServiceImpl configServiceImpl = new APIMConfigServiceImpl();
        WorkflowConfigDTO workflowConfigDTO = configServiceImpl.getWorkFlowConfig("carbon.super");
        Assert.assertEquals(4, workflowConfigDTO.getWorkflowConfigMap().size());
        Assert.assertNotNull(workflowConfigDTO.getWorkflowConfigMap().get("ApplicationCreation"));
        Assert.assertNotNull(workflowConfigDTO.getWorkflowConfigMap().get("ApplicationDeletion").getClassName());
        Assert.assertEquals("org.wso2.example.UserSignUpCustomFlowExecutor",
                workflowConfigDTO.getWorkflowConfigMap().get("UserSignUp").getClassName());

        JsonObject properties = workflowConfigDTO.getWorkflowConfigMap().get("APIStateChange").getProperties();
        Assert.assertEquals("Created:Publish,Published:Block", properties.get("StateList").getAsString());
    }

    /**
     * Test the population proper WorkflowConfigDTO when no explicit workflow JSON config is given
     *
     * @throws IOException
     * @throws APIManagementException
     */
    @Test
    public void testGetWorkflowConfigWhenNotConfigured() throws IOException, APIManagementException {
        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(SystemConfigurationsDAO.class);
        SystemConfigurationsDAO systemConfigurationsDAO = Mockito.mock(SystemConfigurationsDAO.class);
        PowerMockito.when(SystemConfigurationsDAO.getInstance()).thenReturn(systemConfigurationsDAO);

        File siteConfFile = new File(
                Thread.currentThread().getContextClassLoader().getResource("tenant-conf.json").getFile());
        String tenantConfValue = FileUtils.readFileToString(siteConfFile);
        JsonObject tenantConfig = JsonParser.parseString(tenantConfValue).getAsJsonObject();
        tenantConfig.remove("Workflows");

        Mockito.when(systemConfigurationsDAO.getSystemConfig(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(String.valueOf(tenantConfig));

        APIMConfigServiceImpl configServiceImpl = new APIMConfigServiceImpl();
        WorkflowConfigDTO workflowConfigDTO = configServiceImpl.getWorkFlowConfig("carbon.super");
        Assert.assertEquals(0, workflowConfigDTO.getWorkflowConfigMap().size());
    }
}
