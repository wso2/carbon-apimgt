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

import java.lang.reflect.Field;
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
        validateAPIs(apiFromDB, api);
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
        validateAPIs(apiFromDB, expectedAPI);
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
            validateAPIs(apiList.get(0), expectedAPIs.get(0));
        }
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

        API expectedAPI = builder.provider(api.getProvider()).
                id(api.getId()).
                name(api.getName()).
                version(api.getVersion()).
                context(api.getContext()).
                createdTime(api.getCreatedTime()).
                createdBy(api.getCreatedBy()).build();

        Assert.assertNotNull(apiFromDB);
        validateAPIs(apiFromDB, expectedAPI);
    }

    @Test
    public void testGetAPIsForRoles() throws Exception {
        /*
        ApiDAO apiDAO = new ApiDAOImpl(new H2MySQLStatements());
        API.APIBuilder builder = SampleAPICreator.createDefaultAPI();

        builder.visibility(API.Visibility.RESTRICTED);
        builder.visibleRoles(Arrays.asList("topsecret", "classified"));
        API superSecureAPI = builder.build();

        apiDAO.addAPI(superSecureAPI);

        builder = SampleAPICreator.createDefaultAPI();
        builder.visibility(API.Visibility.RESTRICTED);
        builder.visibleRoles(Arrays.asList("secret", "classified"));

        API verySecureAPI = builder.build();

        apiDAO.addAPI(verySecureAPI);

        builder = SampleAPICreator.createDefaultAPI();
        builder.visibility(API.Visibility.RESTRICTED);
        builder.visibleRoles(Arrays.asList("hidden"));

        API hiddenAPI = builder.build();

        apiDAO.addAPI(hiddenAPI);

        builder = SampleAPICreator.createDefaultAPI();
        builder.visibility(API.Visibility.PUBLIC);
        API publicAPI = builder.build();

        apiDAO.addAPI(publicAPI);

        apiDAO.getAPIs(0, 10, Arrays.asList("classified"));
        */
    }

    @Test
    public void testSearchAPIsForRoles() throws Exception {

    }


    @Test
    public void testGetSwaggerDefinition() throws Exception {

    }

    @Test
    public void testUpdateSwaggerDefinition() throws Exception {

    }

    @Test
    public void testGetImage() throws Exception {

    }

    @Test
    public void testUpdateImage() throws Exception {

    }

    @Test
    public void testChangeLifeCylceStatus() throws Exception {

    }

    @Test
    public void testCreateNewAPIVersion() throws Exception {

    }

    @Test
    public void testGetDocumentsInfoList() throws Exception {

    }

    @Test
    public void testGetDocumentInfo() throws Exception {

    }

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
        //Assert.assertEquals(actualAPI.getEndpoints(), expectedAPI.getEndpoints());
        //Assert.assertEquals(actualAPI.getGatewayEnvironments(), expectedAPI.getGatewayEnvironments());
        Assert.assertEquals(actualAPI.getBusinessInformation(), expectedAPI.getBusinessInformation());
        Assert.assertEquals(actualAPI.getCorsConfiguration(), expectedAPI.getCorsConfiguration());
        Assert.assertEquals(actualAPI.getCreatedTime(), expectedAPI.getCreatedTime());
        Assert.assertEquals(actualAPI.getCreatedBy(), expectedAPI.getCreatedBy());
        Assert.assertEquals(actualAPI.getLastUpdatedTime(), expectedAPI.getLastUpdatedTime());
    }

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
