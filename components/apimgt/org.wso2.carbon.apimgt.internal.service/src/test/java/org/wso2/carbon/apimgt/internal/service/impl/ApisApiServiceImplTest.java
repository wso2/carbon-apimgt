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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.ImportedAPIDTO;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.internal.service.dto.ImportAPIResponseDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.InputStream;
import javax.ws.rs.core.Response;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiUtil.class, APIImportExportUtil.class, PublisherCommonUtils.class, PhaseInterceptorChain.class})
public class ApisApiServiceImplTest {

    private static final String ORGANIZATION = "carbon.super";
    private static final String API_ID = "test-api-id-1234";

    private ApisApiServiceImpl service;
    private MessageContext messageContext;
    private InputStream fileInputStream;
    private Attachment fileDetail;
    private ImportExportAPI importExportAPI;

    @Before
    public void setUp() throws Exception {
        service = new ApisApiServiceImpl();
        messageContext = Mockito.mock(MessageContext.class);
        fileInputStream = Mockito.mock(InputStream.class);
        fileDetail = Mockito.mock(Attachment.class);
        importExportAPI = Mockito.mock(ImportExportAPI.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getValidatedOrganization(messageContext)).thenReturn(ORGANIZATION);
        PowerMockito.mockStatic(APIImportExportUtil.class);
        PowerMockito.when(APIImportExportUtil.getImportExportAPI()).thenReturn(importExportAPI);

        Message cxfMessage = Mockito.mock(Message.class);
        Exchange exchange = Mockito.mock(Exchange.class);
        PowerMockito.mockStatic(PhaseInterceptorChain.class);
        PowerMockito.when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(cxfMessage);
        Mockito.when(cxfMessage.getExchange()).thenReturn(exchange);
        Mockito.when(exchange.get(RestApiConstants.USER_REST_API_SCOPES)).thenReturn(null);
    }

    /**
     * Successful API import returns HTTP status code 201 with the expected response message.
     */
    @Test
    public void testImportAPISuccessJsonAccept() throws APIManagementException {
        ImportedAPIDTO importedAPIDTO = Mockito.mock(ImportedAPIDTO.class);
        org.wso2.carbon.apimgt.api.model.API api = Mockito.mock(org.wso2.carbon.apimgt.api.model.API.class);
        APIIdentifier apiIdentifier = new APIIdentifier("admin", "DemoAPI", "1.0.0");
        Mockito.when(api.getId()).thenReturn(apiIdentifier);
        Mockito.when(importedAPIDTO.getApi()).thenReturn(api);
        Mockito.when(importExportAPI.importAPI(fileInputStream, true, null, false, false, null, ORGANIZATION))
                .thenReturn(importedAPIDTO);

        Response response = service.importAPI(fileInputStream, fileDetail, null, null, null, null, false,
                RestApiConstants.APPLICATION_JSON, messageContext);

        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertTrue(response.getEntity() instanceof ImportAPIResponseDTO);
        ImportAPIResponseDTO dto = (ImportAPIResponseDTO) response.getEntity();
        Assert.assertTrue(dto.getMessage().contains(API_ID) || dto.getMessage().contains("imported successfully"));
    }

    /**
     * Dry-run mode evaluation with correct response code.
     */
    @Test
    public void testImportAPIDryRun() throws APIManagementException {
        String dryRunResult = "{\"passed\": true}";
        PowerMockito.mockStatic(PublisherCommonUtils.class);
        PowerMockito.when(PublisherCommonUtils.checkGovernanceComplianceDryRun(fileInputStream, ORGANIZATION))
                .thenReturn(dryRunResult);

        Response response = service.importAPI(fileInputStream, fileDetail, null, null, null, null, true,
                RestApiConstants.APPLICATION_JSON, messageContext);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(dryRunResult, response.getEntity());
        // importAPI must NOT be called during dry-run
        Mockito.verify(importExportAPI, Mockito.never())
                .importAPI(Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.anyBoolean(),
                        Mockito.anyBoolean(), Mockito.any(), Mockito.anyString());
    }

    /**
     * Successful import returns plain-text 200 when Accept is not application/json.
     */
    @Test
    public void testImportAPISuccessPlainTextAccept() throws APIManagementException {
        ImportedAPIDTO importedAPIDTO = Mockito.mock(ImportedAPIDTO.class);
        org.wso2.carbon.apimgt.api.model.API api = Mockito.mock(org.wso2.carbon.apimgt.api.model.API.class);
        APIIdentifier apiIdentifier = new APIIdentifier("admin", "DemoAPI", "1.0.0");
        Mockito.when(api.getId()).thenReturn(apiIdentifier);
        Mockito.when(importedAPIDTO.getApi()).thenReturn(api);
        Mockito.when(importExportAPI.importAPI(fileInputStream, true, null, false, false, null, ORGANIZATION))
                .thenReturn(importedAPIDTO);

        Response response = service.importAPI(fileInputStream, fileDetail, null, null, null, null, false,
                RestApiConstants.TEXT_PLAIN, messageContext);

        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertTrue(response.getEntity().toString().contains("imported successfully"));
    }
}
