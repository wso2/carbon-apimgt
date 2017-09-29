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
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.LinkedHashSet;
import java.util.Set;

public class ResourceConfigContextTest {

    @Test
    public void testResourceConfigContext() throws Exception {

        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIStatus.CREATED);
        api.setContextTemplate("/");
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        URITemplate template = new URITemplate();
        template.setUriTemplate("/test");
        template.setHTTPVerb("GET");
        template.setThrottlingTier("Unlimited");
        template.setAuthType("Application");
        template.setResourceURI("http://maps.googleapis.com/maps/api/geocode/json?address=Colombo");
        template.setResourceSandboxURI("http://maps.googleapis.com/maps/api/geocode/json?address=Colombo");
        uriTemplates.add(template);
        api.setUriTemplates(uriTemplates);
        ConfigContext configcontext = new APIConfigContext(api);
        ResourceConfigContext resourceConfigContext = new ResourceConfigContext(configcontext, api);
        resourceConfigContext.validate();
        Assert.assertNotNull(resourceConfigContext.getContext().get("resources"));
        Assert.assertNotNull(resourceConfigContext.getContext().get("apiStatus"));
    }
}
