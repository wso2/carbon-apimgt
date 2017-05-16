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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.TestUtil;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIComparator;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.core.util.EndPointComparator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ApiDAOImplIT extends DAOIntegrationTestBase {
    private static final String ADMIN = "admin";
    private static final String CREATOR = "creator";
    private static final String CUSTOMER_ROLE = "customer";
    private static final String EMPLOYEE_ROLE = "employee";
    private static final String MANAGER_ROLE = "manager";

    @Test
    public void testGetAPIsByStatusStore() throws Exception {

        //Add few APIs with different attributes.
        List<String> apiIDList = createAPIsAndGetIDsOfAddedAPIs();
        Set<String> userRoles = new HashSet<>();
        List<String> statuses = new ArrayList<>();
        statuses.add(APIStatus.PUBLISHED.getStatus());
        statuses.add(APIStatus.PROTOTYPED.getStatus());
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        String[] expectedAPINames;
        List<API> apiResults;

        //Asserting results for different search queries
        //Role based API retrieval for a user with "admin" role
        userRoles.add(ADMIN);
        apiResults = apiDAO.getAPIsByStatus(userRoles, statuses, ApiType.STANDARD);
        List<String> resultAPINameList = new ArrayList<>();
        for (API api : apiResults) {
            resultAPINameList.add(api.getName());
        }
        expectedAPINames = new String[]{"PublicAPI", "AdminManagerAPI"};
        Assert.assertTrue(resultAPINameList.containsAll(Arrays.asList(expectedAPINames)) &&
                Arrays.asList(expectedAPINames).containsAll(resultAPINameList));
        userRoles.clear();
        apiResults.clear();
        resultAPINameList.clear();

        //Role based API retrieval for a user with "manager" role
        userRoles.add(MANAGER_ROLE);
        apiResults = apiDAO.getAPIsByStatus(userRoles, statuses, ApiType.STANDARD);
        for (API api : apiResults) {
            resultAPINameList.add(api.getName());
        }
        expectedAPINames = new String[]{"PublicAPI", "ManagerOnlyAPI", "AdminManagerAPI",
                "NonAdminAPI"};
        Assert.assertTrue(resultAPINameList.containsAll(Arrays.asList(expectedAPINames)) &&
                Arrays.asList(expectedAPINames).containsAll(resultAPINameList));
        userRoles.clear();
        apiResults.clear();
        resultAPINameList.clear();

        //Role based API retrieval for a user with "manager", "employee" and "customer" roles
        userRoles.add(MANAGER_ROLE);
        userRoles.add(EMPLOYEE_ROLE);
        userRoles.add(CUSTOMER_ROLE);
        apiResults = apiDAO.getAPIsByStatus(userRoles, statuses, ApiType.STANDARD);

        for (API api : apiResults) {
            resultAPINameList.add(api.getName());
        }
        expectedAPINames = new String[]{"PublicAPI", "ManagerOnlyAPI", "AdminManagerAPI",
                "EmployeeAPI", "NonAdminAPI"};
        Assert.assertTrue(resultAPINameList.containsAll(Arrays.asList(expectedAPINames)) &&
                Arrays.asList(expectedAPINames).containsAll(resultAPINameList));
        userRoles.clear();
        apiResults.clear();
        resultAPINameList.clear();

        //cleanup added APIs
        for (String apiID : apiIDList) {
            apiDAO.deleteAPI(apiID);
        }
    }

    @Test
    public void testAttributeSearchAPIsStore() throws Exception {

        //Add few APIs with different attributes.
        List<String> apiIDList = createAPIsAndGetIDsOfAddedAPIs();
        List<String> userRoles = new ArrayList<>();
        Map<String, String> attributeMap = new HashMap<>();
        String[] expectedAPINames;

        //Asserting results for different search queries
        //Attribute search for "provider", for "admin" role
        userRoles.add(ADMIN);
        attributeMap.put("provider", "a");
        expectedAPINames = new String[]{"PublicAPI", "AdminManagerAPI"};
        Assert.assertTrue(compareResults(userRoles, attributeMap, expectedAPINames));
        userRoles.clear();
        attributeMap.clear();

        //Attribute search for "version", for "manager" role
        userRoles.add(MANAGER_ROLE);
        attributeMap.put("version", "2.3");
        expectedAPINames = new String[]{"PublicAPI", "ManagerOnlyAPI"};
        Assert.assertTrue(compareResults(userRoles, attributeMap, expectedAPINames));
        userRoles.clear();
        attributeMap.clear();

        //Attribute search for "context", for "manager", "employee" and "customer" roles
        userRoles.add(MANAGER_ROLE);
        userRoles.add(EMPLOYEE_ROLE);
        userRoles.add(CUSTOMER_ROLE);
        attributeMap.put("context", "Man");
        expectedAPINames = new String[]{"ManagerOnlyAPI", "AdminManagerAPI"};
        Assert.assertTrue(compareResults(userRoles, attributeMap, expectedAPINames));
        userRoles.clear();
        attributeMap.clear();

        //Attribute search for "description", for "admin" role
        userRoles.add(ADMIN);
        attributeMap.put("description", "Admin and manager");
        expectedAPINames = new String[]{"AdminManagerAPI"};
        Assert.assertTrue(compareResults(userRoles, attributeMap, expectedAPINames));
        userRoles.clear();
        attributeMap.clear();

        //Attribute search for "tags", for "manager", "employee" and "customer" roles
        userRoles.add(MANAGER_ROLE);
        userRoles.add(EMPLOYEE_ROLE);
        userRoles.add(CUSTOMER_ROLE);
        attributeMap.put("tags", "E");
        expectedAPINames = new String[]{"ManagerOnlyAPI", "NonAdminAPI"};
        Assert.assertTrue(compareResults(userRoles, attributeMap, expectedAPINames));
        userRoles.clear();
        attributeMap.clear();

        //Attribute search for "subcontext", for "manager", "employee" and "customer" roles
        userRoles.add(MANAGER_ROLE);
        userRoles.add(EMPLOYEE_ROLE);
        userRoles.add(CUSTOMER_ROLE);
        attributeMap.put("subcontext", "C");
        expectedAPINames = new String[]{"AdminManagerAPI", "EmployeeAPI", "NonAdminAPI"};
        Assert.assertTrue(compareResults(userRoles, attributeMap, expectedAPINames));
        userRoles.clear();
        attributeMap.clear();

        //cleanup added APIs
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        for (String apiID : apiIDList) {
            apiDAO.deleteAPI(apiID);
        }
    }

    /**
     * This method creates few APIs and returns the ID list of those APIs
     * @return the ID list of added APIs
     * @throws APIMgtDAOException if it fails to creates APIs
     */
    private List<String> createAPIsAndGetIDsOfAddedAPIs() throws APIMgtDAOException {

        Set<String> visibleRoles = new HashSet<>();
        Set<String> apiTags = new HashSet<>();
        List<String> apiIDList = new ArrayList<>();
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        Map<String, UriTemplate> uriTemplateMap;

        //Construct an API which has public visibility
        apiTags.add("Car");
        apiTags.add("Van");
        uriTemplateMap = getUriTemplateMap(new String[]{"/toyota", "/nissan"});
        addAPIWithGivenData("PublicAPI", "1.2.3", "PublicContext", "Paul", API.Visibility.PUBLIC,
                null, APIStatus.CREATED.getStatus(), "This is a public API, visible to all.",
                apiTags, uriTemplateMap, APIStatus.PUBLISHED.getStatus());
        visibleRoles.clear();
        apiTags.clear();
        uriTemplateMap.clear();
        apiIDList.add(apiDAO.getAPIs(ApiType.STANDARD).get(0).getId());

        //Construct an API which is visible to manager role only
        apiTags.add("Pizza");
        apiTags.add("Cake");
        uriTemplateMap = getUriTemplateMap(new String[]{"/pizzahut", "/dominos"});
        visibleRoles.add(MANAGER_ROLE);
        addAPIWithGivenData("ManagerOnlyAPI", "2.3.4", "managerContext", "Mark",
                API.Visibility.RESTRICTED, visibleRoles, APIStatus.CREATED.getStatus(),
                "Users with manager role can view this API.", apiTags, uriTemplateMap,
                APIStatus.PUBLISHED.getStatus());
        visibleRoles.clear();
        apiTags.clear();
        uriTemplateMap.clear();
        apiIDList.add(apiDAO.getAPIs(ApiType.STANDARD).get(1).getId());

        //Construct an API which is visible to admin and manager roles
        apiTags.add("Java");
        uriTemplateMap = getUriTemplateMap(new String[]{"/desktop", "/laptop", "nikoncam"});
        visibleRoles.add(ADMIN);
        visibleRoles.add(MANAGER_ROLE);
        addAPIWithGivenData("AdminManagerAPI", "3.4.5", "adminManager", "Alex",
                API.Visibility.RESTRICTED, visibleRoles, APIStatus.CREATED.getStatus(),
                "Admin and manager can see this API.", apiTags, uriTemplateMap,
                APIStatus.PUBLISHED.getStatus());
        visibleRoles.clear();
        apiTags.clear();
        uriTemplateMap.clear();
        apiIDList.add(apiDAO.getAPIs(ApiType.STANDARD).get(2).getId());

        //Construct an API in created state, this should not be shown in store
        apiTags.add("Movie");
        apiTags.add("TV");
        uriTemplateMap = getUriTemplateMap(new String[]{"/cnn", "/bbc"});
        addAPIWithGivenData("CreatedStateAPI", "4.5.6", "createdContext", "Colin",
                API.Visibility.PUBLIC, null, APIStatus.CREATED.getStatus(),
                "This API is in created state. Should not be shown in store.", apiTags,
                uriTemplateMap, APIStatus.CREATED.getStatus());
        visibleRoles.clear();
        apiTags.clear();
        uriTemplateMap.clear();
        apiIDList.add(apiDAO.getAPIs(ApiType.STANDARD).get(3).getId());

        //Construct an API which is visible to employee role only
        apiTags.add("Salary");
        apiTags.add("Bonus");
        uriTemplateMap = getUriTemplateMap(new String[]{"/cash", "/cheque"});
        visibleRoles.add(EMPLOYEE_ROLE);
        addAPIWithGivenData("EmployeeAPI", "5.6.7", "employeeCtx", "Emma",
                API.Visibility.RESTRICTED, visibleRoles, APIStatus.CREATED.getStatus(),
                "API for Employees.", apiTags, uriTemplateMap, APIStatus.PUBLISHED.getStatus());
        visibleRoles.clear();
        apiTags.clear();
        uriTemplateMap.clear();
        apiIDList.add(apiDAO.getAPIs(ApiType.STANDARD).get(4).getId());

        //Construct an API which is visible to all roles, except admin role
        apiTags.add("Science");
        apiTags.add("Technology");
        uriTemplateMap = getUriTemplateMap(new String[]{"/velocity", "/distance"});
        visibleRoles.add(EMPLOYEE_ROLE);
        visibleRoles.add(MANAGER_ROLE);
        visibleRoles.add(CUSTOMER_ROLE);
        addAPIWithGivenData("NonAdminAPI", "6.7.8", "nonAdmin", "Nancy", API.Visibility.RESTRICTED,
                visibleRoles, APIStatus.CREATED.getStatus(),
                "This API should be visible to all roles, except admin role.", apiTags,
                uriTemplateMap, APIStatus.PROTOTYPED.getStatus());
        visibleRoles.clear();
        apiTags.clear();
        uriTemplateMap.clear();
        apiIDList.add(apiDAO.getAPIs(ApiType.STANDARD).get(5).getId());

        return apiIDList;
    }

    /**
     * This method creates a map containing the resources to be added to the API
     * @param resourceArray resources to be added to HTTP verbs in the API
     * @return a map containing the resources to be added
     */
    private Map<String, UriTemplate> getUriTemplateMap(String[] resourceArray) {

        Map<String, UriTemplate> uriTemplateMap = new HashMap<>();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();

        for (String resource : resourceArray) {
            String randomIdString = UUID.randomUUID().toString();
            uriTemplateBuilder.endpoint(Collections.emptyMap()).
                    templateId(randomIdString).uriTemplate(resource).
                    authType(APIMgtConstants.AUTH_APPLICATION_LEVEL_TOKEN).policy("Unlimited").
                    httpVerb(APIMgtConstants.FunctionsConstants.GET);
            uriTemplateMap.put(randomIdString, uriTemplateBuilder.build());
        }
        return uriTemplateMap;
    }

    /**
     * Compare the results of attribute search in store
     * @param userRoles List of the roles of the user.
     * @param attributeMap Map containing the attributes to be searched
     * @param expectedAPINames List of expected APIs.
     * @return true if returned API list has all expected APIs, false otherwise
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    private boolean compareResults(List<String> userRoles, Map<String, String> attributeMap,
                                   String[] expectedAPINames) throws APIMgtDAOException {

        ApiDAO apiDAO = DAOFactory.getApiDAO();
        List<API> apiList = apiDAO.attributeSearchAPIsStore(userRoles, attributeMap, 10, 0);
        List<String> resultAPINameList = new ArrayList<>();
        for (API api : apiList) {
            resultAPINameList.add(api.getName());
        }
        List<String> expectedAPINameList = Arrays.asList(expectedAPINames);
        //check if returned API list has all expected APIs
        return resultAPINameList.containsAll(expectedAPINameList) &&
                expectedAPINameList.containsAll(resultAPINameList);
    }

    /**
     * This method adds an API with given information
     * @param apiName API name
     * @param apiVersion API version
     * @param apiContext API context
     * @param apiProvider API provider
     * @param apiVisibility API visibility
     * @param visibleRoles roles that are eligible to consume the API
     * @param initialLifecycleStatus initial lifecycle status
     * @param description API description
     * @param tags tag list for the API
     * @param uriTemplates URI templates, i.e - resources
     * @param finalLifecycleStatus final lifecycle status
     * @throws APIMgtDAOException if it fails to add the API
     */
    private void addAPIWithGivenData(String apiName, String apiVersion, String apiContext,
                                     String apiProvider, API.Visibility apiVisibility,
                                     Set<String> visibleRoles, String initialLifecycleStatus,
                                     String description, Set<String> tags,
                                     Map<String, UriTemplate> uriTemplates,
                                     String finalLifecycleStatus) throws APIMgtDAOException {

        API.APIBuilder builder;
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        builder = SampleTestObjectCreator.createCustomAPI(apiName, apiVersion, apiContext);
        builder.createdBy(apiProvider);
        builder.visibility(apiVisibility);
        //visible roles should be added for restricted APIs
        if (apiVisibility != null && API.Visibility.RESTRICTED.toString().
                equalsIgnoreCase(apiVisibility.toString())) {
            builder.visibleRoles(visibleRoles);
        }
        builder.lifeCycleStatus(initialLifecycleStatus);
        builder.description(description);
        builder.tags(tags);
        builder.uriTemplates(uriTemplates);
        builder.endpoint(Collections.emptyMap());
        API api = builder.build();
        apiDAO.addAPI(api);
        apiDAO.changeLifeCycleStatus(api.getId(), finalLifecycleStatus);
    }
    private static final Logger log = LoggerFactory.getLogger(ApiDAOImplIT.class);

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
        Assert.assertEquals(apiDAO.getAPIs(ApiType.STANDARD).size(), 1);
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
        Assert.assertEquals(apiDAO.getAPIs(ApiType.STANDARD).size(), 2);
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
        Assert.assertEquals(apiDAO.getAPIs(ApiType.STANDARD).size(), 1);
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

        List<API> apiList = apiDAO.getAPIs(ApiType.STANDARD);
        Assert.assertTrue(apiList.isEmpty());

        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api1 = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api1);

        builder = SampleTestObjectCreator.createAlternativeAPI();
        API api2 = builder.build();

        apiDAO.addAPI(api2);

        apiList = apiDAO.getAPIs(ApiType.STANDARD);

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

        List<API> apiList = apiDAO.getAPIsByStatus(singleStatus, ApiType.STANDARD);

        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, publishedAPIsSummary, new APIComparator()));

        // Filter APIs by two statuses
        List<String> twoStatuses = new ArrayList<>();
        twoStatuses.add(publishedStatus);
        twoStatuses.add(blockedStatus);

        apiList = apiDAO.getAPIsByStatus(twoStatuses, ApiType.STANDARD);

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

        apiList = apiDAO.getAPIsByStatus(multipleStatuses, ApiType.STANDARD);

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

        // API Provider, the person who owns the API
        final String provider = "John";

        // Create test data
        Map<String, API> apis = new HashMap<>();
        apis.put(mixedCaseString,
                SampleTestObjectCreator.createUniqueAPI().name(mixedCaseString).provider(provider).build());
        apis.put(lowerCaseString,
                SampleTestObjectCreator.createUniqueAPI().name(lowerCaseString).provider(provider).build());
        apis.put(upperCaseString,
                SampleTestObjectCreator.createUniqueAPI().name(upperCaseString).provider(provider).build());
        apis.put(charSymbolNumString,
                SampleTestObjectCreator.createUniqueAPI().name(charSymbolNumString).provider(provider).build());
        apis.put(symbolSpaceString,
                SampleTestObjectCreator.createUniqueAPI().name(symbolSpaceString).provider(provider).build());

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
        // Expected common string formatApiSearch result
        List<API> commonStringResult = new ArrayList<>();
        commonStringResult.add(apis.get(mixedCaseString));
        commonStringResult.add(apis.get(lowerCaseString));
        commonStringResult.add(apis.get(upperCaseString));

        // Search by common mixed case
        List<API> apiList = apiDAO.searchAPIs(new HashSet<>(), provider, commonMixedCaseSearchString, ApiType
                .STANDARD, 0, 10);
        Assert.assertEquals(apiList.size(), 3);
        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, commonStringResult, new APIComparator()),
                TestUtil.printListDiff(apiList, commonStringResult));

        // Search by common lower case
        apiList = apiDAO.searchAPIs(new HashSet<>(), provider, commonLowerCaseSearchString, ApiType.STANDARD, 0, 10);
        Assert.assertEquals(apiList.size(), 3);
        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, commonStringResult, new APIComparator()),
                TestUtil.printListDiff(apiList, commonStringResult));

        // Search by common upper case
        apiList = apiDAO.searchAPIs(new HashSet<>(), provider, commonUpperCaseSearchString, ApiType.STANDARD, 0, 10);
        Assert.assertEquals(apiList.size(), 3);
        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, commonStringResult, new APIComparator()),
                TestUtil.printListDiff(apiList, commonStringResult));

        // Search by symbol
        apiList = apiDAO.searchAPIs(new HashSet<>(), provider, symbolSearchString, ApiType.STANDARD, 0, 10);
        Assert.assertEquals(apiList.size(), 1);
        API actualAPI = apiList.get(0);
        API expectedAPI = apis.get(charSymbolNumString);
        Assert.assertEquals(actualAPI, expectedAPI, TestUtil.printDiff(actualAPI, expectedAPI));

        // Search by number
        apiList = apiDAO.searchAPIs(new HashSet<>(), provider, numberSearchString, ApiType.STANDARD, 0, 10);
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

        Assert.assertTrue(apiDAO.isAPINameExists(api.getName(), api.getProvider(), ApiType.STANDARD));
        Assert.assertFalse(apiDAO.isAPINameExists("Not-Exists", api.getProvider(), ApiType.STANDARD));

        final String upperCaseName = "CAPITAL";

        // Add API with upper case name
        api = SampleTestObjectCreator.createUniqueAPI().name(upperCaseName).build();
        apiDAO.addAPI(api);
        // Check with upper case format
        Assert.assertTrue(apiDAO.isAPINameExists(upperCaseName, api.getProvider(), ApiType.STANDARD));
        // Check with lower case format
        Assert.assertTrue(apiDAO.isAPINameExists(upperCaseName.toLowerCase(Locale.ENGLISH), api.getProvider(),
                                                    ApiType.STANDARD));
        // Check with mixed case format
        Assert.assertTrue(apiDAO.isAPINameExists(upperCaseName.substring(0, 3) +
                upperCaseName.substring(3).toLowerCase(Locale.ENGLISH), api.getProvider(), ApiType.STANDARD));

        final String lowerCaseName = "simple";

        // Add API with upper case name
        api = SampleTestObjectCreator.createUniqueAPI().name(lowerCaseName).build();
        apiDAO.addAPI(api);
        // Check with lower case format
        Assert.assertTrue(apiDAO.isAPINameExists(lowerCaseName, api.getProvider(), ApiType.STANDARD));
        // Check with upper case format
        Assert.assertTrue(apiDAO.isAPINameExists(lowerCaseName.toUpperCase(Locale.ENGLISH), api.getProvider(),
                                                    ApiType.STANDARD));
        // Check with mixed case format
        Assert.assertTrue(apiDAO.isAPINameExists(lowerCaseName.substring(0, 3) +
                lowerCaseName.substring(3).toUpperCase(Locale.ENGLISH), api.getProvider(), ApiType.STANDARD));

        // Create same API for different providers and check for existence
        final String sameName = "same";

        API api1 = SampleTestObjectCreator.createUniqueAPI().name(sameName).build();
        apiDAO.addAPI(api1);

        API api2 = SampleTestObjectCreator.createUniqueAPI().name(sameName).build();
        apiDAO.addAPI(api2);

        Assert.assertTrue(apiDAO.isAPINameExists(sameName, api1.getProvider(), ApiType.STANDARD));
        Assert.assertTrue(apiDAO.isAPINameExists(sameName, api2.getProvider(), ApiType.STANDARD));
        Assert.assertFalse(apiDAO.isAPINameExists(sameName, "no_such_provider", ApiType.STANDARD));
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
        Set<String> roles = new HashSet<>();
        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put("name", api.getName());
        List<API> apiList = apiDAO.attributeSearchAPIs(roles, api.getProvider(), attributeMap, ApiType.STANDARD, 0, 2);
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
        List<API> apiList = apiDAO.searchAPIsByStatus(searchString, statuses, ApiType.STANDARD);
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
        Assert.assertEquals(endpoint, retrieved, TestUtil.printDiff(endpoint, retrieved));
    }

    @Test(description = "Test adding API with endpointMap")
    public void testAddEndPointsForApi() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        Map<String, Endpoint> endpointMap = new HashMap<>();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, new Endpoint.Builder().id(SampleTestObjectCreator
                .endpointId).applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build());
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
        Endpoint apiSpecificEndpoint = new Endpoint.Builder(SampleTestObjectCreator.createAlternativeEndpoint()).name
                ("APISpecific").applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).id(UUID.randomUUID().toString())
                .build();
        apiDAO.addEndpoint(endpoint1);
        apiDAO.addEndpoint(endpoint2);
        apiDAO.addEndpoint(apiSpecificEndpoint);
        List<Endpoint> endpointListAdd = new ArrayList<>();
        endpointListAdd.add(endpoint1);
        endpointListAdd.add(endpoint2);
        List<Endpoint> endpointList = apiDAO.getEndpoints();
        Assert.assertNotEquals(3, endpointList.size());
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
        Set<String> labelNames = new HashSet<>();
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
        Set<String> labelNames = new HashSet<>();
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
        Set<String> labelNames = new HashSet<>();
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
        Assert.assertEquals(apiFromDB.getLabels(), expectedAPI.getLabels());

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

    @Test
    public void testCheckContextExist() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI().apiDefinition(SampleTestObjectCreator
                .apiDefinition);
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);
        apiDAO.changeLifeCycleStatus(api.getId(), APIStatus.PUBLISHED.getStatus());
        Assert.assertTrue(apiDAO.isAPIContextExists(api.getContext()));
        Assert.assertFalse(apiDAO.isAPIContextExists("/abc"));
    }


    @Test
    public void testDocumentAdd() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI().apiDefinition(SampleTestObjectCreator
                .apiDefinition);
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultFileDocumentationInfo();
        Assert.assertFalse(apiDAO.isDocumentExist(api.getId(), documentInfo));
        apiDAO.addDocumentInfo(api.getId(), documentInfo);
        apiDAO.addDocumentFileContent(documentInfo.getId(), IOUtils.toInputStream(SampleTestObjectCreator
                .createDefaultInlineDocumentationContent()), "inline1.txt", documentInfo.getCreatedBy());
        Assert.assertTrue(apiDAO.isDocumentExist(api.getId(), documentInfo));
        List<DocumentInfo> documentInfoList = apiDAO.getDocumentsInfoList(api.getId());
        Assert.assertEquals(documentInfoList.get(0), documentInfo);
        apiDAO.deleteDocument(documentInfo.getId());
        Assert.assertFalse(apiDAO.isDocumentExist(api.getId(), documentInfo));
    }

    @Test
    public void testAddApiAndResourceSpecificEndpointToApi() throws APIMgtDAOException {
        Endpoint apiSpecificEndpoint = new Endpoint.Builder(SampleTestObjectCreator.createMockEndpoint())
                .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build();
        Endpoint urlSpecificEndpoint = new Endpoint.Builder(SampleTestObjectCreator.createMockEndpoint()).id(UUID
                .randomUUID().toString()).applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).name("URI1")
                .build();
        Endpoint endpointToInsert = SampleTestObjectCreator.createAlternativeEndpoint();
        Endpoint globalEndpoint = new Endpoint.Builder().applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).id
                (endpointToInsert.getId()).build();
        Map<String, Endpoint> apiEndpointMap = new HashMap();

        apiEndpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, apiSpecificEndpoint);
        apiEndpointMap.put(APIMgtConstants.SANDBOX_ENDPOINT, globalEndpoint);
        Map<String, Endpoint> uriTemplateEndpointMap = new HashMap();
        uriTemplateEndpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT, urlSpecificEndpoint);
        Map<String, UriTemplate> uriTemplateMap = SampleTestObjectCreator.getMockUriTemplates();
        uriTemplateMap.forEach((k, v) -> {
            UriTemplate uriTemplate = new UriTemplate.UriTemplateBuilder(v).endpoint(uriTemplateEndpointMap).build();
            uriTemplateMap.replace(k, uriTemplate);
        });
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API api = SampleTestObjectCreator.createDefaultAPI().apiDefinition(SampleTestObjectCreator
                .apiDefinition).endpoint(apiEndpointMap).uriTemplates(uriTemplateMap).build();
        apiDAO.addEndpoint(endpointToInsert);
        apiDAO.addAPI(api);
        Map<String, Endpoint> retrievedApiEndpoint = apiDAO.getAPI(api.getId()).getEndpoint();
        Assert.assertTrue(apiDAO.isEndpointAssociated(globalEndpoint.getId()));
        Assert.assertEquals(apiEndpointMap, retrievedApiEndpoint);
        apiDAO.deleteAPI(api.getId());
        Endpoint retrievedGlobal = apiDAO.getEndpoint(globalEndpoint.getId());
        Assert.assertNotNull(retrievedGlobal);
        Assert.assertEquals(endpointToInsert, retrievedGlobal);
    }
    @Test
    public void testAddGetComment() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI()
                .apiDefinition(SampleTestObjectCreator.apiDefinition);
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        apiDAO.addComment(comment, api.getId());
        Comment commentFromDB = apiDAO.getCommentByUUID(comment.getUuid(), api.getId());
        Assert.assertNotNull(commentFromDB);
    }

    @Test
    public void testDeleteComment() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI()
                .apiDefinition(SampleTestObjectCreator.apiDefinition);
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        apiDAO.addComment(comment, api.getId());
        apiDAO.deleteComment(comment.getUuid(), api.getId());
        Comment commentFromDB = apiDAO.getCommentByUUID(comment.getUuid(), api.getId());
        Assert.assertNull(commentFromDB);
    }

    @Test
    public void testUpdateComment() throws Exception {
        String newCommentText = "updated comment";
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI()
                .apiDefinition(SampleTestObjectCreator.apiDefinition);
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);
        Comment comment1 = SampleTestObjectCreator.createDefaultComment(api.getId());
        apiDAO.addComment(comment1, api.getId());
        Comment comment2 = SampleTestObjectCreator.createDefaultComment(api.getId());
        comment2.setCommentText(newCommentText);
        apiDAO.updateComment(comment2, comment1.getUuid(), api.getId());
        Comment commentFromDB = apiDAO.getCommentByUUID(comment1.getUuid(), api.getId());
        Assert.assertNotNull(commentFromDB);
        Assert.assertEquals(newCommentText, commentFromDB.getCommentText());

    }

    @Test(expectedExceptions = APIMgtDAOException.class)
    public void testGetAPIByStatus() throws Exception {

        ApiDAO apiDAO = DAOFactory.getApiDAO();
        // Define statuses used in test
        final String publishedStatus = "PUBLISHED";
        final String createdStatus = "CREATED";
        final String blockedStatus = "BLOCKED";
        // Define number of APIs to be created for a given status by role
        final int numberOfPublishedAdmin = 4;
        final int numberOfCreatedAdmin = 2;
        final int numberOfBlockedAdmin = 1;
        final int numberOfPublishedCreator = 5;
        final int numberOfCreatedCreator = 3;
        final int numberOfBlockedCreator = 1;

        Set<String> singleRole = new HashSet<>();
        singleRole.add(ADMIN);
        // Add APIs
        List<API> publishedAPIsSummaryAdmin = new ArrayList<>();
        testAddGetEndpoint();
        for (int i = 0; i < numberOfPublishedAdmin; ++i) {
            API api = SampleTestObjectCreator.createUniqueAPI().lifeCycleStatus(publishedStatus).
                    visibleRoles(singleRole).build();
            publishedAPIsSummaryAdmin.add(SampleTestObjectCreator.getSummaryFromAPI(api));
            apiDAO.addAPI(api);
        }
        List<API> createdAPIsSummaryAdmin = new ArrayList<>();
        for (int i = 0; i < numberOfCreatedAdmin; ++i) {
            API api = SampleTestObjectCreator.createUniqueAPI().lifeCycleStatus(createdStatus).
                    visibleRoles(singleRole).build();
            createdAPIsSummaryAdmin.add(SampleTestObjectCreator.getSummaryFromAPI(api));
            apiDAO.addAPI(api);
        }
        List<API> blockedAPIsSummaryAdmin = new ArrayList<>();
        for (int i = 0; i < numberOfBlockedAdmin; ++i) {
            API api = SampleTestObjectCreator.createUniqueAPI().lifeCycleStatus(blockedStatus).
                    visibleRoles(singleRole).build();
            blockedAPIsSummaryAdmin.add(SampleTestObjectCreator.getSummaryFromAPI(api));
            apiDAO.addAPI(api);
        }
        // Filter APIs by single status
        List<String> singleStatus = new ArrayList<>();
        singleStatus.add(publishedStatus);
        List<API> apiList = apiDAO.getAPIsByStatus(singleRole, singleStatus, ApiType.STANDARD);
        Assert.assertTrue(APIUtils.isListsEqualIgnoreOrder(apiList, publishedAPIsSummaryAdmin, new APIComparator()));
        List<String> twoStatus = new ArrayList<>();
        twoStatus.add(createdStatus);
        twoStatus.add(blockedStatus);
        Set<String> twoRoles = new HashSet<>();
        twoRoles.add(ADMIN);
        twoRoles.add(CREATOR);
        // Add APIs
        List<API> publishedAPIsSummaryTwoRoles = new ArrayList<>();
        testAddGetEndpoint();
        for (int i = 0; i < numberOfPublishedCreator; ++i) {
            API api = SampleTestObjectCreator.createUniqueAPI().lifeCycleStatus(publishedStatus).
                    visibleRoles(twoRoles).build();
            publishedAPIsSummaryTwoRoles.add(SampleTestObjectCreator.getSummaryFromAPI(api));
            apiDAO.addAPI(api);
        }
        List<API> createdAPIsSummaryTwoRoles = new ArrayList<>();
        for (int i = 0; i < numberOfCreatedCreator; ++i) {
            API api = SampleTestObjectCreator.createUniqueAPI().lifeCycleStatus(createdStatus).
                    visibleRoles(twoRoles).build();
            createdAPIsSummaryTwoRoles.add(SampleTestObjectCreator.getSummaryFromAPI(api));
            apiDAO.addAPI(api);
        }
        List<API> blockedAPIsSummaryTwoRoles = new ArrayList<>();
        for (int i = 0; i < numberOfBlockedCreator; ++i) {
            API api = SampleTestObjectCreator.createUniqueAPI().lifeCycleStatus(blockedStatus).
                    visibleRoles(twoRoles).build();
            blockedAPIsSummaryTwoRoles.add(SampleTestObjectCreator.getSummaryFromAPI(api));
            apiDAO.addAPI(api);
        }
        apiList = apiDAO.getAPIsByStatus(twoRoles, twoStatus, ApiType.STANDARD);
        Assert.assertEquals(apiList.size(), publishedAPIsSummaryTwoRoles.size() + blockedAPIsSummaryTwoRoles.size());
        for (API api : publishedAPIsSummaryTwoRoles) {
            Assert.assertTrue(apiList.contains(api));
            apiList.remove(api);
        }
        for (API api : blockedAPIsSummaryTwoRoles) {
            Assert.assertTrue(apiList.contains(api));
            apiList.remove(api);
        }
        Assert.assertTrue(apiList.isEmpty());

    }

    @Test
    public void testUpdateAPIWithBlankwsdlUri() throws Exception {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        testAddGetEndpoint();
        apiDAO.addAPI(api);
        Map permissionMap = new HashMap();
        permissionMap.put(APIMgtConstants.Permission.UPDATE, APIMgtConstants.Permission.UPDATE_PERMISSION);
        builder = SampleTestObjectCreator.createAlternativeAPI().permissionMap(permissionMap).wsdlUri(null);
        API substituteAPI = builder.build();
        apiDAO.updateAPI(api.getId(), substituteAPI);
        API apiFromDB = apiDAO.getAPI(api.getId());
        API expectedAPI = SampleTestObjectCreator.copyAPIIgnoringNonEditableFields(api, substituteAPI);
        Assert.assertNotNull(apiFromDB);
        Assert.assertEquals(apiFromDB, expectedAPI, TestUtil.printDiff(apiFromDB, expectedAPI));

    }
}
