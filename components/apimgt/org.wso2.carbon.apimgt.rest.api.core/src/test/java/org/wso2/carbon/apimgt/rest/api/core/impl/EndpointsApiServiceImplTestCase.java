/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.dto.EndpointListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIManagerFactory.class,
        RestApiUtil.class })
public class EndpointsApiServiceImplTestCase {

    @Test
    public void endpointsEndpointIdGatewayConfigGetTest() throws Exception {
        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        Mockito.when(instance.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();

        String endpointId = UUID.randomUUID().toString();

        String endpointGatewayConfig = "package deployment.org.wso2.apim;\n" + "import ballerina.net.http;\n" + "\n"
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
        Mockito.when(apiMgtAdminService.getEndpointGatewayConfig(endpointId)).thenReturn(endpointGatewayConfig);

        Response response = endpointsApiService.endpointsEndpointIdGatewayConfigGet(endpointId, null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void nullEndpointGatewayConfigTest() throws Exception {
        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        Mockito.when(instance.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();

        String endpointId = UUID.randomUUID().toString();

        String endpointGatewayConfig = null;
        Mockito.when(apiMgtAdminService.getEndpointGatewayConfig(endpointId)).thenReturn(endpointGatewayConfig);

        Response response = endpointsApiService.endpointsEndpointIdGatewayConfigGet(endpointId, null, getRequest());
        Assert.assertEquals(response.getStatus(), 404);
    }

    @Test
    public void endpointsEndpointIdGatewayConfigGetExceptionTest() throws Exception {

        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();

        String message = "Error while retrieving gateway configuration.";

        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);

        APIManagementException apiManagementException = new APIManagementException(message,
                ExceptionCodes.ENDPOINT_CONFIG_NOT_FOUND);
        Mockito.when(instance.getAPIMgtAdminService()).thenThrow(apiManagementException);

        String endpointId = UUID.randomUUID().toString();
        Response response = endpointsApiService.endpointsEndpointIdGatewayConfigGet(endpointId, null, getRequest());
        Assert.assertEquals(response.getStatus(), 404);

    }

    @Test
    public void endpointsGetTest() throws Exception {

        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        Endpoint endpointOne = SampleTestObjectCreator.createUniqueEndpoint();
        Endpoint endpointTwo = SampleTestObjectCreator.createUniqueEndpoint();
        Endpoint endpointThree = SampleTestObjectCreator.createUniqueEndpoint();

        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(endpointOne);
        endpointList.add(endpointTwo);
        endpointList.add(endpointThree);

        Mockito.when(apiMgtAdminService.getAllEndpoints()).thenReturn(endpointList);

        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        Response response = endpointsApiService.endpointsGet(null, null, getRequest());

        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(((EndpointListDTO) response.getEntity()).getList().size(), 3);
    }

    @Test
    public void endpointsGetExceptionTest() throws Exception {
        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);

        String message = "Error while retrieving endpoint";

        APIManagementException apiManagementException = new APIManagementException(message,
                ExceptionCodes.ENDPOINT_NOT_FOUND);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenThrow(apiManagementException);
        Mockito.when(apiMgtAdminService.getAllEndpoints()).thenReturn(null);

        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl();
        Response response = endpointsApiService.endpointsGet(null, null, getRequest());
        Assert.assertEquals(response.getStatus(), 404);

    }

    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }

}
