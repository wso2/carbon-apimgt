/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowStatus;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ApplicationUtils;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest({WorkflowExecutorFactory.class, APIUtil.class, GovernanceUtils.class, ApplicationUtils.class,
        KeyManagerHolder.class})
@SuppressStaticInitializationFor("org.wso2.carbon.apimgt.impl.utils.ApplicationUtils")
public class APIConsumerImplTest {

    private static final Log log = LogFactory.getLog(APIConsumerImplTest.class);

    @Test
    public void testReadMonetizationConfig() throws UserStoreException, RegistryException,
            APIManagementException {

        APIMRegistryService apimRegistryService = Mockito.mock(APIMRegistryService.class);
        String json = "{\"EnableMonetization\":\"true\"}";
        when(apimRegistryService.getConfigRegistryResourceContent(Mockito.anyString(), Mockito.anyString())).thenReturn(json);
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        apiConsumer.apimRegistryService = apimRegistryService;
        boolean isEnabled = apiConsumer.isMonetizationEnabled(MultitenantConstants.TENANT_DOMAIN);
        assertTrue("Expected true but returned " + isEnabled, isEnabled);
        Mockito.reset(apimRegistryService);

        // error path UserStoreException
        when(apimRegistryService.getConfigRegistryResourceContent(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(UserStoreException.class);
        try {
            apiConsumer.isMonetizationEnabled(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            assertFalse(true);
        } catch (APIManagementException e) {
            assertEquals("UserStoreException thrown when getting API tenant config from registry", e.getMessage());
        }

        // error path apimRegistryService
        Mockito.reset(apimRegistryService);
        when(apimRegistryService.getConfigRegistryResourceContent(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(RegistryException.class);
        try {
            apiConsumer.isMonetizationEnabled(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            assertFalse(true);
        } catch (APIManagementException e) {
            assertEquals("RegistryException thrown when getting API tenant config from registry", e.getMessage());
        }

        // error path ParseException
        Mockito.reset(apimRegistryService);
        String jsonInvalid = "{EnableMonetization:true}";
        when(apimRegistryService.getConfigRegistryResourceContent(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(jsonInvalid);
        try {
            apiConsumer.isMonetizationEnabled(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            assertFalse(true);
        } catch (APIManagementException e) {
            assertEquals("ParseException thrown when passing API tenant config from registry", e.getMessage());
        }

    }

    /**
     * This test case is to test the URIs generated for tag thumbnails when Tag wise listing is enabled in store page.
     */
    @Test
    public void testTagThumbnailURLGeneration() {
        // Check the URL for super tenant
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        String thumbnailPath = "/apimgt/applicationdata/tags/wso2-group/thumbnail.png";
        String finalURL = APIUtil.getRegistryResourcePathForUI(APIConstants.
                        RegistryResourceTypesForUI.TAG_THUMBNAIL, tenantDomain,
                thumbnailPath);
        log.info("##### Generated Tag Thumbnail URL > " + finalURL);
        assertEquals("/registry/resource/_system/governance" + thumbnailPath, finalURL);

        // Check the URL for other tenants
        tenantDomain = "apimanager3155.com";
        finalURL = APIUtil.getRegistryResourcePathForUI(APIConstants.
                        RegistryResourceTypesForUI.TAG_THUMBNAIL, tenantDomain,
                thumbnailPath);
        log.info("##### Generated Tag Thumbnail URL > " + finalURL);
        assertEquals("/t/" + tenantDomain + "/registry/resource/_system/governance" + thumbnailPath, finalURL);
    }

    @Test
    public void getSubscriberTest() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        when(apiMgtDAO.getSubscriber(Mockito.anyString())).thenReturn(new Subscriber(UUID.randomUUID().toString()));
        apiConsumer.apiMgtDAO = apiMgtDAO;
        assertNotNull(apiConsumer.getSubscriber(UUID.randomUUID().toString()));

        when(apiMgtDAO.getSubscriber(Mockito.anyString())).thenThrow(APIManagementException.class);
        try {
            apiConsumer.getSubscriber(UUID.randomUUID().toString());
            assertTrue(false);
        } catch (APIManagementException e) {
            assertEquals("Failed to get Subscriber", e.getMessage());
        }
    }

    @Test
    public void getUserRatingTest() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        APIIdentifier apiIdentifier = new APIIdentifier("admin", "TestAPI", "1.0.0");
        when(apiMgtDAO.getUserRating(apiIdentifier, "admin")).thenReturn(2);
        apiConsumer.apiMgtDAO = apiMgtDAO;
        assertEquals(2, apiConsumer.getUserRating(apiIdentifier, "admin"));
    }


    @Test
    public void getAPIByConsumerKeyTest() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        apiConsumer.apiMgtDAO = apiMgtDAO;
        Set<APIIdentifier> apiSet = new HashSet<APIIdentifier>();
        apiSet.add(TestUtils.getUniqueAPIIdentifier());
        apiSet.add(TestUtils.getUniqueAPIIdentifier());
        apiSet.add(TestUtils.getUniqueAPIIdentifier());
        when(apiMgtDAO.getAPIByConsumerKey(Mockito.anyString())).thenReturn(apiSet);
        assertNotNull(apiConsumer.getAPIByConsumerKey(UUID.randomUUID().toString()));

        //error path
        when(apiMgtDAO.getAPIByConsumerKey(Mockito.anyString())).thenThrow(APIManagementException.class);
        try {
            apiConsumer.getAPIByConsumerKey(UUID.randomUUID().toString());
            assertTrue(false);
        } catch (APIManagementException e) {
            assertEquals("Error while obtaining API from API key", e.getMessage());
        }
    }

    @Test
    public void resumeWorkflowTest() throws Exception {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        apiConsumer.apiMgtDAO = apiMgtDAO;
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        when(apiMgtDAO.retrieveWorkflow(Mockito.anyString())).thenReturn(workflowDTO);

        // Null input case
        assertNotNull(apiConsumer.resumeWorkflow(null));
        String args[] = {UUID.randomUUID().toString(), WorkflowStatus.CREATED.toString(), UUID.randomUUID().toString
                ()};
        assertNotNull(apiConsumer.resumeWorkflow(args));

        Mockito.reset(apiMgtDAO);
        workflowDTO.setTenantDomain("wso2.com");
        when(apiMgtDAO.retrieveWorkflow(Mockito.anyString())).thenReturn(workflowDTO);
        JSONObject row = apiConsumer.resumeWorkflow(args);
        assertNotNull(row);

        Mockito.reset(apiMgtDAO);
        when(apiMgtDAO.retrieveWorkflow(Mockito.anyString())).thenThrow(APIManagementException.class);
        row = apiConsumer.resumeWorkflow(args);
        assertEquals("Error while resuming the workflow. null",
                row.get("message"));

        // Workflow DAO null case
        Mockito.reset(apiMgtDAO);
        when(apiMgtDAO.retrieveWorkflow(Mockito.anyString())).thenReturn(null);
        row = apiConsumer.resumeWorkflow(args);
        assertNotNull(row);
        assertEquals(true, row.get("error"));
        assertEquals(500, row.get("statusCode"));

        //Invalid status test
        args[1] = "Invalid status";
        Mockito.reset(apiMgtDAO);
        when(apiMgtDAO.retrieveWorkflow(Mockito.anyString())).thenReturn(workflowDTO);
        row = apiConsumer.resumeWorkflow(args);
        assertEquals("Illegal argument provided. Valid values for status are APPROVED and REJECTED.",
                row.get("message"));

    }

    @Test
    public void getPaginatedAPIsWithTagTest() throws Exception {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantRegistry", Mockito.anyInt());

        PowerMockito.mockStatic(GovernanceUtils.class);
        GovernanceArtifact governanceArtifact = new GenericArtifactImpl(UUID.randomUUID().toString(), new QName(UUID.randomUUID().toString(), "UUID.randomUUID().toString()"),
                "api");
        List<GovernanceArtifact> governanceArtifactList = new ArrayList();
        governanceArtifactList.add(governanceArtifact);
        Mockito.when(GovernanceUtils.findGovernanceArtifacts(Mockito.anyString(), (Registry) Mockito.anyObject(),
                Mockito.anyString())).thenReturn(governanceArtifactList);

        assertNotNull(apiConsumer.getPaginatedAPIsWithTag("testTag", 0, 10, MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME));
    }

    @Test
    public void renewAccessTokenTest() throws APIManagementException {
        APIConsumerImpl apiConsumer = new APIConsumerImplWrapper();
        String args[] = {UUID.randomUUID().toString(), UUID.randomUUID().toString()};
        KeyManager keyManager = Mockito.mock(KeyManager.class);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        Mockito.when(keyManager.getNewApplicationAccessToken((AccessTokenRequest) Mockito.anyObject())).thenReturn
                (accessTokenInfo);
        Mockito.when(KeyManagerHolder.getKeyManagerInstance()).thenReturn(keyManager);

        PowerMockito.mockStatic(ApplicationUtils.class);
        Mockito.when(ApplicationUtils.populateTokenRequest(Mockito.anyString(), (AccessTokenRequest) Mockito
                .anyObject()))
                .thenReturn(tokenRequest);
        assertNotNull(apiConsumer.renewAccessToken(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID
                .randomUUID().toString(), "3600", args, "{}"));

        // Error path
        Mockito.when(ApplicationUtils.populateTokenRequest(Mockito.anyString(), (AccessTokenRequest) Mockito
                .anyObject()))
                .thenThrow(APIManagementException.class);
        try {
            apiConsumer.renewAccessToken(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID()
                    .toString(), "3600", args, "{}");
            assertTrue(false);
        } catch (APIManagementException e) {
            assertTrue(true);
        }
    }
}
