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
import org.wso2.carbon.apimgt.core.TestUtil;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.util.APIComparator;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.core.util.EndPointComparator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ApiDAOImplIT extends DAOIntegrationTestBase {
    private static final String ADMIN = "admin";

    @Test
    public void testAddGetAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        API apiFromDB = apiDAO.getAPI(api.getId());

        Assert.assertNotNull(apiFromDB);
        Assert.assertTrue(api.equals(apiFromDB), TestUtil.printDiff(api, apiFromDB));
    }

    @Test
    public void testAddDuplicateProviderNameVersionAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API api = SampleTestObjectCreator.createUniqueAPI().build();
        apiDAO.addAPI(api);

        API.APIBuilder duplicateAPIBuilder = SampleTestObjectCreator.createUniqueAPI();
        duplicateAPIBuilder.provider(api.getProvider());
        duplicateAPIBuilder.name(api.getName());
        duplicateAPIBuilder.version(api.getVersion());

        API duplicateAPI = duplicateAPIBuilder.build();
        try {
            apiDAO.addAPI(duplicateAPI);
            Assert.fail("Exception not thrown for adding duplicate API");
        } catch (APIMgtDAOException e) {
            // Just catch the exception so that we can continue execution
        }

        API apiFromDB = apiDAO.getAPI(api.getId());

        Assert.assertNull(apiDAO.getAPI(duplicateAPI.getId()));
        Assert.assertEquals(apiDAO.getAPIs().size(), 1);
        Assert.assertEquals(apiFromDB, api, TestUtil.printDiff(apiFromDB, api));
    }

    @Test
    public void testAddSameAPIWithDifferentProviders() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API api = SampleTestObjectCreator.createUniqueAPI().build();
        apiDAO.addAPI(api);

        API.APIBuilder duplicateAPIBuilder = SampleTestObjectCreator.createUniqueAPI();
        duplicateAPIBuilder.name(api.getName());
        duplicateAPIBuilder.version(api.getVersion());

        API duplicateAPI = duplicateAPIBuilder.build();
        apiDAO.addAPI(duplicateAPI);

        API apiFromDB = apiDAO.getAPI(api.getId());
        API duplicateApiFromDB = apiDAO.getAPI(duplicateAPI.getId());

        Assert.assertEquals(duplicateApiFromDB.getName(), api.getName());
        Assert.assertEquals(duplicateApiFromDB.getVersion(), api.getVersion());
        Assert.assertEquals(apiDAO.getAPIs().size(), 2);
        Assert.assertEquals(apiFromDB, api, TestUtil.printDiff(apiFromDB, api));
        Assert.assertEquals(duplicateApiFromDB, duplicateAPI, TestUtil.printDiff(duplicateApiFromDB, duplicateAPI));
    }

    @Test
    public void testDuplicateContext() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API api = SampleTestObjectCreator.createUniqueAPI().build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        API.APIBuilder duplicateAPIBuilder = SampleTestObjectCreator.createUniqueAPI();
        duplicateAPIBuilder.context(api.getContext());

        API duplicateAPI = duplicateAPIBuilder.build();
        try {
            apiDAO.addAPI(duplicateAPI);
            Assert.fail("Exception not thrown for adding duplicate API context");
        } catch (APIMgtDAOException e) {
            // Just catch the exception so that we can continue execution
        }

        API apiFromDB = apiDAO.getAPI(api.getId());

        Assert.assertNull(apiDAO.getAPI(duplicateAPI.getId()));
        Assert.assertEquals(apiDAO.getAPIs().size(), 1);
        Assert.assertEquals(apiFromDB, api, TestUtil.printDiff(apiFromDB, api));
    }

    @Test
    public void testGetAPISummary() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
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
        testAddGetEndpoint();
        apiDAO.addAPI(api1);

        builder = SampleTestObjectCreator.createAlternativeAPI();
        API api2 = builder.build();

        apiDAO.addAPI(api2);

        apiList = apiDAO.getAPIs();

        List<API> expectedAPIs = new ArrayList<>();
        expectedAPIs.add(SampleTestObjectCreator.copyAPISummary(api1));
        expectedAPIs.add(SampleTestObjectCreator.copyAPISummary(api2));

        Assert.assertTrue(apiList.size() == 2);

        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, expectedAPIs, new APIComparator()),
                TestUtil.printDiff(apiList, expectedAPIs));
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
        testAddGetEndpoint();
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

        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, expectedAPIs, new APIComparator()),
                TestUtil.printDiff(apiList, expectedAPIs));

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
        testAddGetEndpoint();
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

        Assert.assertEquals(apiList.size(), publishedAPIsSummary.size() + blockedAPIsSummary.size()
                + createdAPIsSummary.size());

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
    public void testSearchAPIs() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();

        // Sample API names
        final String mixedCaseString = "Mixed Case";
        final String lowerCaseString = "lower case";
        final String upperCaseString = "UPPER CASE";
        final String charSymbolNumString = "mi ##symbol 12num";
        final String symbolSpaceString = "_under & Score_";

        // Search string cases
        final String commonMixedCaseSearchString = "CaSe";
        final String commonLowerCaseSearchString = "case";
        final String commonUpperCaseSearchString = "CASE";
        final String symbolSearchString = "##symbol";
        final String numberSearchString = "12n";                 // In some databases numbers are not used in indexing

        // Create test data
        Map<String, API> apis = new HashMap<>();
        apis.put(mixedCaseString, SampleTestObjectCreator.createUniqueAPI().name(mixedCaseString).build());
        apis.put(lowerCaseString, SampleTestObjectCreator.createUniqueAPI().name(lowerCaseString).build());
        apis.put(upperCaseString, SampleTestObjectCreator.createUniqueAPI().name(upperCaseString).build());
        apis.put(charSymbolNumString, SampleTestObjectCreator.createUniqueAPI().name(charSymbolNumString).build());
        apis.put(symbolSpaceString, SampleTestObjectCreator.createUniqueAPI().name(symbolSpaceString).build());

        // Add APIs
        testAddGetEndpoint();
        for (Map.Entry<String, API> entry : apis.entrySet()) {
            API api = entry.getValue();
            apiDAO.addAPI(api);
            // Replace with summary object for validation
            apis.put(entry.getKey(), SampleTestObjectCreator.getSummaryFromAPI(api));
        }
        // Sleep for indexing
        Thread.sleep(5000);
        // Expected common string search result
        List<API> commonStringResult = new ArrayList<>();
        commonStringResult.add(apis.get(mixedCaseString));
        commonStringResult.add(apis.get(lowerCaseString));
        commonStringResult.add(apis.get(upperCaseString));

        // Search by common mixed case
        List<API> apiList = apiDAO.searchAPIs(new ArrayList<>(), "", commonMixedCaseSearchString, 0, 10);
        Assert.assertEquals(apiList.size(), 3);
        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, commonStringResult, new APIComparator()),
                TestUtil.printListDiff(apiList, commonStringResult));

        // Search by common lower case
        apiList = apiDAO.searchAPIs(new ArrayList<>(), "", commonLowerCaseSearchString, 0, 10);
        Assert.assertEquals(apiList.size(), 3);
        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, commonStringResult, new APIComparator()),
                TestUtil.printListDiff(apiList, commonStringResult));

        // Search by common upper case
        apiList = apiDAO.searchAPIs(new ArrayList<>(), "", commonUpperCaseSearchString, 0, 10);
        Assert.assertEquals(apiList.size(), 3);
        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, commonStringResult, new APIComparator()),
                TestUtil.printListDiff(apiList, commonStringResult));

        // Search by symbol
        apiList = apiDAO.searchAPIs(new ArrayList<>(), "", symbolSearchString, 0, 10);
        Assert.assertEquals(apiList.size(), 1);
        API actualAPI = apiList.get(0);
        API expectedAPI = apis.get(charSymbolNumString);
        Assert.assertEquals(actualAPI, expectedAPI, TestUtil.printDiff(actualAPI, expectedAPI));

        // Search by number
        apiList = apiDAO.searchAPIs(new ArrayList<>(), "", numberSearchString, 0, 10);
        Assert.assertEquals(apiList.size(), 1);
        actualAPI = apiList.get(0);
        expectedAPI = apis.get(charSymbolNumString);
        Assert.assertEquals(actualAPI, expectedAPI, TestUtil.printDiff(actualAPI, expectedAPI));
    }

    @Test
    public void testIsAPINameExists() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createUniqueAPI().build();
        apiDAO.addAPI(api);

        Assert.assertTrue(apiDAO.isAPINameExists(api.getName(), api.getProvider()));
        Assert.assertFalse(apiDAO.isAPINameExists("Not-Exists", api.getProvider()));

        final String upperCaseName = "CAPITAL";

        // Add API with upper case name
        api = SampleTestObjectCreator.createUniqueAPI().name(upperCaseName).build();
        apiDAO.addAPI(api);
        // Check with upper case format
        Assert.assertTrue(apiDAO.isAPINameExists(upperCaseName, api.getProvider()));
        // Check with lower case format
        Assert.assertTrue(apiDAO.isAPINameExists(upperCaseName.toLowerCase(Locale.ENGLISH), api.getProvider()));
        // Check with mixed case format
        Assert.assertTrue(apiDAO.isAPINameExists(upperCaseName.substring(0, 3) +
                upperCaseName.substring(3).toLowerCase(Locale.ENGLISH), api.getProvider()));

        final String lowerCaseName = "simple";

        // Add API with upper case name
        api = SampleTestObjectCreator.createUniqueAPI().name(lowerCaseName).build();
        apiDAO.addAPI(api);
        // Check with lower case format
        Assert.assertTrue(apiDAO.isAPINameExists(lowerCaseName, api.getProvider()));
        // Check with upper case format
        Assert.assertTrue(apiDAO.isAPINameExists(lowerCaseName.toUpperCase(Locale.ENGLISH), api.getProvider()));
        // Check with mixed case format
        Assert.assertTrue(apiDAO.isAPINameExists(lowerCaseName.substring(0, 3) +
                lowerCaseName.substring(3).toUpperCase(Locale.ENGLISH), api.getProvider()));

        // Create same API for different providers and check for existence
        final String sameName = "same";

        API api1 = SampleTestObjectCreator.createUniqueAPI().name(sameName).build();
        apiDAO.addAPI(api1);

        API api2 = SampleTestObjectCreator.createUniqueAPI().name(sameName).build();
        apiDAO.addAPI(api2);

        Assert.assertTrue(apiDAO.isAPINameExists(sameName, api1.getProvider()));
        Assert.assertTrue(apiDAO.isAPINameExists(sameName, api2.getProvider()));
        Assert.assertFalse(apiDAO.isAPINameExists(sameName, "no_such_provider"));
    }

    @Test
    public void testIsAPIContextExists() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createUniqueAPI().build();
        apiDAO.addAPI(api);
        Assert.assertTrue(apiDAO.isAPIContextExists(api.getContext()));
    }

    @Test
    public void testGetSwaggerDefinition() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createUniqueAPI().build();
        apiDAO.addAPI(api);
        Assert.assertNotNull(apiDAO.getSwaggerDefinition(api.getId()));
    }

    @Test
    public void testGetGatewayConfig() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        String configString = SampleTestObjectCreator.createSampleGatewayConfig();
        API api = SampleTestObjectCreator.createUniqueAPI().gatewayConfig(configString).build();
        apiDAO.addAPI(api);
        Assert.assertNotNull(apiDAO.getGatewayConfig(api.getId()));
    }

    @Test(description = "Changing the Lifecycle status of a given API")
    public void testChangeLifeCycleStatus() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createUniqueAPI().build();
        apiDAO.addAPI(api);
        apiDAO.changeLifeCycleStatus(api.getId(), APIStatus.PUBLISHED.getStatus());
        API apiFromDB = apiDAO.getAPI(api.getId());
        Assert.assertEquals(apiFromDB.getLifeCycleStatus(), APIStatus.PUBLISHED.getStatus());
        Assert.assertNotEquals(api.getLifeCycleStatus(), apiFromDB.getLifeCycleStatus());

    }

    @Test(description = "Getting document info list for an API")
    public void testGetDocumentInfoList() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        apiDAO.addAPI(api);
        DocumentInfo documentInfo1 = SampleTestObjectCreator.createDefaultDocumentationInfo();
        apiDAO.addDocumentInfo(api.getId(), documentInfo1);
        DocumentInfo documentInfo2 = SampleTestObjectCreator.createDefaultDocumentationInfo();
        apiDAO.addDocumentInfo(api.getId(), documentInfo2);
        List<DocumentInfo> documentInfoList = new ArrayList<>();
        documentInfoList.add(documentInfo1);
        documentInfoList.add(documentInfo2);
        List<DocumentInfo> documentInfoListFromDB = apiDAO.getDocumentsInfoList(api.getId());
        Assert.assertTrue(documentInfoListFromDB.containsAll(documentInfoList));
        Assert.assertTrue(documentInfoList.size() == documentInfoListFromDB.size());
    }

    @Test(description = "Getting document info for an API")
    public void testGetDocumentInfo() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        apiDAO.addAPI(api);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        apiDAO.addDocumentInfo(api.getId(), documentInfo);
        DocumentInfo documentInfoFromDB = apiDAO.getDocumentInfo(documentInfo.getId());
        Assert.assertEquals(documentInfo, documentInfoFromDB);
    }

    @Test(description = "Getting document inline content for an API")
    public void testGetDocumentInlineContent() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        apiDAO.addAPI(api);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        apiDAO.addDocumentInfo(api.getId(), documentInfo);
        String inlineDocContent = SampleTestObjectCreator.createDefaultInlineDocumentationContent();
        apiDAO.addDocumentInlineContent(documentInfo.getId(), inlineDocContent, ADMIN);
        String inlineDocContentFromDB = apiDAO.getDocumentInlineContent(documentInfo.getId());
        Assert.assertEquals(inlineDocContent, inlineDocContentFromDB);
    }

    @Test(description = "Delete documentation for an API")
    public void testDeleteDocumentation() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        apiDAO.addAPI(api);
        //adding documentation
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        String docId = documentInfo.getId();
        apiDAO.addDocumentInfo(api.getId(), documentInfo);
        //delete documentation
        apiDAO.deleteDocument(docId);
        DocumentInfo documentInfoFromDB = apiDAO.getDocumentInfo(docId);
        Assert.assertNull(documentInfoFromDB);
    }

    @Test(description = "Retrieve summary of paginated data of all available APIs that match the given search criteria")
    public void testAttributeSearchAPIs() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        apiDAO.addAPI(api);
        List<String> roles = new ArrayList<>();
        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put("name", api.getName());
        List<API> apiList = apiDAO.attributeSearchAPIs(roles, ADMIN, attributeMap, 1, 2);
        Assert.assertTrue(apiList.size() > 0);
    }

    @Test(description = "Search APIs by status")
    public void testSearchAPIsByStatus() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        apiDAO.addAPI(api);
        List<String> statuses = new ArrayList<>();
        statuses.add(api.getLifeCycleStatus());
        String searchString = api.getName();
        List<API> apiList = apiDAO.searchAPIsByStatus(searchString, statuses);
        Assert.assertTrue(apiList.size() > 0);
    }

    @Test(description = "Get image from API")
    public void testGetImage() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        testAddGetEndpoint();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        apiDAO.addAPI(api);
        apiDAO.updateImage(api.getId(), SampleTestObjectCreator.createDefaultThumbnailImage(), "image/jpg", ADMIN);
        InputStream image = apiDAO.getImage(api.getId());
        Assert.assertNotNull(image);
    }

    @Test
    public void testDeleteAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
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
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        HashMap permissionMap = new HashMap();
        permissionMap.put(APIMgtConstants.Permission.UPDATE, APIMgtConstants.Permission.UPDATE_PERMISSION);
        builder = SampleTestObjectCreator.createAlternativeAPI().permissionMap(permissionMap);
        API substituteAPI = builder.build();

        apiDAO.updateAPI(api.getId(), substituteAPI);
        API apiFromDB = apiDAO.getAPI(api.getId());

        API expectedAPI = SampleTestObjectCreator.copyAPIIgnoringNonEditableFields(api, substituteAPI);

        Assert.assertNotNull(apiFromDB);
        Assert.assertEquals(apiFromDB, expectedAPI, TestUtil.printDiff(apiFromDB, expectedAPI));
    }

    @Test
    public void testFingerprintAfterUpdatingAPI() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        String fingerprintBeforeUpdate = ETagUtils.generateETag(apiDAO.getLastUpdatedTimeOfAPI(api.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);
        Thread.sleep(1);

        builder = SampleTestObjectCreator.createAlternativeAPI();
        API substituteAPI = builder.build();

        apiDAO.updateAPI(api.getId(), substituteAPI);
        String fingerprintAfterUpdate = ETagUtils.generateETag(apiDAO.getLastUpdatedTimeOfAPI(api.getId()));
        Assert.assertNotNull(fingerprintAfterUpdate);

        Assert.assertNotEquals(fingerprintBeforeUpdate, fingerprintAfterUpdate);
    }

    @Test
    public void testFingerprintAfterUpdatingSwaggerDefinition() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        String fingerprintBeforeUpdate = ETagUtils.generateETag(apiDAO.getLastUpdatedTimeOfSwaggerDefinition(
                api.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);
        Thread.sleep(1);

        String swagger = SampleTestObjectCreator.createAlternativeSwaggerDefinition();
        apiDAO.updateSwaggerDefinition(api.getId(), swagger, ADMIN);
        String fingerprintAfterUpdate = ETagUtils
                .generateETag(apiDAO.getLastUpdatedTimeOfSwaggerDefinition(api.getId()));
        Assert.assertNotNull(fingerprintAfterUpdate);

        Assert.assertNotEquals(fingerprintBeforeUpdate, fingerprintAfterUpdate);
    }

    @Test
    public void testFingerprintAfterUpdatingGatewayConfig() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        builder.gatewayConfig(SampleTestObjectCreator.createSampleGatewayConfig());
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        String fingerprintBeforeUpdate = ETagUtils.generateETag(apiDAO.getLastUpdatedTimeOfGatewayConfig(
                api.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);
        Thread.sleep(1);

        String gwConfig = SampleTestObjectCreator.createAlternativeGatewayConfig();
        apiDAO.updateGatewayConfig(api.getId(), gwConfig, ADMIN);
        String fingerprintAfterUpdate = ETagUtils
                .generateETag(apiDAO.getLastUpdatedTimeOfGatewayConfig(api.getId()));
        Assert.assertNotNull(fingerprintAfterUpdate);

        Assert.assertNotEquals(fingerprintBeforeUpdate, fingerprintAfterUpdate);
    }

    @Test
    public void testFingerprintAfterUpdatingThumbnailImage() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);
        apiDAO.updateImage(api.getId(), SampleTestObjectCreator.createDefaultThumbnailImage(), "image/jpg", ADMIN);

        String fingerprintBeforeUpdate = ETagUtils.generateETag(apiDAO.getLastUpdatedTimeOfAPIThumbnailImage(
                api.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);
        Thread.sleep(1);

        apiDAO.updateImage(api.getId(), SampleTestObjectCreator.createAlternativeThumbnailImage(), "image/jpg",
                ADMIN);
        String fingerprintAfterUpdate = ETagUtils
                .generateETag(apiDAO.getLastUpdatedTimeOfAPIThumbnailImage(api.getId()));
        Assert.assertNotNull(fingerprintAfterUpdate);

        Assert.assertNotEquals(fingerprintBeforeUpdate, fingerprintAfterUpdate);
    }

    @Test
    public void testAddGetEndpoint() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        apiDAO.addEndpoint(endpoint);
        Endpoint retrieved = apiDAO.getEndpoint(endpoint.getId());
        Assert.assertEquals(endpoint, retrieved);
    }

    @Test(description = "Test getting endpoint by name")
    public void testGetEndpointByName() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        apiDAO.addEndpoint(endpoint);
        Endpoint retrieved = apiDAO.getEndpointByName(endpoint.getName());
        Assert.assertEquals(endpoint, retrieved);
    }

    @Test(description = "Test adding API with endpointMap")
    public void testAddEndPointsForApi() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        Map<String, String> endpointMap = SampleTestObjectCreator.getMockEndpointMap();
        API api = SampleTestObjectCreator.createDefaultAPI().endpoint(endpointMap).build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        API apiFromDB = apiDAO.getAPI(api.getId());

        Assert.assertNotNull(apiFromDB);
        Assert.assertTrue(api.equals(apiFromDB), TestUtil.printDiff(api, apiFromDB));
    }

    @Test
    public void testAddUpdateGetEndpoint() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        apiDAO.addEndpoint(SampleTestObjectCreator.createMockEndpoint());
        Endpoint updatedEndpoint = SampleTestObjectCreator.createUpdatedEndpoint();
        apiDAO.updateEndpoint(updatedEndpoint);
        Endpoint retrieved = apiDAO.getEndpoint(updatedEndpoint.getId());
        Assert.assertEquals(updatedEndpoint, retrieved);
    }

    @Test
    public void testAddDeleteGetEndpoint() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        apiDAO.addEndpoint(endpoint);
        apiDAO.deleteEndpoint(endpoint.getId());
        Endpoint retrieved = apiDAO.getEndpoint(endpoint.getId());
        Assert.assertNull(retrieved);
    }

    @Test
    public void testAddGetAllEndPoints() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        Endpoint endpoint1 = SampleTestObjectCreator.createMockEndpoint();
        Endpoint endpoint2 = SampleTestObjectCreator.createAlternativeEndpoint();
        apiDAO.addEndpoint(endpoint1);
        apiDAO.addEndpoint(endpoint2);
        List<Endpoint> endpointListAdd = new ArrayList<>();
        endpointListAdd.add(endpoint1);
        endpointListAdd.add(endpoint2);
        List<Endpoint> endpointList = apiDAO.getEndpoints();
        APIUtils.isListsEqualIgnoreOrder(endpointListAdd, endpointList, new EndPointComparator());
    }

    @Test
    public void testAddGetAPIWithLabels() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public").build();
        Label label2 = SampleTestObjectCreator.createLabel("private").build();
        List<Label> labelList = new ArrayList<>();
        labelList.add(label1);
        labelList.add(label2);
        labelDAO.addLabels(labelList);

        ApiDAO apiDAO = DAOFactory.getApiDAO();
        List<String> labelNames = new ArrayList<>();
        labelNames.add(label1.getName());
        labelNames.add(label2.getName());
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.labels(labelNames).build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        API apiFromDB = apiDAO.getAPI(api.getId());
        Assert.assertNotNull(apiFromDB);
        Assert.assertEquals(apiFromDB.getLabels().size(), 2);
        Assert.assertTrue(api.equals(apiFromDB), TestUtil.printDiff(api, apiFromDB));
    }

    @Test
    public void testAddAPIWithoutAddingLabels() throws Exception {

        ApiDAO apiDAO = DAOFactory.getApiDAO();
        List<String> labelNames = new ArrayList<>();
        labelNames.add("public");
        labelNames.add("private");
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.labels(labelNames).build();
        testAddGetEndpoint();

        try {
            apiDAO.addAPI(api);
            Assert.fail("Exception not thrown when adding an API without adding the labels");
        } catch (APIMgtDAOException e) {
            // Just catch the exception so that we can continue execution
        }

        API apiFromDB = apiDAO.getAPI(api.getId());
        Assert.assertNull(apiFromDB);
    }

    @Test
    public void testUpdateAPIWithLabels() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public").build();
        Label label2 = SampleTestObjectCreator.createLabel("private").build();
        List<Label> labelList = new ArrayList<>();
        labelList.add(label1);
        labelList.add(label2);
        labelDAO.addLabels(labelList);

        ApiDAO apiDAO = DAOFactory.getApiDAO();
        List<String> labelNames = new ArrayList<>();
        labelNames.add(label1.getName());
        API.APIBuilder builder1 = SampleTestObjectCreator.createDefaultAPI();
        API api = builder1.labels(labelNames).build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);

        labelNames.add(label2.getName());
        API substituteAPI = new API.APIBuilder(api).labels(labelNames).build();
        apiDAO.updateAPI(api.getId(), substituteAPI);
        API apiFromDB = apiDAO.getAPI(api.getId());

        API expectedAPI = SampleTestObjectCreator.copyAPIIgnoringNonEditableFields(api, substituteAPI);
        Assert.assertNotNull(apiFromDB);
        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiFromDB.getLabels(), expectedAPI.getLabels()),
                TestUtil.printDiff(apiFromDB, expectedAPI));

    }

    @Test
    public void testFingerprintAfterUpdatingEndpoint() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        apiDAO.addEndpoint(endpoint);

        String fingerprintBeforeUpdate = ETagUtils.generateETag(apiDAO.getLastUpdatedTimeOfEndpoint(endpoint.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);
        Thread.sleep(1);

        Endpoint updatedEndpoint = SampleTestObjectCreator.createUpdatedEndpoint();
        apiDAO.updateEndpoint(updatedEndpoint);
        String fingerprintAfterUpdate = ETagUtils.generateETag(apiDAO.getLastUpdatedTimeOfEndpoint(endpoint.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);

        Assert.assertNotEquals(fingerprintBeforeUpdate, fingerprintAfterUpdate);
    }

    @Test
    public void testFingerprintAfterUpdatingDocument() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        apiDAO.addDocumentInfo(api.getId(), documentInfo);

        String fingerprintBeforeUpdate = ETagUtils
                .generateETag(apiDAO.getLastUpdatedTimeOfDocument(documentInfo.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);
        Thread.sleep(1);

        DocumentInfo updateDocument = SampleTestObjectCreator.createAlternativeDocumentationInfo(documentInfo.getId());
        apiDAO.updateDocumentInfo(api.getId(), updateDocument, ADMIN);
        String fingerprintAfterUpdate = ETagUtils
                .generateETag(apiDAO.getLastUpdatedTimeOfDocument(documentInfo.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);

        Assert.assertNotEquals(fingerprintBeforeUpdate, fingerprintAfterUpdate);
    }

    @Test
    public void testFingerprintAfterUpdatingDocumentContent() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        apiDAO.addDocumentInfo(api.getId(), documentInfo);
        apiDAO.addDocumentInlineContent(documentInfo.getId(),
                SampleTestObjectCreator.createDefaultInlineDocumentationContent(), ADMIN);

        String fingerprintBeforeUpdate = ETagUtils
                .generateETag(apiDAO.getLastUpdatedTimeOfDocumentContent(api.getId(), documentInfo.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);
        Thread.sleep(1);

        apiDAO.addDocumentInlineContent(documentInfo.getId(),
                SampleTestObjectCreator.createAlternativeInlineDocumentationContent(), ADMIN);
        String fingerprintAfterUpdate = ETagUtils
                .generateETag(apiDAO.getLastUpdatedTimeOfDocumentContent(api.getId(), documentInfo.getId()));
        Assert.assertNotNull(fingerprintBeforeUpdate);

        Assert.assertNotEquals(fingerprintBeforeUpdate, fingerprintAfterUpdate);
    }

    @Test
    public void testAPIWorkflowStatusUpdate() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);        
        Thread.sleep(10);
        apiDAO.updateAPIWorkflowStatus(api.getId(), APIMgtConstants.APILCWorkflowStatus.PENDING);
        
        API apiFromDB = apiDAO.getAPI(api.getId());

        Assert.assertNotNull(apiFromDB);
        Assert.assertNotEquals(api.getLastUpdatedTime(), apiFromDB.getLastUpdatedTime());
    }
}
