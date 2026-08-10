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
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests which callers may dispatch notification events.
 *
 * Notification events are published on behalf of the whole deployment and the organization they
 * apply to is taken from the event payload, so only the super tenant may dispatch them.
 */
public class NotifyApiServiceImplTest {

    private static final String TENANT_DOMAIN = "event-sender.example";

    @Test
    public void testSuperTenantIsAllowedToDispatch() {

        assertTrue(NotifyApiServiceImpl.isNotificationDispatchAllowed(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
    }

    @Test
    public void testSuperTenantMatchIsCaseInsensitive() {

        assertTrue(NotifyApiServiceImpl.isNotificationDispatchAllowed(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.toUpperCase()));
    }

    @Test
    public void testTenantIsNotAllowedToDispatch() {

        assertFalse(NotifyApiServiceImpl.isNotificationDispatchAllowed(TENANT_DOMAIN));
    }

    @Test
    public void testNullOrganizationIsNotAllowedToDispatch() {

        assertFalse(NotifyApiServiceImpl.isNotificationDispatchAllowed(null));
    }

    @Test
    public void testEmptyOrganizationIsNotAllowedToDispatch() {

        assertFalse(NotifyApiServiceImpl.isNotificationDispatchAllowed(""));
    }
}
