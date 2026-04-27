/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UntraffickedListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryApiServerClient;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryDetailWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryListFilter;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryListItemWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoveryListWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.DiscoverySummaryWire;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery.UntraffickedListWire;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GovernanceApiServiceImpl}. Mocks the
 * DiscoveryApiServerClient via the constructor seam so no real network
 * traffic is required.
 *
 * <p>Each handler is tested for:
 * - happy path: client returns a populated wire object → handler returns
 *   a 200 with a populated DTO.
 * - failure: client throws UnavailableException → handler routes through
 *   RestApiUtil.handleInternalServerError, which throws
 *   APIManagementException (we catch and verify).
 *
 * The detail handler also has a NotFound case.</p>
 */
public class GovernanceApiServiceImplTest {

    /**
     * /summary happy path: wire counts should appear in the DTO.
     */
    @Test
    public void testGetDiscoverySummaryReturnsDTO() throws Exception {
        DiscoveryApiServerClient client = mock(DiscoveryApiServerClient.class);
        when(client.getSummary(anyString())).thenReturn(canonicalSummary());

        GovernanceApiServiceImpl impl = new GovernanceApiServiceImpl(client);
        Response resp = impl.getDiscoverySummary(null);

        assertEquals(200, resp.getStatus());
        DiscoverySummaryDTO dto = (DiscoverySummaryDTO) resp.getEntity();
        assertEquals((Integer) 12, dto.getTotal());
        assertEquals((Integer) 2, dto.getUnmanaged());
        assertNotNull(dto.getByService());
        assertEquals(1, dto.getByService().size());
    }

    /**
     * /summary failure: client throws → handler raises
     * APIManagementException (via RestApiUtil.handleInternalServerError).
     */
    @Test(expected = org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException.class)
    public void testGetDiscoverySummaryUnavailable() throws Exception {
        DiscoveryApiServerClient client = mock(DiscoveryApiServerClient.class);
        when(client.getSummary(anyString()))
                .thenThrow(new DiscoveryApiServerClient.UnavailableException("down"));

        new GovernanceApiServiceImpl(client).getDiscoverySummary(null);
    }

    /**
     * /apis happy path: client receives a DiscoveryListFilter built from
     * the request params, returns wire payload, handler returns DTO.
     */
    @Test
    public void testGetDiscoveredAPIsReturnsListDTO() throws Exception {
        DiscoveryApiServerClient client = mock(DiscoveryApiServerClient.class);
        when(client.listApis(anyString(), any(DiscoveryListFilter.class)))
                .thenReturn(canonicalList());

        GovernanceApiServiceImpl impl = new GovernanceApiServiceImpl(client);
        Response resp = impl.getDiscoveredAPIs("drift", null, null, 25, 0, null);

        assertEquals(200, resp.getStatus());
        DiscoveredAPIListDTO dto = (DiscoveredAPIListDTO) resp.getEntity();
        assertEquals((Integer) 1, dto.getCount());
        assertEquals(1, dto.getList().size());
        assertEquals("k8s:techmart/orders",
                dto.getList().get(0).getServiceIdentity());
    }

    /**
     * /apis/{id} happy path.
     */
    @Test
    public void testGetDiscoveredAPIByIdReturnsDTO() throws Exception {
        DiscoveryApiServerClient client = mock(DiscoveryApiServerClient.class);
        when(client.getApiById(anyString(), eq("uuid-1")))
                .thenReturn(canonicalDetail());

        GovernanceApiServiceImpl impl = new GovernanceApiServiceImpl(client);
        Response resp = impl.getDiscoveredAPIById("uuid-1", null);

        assertEquals(200, resp.getStatus());
        DiscoveredAPIDTO dto = (DiscoveredAPIDTO) resp.getEntity();
        assertEquals("uuid-1", dto.getId());
        assertEquals("k8s:techmart/orders", dto.getServiceIdentity());
        assertEquals("techmart", dto.getNamespace());
        assertEquals(DiscoveredAPIDTO.ClassificationEnum.DRIFT,
                dto.getClassification());
        assertNotNull(dto.getServiceManagedAPIs());
        assertEquals(1, dto.getServiceManagedAPIs().size());
    }

    /**
     * /apis/{id} not found: client throws NotFoundException → handler
     * raises APIManagementException via RestApiUtil.handleResourceNotFoundError.
     */
    @Test(expected = org.wso2.carbon.apimgt.rest.api.util.exception.NotFoundException.class)
    public void testGetDiscoveredAPIByIdNotFound() throws Exception {
        DiscoveryApiServerClient client = mock(DiscoveryApiServerClient.class);
        when(client.getApiById(anyString(), anyString()))
                .thenThrow(new DiscoveryApiServerClient.NotFoundException("404"));

        new GovernanceApiServiceImpl(client).getDiscoveredAPIById("nope", null);
    }

    /**
     * /apis/{id} server error.
     */
    @Test(expected = org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException.class)
    public void testGetDiscoveredAPIByIdUnavailable() throws Exception {
        DiscoveryApiServerClient client = mock(DiscoveryApiServerClient.class);
        when(client.getApiById(anyString(), anyString()))
                .thenThrow(new DiscoveryApiServerClient.UnavailableException("down"));

        new GovernanceApiServiceImpl(client).getDiscoveredAPIById("uuid-1", null);
    }

    /**
     * /untrafficked happy path.
     */
    @Test
    public void testGetUntraffickedReturnsListDTO() throws Exception {
        DiscoveryApiServerClient client = mock(DiscoveryApiServerClient.class);
        when(client.listUntrafficked(anyString())).thenReturn(canonicalUntrafficked());

        GovernanceApiServiceImpl impl = new GovernanceApiServiceImpl(client);
        Response resp = impl.getUntrafficked(null);

        assertEquals(200, resp.getStatus());
        UntraffickedListDTO dto = (UntraffickedListDTO) resp.getEntity();
        assertEquals((Integer) 1, dto.getCount());
        assertEquals("ReviewAPI", dto.getList().get(0).getApimApiName());
    }

    /**
     * /untrafficked failure.
     */
    @Test(expected = org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException.class)
    public void testGetUntraffickedUnavailable() throws Exception {
        DiscoveryApiServerClient client = mock(DiscoveryApiServerClient.class);
        when(client.listUntrafficked(anyString()))
                .thenThrow(new DiscoveryApiServerClient.UnavailableException("down"));

        new GovernanceApiServiceImpl(client).getUntrafficked(null);
    }

    /**
     * Default pagination is applied when limit/offset are null.
     */
    @Test
    public void testListDefaultsLimitTo25AndOffsetTo0() throws Exception {
        DiscoveryApiServerClient client = mock(DiscoveryApiServerClient.class);
        when(client.listApis(anyString(), any(DiscoveryListFilter.class)))
                .thenReturn(canonicalList());

        new GovernanceApiServiceImpl(client)
                .getDiscoveredAPIs(null, null, null, null, null, null);

        org.mockito.ArgumentCaptor<DiscoveryListFilter> cap =
                org.mockito.ArgumentCaptor.forClass(DiscoveryListFilter.class);
        verify(client).listApis(anyString(), cap.capture());
        assertEquals(25, cap.getValue().getLimit());
        assertEquals(0, cap.getValue().getOffset());
    }

    // -- canonical fixtures ------------------------------------------------

    private static DiscoverySummaryWire canonicalSummary() {
        DiscoverySummaryWire w = new DiscoverySummaryWire();
        w.setTotal(12);
        w.setManaged(10);
        w.setUnmanaged(2);
        w.setSkipInternal(false);

        DiscoverySummaryWire.ByType bt = new DiscoverySummaryWire.ByType();
        bt.setShadow(0);
        bt.setDrift(2);
        w.setByType(bt);

        DiscoverySummaryWire.ByReachability br =
                new DiscoverySummaryWire.ByReachability();
        br.setExternal(0);
        br.setInternal(2);
        w.setByReachability(br);

        DiscoverySummaryWire.ByServiceEntry e =
                new DiscoverySummaryWire.ByServiceEntry();
        e.setServiceIdentity("k8s:techmart/orders");
        e.setFullyGoverned(false);
        e.setShadow(0);
        e.setDrift(1);
        w.setByService(Collections.singletonList(e));
        return w;
    }

    private static DiscoveryListWire canonicalList() {
        DiscoveryListItemWire item = new DiscoveryListItemWire();
        item.setId("uuid-1");
        item.setServiceIdentity("k8s:techmart/orders");
        item.setEnvKind("k8s");
        item.setMethod("GET");
        item.setNormalizedPath("/orders/1.0.0/internal/queue/peek");
        item.setClassification("drift");
        item.setInternal(false);
        item.setObservationCount(60);
        item.setDistinctClientCount(2);
        item.setLastSeenAt("2026-04-26T09:01:55Z");

        DiscoveryListWire w = new DiscoveryListWire();
        w.setCount(1);
        w.setList(Collections.singletonList(item));

        DiscoveryListWire.PaginationWire p = new DiscoveryListWire.PaginationWire();
        p.setOffset(0);
        p.setLimit(25);
        p.setTotal(1);
        w.setPagination(p);
        return w;
    }

    private static DiscoveryDetailWire canonicalDetail() {
        DiscoveryDetailWire w = new DiscoveryDetailWire();
        w.setId("uuid-1");
        w.setServiceIdentity("k8s:techmart/orders");
        w.setEnvKind("k8s");
        w.setNamespace("techmart");
        w.setServiceName("orders");
        w.setMethod("GET");
        w.setNormalizedPath("/orders/1.0.0/internal/queue/peek");
        w.setClassification("drift");
        w.setObservationCount(60);
        w.setDistinctClientCount(2);
        w.setAvgDurationUs(14300);

        DiscoveryDetailWire.APIRef ref = new DiscoveryDetailWire.APIRef();
        ref.setApimApiId("ab6adc83");
        ref.setApimApiName("OrdersAPI");
        ref.setApimApiVersion("1.0.0");
        w.setServiceManagedAPIs(Collections.singletonList(ref));
        w.setStatusCodes(Arrays.asList(200, 204));
        return w;
    }

    private static UntraffickedListWire canonicalUntrafficked() {
        UntraffickedListWire.UntraffickedItemWire item =
                new UntraffickedListWire.UntraffickedItemWire();
        item.setApimApiId("uuid-2");
        item.setApimApiName("ReviewAPI");
        item.setApimApiVersion("1.0.0");
        item.setMethod("GET");
        item.setGatewayPath("/reviews/1.0.0/products/{id}/reviews");
        item.setServiceIdentity("k8s:techmart/reviews");
        item.setLastSyncedAt("2026-04-26T09:00:00Z");

        UntraffickedListWire w = new UntraffickedListWire();
        w.setCount(1);
        w.setList(Collections.<UntraffickedListWire.UntraffickedItemWire>singletonList(item));
        return w;
    }
}
