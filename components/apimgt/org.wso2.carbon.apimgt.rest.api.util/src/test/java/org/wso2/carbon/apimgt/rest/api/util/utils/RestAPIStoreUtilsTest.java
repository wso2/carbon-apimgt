/*
 *
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.util.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashSet;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiCommonUtil.class, Application.class, Subscriber.class, ServiceReferenceHolder.class, RestApiUtil.class, LogFactory.class, RestAPIStoreUtils.class, APIManagerFactory.class, MultitenantUtils.class})
public class RestAPIStoreUtilsTest {

    private static Log mocklog;

    @BeforeClass
    public static void mockStaticInitializer() {

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString())).thenReturn("true");
        mocklog = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(mocklog);
    }

    @Test
    public void testIsUserOwnerOfApplicationMatchUsername() {

        String username = "Jacob";

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        PowerMockito.mockStatic(Application.class);
        Application mockApplication = Mockito.mock(Application.class);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(mockApplication.getSubscriber()).thenReturn(subscriber);
        Mockito.when(subscriber.getName()).thenReturn(username);
        Assert.assertEquals(true, RestAPIStoreUtils.isUserOwnerOfApplication(mockApplication));
    }

    @Test
    public void testIsUserOwnerOfApplicationDifferentUsername() {

        String username = "Jacob";
        String anotherUsername = "Willam Black";

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        PowerMockito.mockStatic(Application.class);
        Application mockApplication = Mockito.mock(Application.class);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(mockApplication.getSubscriber()).thenReturn(subscriber);
        Mockito.when(subscriber.getName()).thenReturn(anotherUsername);
        Assert.assertEquals(false, RestAPIStoreUtils.isUserOwnerOfApplication(mockApplication));
    }

    @Test
    public void testIsUserOwnerOfApplicationlowerCase() {

        String username = "William Black";
        String lowerCaseUsername = "william black";

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        PowerMockito.mockStatic(Application.class);
        Application mockApplication = Mockito.mock(Application.class);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(mockApplication.getSubscriber()).thenReturn(subscriber);
        Mockito.when(subscriber.getName()).thenReturn(lowerCaseUsername);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        Mockito.when(configuration.getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS)).thenReturn("true");
        Assert.assertEquals(true, RestAPIStoreUtils.isUserOwnerOfApplication(mockApplication));
    }

    @Test
    public void testIsUserAccessAllowedForAPIByUUID() throws APIManagementException {

        String username = "Chandler";
        String apiUUID = "API_2345678";

        APIConsumer consumer = Mockito.mock(APIConsumer.class);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        Mockito.when(RestApiCommonUtil.getLoggedInUserConsumer()).thenReturn(consumer);
        API api = Mockito.mock(API.class);
        Mockito.when(consumer.getLightweightAPIByUUID(Mockito.anyString(), Mockito.anyString())).thenReturn(api);
        Assert.assertEquals(true, RestAPIStoreUtils.isUserAccessAllowedForAPIByUUID(apiUUID, "wso2.com"));
    }

    @Test
    public void testIsUserAccessAllowedForAPIByUUIDUnexpectedFailure() throws APIManagementException {

        String username = "Chandler";
        String errorMessage = "";
        String apiUUID = "API_2345678";
        APIConsumer consumer = Mockito.mock(APIConsumer.class);

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        Mockito.when(RestApiCommonUtil.getLoggedInUserConsumer()).thenReturn(consumer);
        Mockito.when(consumer.getLightweightAPIByUUID(Mockito.anyString(), Mockito.anyString())).thenThrow(new APIManagementException("Failed to retrieve the API"));

        try {
            RestAPIStoreUtils.isUserAccessAllowedForAPIByUUID(apiUUID, "wso2.com");
        } catch (APIManagementException ex) {
            errorMessage = ex.getMessage();
        }

        Assert.assertEquals("Failed to retrieve the API " + apiUUID + " to check user " + username + " has access to the API", errorMessage);
    }

    @Test
    public void testIsUserAccessAllowedForAPIByUUIDAuthorizationFailure() throws APIManagementException {

        String username = "Chandler";
        String apiUUID = "API_2345678";
        boolean actualResult = true;

        APIConsumer consumer = Mockito.mock(APIConsumer.class);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        Mockito.when(RestApiCommonUtil.getLoggedInUserConsumer()).thenReturn(consumer);
        Mockito.when(consumer.getLightweightAPIByUUID(Mockito.anyString(), Mockito.anyString())).thenAnswer(invocation -> {
            throw new APIManagementException("Failed to access the API");
        });
        PowerMockito.mockStatic(RestApiUtil.class);
        Mockito.when(RestApiUtil.isDueToAuthorizationFailure(Mockito.any())).thenReturn(true);

        try {
            actualResult = RestAPIStoreUtils.isUserAccessAllowedForAPIByUUID(apiUUID, "wso2.com");
        } catch (APIManagementException ex) {
        }

        Assert.assertEquals(false, actualResult);
        Mockito.verify(mocklog).info("user " + username + " failed to access the API " + apiUUID + " due to an authorization failure");
    }

    @Test
    public void testIsUserAccessAllowedForApplication() throws Exception {

        String groupID = "Group_0001";

        Application application = Mockito.mock(Application.class);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(application.getGroupId()).thenReturn(groupID);
        Mockito.when(application.getSubscriber()).thenReturn(subscriber);
        PowerMockito.spy(RestAPIStoreUtils.class);
        PowerMockito.doReturn(true).when(RestAPIStoreUtils.class, "isUserOwnerOfApplication", application);
        Assert.assertEquals(true, RestAPIStoreUtils.isUserAccessAllowedForApplication(application));
    }

    @Test
    public void testIsUserAccessAllowedForApplicationCheckSharedApps() throws Exception {

        String groupID = "Group_0001,Group_0004,Group_0003,Group_0005";
        String userGroupID = "Group_0000,Group_0002,Group_0003,Group_0007";

        Application application = Mockito.mock(Application.class);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(application.getGroupId()).thenReturn(groupID);
        Mockito.when(application.getSubscriber()).thenReturn(subscriber);
        PowerMockito.spy(RestAPIStoreUtils.class);
        PowerMockito.doReturn(false).when(RestAPIStoreUtils.class, "isUserOwnerOfApplication", application);
        PowerMockito.mockStatic(RestApiUtil.class);
        Mockito.when(RestApiUtil.getLoggedInUserGroupId()).thenReturn(userGroupID);
        Assert.assertEquals(true, RestAPIStoreUtils.isUserAccessAllowedForApplication(application));
    }

    @Test
    public void testIsUserAccessAllowedForApplicationCheckNOSharedApps() throws Exception {

        String groupID = "Group_0001,Group_0004,Group_0003,Group_0005";
        String userGroupID = "Group_0000,Group_0002,Group_0008,Group_0007";

        Application application = Mockito.mock(Application.class);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(application.getGroupId()).thenReturn(groupID);
        Mockito.when(application.getSubscriber()).thenReturn(subscriber);
        PowerMockito.spy(RestAPIStoreUtils.class);
        PowerMockito.doReturn(false).when(RestAPIStoreUtils.class, "isUserOwnerOfApplication", application);
        PowerMockito.mockStatic(RestApiUtil.class);
        Mockito.when(RestApiUtil.getLoggedInUserGroupId()).thenReturn(userGroupID);
        Assert.assertEquals(false, RestAPIStoreUtils.isUserAccessAllowedForApplication(application));
    }

    @Test
    public void testIsUserAccessAllowedForApplicationNull() {

        Application application = null;
        Assert.assertEquals(false, RestAPIStoreUtils.isUserAccessAllowedForApplication(application));
    }

    @Test
    public void testCheckSubscriptionAllowed() throws APIManagementException {

        String username = "Clark Kent";
        String userTenantDomain = "Oscorp.com";
        String subscriptionAvailability = "true";
        String subscriptionAllowedTenants = "Orange.com";
        String inputTier = "Silver";

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        Mockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(userTenantDomain);
        APIConsumer apiConsumer = Mockito.mock(APIConsumer.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        Mockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        Mockito.when(apiManagerFactory.getAPIConsumer(username)).thenReturn(apiConsumer);
        ApiTypeWrapper apiTypeWrapper = Mockito.mock(ApiTypeWrapper.class);
        Mockito.when(apiTypeWrapper.isAPIProduct()).thenReturn(true);
        APIProduct product = Mockito.mock(APIProduct.class);
        Mockito.when(apiTypeWrapper.getApiProduct()).thenReturn(product);
        String providerName = "Bruce";
        APIProductIdentifier apiProductIdentifier = Mockito.mock(APIProductIdentifier.class);
        Mockito.when(product.getId()).thenReturn(apiProductIdentifier);
        Mockito.when(apiProductIdentifier.getProviderName()).thenReturn(providerName);
        Set<Tier> availableTiers = new HashSet<>();
        Mockito.when(product.getAvailableTiers()).thenReturn(availableTiers);
        Mockito.when(product.getSubscriptionAvailability()).thenReturn(subscriptionAvailability);
        Mockito.when(product.getSubscriptionAvailableTenants()).thenReturn(subscriptionAllowedTenants);
        String apiTenantDomain = "Oscorp.com";
        PowerMockito.mockStatic(MultitenantUtils.class);
        Mockito.when(MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName))).thenReturn(apiTenantDomain);
        Tier gold = Mockito.mock(Tier.class);
        availableTiers.add(gold);
        Mockito.when(gold.getName()).thenReturn("Gold");
        Tier silver = Mockito.mock(Tier.class);
        availableTiers.add(silver);
        Mockito.when(silver.getName()).thenReturn("Silver");
        Mockito.when(apiConsumer.isTierDeneid(inputTier)).thenReturn(false);
        RestAPIStoreUtils.checkSubscriptionAllowed(apiTypeWrapper, inputTier);
    }

    @Test
    public void testCheckSubscriptionAllowedWhenTierDenied() throws APIManagementException {

        String username = "Clark Kent";
        String userTenantDomain = "Oscorp.com";
        String subscriptionAvailability = "true";
        String subscriptionAllowedTenants = "Orange.com";
        String inputTier = "Silver";
        String errorMessage = "";

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        Mockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(userTenantDomain);
        APIConsumer apiConsumer = Mockito.mock(APIConsumer.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        Mockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        Mockito.when(apiManagerFactory.getAPIConsumer(username)).thenReturn(apiConsumer);
        ApiTypeWrapper apiTypeWrapper = Mockito.mock(ApiTypeWrapper.class);
        Mockito.when(apiTypeWrapper.isAPIProduct()).thenReturn(true);
        APIProduct product = Mockito.mock(APIProduct.class);
        Mockito.when(apiTypeWrapper.getApiProduct()).thenReturn(product);
        String providerName = "Bruce";
        APIProductIdentifier apiProductIdentifier = Mockito.mock(APIProductIdentifier.class);
        Mockito.when(product.getId()).thenReturn(apiProductIdentifier);
        Mockito.when(apiProductIdentifier.getProviderName()).thenReturn(providerName);
        Set<Tier> availableTiers = new HashSet<>();
        Mockito.when(product.getAvailableTiers()).thenReturn(availableTiers);
        Mockito.when(product.getSubscriptionAvailability()).thenReturn(subscriptionAvailability);
        Mockito.when(product.getSubscriptionAvailableTenants()).thenReturn(subscriptionAllowedTenants);
        String apiTenantDomain = "Oscorp.com";
        PowerMockito.mockStatic(MultitenantUtils.class);
        Mockito.when(MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName))).thenReturn(apiTenantDomain);
        Tier gold = Mockito.mock(Tier.class);
        availableTiers.add(gold);
        Mockito.when(gold.getName()).thenReturn("Gold");
        Tier silver = Mockito.mock(Tier.class);
        availableTiers.add(silver);
        Mockito.when(silver.getName()).thenReturn("Silver");
        Mockito.when(apiConsumer.isTierDeneid(inputTier)).thenReturn(true);

        try {
            RestAPIStoreUtils.checkSubscriptionAllowed(apiTypeWrapper, inputTier);
        } catch (APIMgtAuthorizationFailedException ex) {
            errorMessage = ex.getMessage();
        }
        Assert.assertEquals("Tier " + inputTier + " is not allowed for user " + username, errorMessage);
    }

    @Test
    public void testCheckSubscriptionAllowedNot() throws APIManagementException {

        String username = "Clark Kent";
        String userTenantDomain = "Oscorp.com";
        String subscriptionAvailability = "true";
        String subscriptionAllowedTenants = "Orange.com";
        String inputTier = "Unlimited";
        String errorMessage = "";

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        Mockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(userTenantDomain);
        APIConsumer apiConsumer = Mockito.mock(APIConsumer.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        Mockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        Mockito.when(apiManagerFactory.getAPIConsumer(username)).thenReturn(apiConsumer);
        ApiTypeWrapper apiTypeWrapper = Mockito.mock(ApiTypeWrapper.class);
        Mockito.when(apiTypeWrapper.isAPIProduct()).thenReturn(true);
        APIProduct product = Mockito.mock(APIProduct.class);
        Mockito.when(apiTypeWrapper.getApiProduct()).thenReturn(product);
        String providerName = "Bruce";
        APIProductIdentifier apiProductIdentifier = Mockito.mock(APIProductIdentifier.class);
        Mockito.when(product.getId()).thenReturn(apiProductIdentifier);
        Mockito.when(apiProductIdentifier.getProviderName()).thenReturn(providerName);
        Set<Tier> availableTiers = new HashSet<>();
        Mockito.when(product.getAvailableTiers()).thenReturn(availableTiers);
        Mockito.when(product.getSubscriptionAvailability()).thenReturn(subscriptionAvailability);
        Mockito.when(product.getSubscriptionAvailableTenants()).thenReturn(subscriptionAllowedTenants);
        String apiTenantDomain = "Oscorp.com";
        PowerMockito.mockStatic(MultitenantUtils.class);
        Mockito.when(MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName))).thenReturn(apiTenantDomain);
        Tier gold = Mockito.mock(Tier.class);
        availableTiers.add(gold);
        Mockito.when(gold.getName()).thenReturn("Gold");
        Tier silver = Mockito.mock(Tier.class);
        availableTiers.add(silver);
        Mockito.when(silver.getName()).thenReturn("Silver");
        Mockito.when(apiConsumer.isTierDeneid(inputTier)).thenReturn(false);
        try {
            RestAPIStoreUtils.checkSubscriptionAllowed(apiTypeWrapper, inputTier);
        } catch (APIMgtAuthorizationFailedException ex) {
            errorMessage = ex.getMessage();
        }
        boolean isMatching = errorMessage.contains("Tier " + inputTier + " is not allowed for API/API Product " + apiTypeWrapper.toString() + ". Only [Silver, Gold] Tiers are allowed.") || errorMessage.contains("Tier " + inputTier + " is not allowed for API/API Product " + apiTypeWrapper.toString() + ". Only [Gold, Silver] Tiers are allowed.");
        Assert.assertTrue(isMatching);
    }

    @Test
    public void testCheckSubscriptionAllowedWhenTenantDoesntMatch() throws APIManagementException {

        String username = "Clark Kent";
        String userTenantDomain = "Oscorp.com";
        String subscriptionAvailability = APIConstants.SUBSCRIPTION_TO_ALL_TENANTS;
        String subscriptionAllowedTenants = "Orange.com";
        String inputTier = "Silver";
        String apiTenantDomain = "Stark.com";

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        Mockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(userTenantDomain);
        APIConsumer apiConsumer = Mockito.mock(APIConsumer.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        Mockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        Mockito.when(apiManagerFactory.getAPIConsumer(username)).thenReturn(apiConsumer);
        ApiTypeWrapper apiTypeWrapper = Mockito.mock(ApiTypeWrapper.class);
        Mockito.when(apiTypeWrapper.isAPIProduct()).thenReturn(true);
        APIProduct product = Mockito.mock(APIProduct.class);
        Mockito.when(apiTypeWrapper.getApiProduct()).thenReturn(product);
        String providerName = "Bruce";
        APIProductIdentifier apiProductIdentifier = Mockito.mock(APIProductIdentifier.class);
        Mockito.when(product.getId()).thenReturn(apiProductIdentifier);
        Mockito.when(apiProductIdentifier.getProviderName()).thenReturn(providerName);
        Set<Tier> availableTiers = new HashSet<>();
        Mockito.when(product.getAvailableTiers()).thenReturn(availableTiers);
        Mockito.when(product.getSubscriptionAvailability()).thenReturn(subscriptionAvailability);
        Mockito.when(product.getSubscriptionAvailableTenants()).thenReturn(subscriptionAllowedTenants);
        PowerMockito.mockStatic(MultitenantUtils.class);
        Mockito.when(MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName))).thenReturn(apiTenantDomain);
        Tier gold = Mockito.mock(Tier.class);
        availableTiers.add(gold);
        Mockito.when(gold.getName()).thenReturn("Gold");
        Tier silver = Mockito.mock(Tier.class);
        availableTiers.add(silver);
        Mockito.when(silver.getName()).thenReturn("Silver");
        Mockito.when(apiConsumer.isTierDeneid(inputTier)).thenReturn(false);
        RestAPIStoreUtils.checkSubscriptionAllowed(apiTypeWrapper, inputTier);
    }

    @Test
    public void testCheckSubscriptionAllowedWithSpecificTenants() throws APIManagementException {

        String username = "Clark Kent";
        String userTenantDomain = "Oscorp.com";
        String subscriptionAvailability = "specific_tenants";
        String subscriptionAllowedTenants = "Apple.com, Orange.com, Stark.com, Oscorp.com, Rocket.com";
        String inputTier = "Silver";
        String apiTenantDomain = "Stark.com";

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        Mockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(userTenantDomain);
        APIConsumer apiConsumer = Mockito.mock(APIConsumer.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        Mockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        Mockito.when(apiManagerFactory.getAPIConsumer(username)).thenReturn(apiConsumer);
        ApiTypeWrapper apiTypeWrapper = Mockito.mock(ApiTypeWrapper.class);
        Mockito.when(apiTypeWrapper.isAPIProduct()).thenReturn(true);
        APIProduct product = Mockito.mock(APIProduct.class);
        Mockito.when(apiTypeWrapper.getApiProduct()).thenReturn(product);
        String providerName = "Bruce";
        APIProductIdentifier apiProductIdentifier = Mockito.mock(APIProductIdentifier.class);
        Mockito.when(product.getId()).thenReturn(apiProductIdentifier);
        Mockito.when(apiProductIdentifier.getProviderName()).thenReturn(providerName);
        Set<Tier> availableTiers = new HashSet<>();
        Mockito.when(product.getAvailableTiers()).thenReturn(availableTiers);
        Mockito.when(product.getSubscriptionAvailability()).thenReturn(subscriptionAvailability);
        Mockito.when(product.getSubscriptionAvailableTenants()).thenReturn(subscriptionAllowedTenants);
        PowerMockito.mockStatic(MultitenantUtils.class);
        Mockito.when(MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName))).thenReturn(apiTenantDomain);
        Tier gold = Mockito.mock(Tier.class);
        availableTiers.add(gold);
        Mockito.when(gold.getName()).thenReturn("Gold");
        Tier silver = Mockito.mock(Tier.class);
        availableTiers.add(silver);
        Mockito.when(silver.getName()).thenReturn("Silver");
        Mockito.when(apiConsumer.isTierDeneid(inputTier)).thenReturn(false);
        RestAPIStoreUtils.checkSubscriptionAllowed(apiTypeWrapper, inputTier);
    }

    @Test
    public void testCheckSubscriptionAllowedWhenSubscriptionNotAllowed() throws APIManagementException {

        String username = "Clark Kent";
        String userTenantDomain = "Oscorp.com";
        String providerName = "Bruce";
        String inputTier = "Silver";
        String apiTenantDomain = "Stark.com";
        String errorMessage = "";
        String apiSecurity = "JWT";

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(username);
        Mockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(userTenantDomain);
        APIConsumer apiConsumer = Mockito.mock(APIConsumer.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        Mockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        Mockito.when(apiManagerFactory.getAPIConsumer(username)).thenReturn(apiConsumer);
        ApiTypeWrapper apiTypeWrapper = Mockito.mock(ApiTypeWrapper.class);
        Mockito.when(apiTypeWrapper.isAPIProduct()).thenReturn(false);
        API api = Mockito.mock(API.class);
        Mockito.when(apiTypeWrapper.getApi()).thenReturn(api);
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);
        Mockito.when(api.getId()).thenReturn(apiIdentifier);
        Mockito.when(apiIdentifier.getProviderName()).thenReturn(providerName);
        Mockito.when(api.getApiSecurity()).thenReturn(apiSecurity);
        PowerMockito.mockStatic(MultitenantUtils.class);
        Mockito.when(MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName))).thenReturn(apiTenantDomain);
        try {
            RestAPIStoreUtils.checkSubscriptionAllowed(apiTypeWrapper, inputTier);
        } catch (APIMgtAuthorizationFailedException ex) {
            errorMessage = ex.getMessage();
        }
        Assert.assertEquals("Subscription is not allowed for API " + apiTypeWrapper.toString() + ". To access the API, please use the client certificate", errorMessage);
    }

}
