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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class})
public class APIConsumerImplTest {

    private static final Log log = LogFactory.getLog(APIConsumerImplTest.class);

    @Test
    public void testReadMonetizationConfigAnnonymously() {
        APIMRegistryService apimRegistryService = Mockito.mock(APIMRegistryService.class);

        String json = "{\n  EnableMonetization : true\n }";

        try {
            when(apimRegistryService.getConfigRegistryResourceContent("", "")).thenReturn(json);
            /* TODO: Need to mock out ApimgtDAO and usage of registry else where in order to test this
            APIConsumer apiConsumer = new UserAwareAPIConsumer("__wso2.am.anon__", apimRegistryService);

            boolean isEnabled = apiConsumer.isMonetizationEnabled("carbon.super");

            assertTrue("Expected true but returned " + isEnabled, isEnabled);

        } catch (APIManagementException e) {
            e.printStackTrace();
        */} catch (UserStoreException e) {
            e.printStackTrace();
        } catch (RegistryException e) {
            e.printStackTrace();
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
    
}
