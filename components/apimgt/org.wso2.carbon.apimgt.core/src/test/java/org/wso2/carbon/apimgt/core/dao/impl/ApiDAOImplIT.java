/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.util.APIComparator;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ApiDAOImplIT extends DAOIntegrationTestBase {

    @Test
    public void testAddGetAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();

        apiDAO.addAPI(api);

        API apiFromDB = apiDAO.getAPI(api.getId());

        Assert.assertNotNull(apiFromDB);
        Assert.assertEquals(apiFromDB, api);
    }

    @Test
    public void testGetAPISummary() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();

        apiDAO.addAPI(api);

        API apiFromDB = apiDAO.getAPISummary(api.getId());

        API expectedAPI = SampleTestObjectCreator.copyAPISummary(api);

        Assert.assertNotNull(apiFromDB);
        Assert.assertEquals(apiFromDB, expectedAPI);
    }

    @Test
    public void testGetAPIs() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();

        List<API> apiList = apiDAO.getAPIs();
        Assert.assertTrue(apiList.isEmpty());

        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api1 = builder.build();

        apiDAO.addAPI(api1);

        builder = SampleTestObjectCreator.createAlternativeAPI();
        API api2 = builder.build();

        apiDAO.addAPI(api2);

        apiList = apiDAO.getAPIs();

        List<API> expectedAPIs = new ArrayList<>();
        expectedAPIs.add(SampleTestObjectCreator.copyAPISummary(api1));
        expectedAPIs.add(SampleTestObjectCreator.copyAPISummary(api2));

        Assert.assertTrue(apiList.size() == 2);

        for (int i = 0; i < apiList.size(); ++i) {
            Assert.assertEquals(apiList.get(i), expectedAPIs.get(i));
        }
    }

    @Test
    public void testGetAPIsForProvider() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        String provider1 = "Watson";
        String provider2 = "Holmes";

        List<API> apiList = apiDAO.getAPIsForProvider(provider1);
        Assert.assertTrue(apiList.isEmpty());
        apiList = apiDAO.getAPIsForProvider(provider2);
        Assert.assertTrue(apiList.isEmpty());

        // Add APIs belonging to provider1
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        builder.provider(provider1);
        API api1 = builder.build();

        apiDAO.addAPI(api1);

        builder = SampleTestObjectCreator.createAlternativeAPI();
        builder.provider(provider1);
        API api2 = builder.build();

        apiDAO.addAPI(api2);

        // Add APIs belonging to provider2
        builder = SampleTestObjectCreator.createUniqueAPI();
        API api3 = builder.provider(provider2).build();

        apiDAO.addAPI(api3);

        // Get APIs belonging to provider1
        apiList = apiDAO.getAPIsForProvider(provider1);

        List<API> expectedAPIs = new ArrayList<>();
        expectedAPIs.add(SampleTestObjectCreator.copyAPISummary(api1));
        expectedAPIs.add(SampleTestObjectCreator.copyAPISummary(api2));

        Assert.assertTrue(apiList.size() == 2);

        for (int i = 0; i < apiList.size(); ++i) {
            Assert.assertEquals(apiList.get(i), expectedAPIs.get(i));
        }

        // Get APIs belonging to provider2
        apiList = apiDAO.getAPIsForProvider(provider2);

        API expectedAPI = SampleTestObjectCreator.copyAPISummary(api3);

        Assert.assertTrue(apiList.size() == 1);

        Assert.assertEquals(apiList.get(0), expectedAPI);
    }

    @Test
    public void testGetAPIsByStatus() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();

        // Define statuses used in test
        final String publishedStatus = "PUBLISHED";
        final String createdStatus = "CREATED";
        final String blockedStatus = "BLOCKED";

        // Define number of APIs to be created for a given status
        final int numberOfPublished = 4;
        final int numberOfCreated = 2;
        final int numberOfBlocked = 1;

        // Add APIs
        List<API> publishedAPIsSummary = new ArrayList<>();
        for (int i = 0; i < numberOfPublished; ++i) {
            API api = SampleTestObjectCreator.createUniqueAPI().lifeCycleStatus(publishedStatus).build();
            publishedAPIsSummary.add(SampleTestObjectCreator.getSummaryFromAPI(api));
            apiDAO.addAPI(api);
        }

        List<API> createdAPIsSummary = new ArrayList<>();
        for (int i = 0; i < numberOfCreated; ++i) {
            API api = SampleTestObjectCreator.createUniqueAPI().lifeCycleStatus(createdStatus).build();
            createdAPIsSummary.add(SampleTestObjectCreator.getSummaryFromAPI(api));
            apiDAO.addAPI(api);
        }

        List<API> blockedAPIsSummary = new ArrayList<>();
        for (int i = 0; i < numberOfBlocked; ++i) {
            API api = SampleTestObjectCreator.createUniqueAPI().lifeCycleStatus(blockedStatus).build();
            blockedAPIsSummary.add(SampleTestObjectCreator.getSummaryFromAPI(api));
            apiDAO.addAPI(api);
        }

        // Filter APIs by single status
        List<String> singleStatus = new ArrayList<>();
        singleStatus.add(publishedStatus);

        List<API> apiList = apiDAO.getAPIsByStatus(singleStatus);

        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, publishedAPIsSummary, new APIComparator()));

        // Filter APIs by two statuses
        List<String> twoStatuses = new ArrayList<>();
        twoStatuses.add(publishedStatus);
        twoStatuses.add(blockedStatus);

        apiList = apiDAO.getAPIsByStatus(twoStatuses);

        Assert.assertEquals(apiList.size(), publishedAPIsSummary.size() + blockedAPIsSummary.size());

        for (API api : publishedAPIsSummary) {
            Assert.assertTrue(apiList.contains(api));
            apiList.remove(api);
        }

        for (API api : blockedAPIsSummary) {
            Assert.assertTrue(apiList.contains(api));
            apiList.remove(api);
        }

        Assert.assertTrue(apiList.isEmpty());

        // Filter APIs by multiple statuses
        List<String> multipleStatuses = new ArrayList<>();
        multipleStatuses.add(publishedStatus);
        multipleStatuses.add(createdStatus);
        multipleStatuses.add(blockedStatus);

        apiList = apiDAO.getAPIsByStatus(multipleStatuses);

        Assert.assertEquals(apiList.size(), publishedAPIsSummary.size() +
                                            blockedAPIsSummary.size() + createdAPIsSummary.size());

        for (API api : publishedAPIsSummary) {
            Assert.assertTrue(apiList.contains(api));
            apiList.remove(api);
        }

        for (API api : blockedAPIsSummary) {
            Assert.assertTrue(apiList.contains(api));
            apiList.remove(api);
        }

        for (API api : createdAPIsSummary) {
            Assert.assertTrue(apiList.contains(api));
            apiList.remove(api);
        }

        Assert.assertTrue(apiList.isEmpty());
    }

    @Test
    public void testDeleteAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();

        apiDAO.addAPI(api);

        apiDAO.deleteAPI(api.getId());

        API deletedAPI = apiDAO.getAPI(api.getId());
        Assert.assertNull(deletedAPI);
    }

    @Test
    public void testUpdateAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();

        apiDAO.addAPI(api);

        builder = SampleTestObjectCreator.createAlternativeAPI();
        API substituteAPI = builder.build();

        apiDAO.updateAPI(api.getId(), substituteAPI);
        API apiFromDB = apiDAO.getAPI(api.getId());

        API expectedAPI = SampleTestObjectCreator.copyAPIIgnoringNonEditableFields(api, substituteAPI);

        Assert.assertNotNull(apiFromDB);
        Assert.assertEquals(apiFromDB, expectedAPI);
    }

    /*
    private void validateAPIs(API actualAPI, API expectedAPI) {
        Assert.assertEquals(actualAPI.getProvider(), expectedAPI.getProvider());
        Assert.assertEquals(actualAPI.getVersion(), expectedAPI.getVersion());
        Assert.assertEquals(actualAPI.getName(), expectedAPI.getName());
        Assert.assertEquals(actualAPI.getDescription(), expectedAPI.getDescription());
        Assert.assertEquals(actualAPI.getContext(), expectedAPI.getContext());
        Assert.assertEquals(actualAPI.getId(), expectedAPI.getId());
        Assert.assertEquals(actualAPI.getLifeCycleStatus(), expectedAPI.getLifeCycleStatus());
        Assert.assertEquals(actualAPI.getLifecycleInstanceId(), expectedAPI.getLifecycleInstanceId());
        Assert.assertEquals(actualAPI.getApiDefinition(), expectedAPI.getApiDefinition());
        Assert.assertEquals(actualAPI.getWsdlUri(), expectedAPI.getWsdlUri());
        Assert.assertEquals(actualAPI.isResponseCachingEnabled(), expectedAPI.isResponseCachingEnabled());
        Assert.assertEquals(actualAPI.getCacheTimeout(), expectedAPI.getCacheTimeout());
        Assert.assertEquals(actualAPI.isDefaultVersion(), expectedAPI.isDefaultVersion());
        Assert.assertTrue(equalLists(actualAPI.getTransport(), expectedAPI.getTransport()));
        Assert.assertTrue(equalLists(actualAPI.getTags(), expectedAPI.getTags()));
        Assert.assertEquals(actualAPI.getPolicies(), expectedAPI.getPolicies());
        Assert.assertEquals(actualAPI.getVisibility(), expectedAPI.getVisibility());
        Assert.assertTrue(equalLists(actualAPI.getVisibleRoles(), expectedAPI.getVisibleRoles()));
        //Assert.assertEquals(actualAPI.getEndpoint(), expectedAPI.getEndpoint());
        //Assert.assertEquals(actualAPI.getGatewayEnvironments(), expectedAPI.getGatewayEnvironments());
        Assert.assertEquals(actualAPI.getBusinessInformation(), expectedAPI.getBusinessInformation());
        Assert.assertEquals(actualAPI.getCorsConfiguration(), expectedAPI.getCorsConfiguration());
        Assert.assertEquals(actualAPI.getCreatedTime(), expectedAPI.getCreatedTime());
        Assert.assertEquals(actualAPI.getCreatedBy(), expectedAPI.getCreatedBy());
        Assert.assertEquals(actualAPI.getLastUpdatedTime(), expectedAPI.getLastUpdatedTime());
    }
    */

    private boolean equalLists(List<String> one, List<String> two){
        if (one == null && two == null){
            return true;
        }

        if((one == null && two != null)
                || one != null && two == null
                || one.size() != two.size()){
            return false;
        }

        one = new ArrayList<String>(one);
        two = new ArrayList<String>(two);

        Collections.sort(one);
        Collections.sort(two);
        return one.equals(two);
    }



}
