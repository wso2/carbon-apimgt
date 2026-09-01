/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.impl;

import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.subscription.Subscription;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.dto.SubscriptionListDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SubscriptionsApiServiceImplTest {

    private static final int API_ID = 11;
    private static final int APPLICATION_ID = 22;
    private static final String API_UUID = "api-uuid";
    private static final String APPLICATION_UUID = "application-uuid";
    private static final String TENANT_ORGANIZATION = "tenant.example";
    private static final String OTHER_ORGANIZATION = "other.example";

    @Test
    public void testUuidLookupReturnsSubscriptionFromMatchingApplicationOrganization() {

        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();
        Subscription subscription = createSubscription(TENANT_ORGANIZATION, OTHER_ORGANIZATION);
        dao.uuidResult = subscription;

        assertSame(subscription, SubscriptionsApiServiceImpl.getSubscription(
                dao, API_UUID, APPLICATION_UUID, TENANT_ORGANIZATION.toUpperCase()));
        assertEquals(1, dao.uuidCalls);
        assertEquals(API_UUID, dao.apiUUID);
        assertEquals(APPLICATION_UUID, dao.applicationUUID);
    }

    @Test
    public void testUuidLookupReturnsNullForDifferentApplicationOrganization() {

        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();
        dao.uuidResult = createSubscription(OTHER_ORGANIZATION, TENANT_ORGANIZATION);

        assertNull(SubscriptionsApiServiceImpl.getSubscription(
                dao, API_UUID, APPLICATION_UUID, TENANT_ORGANIZATION));
        assertEquals(1, dao.uuidCalls);
    }

    @Test
    public void testNumericLookupUsesApplicationOrganization() {

        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();
        Subscription subscription = createSubscription(TENANT_ORGANIZATION, OTHER_ORGANIZATION);
        dao.numericResult = subscription;

        assertSame(subscription, SubscriptionsApiServiceImpl.getSubscription(
                dao, API_ID, APPLICATION_ID, TENANT_ORGANIZATION));
        assertEquals(1, dao.numericCalls);
        assertEquals(API_ID, dao.apiId);
        assertEquals(APPLICATION_ID, dao.applicationId);
    }

    @Test
    public void testNumericLookupReturnsNullForDifferentApplicationOrganization() {

        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();
        dao.numericResult = createSubscription(OTHER_ORGANIZATION, TENANT_ORGANIZATION);

        assertNull(SubscriptionsApiServiceImpl.getSubscription(
                dao, API_ID, APPLICATION_ID, TENANT_ORGANIZATION));
        assertEquals(1, dao.numericCalls);
    }

    @Test
    public void testSuperTenantCanUseBothLookupFormsAcrossOrganizations() {

        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();
        Subscription subscription = createSubscription(OTHER_ORGANIZATION, OTHER_ORGANIZATION);
        dao.uuidResult = subscription;
        dao.numericResult = subscription;

        assertSame(subscription, SubscriptionsApiServiceImpl.getSubscription(
                dao, API_UUID, APPLICATION_UUID, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        assertSame(subscription, SubscriptionsApiServiceImpl.getSubscription(
                dao, API_ID, APPLICATION_ID, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        assertEquals(1, dao.uuidCalls);
        assertEquals(1, dao.numericCalls);
    }

    @Test
    public void testMissingSubscriptionReturnsNullForBothLookupForms() {

        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();

        assertNull(SubscriptionsApiServiceImpl.getSubscription(
                dao, API_UUID, APPLICATION_UUID, TENANT_ORGANIZATION));
        assertNull(SubscriptionsApiServiceImpl.getSubscription(
                dao, API_ID, APPLICATION_ID, TENANT_ORGANIZATION));
        assertEquals(1, dao.uuidCalls);
        assertEquals(1, dao.numericCalls);
    }

    @Test
    public void testMissingAuthenticatedOrganizationDoesNotQuerySubscriptions() {

        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();

        assertNull(SubscriptionsApiServiceImpl.getSubscription(dao, API_UUID, APPLICATION_UUID, null));
        assertNull(SubscriptionsApiServiceImpl.getSubscription(dao, API_ID, APPLICATION_ID, ""));
        assertEquals(0, dao.uuidCalls);
        assertEquals(0, dao.numericCalls);
    }

    @Test
    public void testMissingApplicationOrganizationReturnsNull() {

        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();
        dao.uuidResult = createSubscription(null, TENANT_ORGANIZATION);

        assertNull(SubscriptionsApiServiceImpl.getSubscription(
                dao, API_UUID, APPLICATION_UUID, TENANT_ORGANIZATION));
        assertEquals(1, dao.uuidCalls);
    }

    @Test
    public void testValidateOrganizationUsesAuthenticatedTenantForDifferentOrganization() {

        assertEquals(TENANT_ORGANIZATION, validateAs(TENANT_ORGANIZATION, OTHER_ORGANIZATION));
    }

    @Test
    public void testValidateOrganizationUsesAuthenticatedTenantForAllOrganizations() {

        assertEquals(TENANT_ORGANIZATION, validateAs(TENANT_ORGANIZATION, "ALL"));
    }

    @Test
    public void testValidateOrganizationPreservesRequestedOrganizationForSuperTenant() {

        assertEquals(OTHER_ORGANIZATION, validateAs(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, OTHER_ORGANIZATION));
    }

    @Test
    public void testValidateOrganizationPreservesMatchingOrganization() {

        assertEquals(TENANT_ORGANIZATION, validateAs(TENANT_ORGANIZATION, TENANT_ORGANIZATION));
    }

    @Test
    public void testValidateOrganizationUsesAuthenticatedTenantWhenRequestedOrganizationIsMissing() {

        assertEquals(TENANT_ORGANIZATION, validateAs(TENANT_ORGANIZATION, null));
    }

    @Test
    public void testValidateOrganizationRecognizesSuperTenantRegardlessOfCase() {

        // Every other super-tenant check in this class is case-insensitive; this one must match.
        assertEquals(OTHER_ORGANIZATION, validateAs(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.toUpperCase(), OTHER_ORGANIZATION));
    }

    @Test
    public void testMissingAuthenticatedOrganizationReturnsEmptyListInsteadOfUnscopedQuery() throws Exception {

        // A caller whose own tenant cannot be resolved must not fall through to the unscoped
        // getAllSubscriptions() branches further down subscriptionsGet(). A thread that never
        // started a tenant flow has no tenant domain set, reproducing that unresolved state
        // directly, since RestApiCommonUtil.getLoggedInUserTenantDomain() is a plain
        // CarbonContext read.
        String previousCarbonHome = System.getProperty("carbon.home");
        System.setProperty("carbon.home", SubscriptionsApiServiceImplTest.class.getResource("/").getFile());
        try {
            Response response = new SubscriptionsApiServiceImpl().subscriptionsGet(
                    null, null, null, null, null, null);

            assertEquals(200, response.getStatus());
            assertTrue(((SubscriptionListDTO) response.getEntity()).getList().isEmpty());
        } finally {
            if (previousCarbonHome != null) {
                System.setProperty("carbon.home", previousCarbonHome);
            } else {
                System.clearProperty("carbon.home");
            }
        }
    }

    private String validateAs(String authenticatedOrganization, String requestedOrganization) {

        return SubscriptionsApiServiceImpl.validateOrganization(requestedOrganization, authenticatedOrganization);
    }

    private Subscription createSubscription(String applicationOrganization, String apiOrganization) {

        Subscription subscription = new Subscription();
        subscription.setAppOrganization(applicationOrganization);
        subscription.setApiOrganization(apiOrganization);
        return subscription;
    }

    private static class RecordingSubscriptionValidationDAO extends SubscriptionValidationDAO {

        private int uuidCalls;
        private int numericCalls;
        private String apiUUID;
        private String applicationUUID;
        private int apiId;
        private int applicationId;
        private Subscription uuidResult;
        private Subscription numericResult;

        @Override
        public Subscription getSubscription(String requestedApiUUID, String requestedApplicationUUID) {

            uuidCalls++;
            apiUUID = requestedApiUUID;
            applicationUUID = requestedApplicationUUID;
            return uuidResult;
        }

        @Override
        public Subscription getSubscription(int requestedApiId, int requestedApplicationId) {

            numericCalls++;
            apiId = requestedApiId;
            applicationId = requestedApplicationId;
            return numericResult;
        }
    }
}
