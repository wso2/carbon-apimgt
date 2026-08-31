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
import org.wso2.carbon.apimgt.internal.service.dto.RevokedEventsDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTConsumerKeyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTSubjectEntityDTO;
import org.wso2.carbon.apimgt.internal.service.model.RevokedJWTEventData;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RevokedjwtApiServiceImplTest {

    private static final String TENANT_DOMAIN = "tenant-a.com";
    private static final String FOREIGN_TENANT_DOMAIN = "tenant-b.com";
    private static final int TENANT_ID = 1;
    private static final int FOREIGN_TENANT_ID = 2;

    @Test
    public void testFilterRevokedJWTEventsForTenant() {

        RevokedEventsDTO result = RevokedjwtApiServiceImpl.filterRevokedJWTEvents(
                createRevokedJWTEventData(), TENANT_DOMAIN, TENANT_ID);

        assertEquals(1, result.getRevokedJWTList().size());
        assertEquals("own-signature", result.getRevokedJWTList().get(0).getJwtSignature());
        assertEquals(1, result.getRevokedJWTConsumerKeyList().size());
        assertEquals("own-consumer-key", result.getRevokedJWTConsumerKeyList().get(0).getConsumerKey());
        assertEquals(1, result.getRevokedJWTSubjectEntityList().size());
        assertEquals("own-subject", result.getRevokedJWTSubjectEntityList().get(0).getEntityId());
    }

    @Test
    public void testFilterRevokedJWTEventsForSuperTenant() {

        RevokedEventsDTO result = RevokedjwtApiServiceImpl.filterRevokedJWTEvents(
                createRevokedJWTEventData(), MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                MultitenantConstants.SUPER_TENANT_ID);

        assertEquals(3, result.getRevokedJWTList().size());
        assertEquals(2, result.getRevokedJWTConsumerKeyList().size());
        assertEquals(2, result.getRevokedJWTSubjectEntityList().size());
    }

    @Test
    public void testFilterRevokedJWTEventsFailsClosedForInconsistentSuperTenantContext() {

        RevokedEventsDTO domainMismatchResult = RevokedjwtApiServiceImpl.filterRevokedJWTEvents(
                createRevokedJWTEventData(), MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, TENANT_ID);
        RevokedEventsDTO idMismatchResult = RevokedjwtApiServiceImpl.filterRevokedJWTEvents(
                createRevokedJWTEventData(), TENANT_DOMAIN, MultitenantConstants.SUPER_TENANT_ID);

        assertEmpty(domainMismatchResult);
        assertEmpty(idMismatchResult);
    }

    @Test
    public void testFilterRevokedJWTEventsForSuperTenantRegardlessOfDomainCase() {

        RevokedEventsDTO result = RevokedjwtApiServiceImpl.filterRevokedJWTEvents(
                createRevokedJWTEventData(), MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.toUpperCase(),
                MultitenantConstants.SUPER_TENANT_ID);

        assertEquals(3, result.getRevokedJWTList().size());
        assertEquals(2, result.getRevokedJWTConsumerKeyList().size());
        assertEquals(2, result.getRevokedJWTSubjectEntityList().size());
    }

    @Test
    public void testFilterRevokedJWTEventsForTenantRegardlessOfOrganizationCase() {

        RevokedEventsDTO result = RevokedjwtApiServiceImpl.filterRevokedJWTEvents(
                createRevokedJWTEventData(), TENANT_DOMAIN.toUpperCase(), TENANT_ID);

        assertEquals(1, result.getRevokedJWTConsumerKeyList().size());
        assertEquals("own-consumer-key", result.getRevokedJWTConsumerKeyList().get(0).getConsumerKey());
        assertEquals(1, result.getRevokedJWTSubjectEntityList().size());
        assertEquals("own-subject", result.getRevokedJWTSubjectEntityList().get(0).getEntityId());
    }

    @Test
    public void testFilterRevokedJWTEventsFailsClosedForInvalidTenantContext() {

        assertEmpty(RevokedjwtApiServiceImpl.filterRevokedJWTEvents(
                createRevokedJWTEventData(), null, TENANT_ID));
        assertEmpty(RevokedjwtApiServiceImpl.filterRevokedJWTEvents(
                createRevokedJWTEventData(), "", TENANT_ID));
        assertEmpty(RevokedjwtApiServiceImpl.filterRevokedJWTEvents(
                createRevokedJWTEventData(), TENANT_DOMAIN, -2));
    }

    private static RevokedJWTEventData createRevokedJWTEventData() {

        RevokedJWTEventData data = new RevokedJWTEventData();
        data.getRevokedJWTList().add(new RevokedJWTEventData.RevokedJWTData(
                "own-signature", 1000L, TENANT_ID));
        data.getRevokedJWTList().add(new RevokedJWTEventData.RevokedJWTData(
                "foreign-signature", 2000L, FOREIGN_TENANT_ID));
        data.getRevokedJWTList().add(new RevokedJWTEventData.RevokedJWTData(
                "legacy-signature", 3000L, MultitenantConstants.INVALID_TENANT_ID));

        data.getRevokedJWTConsumerKeyList().add(new RevokedJWTConsumerKeyDTO()
                .consumerKey("own-consumer-key").revocationTime(1000L).organization(TENANT_DOMAIN));
        data.getRevokedJWTConsumerKeyList().add(new RevokedJWTConsumerKeyDTO()
                .consumerKey("foreign-consumer-key").revocationTime(2000L).organization(FOREIGN_TENANT_DOMAIN));

        data.getRevokedJWTSubjectEntityList().add(new RevokedJWTSubjectEntityDTO()
                .entityId("own-subject").entityType("USER").revocationTime(1000L).organization(TENANT_DOMAIN));
        data.getRevokedJWTSubjectEntityList().add(new RevokedJWTSubjectEntityDTO()
                .entityId("foreign-subject").entityType("USER").revocationTime(2000L)
                .organization(FOREIGN_TENANT_DOMAIN));
        return data;
    }

    private static void assertEmpty(RevokedEventsDTO result) {

        assertTrue(result.getRevokedJWTList().isEmpty());
        assertTrue(result.getRevokedJWTConsumerKeyList().isEmpty());
        assertTrue(result.getRevokedJWTSubjectEntityList().isEmpty());
    }
}
