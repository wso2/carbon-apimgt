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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.rest.api.core.dto.EndpointListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EndpointsApiServiceImplTestCase {

    @Test
    public void endpointsEndpointIdGatewayConfigGetTest() throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl(apiMgtAdminService);

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
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl(apiMgtAdminService);

        String endpointId = UUID.randomUUID().toString();

        String endpointGatewayConfig = null;
        Mockito.when(apiMgtAdminService.getEndpointGatewayConfig(endpointId)).thenReturn(endpointGatewayConfig);

        Response response = endpointsApiService.endpointsEndpointIdGatewayConfigGet(endpointId, null, getRequest());
        Assert.assertEquals(response.getStatus(), 404);
    }

    @Test
    public void endpointsEndpointIdGatewayConfigGetExceptionTest() throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);
        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl(apiMgtAdminService);

        String message = "Error while retrieving gateway configuration.";

        String endpointId = UUID.randomUUID().toString();

        APIManagementException apiManagementException = new APIManagementException(message,
                ExceptionCodes.ENDPOINT_CONFIG_NOT_FOUND);
        Mockito.when(apiMgtAdminService.getEndpointGatewayConfig(endpointId)).thenThrow(apiManagementException);

        Response response = endpointsApiService.endpointsEndpointIdGatewayConfigGet(endpointId, null, getRequest());
        Assert.assertEquals(response.getStatus(), 404);

    }

    @Test
    public void endpointsGetTest() throws Exception {

        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        Endpoint endpointOne = SampleTestObjectCreator.createUniqueEndpoint();
        Endpoint endpointTwo = SampleTestObjectCreator.createUniqueEndpoint();
        Endpoint endpointThree = SampleTestObjectCreator.createUniqueEndpoint();

        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(endpointOne);
        endpointList.add(endpointTwo);
        endpointList.add(endpointThree);

        Mockito.when(apiMgtAdminService.getAllEndpoints()).thenReturn(endpointList);

        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl(apiMgtAdminService);
        Response response = endpointsApiService.endpointsGet(null, null, getRequest());

        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(((EndpointListDTO) response.getEntity()).getList().size(), 3);
    }

    @Test
    public void endpointsGetExceptionTest() throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        String message = "Error while retrieving endpoint";

        APIManagementException apiManagementException = new APIManagementException(message,
                ExceptionCodes.ENDPOINT_NOT_FOUND);
        Mockito.when(apiMgtAdminService.getAllEndpoints()).thenThrow(apiManagementException);

        EndpointsApiServiceImpl endpointsApiService = new EndpointsApiServiceImpl(apiMgtAdminService);
        Response response = endpointsApiService.endpointsGet(null, null, getRequest());
        Assert.assertEquals(response.getStatus(), 404);

    }

    private Request getRequest() {
        return Mockito.mock(Request.class);
    }

}
