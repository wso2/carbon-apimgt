/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
 */

package org.wso2.carbon.apimgt.core.impl;

import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.api.GatewaySourceGenerator;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.API.APIBuilder;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.WorkflowConfig;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.template.APITemplateException;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.APILCWorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExtensionsConfigBuilder;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.lcm.core.beans.AvailableTransitionBean;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import org.wso2.carbon.lcm.core.impl.LifecycleState;
import org.wso2.carbon.lcm.sql.beans.LifecycleHistoryBean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.wso2.carbon.apimgt.core.dao.impl.PolicyDAOImpl.BRONZE_TIER;
import static org.wso2.carbon.apimgt.core.dao.impl.PolicyDAOImpl.GOLD_TIER;
import static org.wso2.carbon.apimgt.core.dao.impl.PolicyDAOImpl.SILVER_TIER;

public class APIPublisherImplTestCase {
    private static final String USER = "admin";
    private static final String ALTERNATIVE_USER = "alternativeUser";
    private static final String USER_ID = "d54de56r-4151-448e-5423-85b4f1f8b069";
    private static final String TIER = "Gold";
    private static final String API_ID = "apiId";
    private static final String DOC_ID = "docId";
    private static final String POLICY_LEVEL = APIMgtAdminService.PolicyLevel.application.name();
    private static final String POLICY_NAME = "policyName";
    private static final String ENDPOINT_NAME = "endpointName";
    private static final String SUB_ID = "subId";
    private static final String ENDPOINT_ID = "endpointId";
    private static final String QUERY_STRING = "queryString";
    private static final String ADMIN_ROLE = "admin";
    private static final String DEVELOPER_ROLE = "developer";
    private static final String ADMIN_ROLE_ID = "cfbde56e-4352-498e-b6dc-85a6f1f8b058";
    private static final String DEVELOPER_ROLE_ID = "cfdce56e-8434-498e-b6dc-85a6f2d8f035";

    @BeforeClass
    void init() {
        File temp = Files.createTempDir();
        temp.deleteOnExit();
        System.setProperty("gwHome", temp.getAbsolutePath());
        //Set the resource path, where contain composer test JS
        System.setProperty("carbon.home", new File("src/test/resources").getAbsolutePath());

        WorkflowExtensionsConfigBuilder.build(new ConfigProvider() {

            @Override
            public <T> T getConfigurationObject(Class<T> configClass) throws CarbonConfigurationException {
                T workflowConfig = (T) new WorkflowConfig();
                return workflowConfig;
            }

            @Override
            public Map getConfigurationMap(String namespace) throws CarbonConfigurationException {
                return null;
            }
        });
    }

    @Test(description = "Test add api with production endpoint")
    public void testAddApi() throws APIManagementException, LifecycleException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id("")
                .endpoint(SampleTestObjectCreator.getMockEndpointMap());
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        String endpointId = apiBuilder.getEndpoint().get("production").getId();
        Endpoint endpoint = new Endpoint.Builder().id(endpointId).name("testEndpoint").build();
        Mockito.when(apiDAO.getEndpoint(endpointId)).thenReturn(endpoint);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);

        //Error path
        //When an APIMgtDAOException is being thrown when the API is created
        Mockito.doThrow(APIMgtDAOException.class).when(apiDAO).addAPI(apiBuilder.build());
        try {
            apiPublisher.addAPI(apiBuilder);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while creating the API - " + apiBuilder.getName());
        }

        //Error path
        //When an GatewayException is being thrown when an error occurred while adding API to the Gateway
        Mockito.doThrow(GatewayException.class).when(gateway).addAPI(apiBuilder.build());
        try {
            apiPublisher.addAPI(apiBuilder);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Error occurred while adding API - " + apiBuilder.getName() + " to gateway");
        }

        //Error path
        //When an APITemplateException is being thrown when generating API configuration for API
        Mockito.when(gatewaySourceGenerator.getConfigStringFromTemplate(Mockito.any()))
                .thenThrow(APITemplateException.class);
        try {
            apiPublisher.addAPI(apiBuilder);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error generating API configuration for API " + apiBuilder.getName());
        }
    }

    @Test(description = "Test add api with sandbox endpoint")
    public void testAddApiSandboxEndpoint() throws APIManagementException, LifecycleException {
        Map<String, Endpoint> endpointMap = new HashMap<>();
        Map<String, Endpoint> resourceEndpointMap = new HashMap<>();
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
        uriTemplateBuilder.endpoint(resourceEndpointMap);
        uriTemplateBuilder.templateId("getApisApiIdGet");
        uriTemplateBuilder.uriTemplate("/apis/");
        uriTemplateBuilder.authType(APIMgtConstants.AUTH_APPLICATION_LEVEL_TOKEN);
        uriTemplateBuilder.policy(APIUtils.getDefaultAPIPolicy());
        uriTemplateBuilder.httpVerb("GET");
        uriTemplateMap.put("getApisApiIdGet", uriTemplateBuilder.build());
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id("").endpoint(endpointMap)
                .uriTemplates(uriTemplateMap);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        String endpointId = String.valueOf(apiBuilder.getEndpoint().get("sandbox"));
        Endpoint endpoint = new Endpoint.Builder().id(endpointId).name("testEndpoint").build();
        Mockito.when(apiDAO.getEndpoint(endpointId)).thenReturn(endpoint);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "Test add api when uri templates are empty")
    public void testAddApiWithEmptyUriTemplate() throws APIManagementException, LifecycleException {
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().uriTemplates(new HashMap<>());
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "Test add api with empty templateId and api definition")
    public void testAddApiWithEmptyTemplateIdAndApiDefinition() throws APIManagementException, LifecycleException {
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
        uriTemplateBuilder.endpoint(Collections.emptyMap());
        uriTemplateBuilder.templateId("");
        uriTemplateBuilder.uriTemplate("/apis/");
        uriTemplateBuilder.authType(APIMgtConstants.AUTH_APPLICATION_LEVEL_TOKEN);
        uriTemplateBuilder.policy(APIUtils.getDefaultAPIPolicy());
        uriTemplateBuilder.httpVerb("GET");
        uriTemplateMap.put("", uriTemplateBuilder.build());
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().uriTemplates(uriTemplateMap)
                .apiDefinition("");
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "Updating API Gateway Config")
    public void testUpdateApiGatewayConfig() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String configString = SampleTestObjectCreator.createSampleGatewayConfig();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, gatewaySourceGenerator, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        apiPublisher.updateApiGatewayConfig(uuid, configString);
        Mockito.verify(apiDAO, Mockito.times(1)).updateGatewayConfig(uuid, configString, USER);

        //Error path
        Mockito.when(gatewaySourceGenerator.getSwaggerFromGatewayConfig(configString))
                .thenThrow(APITemplateException.class);
        try {
            apiPublisher.updateApiGatewayConfig(uuid, configString);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error generating swagger from gateway config " + uuid);
        }
    }

    @Test(description = "Exception updating Gateway Config for API", expectedExceptions = APIManagementException.class)
    public void testUpdateApiGatewayConfigException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        String uuid = api.getId();
        String configString = SampleTestObjectCreator.createSampleGatewayConfig();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, gatewaySourceGenerator, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        Mockito.doThrow(new APIMgtDAOException("Couldn't update configuration for apiId " + uuid)).when(apiDAO)
                .updateGatewayConfig(uuid, configString, USER);
        apiPublisher.updateApiGatewayConfig(uuid, configString);
    }

    @Test(description = "Test add api with duplicate context", expectedExceptions = APIManagementException.class)
    public void testAddApiWithDuplicateContext() throws APIManagementException, LifecycleException {
        /**
         * This method check by adding duplicate api context
         */
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        Mockito.when(apiDAO.isAPIContextExists("weather")).thenReturn(true);
        Mockito.when(apiDAO.isAPINameExists("WeatherAPI", USER)).thenReturn(false);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "Test add api with duplicate name", expectedExceptions = APIManagementException.class)
    public void testAddApiWithDuplicateName() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        Mockito.when(apiDAO.isAPIContextExists("weather")).thenReturn(false);
        Mockito.when(apiDAO.isAPINameExists("WeatherAPI", USER)).thenReturn(true);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "Test add api with API Lifecycle failed",
            expectedExceptions = { LifecycleException.class, APIManagementException.class })
    public void testAddAPILifecycleFailure() throws LifecycleException, APIManagementException {
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenThrow(new LifecycleException("Couldn't add api lifecycle"));
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
    }

    @Test(description = "Test add api with restricted visibility")
    public void testAddApiWithRestrictedVisibility() throws APIManagementException, LifecycleException {
        Set<String> visibleRoles = new HashSet<>();
        visibleRoles.add(ADMIN_ROLE);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI()
                .endpoint(SampleTestObjectCreator.getMockEndpointMap()).visibility(API.Visibility.RESTRICTED)
                .visibleRoles(visibleRoles);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        String endpointId = apiBuilder.getEndpoint().get("production").getId();
        Endpoint endpoint = new Endpoint.Builder().id(endpointId).name("testEndpoint").build();
        Mockito.when(apiDAO.getEndpoint(endpointId)).thenReturn(endpoint);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "Get API with valid APIID")
    public void testGetApi() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        apiPublisher.getAPIbyUUID(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
    }

    @Test(description = "Delete API when the logged in user has no delete permission for the API")
    public void testDeleteApiWhenUserHasNoDeletePermission()
            throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        String uuid = api.getId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(ALTERNATIVE_USER, identityProvider, apiDAO,
                apiSubscriptionDAO, apiLifecycleManager, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);

        //Assuming the user role list retrieved from IS is null
        Mockito.when(identityProvider.getIdOfUser(ALTERNATIVE_USER)).thenReturn(USER_ID);
        Mockito.when(identityProvider.getRoleIdsOfUser(USER_ID)).thenReturn(null);
        try {
            apiPublisher.deleteAPI(uuid);
        } catch (APIManagementException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "The user " + ALTERNATIVE_USER + " does not have permission to delete the api " + api.getName());
        }
    }

    @Test(description = "Delete API with zero Subscriptions")
    public void testDeleteApiWithZeroSubscriptions() throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        List<String> roleIdsOfUser = new ArrayList<>();
        roleIdsOfUser.add(ADMIN_ROLE_ID);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        API api = apiBuilder.build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(ALTERNATIVE_USER, identityProvider, apiDAO,
                apiSubscriptionDAO, apiLifecycleManager, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(identityProvider.getIdOfUser(ALTERNATIVE_USER)).thenReturn(USER_ID);
        Mockito.when(identityProvider.getRoleIdsOfUser(USER_ID)).thenReturn(roleIdsOfUser);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        apiPublisher.deleteAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).removeLifecycle(lifecycleId);
        Mockito.verify(apiDAO, Mockito.times(1)).deleteAPI(uuid);
    }

    @Test(description = "Delete API with zero Subscriptions and pending wf state change")
    public void testDeleteApiWithZeroSubscriptionsAndPendingStateChange()
            throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        builder.workflowStatus(APILCWorkflowStatus.PENDING.toString());
        API api = builder.build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiSubscriptionDAO,
                apiLifecycleManager, workflowDAO, gateway);
        String externalRef = UUID.randomUUID().toString();
        Mockito.when(
                workflowDAO.getExternalWorkflowReferenceForPendingTask(uuid, WorkflowConstants.WF_TYPE_AM_API_STATE))
                .thenReturn(externalRef);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        apiPublisher.deleteAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).removeLifecycle(lifecycleId);
        Mockito.verify(apiDAO, Mockito.times(1)).deleteAPI(uuid);
        Mockito.verify(workflowDAO, Mockito.times(1))
                .getExternalWorkflowReferenceForPendingTask(uuid, WorkflowConstants.WF_TYPE_AM_API_STATE);
    }

    @Test(description = "Exception when getting api subscription count by API",
            expectedExceptions = APIManagementException.class)
    public void testGetAPISubscriptionCountByAPI() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid))
                .thenThrow(new APIMgtDAOException("Couldn't retrieve Subscriptions for API " + uuid));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiSubscriptionDAO);
        apiPublisher.getAPISubscriptionCountByAPI(uuid);

    }

    @Test(description = "Error occurred while deleting API with zero subscriptions")
    public void testDeleteApiWithZeroSubscriptionsException()
            throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(USER, identityProvider, apiDAO, apiSubscriptionDAO,
                apiLifecycleManager, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);

        //LifeCycleException
        Mockito.doThrow(LifecycleException.class).when(apiLifecycleManager)
                .removeLifecycle(api.getLifecycleInstanceId());
        try {
            apiPublisher.deleteAPI(uuid);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Error occurred while Disassociating the API with Lifecycle id " + uuid);
        }

        //ApiDAOException
        Mockito.doThrow(APIMgtDAOException.class).when(apiDAO).deleteAPI(uuid);
        try {
            apiPublisher.deleteAPI(uuid);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while deleting the API with id " + uuid);
        }

        //GatewayException
        Mockito.doThrow(GatewayException.class).when(gateway).deleteAPI(api);
        try {
            apiPublisher.deleteAPI(uuid);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Error occurred while deleting API with id - " + uuid + " from gateway");
        }
    }

    @Test(description = "Search APIs")
    public void testSearchAPIs() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        List<String> roleIdsOfUser = new ArrayList<>();
        roleIdsOfUser.add(ADMIN_ROLE_ID);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(ALTERNATIVE_USER, identityProvider, apiDAO);
        API api1 = SampleTestObjectCreator.createDefaultAPI().build();
        List<API> apimResultsFromDAO = new ArrayList<>();
        apimResultsFromDAO.add(api1);
        Mockito.when(apiDAO.searchAPIs(new HashSet<>(roleIdsOfUser), ALTERNATIVE_USER, api1.getName(), 1, 2)).
                thenReturn(apimResultsFromDAO);
        Mockito.when(identityProvider.getIdOfUser(ALTERNATIVE_USER)).thenReturn(USER_ID);
        Mockito.when(identityProvider.getRoleIdsOfUser(USER_ID)).thenReturn(roleIdsOfUser);
        List<API> apis = apiPublisher.searchAPIs(2, 1, api1.getName());
        Assert.assertNotNull(apis);
        Mockito.verify(apiDAO, Mockito.atLeastOnce())
                .searchAPIs(new HashSet<>(roleIdsOfUser), ALTERNATIVE_USER, api1.getName(), 1, 2);
    }

    @Test(description = "Search APIs with null query string")
    public void testSearchAPIsWithNullQuery() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        List<API> apimResultsFromDAO = new ArrayList<>();
        Mockito.when(apiDAO.searchAPIs(new HashSet<>(), USER, null, 1, 2)).thenReturn(apimResultsFromDAO);
        apiPublisher.searchAPIs(2, 1, null);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPIs(new HashSet<String>(), USER);
    }

    @Test(description = "Exception when searching APIs")
    public void testSearchAPIsException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(ALTERNATIVE_USER, identityProvider, apiDAO);

        //Error path
        //APIMgtDAOException
        Mockito.when(apiDAO.searchAPIs(new HashSet<>(), ALTERNATIVE_USER, QUERY_STRING, 1, 2))
                .thenThrow(APIMgtDAOException.class);
        try {
            apiPublisher.searchAPIs(2, 1, QUERY_STRING);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while Searching the API with query " + QUERY_STRING);
        }

        //Error path
        //IdentityProviderException
        Mockito.when(identityProvider.getIdOfUser(ALTERNATIVE_USER)).thenThrow(IdentityProviderException.class);
        try {
            apiPublisher.searchAPIs(2, 1, QUERY_STRING);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Error occurred while calling SCIM endpoint to retrieve user " + ALTERNATIVE_USER
                            + "'s information");
        }
    }

    @Test(description = "Get subscriptions for a provider's APIs")
    public void testGetSubscribersOfProvider() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiSubscriptionDAO);
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionsForUser(1, 2, USER))
                .thenReturn(new ArrayList<Subscription>());
        apiPublisher.getSubscribersOfProvider(1, 2, USER);
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).getAPISubscriptionsForUser(1, 2, USER);
    }

    @Test(description = "Exception when getting subscriptions for a provider's APIs",
            expectedExceptions = APIManagementException.class)
    public void testGetSubscribersOfProviderException() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiSubscriptionDAO);
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionsForUser(1, 2, USER))
                .thenThrow(new APIMgtDAOException("Unable to fetch subscriptions APIs of provider " + USER));
        apiPublisher.getSubscribersOfProvider(1, 2, USER);
    }

    @Test(description = "Error occurred while disassociating the API with Lifecycle when deleting the API",
            expectedExceptions = APIManagementException.class)
    public void testDeleteApiWithZeroSubscriptionsLifeCycleException()
            throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiSubscriptionDAO,
                apiLifecycleManager);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        Mockito.doThrow(new LifecycleException("Error occurred while Disassociating the API with Lifecycle id " + uuid))
                .when(apiLifecycleManager).removeLifecycle(lifecycleId);
        apiPublisher.deleteAPI(uuid);
    }

    @Test(description = "Delete API with Subscriptions", expectedExceptions = ApiDeleteFailureException.class)
    public void testDeleteApiWithSubscriptions() throws LifecycleException, APIManagementException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        API api = apiBuilder.build();
        String uuid = apiBuilder.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(2L);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiSubscriptionDAO, apiLifecycleManager);
        apiPublisher.deleteAPI(uuid);
    }

    @Test(description = "Test UpdateAPI with Status unchanged")
    public void testUpdateAPIWithStatusUnchanged() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager,
                gatewaySourceGenerator, gateway, policyDAO);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
        Mockito.when(identityProvider.getRoleId(ADMIN_ROLE)).thenReturn(ADMIN_ROLE_ID);
        Mockito.when(identityProvider.getRoleId(DEVELOPER_ROLE)).thenReturn(DEVELOPER_ROLE_ID);
        Mockito.when(apiDAO.isAPIContextExists(api.getContext())).thenReturn(true);
        String configString = SampleTestObjectCreator.createSampleGatewayConfig();
        Mockito.when(apiDAO.getGatewayConfigOfAPI(uuid)).thenReturn(configString);
        apiPublisher.updateAPI(api.lifeCycleStatus(APIStatus.CREATED.getStatus()).id(uuid));
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(0)).isAPIContextExists(api.getContext());
        Mockito.verify(apiDAO, Mockito.times(1))
                .updateAPI(uuid, api.lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
    }

    @Test(description = "Test UpdateAPI with Status changed", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithStatusChanged() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager, gateway);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        apiPublisher.updateAPI(api.lifeCycleStatus(APIStatus.PUBLISHED.getStatus()));
    }

    @Test(description = "Test UpdateAPI with context changed")
    public void testUpdateAPIWithContextChange() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager,
                gatewaySourceGenerator, gateway, policyDAO);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(identityProvider.getRoleId(ADMIN_ROLE)).thenReturn(ADMIN_ROLE_ID);
        Mockito.when(identityProvider.getRoleId(DEVELOPER_ROLE)).thenReturn(DEVELOPER_ROLE_ID);
        apiPublisher.updateAPI(api.context("test"));
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid, api.context("test").build());
    }

    @Test(description = "Test UpdateAPI with api name changed", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithNameChange() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager, gateway);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        apiPublisher.updateAPI(api.name("testApi"));
    }

    @Test(description = "Test UpdateAPI with version changed", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithVersionChange() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager, gateway);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        apiPublisher.updateAPI(api.version("1.1.0"));
    }

    @Test(description = "Test UpdateAPI with provider changed", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithProviderChange() throws APIManagementException {
        String newProvider = "testProvider";
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(newProvider, identityProvider, apiDAO, apiLifecycleManager,
                gatewaySourceGenerator, gateway);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(identityProvider.getIdOfUser(newProvider)).thenReturn("acfde54e-4342-412a-b4dc-84a6f6b8d053");
        apiPublisher.updateAPI(api.provider(newProvider));
    }

    @Test(description = "Test UpdateAPI with both context and version changed",
            expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithContextAndVersionChange() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager, gateway);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        apiPublisher.updateAPI(api.version("1.1.0").context("test"));
    }

    @Test(description = "Exception when updating API")
    public void testUpdateAPIException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy apiPolicy = new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY);
        apiPolicy.setUuid(UUID.randomUUID().toString());
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(apiPolicy);
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager,
                gatewaySourceGenerator, gateway, policyDAO);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(identityProvider.getRoleId(ADMIN_ROLE)).thenReturn(ADMIN_ROLE_ID);
        Mockito.when(identityProvider.getRoleId(DEVELOPER_ROLE)).thenReturn(DEVELOPER_ROLE_ID);

        //APIMgtDAOException
        Mockito.doThrow(new APIMgtDAOException("Error occurred while updating the API - " + api.getName())).when(apiDAO)
                .updateAPI(uuid, api.build());
        try {
            apiPublisher.updateAPI(api);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while updating the API - " + api.getName());
        }

        //ParseException
        try {
            apiPublisher.updateAPI(api.apiPermission("data{{]"));
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Error occurred while parsing the permission json from swagger - " + api.getName());
        }

        //GatewayException
        Mockito.doThrow(GatewayException.class).when(gateway).updateAPI(api.apiPermission("").build());
        try {
            apiPublisher.updateAPI(api.apiPermission(""));
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while updating API - " + api.getName() + " in gateway");
        }

        //Error path
        //When Parse Exception is thrown during getAPIByUUID - replacing group ids with names
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.apiPermission("data{{]").build());
        try {
            apiPublisher.updateAPI(api.apiPermission("data{{]"));
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Error occurred while parsing the permission json string for API " + api.getName());
        }
    }

    @Test(description = "IdentityProviderException when updating API when getting permissions of logged in user")
    public void testUpdateAPIIdentityProviderException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(ALTERNATIVE_USER, identityProvider, apiDAO);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(identityProvider.getIdOfUser(ALTERNATIVE_USER)).thenThrow(IdentityProviderException.class);
        try {
            apiPublisher.updateAPI(api);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Error occurred while calling SCIM endpoint to retrieve user " + ALTERNATIVE_USER
                            + "'s information");
        }

        //
    }

    @Test(description = "Test UpdateAPI with Status unchanged but different context")
    public void testUpdateAPIWithStatusUnchangedButDifferentContext() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager,
                gatewaySourceGenerator, gateway, policyDAO);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(identityProvider.getRoleId(ADMIN_ROLE)).thenReturn(ADMIN_ROLE_ID);
        Mockito.when(identityProvider.getRoleId(DEVELOPER_ROLE)).thenReturn(DEVELOPER_ROLE_ID);
        apiPublisher.updateAPI(api.context("testContext"));
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).isAPIContextExists(api.getContext());
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid, api.build());
    }

    @Test(description = "Test UpdateAPI with Status unchanged but context exist",
            expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithStatusUnchangedWhenContextExists() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy apiPolicy = new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY);
        apiPolicy.setUuid(UUID.randomUUID().toString());
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(apiPolicy);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager,
                gatewaySourceGenerator, gateway, policyDAO);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(apiDAO.isAPIContextExists("testContext")).thenReturn(true);
        Mockito.when(identityProvider.getRoleId(ADMIN_ROLE)).thenReturn(ADMIN_ROLE_ID);
        Mockito.when(identityProvider.getRoleId(DEVELOPER_ROLE)).thenReturn(DEVELOPER_ROLE_ID);
        apiPublisher.updateAPI(api.context("testContext"));
    }

    @Test(description = "Test UpdateAPI with null api returned by uuid",
            expectedExceptions = APIManagementException.class)
    public void testUpdateNullAPI() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(null);
        apiPublisher.updateAPI(api);
    }

    @Test(description = "Test UpdateAPI with restricted visibility")
    public void testUpdateAPIWithRestrictedVisibility() throws APIManagementException, LifecycleException {
        Set<String> visibleRoles = new HashSet<>();
        visibleRoles.add(ADMIN_ROLE);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI().visibility(API.Visibility.RESTRICTED)
                .visibleRoles(visibleRoles);
        String uuid = api.getId();
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager,
                gatewaySourceGenerator, gateway, policyDAO);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
        Mockito.when(apiDAO.isAPIContextExists(api.getContext())).thenReturn(true);
        String configString = SampleTestObjectCreator.createSampleGatewayConfig();
        Mockito.when(apiDAO.getGatewayConfigOfAPI(uuid)).thenReturn(configString);
        Mockito.when(identityProvider.getRoleId(ADMIN_ROLE)).thenReturn(ADMIN_ROLE_ID);
        Mockito.when(identityProvider.getRoleId(DEVELOPER_ROLE)).thenReturn(DEVELOPER_ROLE_ID);
        apiPublisher.updateAPI(api.lifeCycleStatus(APIStatus.CREATED.getStatus()).id(uuid));
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(0)).isAPIContextExists(api.getContext());
        Mockito.verify(apiDAO, Mockito.times(1))
                .updateAPI(uuid, api.lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
    }

    @Test(description = "Test UpdateAPI with restricted visibility but different context")
    public void testUpdateAPIWithRestrictedVisibilityButDifferentContext() throws APIManagementException {
        Set<String> visibleRoles = new HashSet<>();
        visibleRoles.add(ADMIN_ROLE);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI().visibility(API.Visibility.RESTRICTED)
                .visibleRoles(visibleRoles);
        String uuid = api.getId();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager,
                gatewaySourceGenerator, gateway, policyDAO);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(identityProvider.getRoleId(ADMIN_ROLE)).thenReturn(ADMIN_ROLE_ID);
        Mockito.when(identityProvider.getRoleId(DEVELOPER_ROLE)).thenReturn(DEVELOPER_ROLE_ID);
        apiPublisher.updateAPI(api.context("testContext"));
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).isAPIContextExists(api.getContext());
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid, api.build());
    }

    @Test(description = "Update api status")
    public void testUpdateAPIStatus() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);

        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiLifecycleManager, apiDAO, workflowDAO, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lcState = api.getLifeCycleStatus();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        LifecycleState lifecycleState = SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId);
        Mockito.when(apiLifecycleManager.getLifecycleDataForState(lifecycleId, lcState)).thenReturn(lifecycleState);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        USER, api)).thenReturn(lifecycleState);
        API.APIBuilder apiBuilder = new API.APIBuilder(api);
        apiBuilder.lifecycleState(lifecycleState);
        apiBuilder.updatedBy(USER);
        api = apiBuilder.build();
        lifecycleState.setState(APIStatus.PUBLISHED.getStatus());
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), new HashMap<>());
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        USER, api);
    }

    @Test(description = "Update api status", expectedExceptions = { APIManagementException.class })
    public void testUpdateAPIStatusWhileAPINotAvailable() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(null);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), uuid, USER, api))
                .thenReturn(SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId));
        Mockito.doThrow(new APIMgtDAOException("Couldn't change the status of api ID " + uuid)).when(apiDAO)
                .changeLifeCycleStatus(uuid, APIStatus.PUBLISHED.getStatus());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), Collections.emptyMap());
    }

    @Test(description = "Update api status",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    public void testUpdateAPIStatusWhileGettingDBFailure() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenThrow(new APIMgtDAOException("Couldn't Create connection"));
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), uuid, USER, api))
                .thenReturn(SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId));
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), Collections.emptyMap());
    }

    @Test(description = "Update api status with deprecating previous versions and not require re-subscriptions")
    public void testUpdateAPIStatusDeprecatePreviousVersionsAndNotRequireReSubscription()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, applicationDAO, apiSubscriptionDAO,
                apiLifecycleManager, gatewaySourceGenerator, workflowDAO, gateway);
        API previousApi = SampleTestObjectCreator.createDefaultAPI().build();
        String previousApiUUID = previousApi.getId();
        String lifecycleIdPrevious = previousApi.getLifecycleInstanceId();
        String lcStatePrevious = previousApi.getLifeCycleStatus();
        LifecycleState previousLifecycleState = SampleTestObjectCreator
                .getMockLifecycleStateObject(lifecycleIdPrevious);
        List<AvailableTransitionBean> list = new ArrayList<>();
        AvailableTransitionBean bean = new AvailableTransitionBean("Deprecate", APIStatus.DEPRECATED.getStatus());
        list.add(bean);
        previousLifecycleState.setAvailableTransitionBeanList(list);
        Mockito.when(apiLifecycleManager.getLifecycleDataForState(lifecycleIdPrevious, lcStatePrevious))
                .thenReturn(previousLifecycleState);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.PUBLISHED.getStatus(), APIStatus.DEPRECATED.getStatus(),
                        lifecycleIdPrevious, USER, previousApi)).thenReturn(previousLifecycleState);
        previousLifecycleState.setState(APIStatus.DEPRECATED.getStatus());

        API api = SampleTestObjectCreator.createDefaultAPI().copiedFromApiId(previousApiUUID).build();
        String uuid = api.getId();
        String lcState = api.getLifeCycleStatus();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        LifecycleState lifecycleState = SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId);
        Mockito.when(apiLifecycleManager.getLifecycleDataForState(lifecycleId, lcState)).thenReturn(lifecycleState);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        USER, api)).thenReturn(lifecycleState);
        lifecycleState.setState(APIStatus.PUBLISHED.getStatus());
        API.APIBuilder apiBuilder = new API.APIBuilder(api);
        apiBuilder.lifecycleState(lifecycleState);
        apiBuilder.updatedBy(USER);
        api = apiBuilder.build();

        Mockito.when(apiDAO.getAPI(previousApiUUID)).thenReturn(previousApi);
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put(APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS, true);

        Application application = SampleTestObjectCreator.createDefaultApplication();

        List<Subscription> subscriptions = new ArrayList<>();
        Subscription subscription = new Subscription(previousApiUUID, application, previousApi,
                new SubscriptionPolicy(TIER));
        subscriptions.add(subscription);
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionsByAPI(previousApiUUID)).thenReturn(subscriptions);
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), checklist);
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        USER, api);
    }

    @Test(description = "Update api status with re-subscriptions")
    public void testUpdateAPIStatusRequireReSubscription() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, applicationDAO, apiSubscriptionDAO,
                apiLifecycleManager, gatewaySourceGenerator, workflowDAO, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lcState = api.getLifeCycleStatus();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        LifecycleState lifecycleState = SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId);
        Mockito.when(apiLifecycleManager.getLifecycleDataForState(lifecycleId, lcState)).thenReturn(lifecycleState);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        USER, api)).thenReturn(lifecycleState);
        API.APIBuilder apiBuilder = new API.APIBuilder(api);
        apiBuilder.lifecycleState(lifecycleState);
        apiBuilder.updatedBy(USER);
        api = apiBuilder.build();
        lifecycleState.setState(APIStatus.PUBLISHED.getStatus());
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put(APIMgtConstants.REQUIRE_RE_SUBSCRIPTIONS, true);
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), checklist);
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        USER, api);
    }

    @Test(description = "Update API Status when its workflow status is pending",
            expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWorkflowStatus() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiLifecycleManager, apiDAO, workflowDAO, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI()
                .workflowStatus(APIMgtConstants.APILCWorkflowStatus.PENDING.toString()).build();
        String uuid = api.getId();
        String lcState = api.getLifeCycleStatus();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        LifecycleState lifecycleState = SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId);
        Mockito.when(apiLifecycleManager.getLifecycleDataForState(lifecycleId, lcState)).thenReturn(lifecycleState);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        USER, api)).thenReturn(lifecycleState);
        API.APIBuilder apiBuilder = new API.APIBuilder(api);
        apiBuilder.lifecycleState(lifecycleState);
        apiBuilder.updatedBy(USER);
        api = apiBuilder.build();
        lifecycleState.setState(APIStatus.PUBLISHED.getStatus());
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), new HashMap<>());
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        USER, api);
    }

    @Test(description = "Update checklist item")
    public void testUpdateCheckListItem() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put(APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS, true);
        apiPublisher.updateCheckListItem(uuid, APIStatus.CREATED.getStatus(), checklist);
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .checkListItemEvent(lifecycleId, APIStatus.CREATED.getStatus(),
                        APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS, true);
    }

    @Test(description = "Update API when there is a list of invalid roles specified for permission")
    public void testReplaceGroupNamesWithIdWithInvalidRoles() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        String permissionString = "[{\"groupId\" : \"developer\", \"permission\" : [\"READ\",\"UPDATE\"]},"
                + "{\"groupId\" : \"invalid_role\", \"permission\" : [\"READ\",\"UPDATE\",\"DELETE\"]}]";
        String errorMessage = "There are invalid roles in the permission string";
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI().apiPermission(permissionString);
        String uuid = api.getId();
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager,
                gatewaySourceGenerator, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
        Mockito.when(identityProvider.getRoleId("invalid_role"))
                .thenThrow(new IdentityProviderException(errorMessage, ExceptionCodes.ROLE_DOES_NOT_EXIST));
        Mockito.when(identityProvider.getRoleId(DEVELOPER_ROLE)).thenReturn(DEVELOPER_ROLE_ID);
        Mockito.when(apiDAO.isAPIContextExists(api.getContext())).thenReturn(true);
        String configString = SampleTestObjectCreator.createSampleGatewayConfig();
        Mockito.when(apiDAO.getGatewayConfigOfAPI(uuid)).thenReturn(configString);
        try {
            apiPublisher.updateAPI(api.lifeCycleStatus(APIStatus.CREATED.getStatus()).id(uuid));
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "There are invalid roles in the permission string");
        }
    }

    @Test(description = "Remove api pending lc status change request")
    public void testRemovePendingLifecycleWorkflowTaskForAPI() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiLifecycleManager, apiDAO, workflowDAO, gateway);
        APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        builder.workflowStatus(APILCWorkflowStatus.PENDING.toString());
        API api = builder.build();
        String uuid = api.getId();
        String externalRef = UUID.randomUUID().toString();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(
                workflowDAO.getExternalWorkflowReferenceForPendingTask(uuid, WorkflowConstants.WF_TYPE_AM_API_STATE))
                .thenReturn(externalRef);

        apiPublisher.removePendingLifecycleWorkflowTaskForAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPIWorkflowStatus(uuid, APILCWorkflowStatus.APPROVED);
        Mockito.verify(workflowDAO, Mockito.times(1))
                .getExternalWorkflowReferenceForPendingTask(uuid, WorkflowConstants.WF_TYPE_AM_API_STATE);
    }

    @Test(description = "Remove api pending lc status change request for an api without a pending task",
            expectedExceptions = APIManagementException.class)
    public void testRemovePendingLifecycleWorkflowTaskForAPIForAPIWithoutPendingLCState()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiLifecycleManager, apiDAO, workflowDAO, gateway);
        APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        builder.workflowStatus(APILCWorkflowStatus.APPROVED.toString());
        API api = builder.build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        apiPublisher.removePendingLifecycleWorkflowTaskForAPI(uuid);

    }

    @Test(description = "Exception when removing api pending lc status change request for an api",
            expectedExceptions = APIManagementException.class)
    public void testTemovePendingLifecycleWorkflowTaskForAPIForAPIWithoutPendingLCState()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiLifecycleManager, apiDAO, workflowDAO, gateway);
        APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        builder.workflowStatus(APILCWorkflowStatus.PENDING.toString());
        API api = builder.build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api); /*
        Mockito.doThrow(new APIMgtDAOException("Error while executing sql query")).when(workflowDAO)
        .getExternalWorkflowReferenceForPendingTask(uuid, WorkflowConstants.WF_TYPE_AM_API_STATE);
        */
        Mockito.when(
                workflowDAO.getExternalWorkflowReferenceForPendingTask(uuid, WorkflowConstants.WF_TYPE_AM_API_STATE))
                .thenThrow(new APIMgtDAOException("Error occurred while changing api lifecycle workflow status"));
        apiPublisher.removePendingLifecycleWorkflowTaskForAPI(uuid);
        Mockito.verify(workflowDAO, Mockito.times(1))
                .getExternalWorkflowReferenceForPendingTask(uuid, WorkflowConstants.WF_TYPE_AM_API_STATE);
    }

    @Test(description = "Remove api pending lc status change request for an invalid",
            expectedExceptions = APIManagementException.class)
    public void testRemovePendingLifecycleWorkflowTaskForInvalidAPI()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiLifecycleManager, apiDAO, workflowDAO, gateway);
        API api = null;
        String uuid = UUID.randomUUID().toString();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        apiPublisher.removePendingLifecycleWorkflowTaskForAPI(uuid);

    }

    @Test(description = "Create new  API version with valid APIID")
    public void testCreateNewAPIVersion() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        String newUUid = apiPublisher.createNewAPIVersion(uuid, "2.0.0");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(0)).addAPI(api);
        Assert.assertNotEquals(uuid, newUUid);
    }

    @Test(description = "Create new  API version when API definition is empty")
    public void testCreateNewAPIVersionWithEmptyAPIDefinition() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().apiDefinition("").build();
        String uuid = api.getId();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        String newUUid = apiPublisher.createNewAPIVersion(uuid, "2.0.0");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(0)).addAPI(api);
        Assert.assertNotEquals(uuid, newUUid);
    }

    @Test(description = "Create new  API version with invalid APIID",
            expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testCreateNewAPIVersionWithInvalidUUID() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        Mockito.when(apiDAO.getAPI("xxxxxx")).thenReturn(null);
        apiPublisher.createNewAPIVersion("xxxxxx", "2.0.0");
    }

    @Test(description = "Create new  API version with empty APIID")
    void testCreateNewAPIVersionWithEmptyUUID() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        try {
            apiPublisher.createNewAPIVersion(null, "2.0.0");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("API ID cannot be empty"));
        }
    }

    @Test(description = "Create new  API version with invalid API version")
    void testCreateNewAPIVersionWithEmptyVersion() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        try {
            apiPublisher.createNewAPIVersion(uuid, null);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("New API version cannot be empty"));
        }
    }

    @Test(description = "Create new  API version with previous API version")
    void testCreateNewAPIVersionWithPreviousVersion() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        try {
            apiPublisher.createNewAPIVersion(uuid, api.getVersion());
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("cannot be same as the previous version"));
        }
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed",
            expectedExceptions = { LifecycleException.class, APIManagementException.class })
    public void testCreateNewAPIVersionAndCheckNewApiLifecycleAddFailure()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenThrow(new LifecycleException(""));
        apiPublisher.createNewAPIVersion(uuid, "2.0.0");
        Mockito.verify(apiDAO, Mockito.times(0)).addAPI(api);
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    public void testCreateNewAPIVersionAndGetAPIByUuidFailure() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        Mockito.when(apiDAO.getAPI("yyyyy")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.createNewAPIVersion("yyyyy", "2.0.0");
    }

    @Test(description = "Check if api exist with valid uuid")
    public void testCheckIfAPIExistForValidUuid() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getAPISummary("zzzzz")).thenReturn(SampleTestObjectCreator.getMockApiSummaryObject());
        Assert.assertTrue(apiPublisher.checkIfAPIExists("zzzzz"));
    }

    @Test(description = "Check if api exist with invalid uuid")
    public void testCheckIfAPIExistForInValidUuid() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getAPISummary("aaaaa")).thenReturn(null);
        Assert.assertFalse(apiPublisher.checkIfAPIExists("aaaaa"));
    }

    @Test(description = "Check if api exist with invalid uuid",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    public void testCheckIfAPIExistWhileGettingJDBCConnectionFailure() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getAPISummary("bbbbb")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.checkIfAPIExists("bbbbb");
    }

    @Test(description = "Add Documentation Info")
    public void testAddDocumentationInfo() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id(DOC_ID)
                .permission("[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\"]}]").build();
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
        Mockito.verify(apiDAO, Mockito.times(1)).addDocumentInfo(API_ID, documentInfo);
    }

    @Test(description = "Document already exists error when adding Documentation Info",
            expectedExceptions = APIManagementException.class)
    public void testAddDocumentationInfoDocAlreadyExists() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.isDocumentExist(API_ID, documentInfo)).thenReturn(true);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
    }

    @Test(description = "Unable to add documentation info", expectedExceptions = APIManagementException.class)
    public void testUnableToAddDocumentationException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation")).when(apiDAO)
                .addDocumentInfo(API_ID, documentInfo);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
    }

    @Test(description = "Parse exception when adding documentation info",
            expectedExceptions = APIManagementException.class)
    public void testAddDocumentationInfoJsonParseException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id("")
                .permission("data").build();
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
    }

    @Test(description = "Remove Documentation Info")
    public void testRemoveDocumentationInfo() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        apiPublisher.removeDocumentation(DOC_ID);
        Mockito.verify(apiDAO, Mockito.times(1)).deleteDocument(DOC_ID);
    }

    @Test(description = "Exception when removing Documentation Info", expectedExceptions = APIManagementException.class)
    public void testRemoveDocumentationInfoException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation with file")).when(apiDAO)
                .deleteDocument(DOC_ID);
        apiPublisher.removeDocumentation(DOC_ID);
    }

    @Test(description = "Upload Documentation File")
    public void testUploadDocumentationFile() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        apiPublisher.uploadDocumentationFile(DOC_ID, null, "text/plain");
        Mockito.verify(apiDAO, Mockito.times(1)).addDocumentFileContent(DOC_ID, null, "text/plain", USER);
    }

    @Test(description = "Exception when uploading Documentation File",
            expectedExceptions = APIManagementException.class)
    public void testUploadDocumentationFileException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation with file")).when(apiDAO)
                .addDocumentFileContent(DOC_ID, null, "text/plain", USER);
        apiPublisher.uploadDocumentationFile(DOC_ID, null, "text/plain");
    }

    @Test(description = "Add documentation inline content")
    public void testAddDocumentationContent() throws APIManagementException, IOException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        String inlineContent = SampleTestObjectCreator.createDefaultInlineDocumentationContent();
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        apiPublisher.addDocumentationContent(DOC_ID, inlineContent);
        Mockito.verify(apiDAO, Mockito.times(1)).addDocumentInlineContent(DOC_ID, inlineContent, USER);
    }

    @Test(description = "Update Documentation Info")
    public void testUpdateDocumentation() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id(DOC_ID)
                .permission("[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\"]}]").build();
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.isDocumentExist(API_ID, documentInfo)).thenReturn(true);
        apiPublisher.updateDocumentation(API_ID, documentInfo);
        Mockito.verify(apiDAO, Mockito.times(1)).updateDocumentInfo(API_ID, documentInfo, USER);
    }

    @Test(description = "Documentation does not exists error when updating Documentation Info",
            expectedExceptions = APIManagementException.class)
    public void testUpdateDocumentationDocNotExists() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        Mockito.when(apiDAO.isDocumentExist(API_ID, documentInfo)).thenReturn(false);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        apiPublisher.updateDocumentation(API_ID, documentInfo);
    }

    @Test(description = "Unable to update documentation info")
    public void testUnableToUpdateDocumentationException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.isDocumentExist(API_ID, documentInfo)).thenReturn(true);
        Mockito.doThrow(APIMgtDAOException.class).when(apiDAO).updateDocumentInfo(API_ID, documentInfo, USER);
        try {
            apiPublisher.updateDocumentation(API_ID, documentInfo);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Unable to update the documentation");
        }
    }

    @Test(description = "Parse exception when updating documentation info",
            expectedExceptions = APIManagementException.class)
    public void testUpdateDocumentationInfoJsonParseException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id("")
                .permission("data").build();
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        apiPublisher.updateDocumentation(API_ID, documentInfo);
    }

    @Test(description = "Exception when updating api status", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIStatusException() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);

        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiLifecycleManager, apiDAO, workflowDAO, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lcState = api.getLifeCycleStatus();
        String lifecycleId = api.getLifecycleInstanceId();
        LifecycleState lifecycleState = SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId);
        Mockito.when(apiLifecycleManager.getLifecycleDataForState(lifecycleId, lcState)).thenReturn(lifecycleState);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        API.APIBuilder apiBuilder = new API.APIBuilder(api);
        apiBuilder.lifecycleState(lifecycleState);
        apiBuilder.updatedBy(USER);
        api = apiBuilder.build();
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        USER, api)).thenThrow(new LifecycleException("Couldn't change the status of api ID " + uuid));
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), new HashMap<>());
    }

    @Test(description = "Exception when updating checklist item", expectedExceptions = APIManagementException.class)
    public void testUpdateCheckListItemException() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.checkListItemEvent(lifecycleId, APIStatus.CREATED.getStatus(),
                APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS, true))
                .thenThrow(new LifecycleException("Couldn't get the lifecycle status of api ID " + uuid));
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put(APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS, true);
        apiPublisher.updateCheckListItem(uuid, APIStatus.CREATED.getStatus(), checklist);
    }

    @Test(description = "Get lifecycle events list of an API")
    public void testGetLifeCycleEvents() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        List<LifecycleHistoryBean> lifecycleHistoryBeanList = new ArrayList<>();
        LifecycleHistoryBean bean = new LifecycleHistoryBean();
        bean.setPreviousState(APIStatus.CREATED.getStatus());
        bean.setPostState(APIStatus.DEPRECATED.getStatus());
        bean.setUser(USER);
        lifecycleHistoryBeanList.add(bean);
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doReturn(lifecycleHistoryBeanList).when(apiLifecycleManager).getLifecycleHistory(lifecycleId);
        apiPublisher.getLifeCycleEvents(uuid);
    }

    @Test(description = "Exception finding APISummary Resource when getting lifecycle events list of an API",
            expectedExceptions = APIManagementException.class)
    public void testGetLifeCycleEventsExceptionFindingApiSummaryResource()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        Mockito.when(apiDAO.getAPISummary(API_ID))
                .thenThrow(new APIMgtDAOException("Couldn't find APISummary Resource for ID " + API_ID));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        apiPublisher.getLifeCycleEvents(API_ID);
    }

    @Test(description = "Exception finding API LifeCycle History when getting lifecycle events list of an API",
            expectedExceptions = APIManagementException.class)
    public void testGetLifeCycleEventsExceptionFindingAPILifeCycleHistory()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doThrow(new LifecycleException("Couldn't find APILifecycle History for ID " + uuid))
                .when(apiLifecycleManager).getLifecycleHistory(lifecycleId);
        apiPublisher.getLifeCycleEvents(uuid);
    }

    @Test(description = "Get api lifecycle data")
    public void testGetAPILifeCycleData() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        String lcState = api.getLifeCycleStatus();
        LifecycleState bean = new LifecycleState();
        bean.setState(APIStatus.CREATED.getStatus());
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doReturn(bean).when(apiLifecycleManager).getLifecycleDataForState(lifecycleId, lcState);
        apiPublisher.getAPILifeCycleData(uuid);
    }

    @Test(description = "Get api lifecycle data for a null api",
            expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testGetAPILifeCycleDataForNullAPI() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getAPISummary(API_ID)).thenReturn(null);
        apiPublisher.getAPILifeCycleData(API_ID);
    }

    @Test(description = "Could not retrieve api summary when Getting api lifecycle data",
            expectedExceptions = APIManagementException.class)
    public void testGetAPILifeCycleDataExceptionWhenRetrievingAPISummary()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getAPISummary(API_ID))
                .thenThrow(new APIMgtDAOException("Couldn't retrieve API Summary for " + API_ID));
        apiPublisher.getAPILifeCycleData(API_ID);
    }

    @Test(description = "Could not retrieve api lifecycle when Getting api lifecycle data",
            expectedExceptions = APIManagementException.class)
    public void testGetAPILifeCycleDataExceptionWhenRetrievingAPILifeCycle()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lcState = api.getLifeCycleStatus();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doThrow(new LifecycleException("Couldn't retrieve API Lifecycle for " + uuid)).when(apiLifecycleManager)
                .getLifecycleDataForState(lifecycleId, lcState);
        apiPublisher.getAPILifeCycleData(uuid);
    }

    @Test(description = "Save thumbnail image for API")
    public void testSaveThumbnailImage() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        InputStream image = SampleTestObjectCreator.createDefaultThumbnailImage();
        apiPublisher.saveThumbnailImage(API_ID, image, "png");
        Mockito.verify(apiDAO, Mockito.times(1)).updateImage(API_ID, image, "png", USER);
    }

    @Test(description = "Exception when saving thumbnail image for API",
            expectedExceptions = APIManagementException.class)
    public void testSaveThumbnailImageException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.doThrow(new APIMgtDAOException("Couldn't save the thumbnail image")).when(apiDAO)
                .updateImage(API_ID, null, "jpeg", USER);
        apiPublisher.saveThumbnailImage(API_ID, null, "jpeg");
    }

    @Test(description = "Get thumbnail image for API")
    public void testGetThumbnailImage() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getImage(uuid)).thenReturn(null);
        apiPublisher.getThumbnailImage(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getImage(uuid);
    }

    @Test(description = "Exception when getting thumbnail image for API",
            expectedExceptions = APIManagementException.class)
    public void testGetThumbnailImageException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getImage(API_ID))
                .thenThrow(new APIMgtDAOException("Couldn't retrieve thumbnail for api " + API_ID));
        apiPublisher.getThumbnailImage(API_ID);
    }

    @Test(description = "Retrieving all labels")
    public void testGetAllLabels() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(labelDAO);
        Mockito.when(labelDAO.getLabels()).thenReturn(new ArrayList<Label>());
        apiPublisher.getAllLabels();
        Mockito.verify(labelDAO, Mockito.times(1)).getLabels();
    }

    @Test(description = "Exception when retrieving all labels", expectedExceptions = LabelException.class)
    public void testGetAllLabelsException() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(labelDAO);
        Mockito.when(labelDAO.getLabels()).thenThrow(new APIMgtDAOException("Error occurred while retrieving labels"));
        apiPublisher.getAllLabels();
    }

    @Test(description = "Update subscription status")
    public void testUpdateSubscriptionStatus() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIGateway apiGatewayPublisher = Mockito.mock(APIGateway.class);
        Application application = SampleTestObjectCreator.createDefaultApplication();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Policy policy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        Subscription subscription = new Subscription(SUB_ID, application, api, policy);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiSubscriptionDAO, apiGatewayPublisher);
        Mockito.when(apiSubscriptionDAO.getAPISubscription(SUB_ID)).thenReturn(subscription);
        apiPublisher.updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1))
                .updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
    }

    @Test(description = "Error when updating subscription status", expectedExceptions = APIManagementException.class)
    public void testUpdateSubscriptionStatusException() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiSubscriptionDAO);
        Mockito.doThrow(new APIMgtDAOException("", new Throwable())).when(apiSubscriptionDAO)
                .updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
        apiPublisher.updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
    }

    @Test(description = "Update subscription policy")
    public void testUpdateSubscriptionPolicy() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiSubscriptionDAO);
        apiPublisher.updateSubscriptionPolicy(SUB_ID, "test policy");
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).updateSubscriptionPolicy(SUB_ID, "test policy");
    }

    @Test(description = "Error when updating subscription policy", expectedExceptions = APIManagementException.class)
    public void testUpdateSubscriptionPolicyException() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiSubscriptionDAO);
        Mockito.doThrow(new APIMgtDAOException("", new Throwable())).when(apiSubscriptionDAO)
                .updateSubscriptionPolicy(SUB_ID, "test policy");
        apiPublisher.updateSubscriptionPolicy(SUB_ID, "test policy");
    }

    @Test(description = "Get last updated time of endpoint")
    public void testGetLastUpdatedTimeOfEndpoint() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getLastUpdatedTimeOfEndpoint(ENDPOINT_ID)).thenReturn("2017-03-19T13:45:30");
        apiPublisher.getLastUpdatedTimeOfEndpoint(ENDPOINT_ID);
        Mockito.verify(apiDAO, Mockito.times(1)).getLastUpdatedTimeOfEndpoint(ENDPOINT_ID);
    }

    @Test(description = "Exception when getting last updated time of endpoint",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfEndpointException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getLastUpdatedTimeOfEndpoint(ENDPOINT_ID)).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving the last update time of the endpoint with id " + ENDPOINT_ID));
        apiPublisher.getLastUpdatedTimeOfEndpoint(ENDPOINT_ID);
    }

    @Test(description = "Get last updated time of Throttling Policy")
    public void testGetLastUpdatedTimeOfThrottlingPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(policyDAO);
        Mockito.when(
                policyDAO.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel.application, POLICY_NAME))
                .thenReturn("2017-03-19T13:45:30");
        apiPublisher.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
        Mockito.verify(policyDAO, Mockito.times(1))
                .getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
    }

    @Test(description = "Get last updated time of Gateway Config")
    public void testGetLastUpdatedTimeOfGatewayConfig() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getLastUpdatedTimeOfGatewayConfig(API_ID)).thenReturn("2017-03-19T13:45:30");
        apiPublisher.getLastUpdatedTimeOfGatewayConfig(API_ID);
        Mockito.verify(apiDAO, Mockito.times(1)).getLastUpdatedTimeOfGatewayConfig(API_ID);
    }

    @Test(description = "Exception when getting last updated time of Gateway Config",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfGatewayConfigException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getLastUpdatedTimeOfGatewayConfig(API_ID)).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving the last update time of the gateway configuration of API with id "
                        + API_ID));
        apiPublisher.getLastUpdatedTimeOfGatewayConfig(API_ID);
    }

    @Test(description = "Exception when getting last updated time of Throttling Policy",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfThrottlingPolicyException() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(policyDAO);
        Mockito.when(
                policyDAO.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel.application, POLICY_NAME))
                .thenThrow(new APIMgtDAOException(
                        "Error while retrieving last updated time of policy :" + POLICY_LEVEL + "/" + POLICY_LEVEL));
        apiPublisher.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
    }

    @Test(description = "Get all policies by level")
    public void testGetAllPoliciesByLevel() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        List<Policy> policies = new ArrayList<>();
        Policy policy = Mockito.mock(Policy.class);
        policy.setPolicyName(POLICY_NAME);
        policies.add(policy);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(policyDAO);
        Mockito.when(policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application)).thenReturn(policies);
        apiPublisher.getAllPoliciesByLevel(APIMgtAdminService.PolicyLevel.application);
        Mockito.verify(policyDAO, Mockito.times(1)).getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application);

        //Error path
        Mockito.when(policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application))
                .thenThrow(APIMgtDAOException.class);
        try {
            apiPublisher.getAllPoliciesByLevel(APIMgtAdminService.PolicyLevel.application);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Error while retrieving Policies for level: " + APIMgtAdminService.PolicyLevel.application);
        }
    }

    @Test(description = "Get all policy by name")
    public void testGetPolicyByName() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy policy = Mockito.mock(Policy.class);
        policy.setPolicyName(POLICY_NAME);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(policyDAO);
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME))
                .thenReturn(policy);
        apiPublisher.getPolicyByName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
        Mockito.verify(policyDAO, Mockito.times(1))
                .getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);

        //Error path
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME))
                .thenThrow(APIMgtDAOException.class);
        try {
            apiPublisher.getPolicyByName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Error while retrieving Policy for level: " + APIMgtAdminService.PolicyLevel.application
                            + ", name: " + POLICY_NAME);
        }
    }

    @Test(description = "Test if endpoint exists")
    public void testIsEndpointExist() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.isEndpointExist(ENDPOINT_NAME)).thenReturn(true);
        apiPublisher.isEndpointExist(ENDPOINT_NAME);

        //Error path
        Mockito.when(apiDAO.isEndpointExist(ENDPOINT_NAME)).thenThrow(APIMgtDAOException.class);
        try {
            apiPublisher.isEndpointExist(ENDPOINT_NAME);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't find existence of endpoint :" + ENDPOINT_NAME);
        }
    }

    @Test(description = "Test adding an API from WSDL file")
    public void testAddUpdateAPIFromWSDLFile() throws APIManagementException, LifecycleException, IOException {
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI()
                .endpoint(SampleTestObjectCreator.getMockEndpointMap());
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APILifecycleManager apiLifecycleManager = getDefaultMockedAPILifecycleManager();
        PolicyDAO policyDAO = getDefaultMockedPolicyDAO();
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        String endpointId = apiBuilder.getEndpoint().get("production").getId();
        Endpoint endpoint = new Endpoint.Builder().id(endpointId).name("testEndpoint").build();
        Mockito.when(apiDAO.getEndpoint(endpointId)).thenReturn(endpoint);
        apiPublisher
                .addAPIFromWSDLFile(apiBuilder, SampleTestObjectCreator.createDefaultWSDL11ContentInputStream(), true);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiDAO, Mockito.times(1))
                .addOrUpdateWSDL(apiBuilder.getId(),
                        IOUtils.toByteArray(SampleTestObjectCreator.createDefaultWSDL11ContentInputStream()), USER);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);

        apiPublisher
                .updateAPIWSDL(apiBuilder.getId(), SampleTestObjectCreator.createAlternativeWSDL11ContentInputStream());
        Mockito.verify(apiDAO, Mockito.times(1))
                .addOrUpdateWSDL(apiBuilder.getId(),
                        IOUtils.toByteArray(SampleTestObjectCreator.createAlternativeWSDL11ContentInputStream()), USER);
    }

    @Test(description = "Test adding an API from WSDL archive")
    public void testAddUpdateAPIFromWSDLArchive() throws APIManagementException, LifecycleException, IOException {
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI()
                .endpoint(SampleTestObjectCreator.getMockEndpointMap());
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APILifecycleManager apiLifecycleManager = getDefaultMockedAPILifecycleManager();
        PolicyDAO policyDAO = getDefaultMockedPolicyDAO();
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        String endpointId = apiBuilder.getEndpoint().get("production").getId();
        Endpoint endpoint = new Endpoint.Builder().id(endpointId).name("testEndpoint").build();
        Mockito.when(apiDAO.getEndpoint(endpointId)).thenReturn(endpoint);
        apiPublisher.addAPIFromWSDLArchive(apiBuilder, SampleTestObjectCreator.createDefaultWSDL11ArchiveInputStream(),
                true);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiDAO, Mockito.times(1))
                .addOrUpdateWSDLArchive(Mockito.eq(apiBuilder.getId()), Mockito.anyObject(), Mockito.eq(USER));

        apiPublisher.updateAPIWSDLArchive(apiBuilder.getId(),
                SampleTestObjectCreator.createAlternativeWSDL11ArchiveInputStream());
        Mockito.verify(apiDAO, Mockito.times(2))
                .addOrUpdateWSDLArchive(Mockito.eq(apiBuilder.getId()), Mockito.anyObject(), Mockito.eq(USER));
    }

    @Test(description = "Retrieve a WSDL of an API")
    public void testGetAPIWSDL() throws APIManagementException, IOException {
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Mockito.when(apiDAO.getWSDL(api.getId()))
                .thenReturn(new String(SampleTestObjectCreator.createDefaultWSDL11Content()));
        String updatedWSDL = apiPublisher.getAPIWSDL(api.getId());
        Assert.assertNotNull(updatedWSDL);
        Assert.assertTrue(updatedWSDL.contains(SampleTestObjectCreator.ORIGINAL_ENDPOINT_WEATHER));
    }

    @Test(description = "Retrieve a WSDL archive of an API")
    public void testGetAPIWSDLArchive() throws APIManagementException, IOException {
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisher apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getWSDLArchive(api.getId()))
                .thenReturn(SampleTestObjectCreator.createDefaultWSDL11ArchiveInputStream());
        InputStream inputStream = apiPublisher.getAPIWSDLArchive(api.getId());
        Assert.assertNotNull(inputStream);
    }

    @Test(description = "Save swagger definition for API")
    public void testSaveSwagger20Definition() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, gatewaySourceGenerator, gateway);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        apiPublisher.saveSwagger20Definition(uuid, SampleTestObjectCreator.apiDefinition);
        Mockito.verify(apiDAO, Mockito.times(1)).updateApiDefinition(uuid, SampleTestObjectCreator.apiDefinition, USER);
    }

    @Test(description = "Exception when saving swagger definition for API",
            expectedExceptions = APIManagementException.class)
    public void testSaveSwagger20DefinitionException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.doThrow(new APIMgtDAOException("Couldn't update the Swagger Definition")).when(apiDAO)
                .updateApiDefinition(uuid, SampleTestObjectCreator.apiDefinition, USER);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, gatewaySourceGenerator, gateway);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.DEVELOPER_ROLE_ID))
                .thenReturn(DEVELOPER_ROLE);
        Mockito.when(identityProvider.getRoleName(SampleTestObjectCreator.ADMIN_ROLE_ID)).thenReturn(ADMIN_ROLE);
        apiPublisher.saveSwagger20Definition(uuid, SampleTestObjectCreator.apiDefinition);
    }

    @Test(description = "Test getting all endpoints")
    public void testGetAllEndpoints() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        List<Endpoint> endpointList = new ArrayList<>();
        APIPublisher apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getEndpoints()).thenReturn(endpointList);
        apiPublisher.getAllEndpoints();
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpoints();

        //Error path
        Mockito.when(apiDAO.getEndpoints()).thenThrow(APIMgtDAOException.class);
        try {
            apiPublisher.getAllEndpoints();
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Failed to get all Endpoints");
        }
    }

    @Test(description = "Test getting endpoint")
    public void testGetEndpoint() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        String endpointId = endpoint.getId();
        APIPublisher apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getEndpoint(endpointId)).thenReturn(endpoint);
        apiPublisher.getEndpoint(endpointId);
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpoint(endpointId);

        //Error path
        Mockito.when(apiDAO.getEndpoint(endpointId)).thenThrow(APIMgtDAOException.class);
        try {
            apiPublisher.getEndpoint(endpointId);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Failed to get Endpoint : " + endpointId);
        }
    }

    @Test(description = "Test getting endpoint by name")
    public void testGetEndpointByName() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        String endpointName = endpoint.getName();
        APIPublisher apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getEndpointByName(endpointName)).thenReturn(endpoint);
        apiPublisher.getEndpointByName(endpointName);
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(endpointName);

        //Error path
        Mockito.when(apiDAO.getEndpointByName(endpointName)).thenThrow(APIMgtDAOException.class);
        try {
            apiPublisher.getEndpointByName(endpointName);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Failed to get Endpoint : " + endpointName);
        }
    }

    @Test(description = "Event Observers registration and removal")
    public void testObserverRegistration() throws APIManagementException {

        EventLogger observer = new EventLogger();

        APIPublisherImpl apiPub = getApiPublisherImpl();

        apiPub.registerObserver(new EventLogger());

        Map<String, EventObserver> observers = apiPub.getEventObservers();
        Assert.assertEquals(observers.size(), 1);

        apiPub.removeObserver(observers.get(observer.getClass().getName()));

        Assert.assertEquals(observers.size(), 0);

    }

    @Test(description = "Event Observers for event listening")
    public void testObserverEventListener() throws APIManagementException {

        EventLogger observer = Mockito.mock(EventLogger.class);

        APIPublisherImpl apiPub = getApiPublisherImpl();
        apiPub.registerObserver(observer);

        Event event = Event.APP_CREATION;
        String username = USER;
        Map<String, String> metaData = new HashMap<>();
        ZonedDateTime eventTime = ZonedDateTime.now(ZoneOffset.UTC);
        apiPub.notifyObservers(event, username, eventTime, metaData);

        Mockito.verify(observer, Mockito.times(1)).captureEvent(event, username, eventTime, metaData);

    }

    @Test(description = "Add api from definition")
    public void testAddApiFromDefinition() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        String def = SampleTestObjectCreator.apiDefinition;
        InputStream apiDefinition = new ByteArrayInputStream(def.getBytes());
        apiPublisher.addApiFromDefinition(apiDefinition);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "Add api from definition using httpUrlConnection")
    public void testAddApiFromDefinitionFromUrlConnection()
            throws APIManagementException, LifecycleException, IOException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        HttpURLConnection httpURLConnection = Mockito.mock(HttpURLConnection.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        String def = SampleTestObjectCreator.apiDefinition;
        InputStream apiDefinition = new ByteArrayInputStream(def.getBytes());
        Mockito.when(httpURLConnection.getInputStream()).thenReturn(apiDefinition);
        Mockito.when(httpURLConnection.getResponseCode()).thenReturn(200);
        apiPublisher.addApiFromDefinition(httpURLConnection);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "Couldn't retrieve swagger definition for api when getting api gateway config",
            expectedExceptions = APIManagementException.class)
    public void testGetApiGatewayConfigException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO);
        Mockito.when(apiDAO.getGatewayConfigOfAPI(API_ID))
                .thenThrow(new APIMgtDAOException("Error generating swagger from gateway config " + API_ID));
        apiPublisher.getApiGatewayConfig(API_ID);
    }

    @Test(description = "Response not 200 when getting swagger resource from url when adding api from swagger resource",
            expectedExceptions = APIManagementException.class)
    public void testAddApiDefinitionErrorGettingSwaggerResource()
            throws APIManagementException, LifecycleException, IOException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        HttpURLConnection httpURLConnection = Mockito.mock(HttpURLConnection.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        Mockito.when(httpURLConnection.getResponseCode()).thenReturn(400);
        apiPublisher.addApiFromDefinition(httpURLConnection);
        Mockito.verify(apiLifecycleManager, Mockito.times(0)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "Test protocol exception when adding api definition from swagger resource",
            expectedExceptions = APIManagementException.class)
    public void testAddApiDefinitionErrorProtocolException()
            throws APIManagementException, LifecycleException, IOException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        HttpURLConnection httpURLConnection = Mockito.mock(HttpURLConnection.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        Mockito.doThrow(new ProtocolException()).when(httpURLConnection).setRequestMethod(APIMgtConstants.HTTP_GET);
        apiPublisher.addApiFromDefinition(httpURLConnection);
        Mockito.verify(apiLifecycleManager, Mockito.times(0)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "Test IO exception when adding api definition from swagger resource",
            expectedExceptions = APIManagementException.class)
    public void testAddApiDefinitionErrorIOException() throws APIManagementException, LifecycleException, IOException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        HttpURLConnection httpURLConnection = Mockito.mock(HttpURLConnection.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gateway);
        Mockito.doThrow(new IOException()).when(httpURLConnection).getResponseCode();
        apiPublisher.addApiFromDefinition(httpURLConnection);
        Mockito.verify(apiLifecycleManager, Mockito.times(0)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
    }

    @Test(description = "SwaggerDefinition get for api")
    public void testGetApiDefinition() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        String uuid = UUID.randomUUID().toString();
        Mockito.when(apiDAO.getApiSwaggerDefinition(uuid)).thenReturn(SampleTestObjectCreator.apiDefinition);
        String returnedSwagger = getApiPublisherImpl(apiDAO).getApiSwaggerDefinition(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getApiSwaggerDefinition(uuid);
    }

    @Test(description = "Endpoint add")
    public void testAddEndpoint() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        String id = getApiPublisherImpl(apiDAO, gatewaySourceGenerator, gateway)
                .addEndpoint(SampleTestObjectCreator.createMockEndpoint());
        Endpoint endpoint = new Endpoint.Builder(SampleTestObjectCreator.createMockEndpoint()).id(id)
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build();
        Mockito.verify(apiDAO, Mockito.times(1)).addEndpoint(endpoint);
    }

    @Test(description = "Add endpoint when endpoint name is null")
    public void testAddEndpointWhenNameNull() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, gatewaySourceGenerator, gateway);
        try {
            apiPublisher
                    .addEndpoint(new Endpoint.Builder(SampleTestObjectCreator.createMockEndpoint()).name(null).build());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Endpoint name is not provided");
        }
    }

    @Test(description = "Add endpoint when endpoint name is empty")
    public void testAddEndpointWhenNameEmpty() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, gatewaySourceGenerator, gateway);
        try {
            apiPublisher
                    .addEndpoint(new Endpoint.Builder(SampleTestObjectCreator.createMockEndpoint()).name("").build());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Endpoint name is not provided");
        }
    }

    @Test(description = "Add endpoint when endpoint name already exist")
    public void testAddEndpointWhenEndpointExist() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, gatewaySourceGenerator, gateway);
        Endpoint endpointToAdd = new Endpoint.Builder(SampleTestObjectCreator.createMockEndpoint()).build();
        Mockito.when(apiDAO.getEndpointByName(endpointToAdd.getName())).thenReturn(endpointToAdd);
        try {
            apiPublisher.addEndpoint(endpointToAdd);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Endpoint already exist with name " + endpointToAdd.getName());
        }

    }

    @Test(description = "Exception when adding endpoint")
    public void testAddEndpointException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, gatewaySourceGenerator, gateway);
        Endpoint endpointToAdd = new Endpoint.Builder(SampleTestObjectCreator.createMockEndpoint()).build();
        Mockito.when(apiDAO.getEndpointByName(endpointToAdd.getName())).thenReturn(null);
        Mockito.doThrow(APIMgtDAOException.class).when(apiDAO).addEndpoint(Mockito.any());
        try {
            apiPublisher.addEndpoint(endpointToAdd);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Failed to add Endpoint : " + endpointToAdd.getName());
        }
    }

    @Test(description = "Update endpoint")
    public void testUpdateEndpoint() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);

        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, gatewaySourceGenerator, gateway);
        Endpoint endpoint = new Endpoint.Builder(SampleTestObjectCreator.createMockEndpoint()).build();
        apiPublisher.updateEndpoint(endpoint);
        Mockito.verify(apiDAO, Mockito.times(1)).updateEndpoint(endpoint);

        //Error path
        Mockito.when(apiDAO.updateEndpoint(endpoint)).thenThrow(APIMgtDAOException.class);
        try {
            apiPublisher.updateEndpoint(endpoint);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Failed to update Endpoint : " + endpoint.getName());
        }
    }

    @Test(description = "Delete endpoint")
    public void testDeleteEndpoint() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, gatewaySourceGenerator, gateway);
        Endpoint endpoint = new Endpoint.Builder(SampleTestObjectCreator.createMockEndpoint()).build();
        Mockito.when(apiDAO.isEndpointAssociated(endpoint.getId())).thenReturn(false);
        apiPublisher.deleteEndpoint(endpoint.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).deleteEndpoint(endpoint.getId());

        //Error path - APIMgtDAOException
        Mockito.when(apiDAO.deleteEndpoint(endpoint.getId())).thenThrow(APIMgtDAOException.class);
        try {
            apiPublisher.deleteEndpoint(endpoint.getId());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Failed to delete Endpoint : " + endpoint.getId());
        }

        //Error path - Endpoint already associated with an API
        Mockito.when(apiDAO.isEndpointAssociated(endpoint.getId())).thenReturn(true);
        try {
            apiPublisher.deleteEndpoint(endpoint.getId());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Endpoint Already Have Associated With API");
        }
    }

    @Test(description = "Test add api with Api Specific Endpoint",
            expectedExceptions = { APIManagementException.class })
    public void testAddApiSpecificEndpoint() throws APIManagementException, LifecycleException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Endpoint globalEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("testEndpoint")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build();
        Endpoint apiEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("apiEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, globalEndpoint);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, apiEndpoint);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().endpoint(endpointMap);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy apiPolicy = new APIPolicy(UUID.randomUUID().toString(), APIMgtConstants.DEFAULT_API_POLICY);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(apiPolicy);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        Mockito.when(apiDAO.getEndpoint(globalEndpoint.getId())).thenReturn(globalEndpoint);
        Mockito.when(apiDAO.getEndpointByName(apiEndpoint.getName())).thenReturn(null);
        Mockito.when(apiDAO.isAPINameExists(apiBuilder.getName(), USER)).thenReturn(false);
        Mockito.when(apiDAO.isEndpointAssociated(globalEndpoint.getId())).thenReturn(true);
        apiPublisher.addAPI(apiBuilder);
        apiPublisher.deleteEndpoint(globalEndpoint.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(apiEndpoint.getName());
        Mockito.verify(apiDAO, Mockito.times(1)).isAPINameExists(apiBuilder.getName(), USER);
    }

    @Test(description = "Test add api with Api Specific Endpoint",
            expectedExceptions = { APIManagementException.class })
    public void testAddAlreadyAddedEndpointToApi() throws APIManagementException, LifecycleException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Endpoint globalEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("testEndpoint")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build();
        Endpoint apiEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("apiEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, globalEndpoint);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, apiEndpoint);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().endpoint(endpointMap);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway);
        Mockito.when(apiDAO.getEndpoint(globalEndpoint.getId())).thenReturn(globalEndpoint);
        Mockito.when(apiDAO.getEndpointByName(apiEndpoint.getName())).thenReturn(apiEndpoint);
        Mockito.when(apiDAO.isAPINameExists(apiBuilder.getName(), USER)).thenReturn(false);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, USER);
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(apiEndpoint.getName());
        Mockito.verify(apiDAO, Mockito.times(1)).isAPINameExists(apiBuilder.getName(), USER);
    }

    @Test(description = "Test add api with Api Specific Endpoint",
            expectedExceptions = { APIManagementException.class })
    public void testApiLevelEndpointAddWhileDbGetError() throws APIManagementException, LifecycleException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Endpoint globalEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("testEndpoint")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build();
        Endpoint apiEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("apiEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, globalEndpoint);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, apiEndpoint);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().endpoint(endpointMap);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway);
        Mockito.when(apiDAO.getEndpoint(globalEndpoint.getId())).thenReturn(globalEndpoint);
        Mockito.when(apiDAO.getEndpointByName(apiEndpoint.getName())).thenThrow(APIMgtDAOException.class);
        Mockito.when(apiDAO.isAPINameExists(apiBuilder.getName(), USER)).thenReturn(false);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(apiEndpoint.getName());
    }

    @Test(description = "Test add api with Api Specific Endpoint")
    public void testAddResourceLevelEndpoint() throws APIManagementException, LifecycleException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Endpoint globalEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("testEndpoint")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build();
        Endpoint apiEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("apiEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint resourceEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("resourceEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, globalEndpoint);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, apiEndpoint);
        Map<String, Endpoint> resourceEndpoints = new HashMap();
        resourceEndpoints.put(APIMgtConstants.SANDBOX_ENDPOINT, resourceEndpoint);
        Map<String, UriTemplate> uriTemplateMap = SampleTestObjectCreator.getMockUriTemplates();
        uriTemplateMap.forEach((k, v) -> {
            UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder(v);
            uriTemplateBuilder.endpoint(resourceEndpoints);
            uriTemplateMap.replace(k, uriTemplateBuilder.build());
        });
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().endpoint(endpointMap)
                .uriTemplates(uriTemplateMap);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        Mockito.when(apiDAO.getEndpoint(globalEndpoint.getId())).thenReturn(globalEndpoint);
        Mockito.when(apiDAO.getEndpointByName(apiEndpoint.getName())).thenReturn(null);
        Mockito.when(apiDAO.getEndpointByName(resourceEndpoint.getName())).thenReturn(null);
        Mockito.when(apiDAO.isAPINameExists(apiBuilder.getName(), USER)).thenReturn(false);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(apiEndpoint.getName());
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(resourceEndpoint.getName());

    }

    @Test(description = "Test add api with Api Specific Endpoint",
            expectedExceptions = { APIManagementException.class })
    public void testAddResourceLevelEndpointWhileResourceEndpointAlreadyExists()
            throws APIManagementException, LifecycleException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Endpoint globalEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("testEndpoint")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build();
        Endpoint apiEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("apiEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint resourceEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("resourceEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, globalEndpoint);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, apiEndpoint);
        Map<String, Endpoint> resourceEndpoints = new HashMap();
        resourceEndpoints.put(APIMgtConstants.SANDBOX_ENDPOINT, resourceEndpoint);
        Map<String, UriTemplate> uriTemplateMap = SampleTestObjectCreator.getMockUriTemplates();
        uriTemplateMap.forEach((k, v) -> {
            UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder(v);
            uriTemplateBuilder.endpoint(resourceEndpoints);
            uriTemplateMap.replace(k, uriTemplateBuilder.build());
        });
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().endpoint(endpointMap)
                .uriTemplates(uriTemplateMap);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway);
        Mockito.when(apiDAO.getEndpoint(globalEndpoint.getId())).thenReturn(globalEndpoint);
        Mockito.when(apiDAO.getEndpointByName(apiEndpoint.getName())).thenReturn(null);
        Mockito.when(apiDAO.getEndpointByName(resourceEndpoint.getName())).thenReturn(resourceEndpoint);
        Mockito.when(apiDAO.isAPINameExists(apiBuilder.getName(), USER)).thenReturn(false);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(apiEndpoint.getName());
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(resourceEndpoint.getName());
    }

    @Test(description = "Test add api with Api Specific Endpoint",
            expectedExceptions = { APIManagementException.class })
    public void testAddResourceLevelEndpointWhileResourceEndpointAlreadyExistsWhileDatabaseFailure()
            throws APIManagementException, LifecycleException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Endpoint globalEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("testEndpoint")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build();
        Endpoint apiEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("apiEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint resourceEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("resourceEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, globalEndpoint);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, apiEndpoint);
        Map<String, Endpoint> resourceEndpoints = new HashMap();
        resourceEndpoints.put(APIMgtConstants.SANDBOX_ENDPOINT, resourceEndpoint);
        Map<String, UriTemplate> uriTemplateMap = SampleTestObjectCreator.getMockUriTemplates();
        uriTemplateMap.forEach((k, v) -> {
            UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder(v);
            uriTemplateBuilder.endpoint(resourceEndpoints);
            uriTemplateMap.replace(k, uriTemplateBuilder.build());
        });
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().endpoint(endpointMap)
                .uriTemplates(uriTemplateMap);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway);
        Mockito.when(apiDAO.getEndpoint(globalEndpoint.getId())).thenReturn(globalEndpoint);
        Mockito.when(apiDAO.getEndpointByName(apiEndpoint.getName())).thenReturn(null);
        Mockito.when(apiDAO.getEndpointByName(resourceEndpoint.getName())).thenThrow(APIMgtDAOException.class);
        Mockito.when(apiDAO.isAPINameExists(apiBuilder.getName(), USER)).thenReturn(false);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(apiEndpoint.getName());
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(resourceEndpoint.getName());
    }

    @Test(description = "Test add api with Api Specific Endpoint")
    public void testResourceProductionAndSandboxEndpoint() throws APIManagementException, LifecycleException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Endpoint globalEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("testEndpoint")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build();
        Endpoint apiEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("apiEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint resourceEndpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("resourceEndpoint")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint resourceEndpoint1 = new Endpoint.Builder().id(UUID.randomUUID().toString()).name("resourceEndpoint1")
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, globalEndpoint);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, apiEndpoint);
        Map<String, Endpoint> resourceEndpoints = new HashMap();
        resourceEndpoints.put(APIMgtConstants.SANDBOX_ENDPOINT, resourceEndpoint);
        resourceEndpoints.put(APIMgtConstants.PRODUCTION_ENDPOINT, resourceEndpoint1);
        Map<String, UriTemplate> uriTemplateMap = SampleTestObjectCreator.getMockUriTemplates();
        uriTemplateMap.forEach((k, v) -> {
            UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder(v);
            uriTemplateBuilder.endpoint(resourceEndpoints);
            uriTemplateMap.replace(k, uriTemplateBuilder.build());
        });
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().endpoint(endpointMap)
                .uriTemplates(uriTemplateMap);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        Mockito.when(apiDAO.getEndpoint(globalEndpoint.getId())).thenReturn(globalEndpoint);
        Mockito.when(apiDAO.getEndpointByName(apiEndpoint.getName())).thenReturn(null);
        Mockito.when(apiDAO.getEndpointByName(resourceEndpoint.getName())).thenReturn(null);
        Mockito.when(apiDAO.isAPINameExists(apiBuilder.getName(), USER)).thenReturn(false);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(apiEndpoint.getName());
        Mockito.verify(apiDAO, Mockito.times(1)).getEndpointByName(resourceEndpoint.getName());
    }

    @Test(description = "Test add api with production endpoint")
    public void testUpdateDescription() throws APIManagementException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        Map<String, Endpoint> endpointMap = SampleTestObjectCreator.getMockEndpointMap();
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id(UUID.randomUUID().toString())
                .endpoint(endpointMap);
        apiBuilder.apiPermission("");
        apiBuilder.permissionMap(null);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        Mockito.when(apiDAO.getAPI(apiBuilder.getId())).thenReturn(apiBuilder.build());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(identityProvider, apiDAO, apiLifecycleManager,
                gatewaySourceGenerator, gateway, policyDAO);
        Mockito.when(apiDAO.getEndpoint(endpointMap.get(APIMgtConstants.PRODUCTION_ENDPOINT).getId()))
                .thenReturn(endpointMap.get(APIMgtConstants.PRODUCTION_ENDPOINT));
        Mockito.when(apiDAO.getEndpoint(endpointMap.get(APIMgtConstants.SANDBOX_ENDPOINT).getId()))
                .thenReturn(endpointMap.get(APIMgtConstants.SANDBOX_ENDPOINT));
        Mockito.when(apiDAO.getEndpointByName(endpointMap.get(APIMgtConstants.PRODUCTION_ENDPOINT).getName()))
                .thenReturn(endpointMap.get(APIMgtConstants.PRODUCTION_ENDPOINT));
        Mockito.when(apiDAO.getEndpointByName(endpointMap.get(APIMgtConstants.SANDBOX_ENDPOINT).getName()))
                .thenReturn(endpointMap.get(APIMgtConstants.SANDBOX_ENDPOINT));
        apiBuilder.description("aaaaaa");
        apiPublisher.updateAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(apiBuilder.getId(), apiBuilder.build());
    }

    @Test(description = "Test add api with production endpoint")
    public void testUpdateApiEndpointWithNewApiLevel() throws APIManagementException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        Endpoint endpoint1 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint1").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint endpoint2 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint2").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint endpoint3 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint3").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap<>();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, endpoint1);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, endpoint2);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id(UUID.randomUUID().toString())
                .endpoint(endpointMap);
        apiBuilder.apiPermission("");
        apiBuilder.permissionMap(null);
        apiBuilder.policies(Collections.emptySet());
        apiBuilder.apiPolicy(null);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        Mockito.when(apiDAO.getAPI(apiBuilder.getId())).thenReturn(apiBuilder.build());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getId())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpoint(endpoint2.getId())).thenReturn(endpoint2);
        Mockito.when(apiDAO.getEndpoint(endpoint3.getId())).thenReturn(null);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getName())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpointByName(endpoint2.getName())).thenReturn(endpoint2);
        Map<String, Endpoint> updatedEndpointMap = new HashMap<>(endpointMap);
        updatedEndpointMap.replace(APIMgtConstants.SANDBOX_ENDPOINT, endpoint3);
        apiBuilder.endpoint(updatedEndpointMap);
        apiPublisher.updateAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(apiBuilder.getId(), apiBuilder.build());
    }

    @Test(description = "Test add api with production endpoint", expectedExceptions = { APIManagementException.class },
            expectedExceptionsMessageRegExp = "Endpoint Already Exist By Name : endpoint2")
    public void testUpdateApiEndpointWithAlreadyAvailableEndpointName() throws APIManagementException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        Endpoint endpoint1 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint1").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint endpoint2 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint2").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint endpoint3 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint2").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap<>();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, endpoint1);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, endpoint2);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id(UUID.randomUUID().toString())
                .endpoint(endpointMap);
        apiBuilder.apiPermission("");
        apiBuilder.permissionMap(null);
        apiBuilder.policies(Collections.emptySet());
        apiBuilder.apiPolicy(null);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        Mockito.when(apiDAO.getAPI(apiBuilder.getId())).thenReturn(apiBuilder.build());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getId())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpoint(endpoint2.getId())).thenReturn(endpoint2);
        Mockito.when(apiDAO.getEndpoint(endpoint3.getId())).thenReturn(null);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getName())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpointByName(endpoint2.getName())).thenReturn(endpoint2);
        Map<String, Endpoint> updatedEndpointMap = new HashMap<>(endpointMap);
        updatedEndpointMap.replace(APIMgtConstants.SANDBOX_ENDPOINT, endpoint3);
        apiBuilder.endpoint(updatedEndpointMap);
        apiPublisher.updateAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(apiBuilder.getId(), apiBuilder.build());
    }

    @Test(description = "Test add api with production endpoint")
    public void testUpdateApiEndpointName() throws APIManagementException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        Endpoint endpoint1 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint1").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint endpoint2 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint2").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap<>();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, endpoint1);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, endpoint2);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id(UUID.randomUUID().toString())
                .endpoint(endpointMap);
        apiBuilder.apiPermission("");
        apiBuilder.permissionMap(null);
        apiBuilder.policies(Collections.emptySet());
        apiBuilder.apiPolicy(null);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        Mockito.when(apiDAO.getAPI(apiBuilder.getId())).thenReturn(apiBuilder.build());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getId())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpoint(endpoint2.getId())).thenReturn(endpoint2);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getName())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpointByName(endpoint2.getName())).thenReturn(endpoint2);
        Endpoint endpoint3 = new Endpoint.Builder(endpoint2).name("endpoint3").build();
        Map<String, Endpoint> updatedEndpointMap = new HashMap<>(endpointMap);
        updatedEndpointMap.replace(APIMgtConstants.SANDBOX_ENDPOINT, endpoint3);
        apiBuilder.endpoint(updatedEndpointMap);
        apiPublisher.updateAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(apiBuilder.getId(), apiBuilder.build());
    }

    @Test(description = "Test add api with production endpoint", expectedExceptions = { APIManagementException.class },
            expectedExceptionsMessageRegExp = "Endpoint Already Exist By Name : endpoint3")
    public void testUpdateApiEndpointNameWithAlreadyExistingName() throws APIManagementException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        Endpoint endpoint1 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint1").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint endpoint2 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint2").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap<>();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, endpoint1);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, endpoint2);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id(UUID.randomUUID().toString())
                .endpoint(endpointMap);
        apiBuilder.apiPermission("");
        apiBuilder.permissionMap(null);
        apiBuilder.policies(Collections.emptySet());
        apiBuilder.apiPolicy(null);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        Mockito.when(apiDAO.getAPI(apiBuilder.getId())).thenReturn(apiBuilder.build());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getId())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpoint(endpoint2.getId())).thenReturn(endpoint2);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getName())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpointByName(endpoint2.getName())).thenReturn(endpoint2);
        Endpoint endpoint3 = new Endpoint.Builder(endpoint2).name("endpoint3").build();
        Mockito.when(apiDAO.getEndpointByName(endpoint3.getName())).thenReturn(endpoint3);
        Map<String, Endpoint> updatedEndpointMap = new HashMap<>(endpointMap);
        updatedEndpointMap.replace(APIMgtConstants.SANDBOX_ENDPOINT, endpoint3);
        apiBuilder.endpoint(updatedEndpointMap);
        apiPublisher.updateAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(apiBuilder.getId(), apiBuilder.build());
    }

    @Test(description = "Test add api with production endpoint")
    public void testUpdateApiEndpointUrl() throws APIManagementException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        Endpoint endpoint1 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint1").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint endpoint2 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint2").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap<>();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, endpoint1);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, endpoint2);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id(UUID.randomUUID().toString())
                .endpoint(endpointMap);
        apiBuilder.apiPermission("");
        apiBuilder.permissionMap(null);
        apiBuilder.policies(Collections.emptySet());
        apiBuilder.apiPolicy(null);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        Mockito.when(apiDAO.getAPI(apiBuilder.getId())).thenReturn(apiBuilder.build());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getId())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpoint(endpoint2.getId())).thenReturn(endpoint2);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getName())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpointByName(endpoint2.getName())).thenReturn(endpoint2);
        Endpoint endpoint3 = new Endpoint.Builder(endpoint2).maxTps(1200).build();
        Map<String, Endpoint> updatedEndpointMap = new HashMap<>(endpointMap);
        updatedEndpointMap.replace(APIMgtConstants.SANDBOX_ENDPOINT, endpoint3);
        apiBuilder.endpoint(updatedEndpointMap);
        apiPublisher.updateAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(apiBuilder.getId(), apiBuilder.build());
    }

    @Test(description = "Test add api with production endpoint")
    public void testUpdateApiEndpointOfUriTemplate() throws APIManagementException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        Endpoint endpoint1 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint1").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint endpoint2 = new Endpoint.Builder().id(UUID.randomUUID().toString()).endpointConfig("http://localhost")
                .name("endpoint2").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Map<String, Endpoint> endpointMap = new HashMap<>();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, endpoint1);
        endpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, endpoint2);
        Map<String, UriTemplate> uriTemplateMap = SampleTestObjectCreator.getMockUriTemplates();
        uriTemplateMap.forEach((s, uriTemplate) -> uriTemplateMap
                .replace(s, new UriTemplate.UriTemplateBuilder(uriTemplate).endpoint(endpointMap).build()));
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id(UUID.randomUUID().toString())
                .endpoint(Collections.emptyMap()).uriTemplates(uriTemplateMap);
        apiBuilder.apiPermission("");
        apiBuilder.permissionMap(null);
        apiBuilder.policies(Collections.emptySet());
        apiBuilder.apiPolicy(null);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        Mockito.when(apiDAO.getAPI(apiBuilder.getId())).thenReturn(apiBuilder.build());
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIGateway gateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        APIPublisherImpl apiPublisher = getApiPublisherImpl(apiDAO, apiLifecycleManager, gatewaySourceGenerator,
                gateway, policyDAO);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getId())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpoint(endpoint2.getId())).thenReturn(endpoint2);
        Mockito.when(apiDAO.getEndpoint(endpoint1.getName())).thenReturn(endpoint1);
        Mockito.when(apiDAO.getEndpointByName(endpoint2.getName())).thenReturn(endpoint2);
        apiPublisher.updateAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(apiBuilder.getId(), apiBuilder.build());
    }

    private PolicyDAO getDefaultMockedPolicyDAO() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api,
                APIMgtConstants.DEFAULT_API_POLICY)).thenReturn(new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, GOLD_TIER))
                .thenReturn(new SubscriptionPolicy(GOLD_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, SILVER_TIER))
                .thenReturn(new SubscriptionPolicy(SILVER_TIER));
        Mockito.when(
                policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, BRONZE_TIER))
                .thenReturn(new SubscriptionPolicy(BRONZE_TIER));
        return policyDAO;
    }

    private APILifecycleManager getDefaultMockedAPILifecycleManager() throws LifecycleException {
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, USER))
                .thenReturn(new LifecycleState());
        return apiLifecycleManager;
    }

    private APIPublisherImpl getApiPublisherImpl() {
        return new APIPublisherImpl(USER, null, null, null, null, null, null, null, null, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, APILifecycleManager apiLifecycleManager,
            APIGateway apiGatewayPublisher) {
        return new APIPublisherImpl(USER, null, null, apiDAO, null, null, null, apiLifecycleManager, null, null, null,
                new GatewaySourceGeneratorImpl(), apiGatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(IdentityProvider identityProvider, ApiDAO apiDAO,
            APILifecycleManager apiLifecycleManager, APIGateway apiGatewayPublisher) {
        return new APIPublisherImpl(USER, identityProvider, null, apiDAO, null, null, null, apiLifecycleManager,
                null, null, null, new GatewaySourceGeneratorImpl(), apiGatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO) {
        return new APIPublisherImpl(USER, null, null, apiDAO, null, null, null, null, null, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(String user, IdentityProvider identityProvider, ApiDAO apiDAO) {
        return new APIPublisherImpl(user, identityProvider, null, apiDAO, null, null, null, null, null, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, APISubscriptionDAO apiSubscriptionDAO,
                                                 APILifecycleManager apiLifecycleManager) {
        return new APIPublisherImpl(USER, null, null, apiDAO, null, apiSubscriptionDAO, null, apiLifecycleManager,
                null, null, null, new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(IdentityProvider identityProvider, ApiDAO apiDAO,
            APISubscriptionDAO apiSubscriptionDAO, APILifecycleManager apiLifecycleManager) {
        return new APIPublisherImpl(USER, identityProvider, null, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null, null, null, new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, APISubscriptionDAO apiSubscriptionDAO,
                                                 APILifecycleManager apiLifecycleManager, APIGateway
                                                         apiGatewayPublisher) {
        return new APIPublisherImpl(USER, null, null, apiDAO, null, apiSubscriptionDAO, null, apiLifecycleManager,
                null, null, null, new GatewaySourceGeneratorImpl(), apiGatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, APISubscriptionDAO apiSubscriptionDAO,
                                                 APILifecycleManager apiLifecycleManager, WorkflowDAO workfloDAO,
                                                 APIGateway apiGatewayPublisher) {
        return new APIPublisherImpl(USER, null, null, apiDAO, null, apiSubscriptionDAO, null, apiLifecycleManager, null,
                workfloDAO, null, new GatewaySourceGeneratorImpl(), apiGatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(String user, IdentityProvider identityProvider, ApiDAO apiDAO,
                                                 APISubscriptionDAO apiSubscriptionDAO,
                                                 APILifecycleManager apiLifecycleManager,
                                                 APIGateway apiGatewayPublisher) {
        return new APIPublisherImpl(user, identityProvider, null, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null, null, null, new GatewaySourceGeneratorImpl(), apiGatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(IdentityProvider identityProvider, ApiDAO apiDAO,
            APISubscriptionDAO apiSubscriptionDAO, APILifecycleManager apiLifecycleManager, WorkflowDAO workfloDAO,
            APIGateway apiGatewayPublisher) {
        return new APIPublisherImpl(USER, identityProvider, null, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null, workfloDAO, null, new GatewaySourceGeneratorImpl(), apiGatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, APISubscriptionDAO apiSubscriptionDAO) {
        return new APIPublisherImpl(USER, null, null, apiDAO, null, apiSubscriptionDAO, null, null, null, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(LabelDAO labelDAO) {
        return new APIPublisherImpl(USER, null, null, null, null, null, null, null, labelDAO, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(PolicyDAO policyDAO) {
        return new APIPublisherImpl(USER, null, null, null, null, null, policyDAO, null, null, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(APISubscriptionDAO apiSubscriptionDAO) {
        return new APIPublisherImpl(USER, null, null, null, null, apiSubscriptionDAO, null, null, null, null, null,
                new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(APISubscriptionDAO apiSubscriptionDAO, APIGateway gatewayPublisher) {
        return new APIPublisherImpl(USER, null, null, null, null, apiSubscriptionDAO, null, null, null, null, null,
                new GatewaySourceGeneratorImpl(), gatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, APILifecycleManager apiLifecycleManager,
                                                 GatewaySourceGenerator gatewaySourceGenerator,
                                                 APIGateway gatewayPublisher) {
        return new APIPublisherImpl(USER, null,  null, apiDAO, null, null, null, apiLifecycleManager, null, null, null,
                gatewaySourceGenerator, gatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, GatewaySourceGenerator gatewaySourceGenerator,
                                                 APIGateway apiGatewayPublisher) {
        return new APIPublisherImpl(USER, null,  null, apiDAO, null, null, null, null, null, null, null,
                gatewaySourceGenerator, apiGatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO, APILifecycleManager apiLifecycleManager, GatewaySourceGenerator
                                                         gatewaySourceGenerator) {
        return new APIPublisherImpl(USER, null, null, apiDAO, applicationDAO, apiSubscriptionDAO, null,
                apiLifecycleManager, null, null, null, gatewaySourceGenerator, new APIGatewayPublisherImpl());
    }

    private APIPublisherImpl getApiPublisherImpl(String user, ApiDAO apiDAO, APILifecycleManager apiLifecycleManager,
                                                 GatewaySourceGenerator gatewaySourceGenerator,
                                                 APIGateway apiGatewayPublisher) {
        return new APIPublisherImpl(user, null,  null, apiDAO, null, null, null, apiLifecycleManager, null, null, null,
                gatewaySourceGenerator, apiGatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(APILifecycleManager apiLifecycleManager, ApiDAO apiDAO, WorkflowDAO
            workflowDAO, APIGateway apiGatewayPublisher) {
        return new APIPublisherImpl(USER, null,  null, apiDAO, null, null, null, apiLifecycleManager, null, workflowDAO
                , null, new GatewaySourceGeneratorImpl(), apiGatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO, APILifecycleManager apiLifecycleManager, GatewaySourceGenerator
                                                         gatewaySourceGenerator, WorkflowDAO workflowDAO,
                                                 APIGateway apiGatewayPublisher) {
        return new APIPublisherImpl(USER, null,  null, apiDAO, applicationDAO, apiSubscriptionDAO, null,
                apiLifecycleManager, null, workflowDAO, null, gatewaySourceGenerator, apiGatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(IdentityProvider idp, ApiDAO apiDAO,
                                                 APILifecycleManager apiLifecycleManager, GatewaySourceGenerator
                                                         gatewaySourceGenerator,
                                                 APIGateway gatewayPublisher) {
        return new APIPublisherImpl(USER, idp, null, apiDAO, null, null, null, apiLifecycleManager, null, null, null,
                gatewaySourceGenerator, gatewayPublisher);
    }

    private APIPublisherImpl getApiPublisherImpl(ApiDAO apiDAO, APILifecycleManager apiLifecycleManager,
                                                 GatewaySourceGenerator gatewaySourceGenerator,
                                                 APIGateway gateway, PolicyDAO policyDAO) {
        return new APIPublisherImpl(USER, null, null, apiDAO, null, null, policyDAO, apiLifecycleManager, null,
                null, null, gatewaySourceGenerator, gateway);
    }

    private APIPublisherImpl getApiPublisherImpl(IdentityProvider identityProvider, ApiDAO apiDAO, APILifecycleManager
            apiLifecycleManager, GatewaySourceGenerator gatewaySourceGenerator, APIGateway gateway,
                                                 PolicyDAO policyDAO) {
        return new APIPublisherImpl(USER, identityProvider, null, apiDAO, null, null, policyDAO, apiLifecycleManager,
                null, null, null, gatewaySourceGenerator, gateway);
    }

    private APIPublisherImpl getApiPublisherImpl(IdentityProvider identityProvider, ApiDAO apiDAO,
                                                 GatewaySourceGenerator gatewaySourceGenerator, APIGateway gateway) {
        return new APIPublisherImpl(USER, identityProvider, null, apiDAO, null, null, null, null,
                null, null, null, gatewaySourceGenerator, gateway);
    }

    private APIPublisherImpl getApiPublisherImpl(String user, IdentityProvider identityProvider, ApiDAO apiDAO,
                                                 APILifecycleManager apiLifecycleManager,
                                                 GatewaySourceGenerator gatewaySourceGenerator, APIGateway gateway) {
        return new APIPublisherImpl(user, identityProvider, null, apiDAO, null, null, null, apiLifecycleManager,
                null, null, null, gatewaySourceGenerator, gateway);
    }
}
