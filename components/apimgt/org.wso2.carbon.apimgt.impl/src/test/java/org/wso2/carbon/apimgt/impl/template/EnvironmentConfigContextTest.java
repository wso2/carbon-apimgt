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

package org.wso2.carbon.apimgt.impl.template;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.dto.Environment;

public class EnvironmentConfigContextTest {

    @Test
    public void testEnvironmentConfigContext() throws Exception {

        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIStatus.CREATED);
        api.setContextTemplate("/");
        String url = "http://maps.googleapis.com/maps/api/geocode/json?address=Colombo";
        String endpointConfig = "{\"production_endpoints\":{\"url\":\"" + url + "\", \"config\":null}," +
                "\"sandbox_endpoint\":{\"url\":\"" + url + "\",\"config\":null},\"endpoint_type\":\"http\"}";
        api.setEndpointConfig(endpointConfig);
        api.setUrl(url);
        api.setSandboxUrl(url);
        ConfigContext configcontext = new APIConfigContext(api);
        Environment environment = new Environment();
        environment.setType("production");
        EnvironmentConfigContext environmentConfigContext = new EnvironmentConfigContext(configcontext, environment);
        Assert.assertNotNull(environmentConfigContext.getContext().get("environment"));
        Assert.assertNotNull(environmentConfigContext.getContext().get("environmentType"));
    }
}
