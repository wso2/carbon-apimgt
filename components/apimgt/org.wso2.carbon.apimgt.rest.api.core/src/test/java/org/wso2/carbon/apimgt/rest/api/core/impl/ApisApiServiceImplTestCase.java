/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 */

package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIManagerFactory.class, RestApiUtil.class })
public class ApisApiServiceImplTestCase {

    @Test
    public void apisApiIdGatewayConfigGetTestCase() throws Exception {

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);

        String apiID = UUID.randomUUID().toString();
        String gatewayConfig = "package deployment.org.wso2.apim;\n" + "import ballerina.net.http;\n" + "\n"
                + "@http:BasePath(\"/aaa1\")\n" + "service aaa1_1489666767745 {\n" + "\n" + "    @http:GET\n"
                + "    @http:Path(\"/*\")\n" + "    resource get_star_ (message m) {\n"
                + "        http:ClientConnector productionEndpoint = create http:ClientConnector(getUrlFromKey"
                + "(\"aaa1_1.0.0__ep\"));\n   http:ClientConnector sandboxEndpoint = create http:ClientConnector"
                + "(getUrlFromKey(\"aaa1_1.0.0__ep\"));\n message response;\n string endpointType;\n string "
                + "productionType;\n \n \n      endpointType = \"production\";\n productionType = \"production\";\n"
                + "\n      if (endpointType == productionType) {\n"
                + "            response = http:ClientConnector.execute(productionEndpoint, \"get\", \"\", m);\n"
                + "        } else {\n"
                + "            response = http:ClientConnector.execute(sandboxEndpoint, \"get\", \"\", m);\n"
                + "        }\n" + "\n" + "        reply response;\n" + "    }\n" + "}";

        Mockito.when(adminService.getAPIGatewayServiceConfig(apiID)).thenReturn(gatewayConfig);
        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);

        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        Mockito.when(instance.getAPIMgtAdminService()).thenReturn(adminService);

        Response response = apisApiService.apisApiIdGatewayConfigGet(apiID, null, getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void apisApiIdGatewayConfigGetWhenGatewayConfigNullTestCase() throws Exception {

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);

        String apiID = UUID.randomUUID().toString();
        String gatewayConfig = null;

        Mockito.when(adminService.getAPIGatewayServiceConfig(apiID)).thenReturn(gatewayConfig);
        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);

        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        Mockito.when(instance.getAPIMgtAdminService()).thenReturn(adminService);

        Response response = apisApiService.apisApiIdGatewayConfigGet(apiID, null, getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void apisApiIdGatewayConfigExceptionTestCase() throws Exception {

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();

        String apiID = UUID.randomUUID().toString();

        String message = "Error while retrieving gateway config of API " + apiID;

        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);

        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);

        APIManagementException apiManagementException = new APIManagementException(message,
                ExceptionCodes.GATEWAY_EXCEPTION);

        Mockito.when(instance.getAPIMgtAdminService()).thenThrow(apiManagementException);

        Response response = apisApiService.apisApiIdGatewayConfigGet(apiID, null, getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void apisGetTestCase() throws Exception {

        String labels = "ZONE_ONE,ZONE_TWO";
        String[] gatewayLabels = labels.split(",");
        List<String> labelList = new ArrayList<String>(Arrays.asList(gatewayLabels));

        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        List<API> apiList = new ArrayList<>();
        apiList.add(SampleTestObjectCreator.createUniqueAPI().build());
        apiList.add(SampleTestObjectCreator.createUniqueAPI().build());
        apiList.add(SampleTestObjectCreator.createUniqueAPI().build());

        Mockito.when(apiMgtAdminService.getAPIsByStatus(labelList, "Published")).thenReturn(apiList);

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        Response response = apisApiService.apisGet(labels, "Published", getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        Integer count = ((APIListDTO) response.getEntity()).getCount();

        Assert.assertEquals(count.intValue(), 3);
    }

    @Test
    public void apisGetApisNullStatusTestCase() throws Exception {

        String labels = "ZONE_ONE,ZONE_TWO";

        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        Response response = apisApiService.apisGet(labels, null, getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void apisGetApisEmptyStatusTestCase() throws Exception {

        String labels = "ZONE_ONE,ZONE_TWO";

        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        Response response = apisApiService.apisGet(labels, "", getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void apisGetApisNullLabelsTestCase() throws Exception {

        String labels = null;

        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        Response response = apisApiService.apisGet(labels, "Published", getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void apisGetApisEmptyLabelsTestCase() throws Exception {

        String labels = "";

        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        Response response = apisApiService.apisGet(labels, "Published", getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void apisGetApisForExceptionTestCase() throws Exception {

        String labels = "ZONE_ONE,ZONE_TWO";

        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        String apiID = UUID.randomUUID().toString();
        String message = "Error while retrieving gateway config of API " + apiID;

        APIManagementException apiManagementException = new APIManagementException(message,
                ExceptionCodes.GATEWAY_EXCEPTION);

        Mockito.when(RestApiUtil.getAPIMgtAdminService()).thenThrow(apiManagementException);

        ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();
        Response response = apisApiService.apisGet(labels, "Published", getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }
}
