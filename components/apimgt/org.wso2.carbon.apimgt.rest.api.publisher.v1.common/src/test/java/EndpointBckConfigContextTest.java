/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.APIConfigContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.ConfigContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.EndpointBckConfigContext;

public class EndpointBckConfigContextTest {

    @Test
    public void testEndpointBckConfigContext() throws Exception {
        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIConstants.CREATED);
        api.setContextTemplate("/");
        String url = "http://maps.googleapis.com/maps/api/geocode/json?address=Colombo";
        api.setUrl(url);
        api.setSandboxUrl(url);
        ConfigContext configcontext = new APIConfigContext(api);
        EndpointBckConfigContext endpointBckConfigContext = new EndpointBckConfigContext(configcontext, api);
        Assert.assertTrue(api.getEndpointConfig().contains(url));
        //setting an empty string as the endpoint config and checking the value which is returned
        api.setEndpointConfig("");
        String endpoint_config = "{\"production_endpoints\":{\"url\":\"" + api.getUrl() + "\", \"config\":null}," +
                "\"sandbox_endpoint\":{\"url\":\"" + api.getSandboxUrl() + "\"," +
                "\"config\":null},\"endpoint_type\":\"http\"}";
        EndpointBckConfigContext secondEndpointBckConfigContext = new EndpointBckConfigContext(configcontext, api);
        Assert.assertTrue(api.getEndpointConfig().contains(endpoint_config));
        //setting null as the endpoint config and checking the value which is returned
        api.setEndpointConfig(null);
        EndpointBckConfigContext thirdEndpointBckConfigContext = new EndpointBckConfigContext(configcontext, api);
        Assert.assertTrue(api.getEndpointConfig().contains(endpoint_config));
    }
}
