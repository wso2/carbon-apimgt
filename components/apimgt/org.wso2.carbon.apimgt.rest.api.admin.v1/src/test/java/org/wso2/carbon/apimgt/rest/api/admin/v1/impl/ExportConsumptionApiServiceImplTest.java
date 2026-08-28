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
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.usage.data.exporter.ConsumptionDataExportService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiCommonUtil.class, ServiceReferenceHolder.class})
public class ExportConsumptionApiServiceImplTest {

    /**
     * Regression guard for the consumption report download hanging on the client.
     *
     * The admin webapp registers CXF's GZIPOutInterceptor, which compresses the response body after
     * this resource method returns and does not recompute Content-Length. Setting that header here
     * from the uncompressed ZIP made it describe the pre-compression body, so a client sending
     * Accept-Encoding: gzip blocked waiting for bytes that were never written.
     *
     * The endpoint must therefore leave Content-Length to the container.
     */
    @Test
    public void testExportConsumptionDataDoesNotSetContentLength() throws Exception {
        byte[] zipBytes = "PK-pretend-this-is-a-zip".getBytes(StandardCharsets.UTF_8);

        // checkSuperTenantAccess() reads the tenant domain off RestApiCommonUtil; super tenant passes.
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn("carbon.super");

        ConsumptionDataExportService exportService = Mockito.mock(ConsumptionDataExportService.class);
        when(exportService.exportConsumptionDataAsZip(any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(zipBytes);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        when(serviceReferenceHolder.getConsumptionDataExportService()).thenReturn(exportService);

        ExportConsumptionApiServiceImpl exportConsumptionApiService = new ExportConsumptionApiServiceImpl();
        Response response = exportConsumptionApiService.exportConsumptionData("2026-06-30", "2026-07-30",
                Mockito.mock(MessageContext.class));

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertNull("The endpoint must not set Content-Length itself - a value derived from the "
                        + "uncompressed ZIP goes stale as soon as the GZIP out-interceptor compresses the body",
                response.getHeaderString(HttpHeaders.CONTENT_LENGTH));
    }
}
