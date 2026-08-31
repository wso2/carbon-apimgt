/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.apimgt.api.model.subscription.Application;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApplicationsApiServiceImplTest {

    private static final int APPLICATION_ID = 42;
    private static final String TENANT_DOMAIN = "application-owner.example";

    @Test
    public void testTenantLookupReturnsApplicationFromMatchingOrganization() {
        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();
        dao.applicationResult = Collections.singletonList(application(TENANT_DOMAIN));

        List<Application> applications =
                ApplicationsApiServiceImpl.getApplicationById(dao, APPLICATION_ID, TENANT_DOMAIN);

        assertEquals(1, applications.size());
        assertEquals(1, dao.calls);
    }

    @Test
    public void testTenantLookupReturnsEmptyForDifferentOrganization() {
        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();
        dao.applicationResult = Collections.singletonList(application("other.example"));

        List<Application> applications =
                ApplicationsApiServiceImpl.getApplicationById(dao, APPLICATION_ID, TENANT_DOMAIN);

        assertTrue(applications.isEmpty());
        assertEquals(1, dao.calls);
    }

    @Test
    public void testSuperTenantLookupPreservesApplicationFromAnyOrganization() {
        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();
        dao.applicationResult = Collections.singletonList(application("other.example"));

        List<Application> applications = ApplicationsApiServiceImpl.getApplicationById(
                dao, APPLICATION_ID, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

        assertEquals(1, applications.size());
        assertEquals(1, dao.calls);
    }

    @Test
    public void testMissingOrganizationDoesNotQueryApplication() {
        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();

        assertTrue(ApplicationsApiServiceImpl.getApplicationById(dao, APPLICATION_ID, null).isEmpty());
        assertTrue(ApplicationsApiServiceImpl.getApplicationById(dao, APPLICATION_ID, "").isEmpty());
        assertEquals(0, dao.calls);
    }

    @Test
    public void testTenantLookupPreservesEmptyResult() {
        RecordingSubscriptionValidationDAO dao = new RecordingSubscriptionValidationDAO();
        dao.applicationResult = Collections.emptyList();

        assertTrue(ApplicationsApiServiceImpl.getApplicationById(dao, APPLICATION_ID, TENANT_DOMAIN).isEmpty());
        assertEquals(1, dao.calls);
    }

    private static Application application(String organization) {
        Application application = new Application();
        application.setOrganization(organization);
        return application;
    }

    private static class RecordingSubscriptionValidationDAO extends SubscriptionValidationDAO {

        private int calls;
        private List<Application> applicationResult = Collections.emptyList();

        @Override
        public List<Application> getApplicationById(int applicationId) {
            calls++;
            return applicationResult;
        }
    }
}
