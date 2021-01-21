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

import org.apache.axis2.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.VelocityContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.EndpointSecurity;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.APIConfigContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.ConfigContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.EndpointSecurityModel;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.SecurityConfigContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SecurityConfigContextTest {

    private APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);

    @Test
    public void testSecurityConfigContext() throws Exception {

        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIConstants.CREATED);
        api.setContextTemplate("/");
        api.setTransports(Constants.TRANSPORT_HTTP);
        api.setEndpointUTUsername("admin");
        api.setEndpointUTPassword("admin123");
        api.setEndpointSecured(true);
        api.setEndpointAuthDigest(true);
        ConfigContext configcontext = new APIConfigContext(api);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE)).thenReturn("true");
        SecurityConfigContext securityConfigContext =
                new SecurityConfigContextWrapper(configcontext, api, apiManagerConfiguration);
        securityConfigContext.validate();
        VelocityContext velocityContext = securityConfigContext.getContext();
        Assert.assertNotNull(velocityContext.get("endpoint_security"));
        Map<String, EndpointSecurityModel> endpointSecurityModelMap =
                (Map<String, EndpointSecurityModel>) velocityContext.get("endpoint_security");
        for (Map.Entry<String, EndpointSecurityModel> endpointSecurityModelEntry : endpointSecurityModelMap
                .entrySet()) {
            Assert.assertTrue("Property isEndpointSecured cannot be false.",
                    endpointSecurityModelEntry.getValue().isEnabled());
            Assert.assertTrue("Property isEndpointAuthDigest cannot be false.",
                    endpointSecurityModelEntry.getValue().getType().contains("digest"));
            Assert.assertTrue("Property username does not match.",
                    "admin".equals(endpointSecurityModelEntry.getValue().getUsername()));
            Assert.assertTrue("Property base64unpw does not match. ",
                    new String(Base64.encodeBase64("admin:admin123".getBytes()))
                            .equalsIgnoreCase(endpointSecurityModelEntry.getValue().getBase64EncodedPassword()));
            Assert.assertTrue("Property securevault_alias does not match.",
                    "admin--TestAPI1.0.0".equalsIgnoreCase(endpointSecurityModelEntry.getValue().getAlias()));
        }
        Assert.assertTrue("Property isSecureVaultEnabled cannot be false. ",
                velocityContext.get("isSecureVaultEnabled").equals(true));
    }

    @Test
    public void testSecurityConfigContextPerEndpointProductionType() throws Exception {

        String json = "{\"endpoint_security\":{\n" +
                "  \"production\":{\n" +
                "    \"enabled\":true,\n" +
                "    \"type\":\"BASIC\",\n" +
                "    \"username\":\"admin\",\n" +
                "    \"password\":\"admin123#QA\"\n" +
                "  }\n" +
                "  }\n" +
                "}";
        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIConstants.CREATED);
        api.setContextTemplate("/");
        api.setTransports(Constants.TRANSPORT_HTTP);
        api.setEndpointConfig(json);
        ConfigContext configcontext = new APIConfigContext(api);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE)).thenReturn("true");
        SecurityConfigContext securityConfigContext =
                new SecurityConfigContextWrapper(configcontext, api, apiManagerConfiguration);
        securityConfigContext.validate();
        VelocityContext velocityContext = securityConfigContext.getContext();
        Assert.assertNotNull(velocityContext.get("endpoint_security"));
        Map<String, EndpointSecurityModel> endpointSecurityModelMap =
                (Map<String, EndpointSecurityModel>) velocityContext.get("endpoint_security");
        EndpointSecurityModel production = endpointSecurityModelMap.get("production");
        Assert.assertTrue("Property enabled cannot be false.", production.isEnabled());
        Assert.assertTrue("Property type cannot be other.", production.getType().equalsIgnoreCase("basic"));
        Assert.assertTrue("Property username does not match.", "admin".equals(production.getUsername()));
        Assert.assertTrue("Property base64value does not match. ",
                new String(Base64.encodeBase64("admin:admin123#QA".getBytes()))
                        .equalsIgnoreCase(production.getBase64EncodedPassword()));
        Assert.assertTrue("Property securevault_alias does not match.",
                "admin--TestAPI1.0.0--production".equalsIgnoreCase(production.getAlias()));
        Assert.assertTrue("Property isSecureVaultEnabled cannot be false. ",
                velocityContext.get("isSecureVaultEnabled").equals(true));
        EndpointSecurityModel sandbox = endpointSecurityModelMap.get("sandbox");
        Assert.assertFalse("Property enabled cannot be true.", sandbox.isEnabled());
    }

    @Test
    public void testSecurityConfigContextPerEndpointBothType() throws Exception {

        String json = "{\"endpoint_security\":{\n" +
                "  \"production\":{\n" +
                "    \"enabled\":true,\n" +
                "    \"type\":\"BASIC\",\n" +
                "    \"username\":\"admin\",\n" +
                "    \"password\":\"admin123#QA\"\n" +
                "  },\n" +
                "  \"sandbox\":{\n" +
                "    \"enabled\":true,\n" +
                "    \"type\":\"DIGEST\",\n" +
                "    \"username\":\"admin\",\n" +
                "    \"password\":\"admin123\"\n" +
                "  }\n" +
                "  }\n" +
                "}";
        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIConstants.CREATED);
        api.setContextTemplate("/");
        api.setTransports(Constants.TRANSPORT_HTTP);
        api.setEndpointConfig(json);
        ConfigContext configcontext = new APIConfigContext(api);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE)).thenReturn("true");
        SecurityConfigContext securityConfigContext =
                new SecurityConfigContextWrapper(configcontext, api, apiManagerConfiguration);
        securityConfigContext.validate();
        VelocityContext velocityContext = securityConfigContext.getContext();
        Assert.assertNotNull(velocityContext.get("endpoint_security"));
        Map<String, EndpointSecurityModel> endpointSecurityModelMap =
                (Map<String, EndpointSecurityModel>) velocityContext.get("endpoint_security");
        EndpointSecurityModel production = endpointSecurityModelMap.get("production");
        Assert.assertTrue("Property enabled cannot be false.", production.isEnabled());
        Assert.assertTrue("Property type cannot be other.", production.getType().equalsIgnoreCase("basic"));
        Assert.assertTrue("Property username does not match.", "admin".equals(production.getUsername()));
        Assert.assertTrue("Property base64value does not match. ",
                new String(Base64.encodeBase64("admin:admin123#QA".getBytes()))
                        .equalsIgnoreCase(production.getBase64EncodedPassword()));
        Assert.assertTrue("Property securevault_alias does not match.",
                "admin--TestAPI1.0.0--production".equalsIgnoreCase(production.getAlias()));
        EndpointSecurityModel sandbox = endpointSecurityModelMap.get("sandbox");
        Assert.assertTrue("Property enabled cannot be false.", sandbox.isEnabled());
        Assert.assertTrue("Property type cannot be other.", sandbox.getType().equalsIgnoreCase("digest"));
        Assert.assertTrue("Property username does not match.", "admin".equals(sandbox.getUsername()));
        Assert.assertTrue("Property base64value does not match. ",
                new String(Base64.encodeBase64("admin:admin123".getBytes()))
                        .equalsIgnoreCase(sandbox.getBase64EncodedPassword()));
        Assert.assertTrue("Property securevault_alias does not match.",
                "admin--TestAPI1.0.0--sandbox".equalsIgnoreCase(sandbox.getAlias()));
        Assert.assertTrue("Property isSecureVaultEnabled cannot be false. ",
                velocityContext.get("isSecureVaultEnabled").equals(true));
    }

    @Test
    public void testSecurityConfigContextPerEndpointSandbox() throws Exception {

        String json = "{\"endpoint_security\":{\n" +
                "  \"sandbox\":{\n" +
                "    \"enabled\":true,\n" +
                "    \"type\":\"DIGEST\",\n" +
                "    \"username\":\"admin\",\n" +
                "    \"password\":\"admin123#QA\"\n" +
                "  }\n" +
                "  }\n" +
                "}";
        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIConstants.CREATED);
        api.setContextTemplate("/");
        api.setTransports(Constants.TRANSPORT_HTTP);
        api.setEndpointConfig(json);
        ConfigContext configcontext = new APIConfigContext(api);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE)).thenReturn("true");
        SecurityConfigContext securityConfigContext =
                new SecurityConfigContextWrapper(configcontext, api, apiManagerConfiguration);
        securityConfigContext.validate();
        VelocityContext velocityContext = securityConfigContext.getContext();
        Assert.assertNotNull(velocityContext.get("endpoint_security"));
        Map<String, EndpointSecurityModel> endpointSecurityModelMap =
                (Map<String, EndpointSecurityModel>) velocityContext.get("endpoint_security");
        EndpointSecurityModel sandbox = endpointSecurityModelMap.get("sandbox");
        Assert.assertTrue("Property enabled cannot be false.", sandbox.isEnabled());
        Assert.assertTrue("Property type cannot be other.", sandbox.getType().equalsIgnoreCase("digest"));
        Assert.assertTrue("Property username does not match.", "admin".equals(sandbox.getUsername()));
        Assert.assertTrue("Property base64value does not match. ",
                new String(Base64.encodeBase64("admin:admin123#QA".getBytes()))
                        .equalsIgnoreCase(sandbox.getBase64EncodedPassword()));
        Assert.assertTrue("Property securevault_alias does not match.",
                "admin--TestAPI1.0.0--sandbox".equalsIgnoreCase(sandbox.getAlias()));
        Assert.assertTrue("Property isSecureVaultEnabled cannot be false. ",
                velocityContext.get("isSecureVaultEnabled").equals(true));
        EndpointSecurityModel production = endpointSecurityModelMap.get("production");
        Assert.assertFalse("Property enabled cannot be true.", production.isEnabled());
    }

    @Test
    public void testSecurityConfigContextIgnoringEndpointConfig() throws Exception {

        String json = "{\"endpoint_security\":{\n" +
                "  \"sandbox\":{\n" +
                "    \"enabled\":true,\n" +
                "    \"type\":\"DIGEST\",\n" +
                "    \"username\":\"admin\",\n" +
                "    \"password\":\"admin123#QA\"\n" +
                "  }\n" +
                "  }\n" +
                "}";

        API api = new API(new APIIdentifier("admin", "TestAPI", "1.0.0"));
        api.setStatus(APIConstants.CREATED);
        api.setContextTemplate("/");
        api.setTransports(Constants.TRANSPORT_HTTP);
        api.setEndpointConfig(json);
        api.setEndpointUTUsername("admin");
        api.setEndpointUTPassword("admin123");
        api.setEndpointSecured(true);
        api.setEndpointAuthDigest(true);
        ConfigContext configcontext = new APIConfigContext(api);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE)).thenReturn("true");
        SecurityConfigContext securityConfigContext =
                new SecurityConfigContextWrapper(configcontext, api, apiManagerConfiguration);
        securityConfigContext.validate();
        VelocityContext velocityContext = securityConfigContext.getContext();
        Assert.assertNotNull(velocityContext.get("endpoint_security"));
        Map<String, EndpointSecurityModel> endpointSecurityModelMap =
                (Map<String, EndpointSecurityModel>) velocityContext.get("endpoint_security");
        for (Map.Entry<String, EndpointSecurityModel> endpointSecurityModelEntry : endpointSecurityModelMap
                .entrySet()) {
            Assert.assertTrue("Property isEndpointSecured cannot be false.",
                    endpointSecurityModelEntry.getValue().isEnabled());
            Assert.assertTrue("Property isEndpointAuthDigest cannot be false.",
                    endpointSecurityModelEntry.getValue().getType().contains("digest"));
            Assert.assertTrue("Property username does not match.",
                    "admin".equals(endpointSecurityModelEntry.getValue().getUsername()));
            Assert.assertTrue("Property base64unpw does not match. ",
                    new String(Base64.encodeBase64("admin:admin123".getBytes()))
                            .equalsIgnoreCase(endpointSecurityModelEntry.getValue().getBase64EncodedPassword()));
            Assert.assertTrue("Property securevault_alias does not match.",
                    "admin--TestAPI1.0.0".equalsIgnoreCase(endpointSecurityModelEntry.getValue().getAlias()));
        }
        Assert.assertTrue("Property isSecureVaultEnabled cannot be false. ",
                velocityContext.get("isSecureVaultEnabled").equals(true));
    }

    @Test
    public void testSecurityConfigContextForAPIProduct() throws Exception {

        APIProduct apiProduct = new APIProduct(new APIProductIdentifier("admin", "TestProduct", "1.0.0"));
        apiProduct.setUuid(UUID.randomUUID().toString());
        String apiid = UUID.randomUUID().toString();
        List<APIProductResource> apiProductResourceList = new ArrayList<>();
        APIProductResource apiProductResource = new APIProductResource();
        apiProductResource.setApiIdentifier(new APIIdentifier("admin_api1_v1"));
        apiProductResource.setApiId(apiid);
        Map<String, EndpointSecurity> endpointSecurityMap = new HashMap<>();
        EndpointSecurity endpointSecurity = new EndpointSecurity();
        endpointSecurity.setType("BASIC");
        endpointSecurity.setUsername("admin");
        endpointSecurity.setPassword("admin123");
        endpointSecurity.setEnabled(true);
        endpointSecurityMap.put("production", endpointSecurity);
        apiProductResource.setApiId(apiid);
        apiProductResource.setEndpointSecurityMap(endpointSecurityMap);
        apiProductResourceList.add(apiProductResource);
        apiProduct.setProductResources(apiProductResourceList);
        ConfigContext configcontext = new APIConfigContext(apiProduct);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE)).thenReturn("true");
        Map<String, APIDTO> apidtoMap = new HashMap<>();
        apidtoMap.put(apiid, new APIDTO().name("api1").version("v1").provider("admin"));
        SecurityConfigContext securityConfigContext =
                new SecurityConfigContextWrapper(configcontext, apiProduct, apiManagerConfiguration,apidtoMap);
        securityConfigContext.validate();
        VelocityContext velocityContext = securityConfigContext.getContext();
        Assert.assertNotNull(velocityContext.get("endpoint_security"));
        Map<String, Map<String, EndpointSecurityModel>> endpointSecurityModelMap =
                (Map<String, Map<String, EndpointSecurityModel>>) velocityContext.get("endpoint_security");
        Map<String, EndpointSecurityModel> endpointSecurityModelMap1 =
                endpointSecurityModelMap.get(apiProductResource.getApiId());
        EndpointSecurityModel production = endpointSecurityModelMap1.get("production");
        Assert.assertTrue("Property enabled cannot be false.", production.isEnabled());
        Assert.assertTrue("Property type cannot be other.", production.getType().equalsIgnoreCase("basic"));
        Assert.assertTrue("Property username does not match.", "admin".equals(production.getUsername()));
        Assert.assertTrue("Property base64value does not match. ",
                new String(Base64.encodeBase64("admin:admin123".getBytes()))
                        .equalsIgnoreCase(production.getBase64EncodedPassword()));
        Assert.assertTrue("Property securevault_alias does not match.",
                "admin--api1v1--production".equalsIgnoreCase(production.getAlias()));
        Assert.assertTrue("Property isSecureVaultEnabled cannot be false. ",
                velocityContext.get("isSecureVaultEnabled").equals(true));
    }
}
