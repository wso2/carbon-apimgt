/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SearchResultListDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiCommonUtil.class, RestApiUtil.class})
public class ApisApiServiceImplTest {

    @Test
    public void testGetAllAPIs() throws Exception {
        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        MessageContext messageContext = Mockito.mock(MessageContext.class);

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);
        when(RestApiCommonUtil.getPaginationParams(anyInt(), anyInt(), anyInt())).thenCallRealMethod();
        when(RestApiCommonUtil.getAPIPaginatedURL(anyInt(), anyInt(), anyString())).thenCallRealMethod();

        PowerMockito.mockStatic(RestApiUtil.class);
        when(RestApiUtil.getOrganization(any(MessageContext.class))).thenReturn("carbon.super");

        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setUuid("uuid-1");
        List<Object> apiList = new ArrayList<>();
        apiList.add(api);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("apis", apiList);
        resultMap.put(APIConstants.API_DATA_LENGTH, 100);

        when(apiProvider.searchPaginatedAPIs(anyString(), anyString(), anyInt(), anyInt())).thenReturn(resultMap);

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        Response response = apisApiService.getAllAPIs(10, 5, "test", null, messageContext);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        SearchResultListDTO resultDTO = (SearchResultListDTO) response.getEntity();
        Assert.assertEquals(1, resultDTO.getCount().intValue());
        Assert.assertNotNull(resultDTO.getPagination());
        Assert.assertEquals(10, resultDTO.getPagination().getLimit().intValue());
        Assert.assertEquals(5, resultDTO.getPagination().getOffset().intValue());
        Assert.assertEquals(100, resultDTO.getPagination().getTotal().intValue());
        Assert.assertTrue(resultDTO.getPagination().getNext().contains("query=test"));
        Assert.assertTrue(resultDTO.getPagination().getPrevious().contains("query=test"));
    }

    @Test
    public void testGetAllAPIsWithNullParams() throws Exception {
        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        MessageContext messageContext = Mockito.mock(MessageContext.class);

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);
        when(RestApiCommonUtil.getPaginationParams(anyInt(), anyInt(), anyInt())).thenCallRealMethod();
        when(RestApiCommonUtil.getAPIPaginatedURL(anyInt(), anyInt(), anyString())).thenCallRealMethod();

        PowerMockito.mockStatic(RestApiUtil.class);
        when(RestApiUtil.getOrganization(any(MessageContext.class))).thenReturn("carbon.super");

        List<Object> apiList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("apis", apiList);
        resultMap.put(APIConstants.API_DATA_LENGTH, 0);

        when(apiProvider.searchPaginatedAPIs(anyString(), anyString(), anyInt(), anyInt())).thenReturn(resultMap);

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        Response response = apisApiService.getAllAPIs(null, null, null, null, messageContext);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        SearchResultListDTO resultDTO = (SearchResultListDTO) response.getEntity();
        Assert.assertEquals(0, resultDTO.getCount().intValue());
        Assert.assertNotNull(resultDTO.getPagination());
        Assert.assertEquals(25, resultDTO.getPagination().getLimit().intValue());
        Assert.assertEquals(0, resultDTO.getPagination().getOffset().intValue());
        Assert.assertEquals(0, resultDTO.getPagination().getTotal().intValue());
    }

    @Test
    public void testGetAllAPIsWithEmptyResults() throws Exception {
        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        MessageContext messageContext = Mockito.mock(MessageContext.class);

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);
        when(RestApiCommonUtil.getPaginationParams(anyInt(), anyInt(), anyInt())).thenCallRealMethod();
        when(RestApiCommonUtil.getAPIPaginatedURL(anyInt(), anyInt(), anyString())).thenCallRealMethod();

        PowerMockito.mockStatic(RestApiUtil.class);
        when(RestApiUtil.getOrganization(any(MessageContext.class))).thenReturn("carbon.super");

        List<Object> apiList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("apis", apiList);
        resultMap.put(APIConstants.API_DATA_LENGTH, 0);

        when(apiProvider.searchPaginatedAPIs(anyString(), anyString(), anyInt(), anyInt())).thenReturn(resultMap);

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        Response response = apisApiService.getAllAPIs(10, 0, "query", null, messageContext);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        SearchResultListDTO resultDTO = (SearchResultListDTO) response.getEntity();
        Assert.assertEquals(0, resultDTO.getCount().intValue());
        Assert.assertNotNull(resultDTO.getPagination());
        Assert.assertEquals("", resultDTO.getPagination().getNext());
        Assert.assertEquals("", resultDTO.getPagination().getPrevious());
    }
}
