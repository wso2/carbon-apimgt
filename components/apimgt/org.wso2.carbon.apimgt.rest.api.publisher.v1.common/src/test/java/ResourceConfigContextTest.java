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

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.velocity.VelocityContext;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.APIConfigContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.ConfigContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.ResourceConfigContext;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ResourceConfigContextTest {

    @Test
    public void testResourceConfigContextForAPI() throws Exception {

        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIConstants.CREATED);
        api.setContextTemplate("/");
        api.setUriTemplates(setAPIUriTemplates());
        ConfigContext configcontext = new APIConfigContext(api);
        ResourceConfigContext resourceConfigContext = new ResourceConfigContext(configcontext, api);
        resourceConfigContext.validate();
        Assert.assertNotNull(resourceConfigContext.getContext().get("resources"));
        //assign an empty URITemplate set and check the result
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        api.setUriTemplates(uriTemplates);
        configcontext = new APIConfigContext(api);
        resourceConfigContext = new ResourceConfigContext(configcontext, api);
        String errorClass = "org.wso2.carbon.apimgt.api.APIManagementException";
        String expectedErrorMessage = "At least one resource is required";
        try {
            resourceConfigContext.validate();
        } catch (APIManagementException e) {
            Assert.assertTrue(errorClass.equalsIgnoreCase(e.getClass().getName()));
            Assert.assertTrue(expectedErrorMessage.equalsIgnoreCase(e.getMessage()));
        }
        //set a null value for URITemplate and check the result
        api.setUriTemplates(null);
        configcontext = new APIConfigContext(api);
        resourceConfigContext = new ResourceConfigContext(configcontext, api);
        try {
            resourceConfigContext.validate();
        } catch (APIManagementException e) {
            Assert.assertTrue(errorClass.equalsIgnoreCase(e.getClass().getName()));
            Assert.assertTrue(expectedErrorMessage.equalsIgnoreCase(e.getMessage()));
        }
    }

    @Test
    public void testResourceConfigContextForAPIProduct() throws Exception {

        APIProductIdentifier apiProductIdentifier = new APIProductIdentifier("admin", "TestAPIProduct", "1.0.0",
                UUID.randomUUID().toString());
        APIProduct apiProduct = new APIProduct(apiProductIdentifier);
        apiProduct.setProductResources(getAPIProductResources(apiProductIdentifier));
        apiProduct.setType(APIConstants.API_PRODUCT);
        ConfigContext configcontext = new APIConfigContext(apiProduct);
        ResourceConfigContext resourceConfigContext = new ResourceConfigContext(configcontext, apiProduct);
        resourceConfigContext.validate();
        VelocityContext context = resourceConfigContext.getContext();
        Assert.assertNotNull(context.get("apiType"));
        Assert.assertEquals(context.get("apiType"), APIConstants.API_PRODUCT);
        Object aggregates = context.get("aggregates");
        Assert.assertNotNull(aggregates);
        Assert.assertTrue(aggregates instanceof List);
        List<APIProductResource> apiProductResources = (List<APIProductResource>) aggregates;
        Assert.assertTrue(assertAPIProductResourceList(apiProduct.getProductResources(), apiProductResources));
    }

    private boolean assertAPIProductResourceList(List<APIProductResource> productResources,
                                                 List<APIProductResource> aggregatedResources) {
        for (APIProductResource aggregatedResource : aggregatedResources) {
            for (APIProductResource productResource : productResources) {
                if (aggregatedResource.getApiIdentifier().equals(productResource.getApiIdentifier())) {
                    if (aggregatedResource.getUriTemplate().getUriTemplate()
                            .equals(productResource.getUriTemplate().getUriTemplate())) {
                        if (!aggregatedResource.getUriTemplate().getHttpVerbs()
                                .contains(productResource.getUriTemplate().getHTTPVerb())) {
                            return false;
                        }
                    }
                } else {

                }
            }
        }
        return true;
    }

    private Set<URITemplate> setAPIUriTemplates() {

        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        URITemplate template = new URITemplate();
        template.setUriTemplate("/test");
        template.setHTTPVerb("GET");
        template.setThrottlingTier("Unlimited");
        template.setAuthType("Application");
        template.setResourceURI("http://maps.googleapis.com/maps/api/geocode/json?address=Colombo");
        template.setResourceSandboxURI("http://maps.googleapis.com/maps/api/geocode/json?address=Colombo");
        uriTemplates.add(template);
        return uriTemplates;
    }

    private List<APIProductResource> getAPIProductResources(APIProductIdentifier apiProductIdentifier) {

        APIProductResource apiProductResource = new APIProductResource();
        apiProductResource.setApiIdentifier(new APIIdentifier("admin", "api1", "1.0.0", UUID.randomUUID().toString()));
        URITemplate template = new URITemplate();
        template.setUriTemplate("/test");
        template.setHTTPVerb("GET");
        template.setThrottlingTier("Unlimited");
        template.setAuthType("Application");
        template.setResourceURI("http://maps.googleapis.com/maps/api/geocode/json?address=Colombo");
        template.setResourceSandboxURI("http://maps.googleapis.com/maps/api/geocode/json?address=Colombo");
        apiProductResource.setProductIdentifier(apiProductIdentifier);
        apiProductResource.setUriTemplate(template);
        return Arrays.asList(new APIProductResource[]{apiProductResource});
    }
}
