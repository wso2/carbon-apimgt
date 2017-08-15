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
 */
package org.wso2.carbon.apimgt.core.template;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.template.dto.TemplateBuilderDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test cases for APIConfigContext
 *
 */
public class APIConfigContextTestCase {

    @Test
    public void testValidAPIConfigContext() throws APITemplateException {
        APIConfigContext apiConfigContext = new APIConfigContext(SampleTestObjectCreator.createDefaultAPI().build(),
                "org.test");
        apiConfigContext.validate();
        String actualPackageFromContext = (String) apiConfigContext.getContext().get("package");
        Assert.assertEquals(actualPackageFromContext, "org.test");
    }

    @Test(expectedExceptions = APITemplateException.class)
    public void testWithEmptyParameters() throws APITemplateException {
        API emptyNamedAPI = new API.APIBuilder("", "", "").context("testcontext")
                .id(UUID.randomUUID().toString()).build(); // empty name
        APIConfigContext apiConfigContext = new APIConfigContext(emptyNamedAPI, "org.test");
        apiConfigContext.validate();
    }
    
    @Test(expectedExceptions = APITemplateException.class)
    public void testWithNullParameters() throws APITemplateException {
        API emptyNamedAPI = new API.APIBuilder(null, null, "1.0.0").context("testcontext")
                .id(UUID.randomUUID().toString()).build(); // name and provider null
        APIConfigContext apiConfigContext = new APIConfigContext(emptyNamedAPI, "org.test");
        apiConfigContext.validate();
    }

    
    @Test
    public void testAPINameWithNumber() throws APITemplateException {
        String apiId = UUID.randomUUID().toString();
        API nameWithNumberAPI = new API.APIBuilder("provider", "1111testapi", "1.0.0").id(apiId).context("testcontext")
                .build();
        APIConfigContext apiConfigContext = new APIConfigContext(nameWithNumberAPI, "org.test");
        apiConfigContext.validate();
        String actualServiceName = (String) apiConfigContext.getContext().get("serviceName");
        Assert.assertEquals(actualServiceName, "prefix_1111testapi_" + apiId.replaceAll("-", "_"));
    }

    @Test
    public void testCompositeAPIConfigContext() {
        APIConfigContext apiConfigContext = new APIConfigContext(SampleTestObjectCreator.createDefaultAPI().build(),
                "org.test");
        CompositeAPIConfigContext compositeAPIConfigContext = new CompositeAPIConfigContext(apiConfigContext, null,
                null);
        String actualPackageFromCompositeContext = (String) compositeAPIConfigContext.getContext().get("package");
        Assert.assertEquals(actualPackageFromCompositeContext, "org.test");
    }

    @Test
    public void testResourceConfigContext() {
        APIConfigContext apiConfigContext = new APIConfigContext(SampleTestObjectCreator.createDefaultAPI().build(),
                "org.test");
        TemplateBuilderDTO templateBuilderDTO = new TemplateBuilderDTO();
        templateBuilderDTO.setTemplateId("t1");
        List<TemplateBuilderDTO> templateList = new ArrayList<TemplateBuilderDTO>();
        templateList.add(templateBuilderDTO);
        ResourceConfigContext resourceConfigContext = new ResourceConfigContext(apiConfigContext, templateList);
        List<TemplateBuilderDTO> templatesFromContext = (List<TemplateBuilderDTO>) resourceConfigContext.getContext()
                .get("apiResources");
        String templateIdFromContext = templatesFromContext.get(0).getTemplateId();
        Assert.assertEquals(templateIdFromContext, "t1");
    }

    @Test
    public void testEndpointContext() {

        Endpoint ep = Endpoint.newEndpoint().name("Ep1").build();
        EndpointContext endpointContext = new EndpointContext(ep, "org.test");
        Endpoint epFromContext = (Endpoint) endpointContext.getContext().get("endpoint");
        Assert.assertEquals(epFromContext.getName(), "Ep1");
    }

}
