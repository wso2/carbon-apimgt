/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.wsdl;

import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;

import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class SOAPOperationBindingTestCase {

    @Test
    public void testGetSoapOperationMapping() throws Exception {
        String mapping = SOAPOperationBindingUtils.getSoapOperationMappingForUrl(Thread.currentThread().getContextClassLoader()
                .getResource("wsdls/phoneverify.wsdl").toExternalForm());
        Assert.assertTrue("Failed getting soap operation mapping from the WSDL", !mapping.isEmpty());
    }

    @Test
    public void testGetWSDLProcessor() throws Exception {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(Thread.currentThread().getContextClassLoader()
                .getResource("wsdls/phoneverify.wsdl").toExternalForm());
        byte[] wsdlContent = wsdlReader.getWSDL();
        WSDL11SOAPOperationExtractor processor = SOAPOperationBindingUtils.getWSDL11SOAPOperationExtractor(wsdlContent, wsdlReader);
        Assert.assertNotNull(processor);
        Assert.assertTrue("Failed to get soap binding operations from the WSDL", processor.getWsdlInfo().getSoapBindingOperations().size() > 0);
    }

    @Test
    public void testGetSwaggerFromWSDL() throws Exception {
        String swaggerStr = SOAPOperationBindingUtils.getSoapOperationMappingForUrl(Thread.currentThread().getContextClassLoader()
                .getResource("wsdls/phoneverify.wsdl").toExternalForm());

        Swagger swagger = new SwaggerParser().parse(swaggerStr);
        Assert.assertTrue("Unable to parse the swagger from the given string", swagger!= null);
        Assert.assertNotNull(swagger.getPaths());
        Assert.assertEquals(2, swagger.getPaths().size());
        Assert.assertEquals(12, swagger.getDefinitions().size());
        Assert.assertTrue(swagger.getDefinitions().get("CheckPhoneNumber").getProperties().size() == 2);
    }

    @Test public void testGetSwaggerFromWSDLWithExternalXSDs() throws Exception {

        String externalXSDElementName = "InvokeClaimGeniousDataIntoCordysElement";
        String externalXSDElementInputOperation = "invokeClaimGeniousDataIntoCordysBindingOperationInput";
        String externalXSDElementOutputOperation = "invokeClaimGeniousDataIntoCordysBindingOperationOutput";
        String externalXSDElementPropertyName = "LocalizableMessage";

        String swaggerStr = SOAPOperationBindingUtils.getSoapOperationMappingForUrl(
                Thread.currentThread().getContextClassLoader()
                        .getResource("wsdls/wsdl-with-external-xsds/sampleWSDLWithExternalXSDFiles.wsdl")
                        .toExternalForm());

        Swagger swagger = new SwaggerParser().parse(swaggerStr);
        Assert.assertTrue("Unable to parse the swagger from the given string", swagger != null);
        Assert.assertNotNull(swagger.getPaths());

        //Assert WSDL elements
        Assert.assertEquals(1, swagger.getPaths().size());
        Assert.assertEquals(5, swagger.getDefinitions().size());
        Assert.assertNotNull(swagger.getDefinitions().get(externalXSDElementOutputOperation));
        Assert.assertNotNull(swagger.getDefinitions().get(externalXSDElementInputOperation));
        Assert.assertNotNull(swagger.getDefinitions().get(externalXSDElementName));
        //Assert WSDL external XSD element
        Assert.assertTrue(swagger.getDefinitions().get(externalXSDElementName).getProperties().size() == 1);
        Assert.assertNotNull(swagger.getDefinitions().get(externalXSDElementName).getProperties()
                .get(externalXSDElementPropertyName));
    }

    @Test
    public void testCompareGeneratedSwagger() throws Exception {
        String swaggerString = "{\n" + "  \"swagger\": \"2.0\",\n" + "  \"paths\": {\n" + "    \"/getCustomer\": {\n"
                + "      \"get\": {\n" + "        \"operationId\": \"getCustomer\",\n" + "        \"parameters\": [],\n"
                + "        \"responses\": {\n" + "          \"default\": {\n" + "            \"description\": \"\",\n"
                + "            \"schema\": {\n" + "              \"$ref\": \"#/definitions/getCustomerOutput\"\n"
                + "            }\n" + "          }\n" + "        },\n" + "        \"x-wso2-soap\": {\n"
                + "          \"soap-action\": \"getCustomer\",\n" + "          \"soap-operation\": \"getCustomer\",\n"
                + "          \"namespace\": \"http://service.jcombat.com/\"\n" + "        }\n" + "      }\n" + "    }\n"
                + "  },\n" + "  \"info\": {\n" + "    \"title\": \"\",\n" + "    \"version\": \"\"\n" + "  },\n"
                + "  \"definitions\": {\n" + "    \"getCustomerOutput\": {\n" + "      \"type\": \"object\"\n"
                + "    }\n" + "  }\n" + "}";
        String generatedSwagger = SOAPOperationBindingUtils.getSoapOperationMappingForUrl(
                Thread.currentThread().getContextClassLoader().getResource("wsdls/simpleCustomerService.wsdl")
                        .toExternalForm());
        Swagger swagger = new SwaggerParser().parse(generatedSwagger);
        Swagger actualSwagger = new SwaggerParser().parse(swaggerString);
        Assert.assertTrue(actualSwagger.getPaths().size() == swagger.getPaths().size());
        Assert.assertTrue(actualSwagger.getDefinitions().size() == swagger.getDefinitions().size());
    }

    @Test
    public void testVendorExtensions() throws Exception {
        String swaggerStr = SOAPOperationBindingUtils.getSoapOperationMappingForUrl(
                Thread.currentThread().getContextClassLoader().getResource("wsdls/simpleCustomerService.wsdl")
                        .toExternalForm());
        Swagger swagger = new SwaggerParser().parse(swaggerStr);
        Assert.assertNotNull(
                swagger.getPath("/getCustomer").getOperationMap().get(HttpMethod.GET).getVendorExtensions());
        Assert.assertNotNull(swagger.getPath("/getCustomer").getOperationMap().get(HttpMethod.GET).getVendorExtensions()
                .get("x-wso2-soap"));
        Map<String, Object> vendorExtensions = swagger.getPath("/getCustomer").getOperationMap().get(HttpMethod.GET)
                .getVendorExtensions();
        for (String s : vendorExtensions.keySet()) {
            if (s.equals("soap-action")) {
                Assert.assertEquals(vendorExtensions.get("soap-action"), "getCustomer");
            }
            if (s.equals("soap-operation")) {
                Assert.assertEquals(vendorExtensions.get("soap-operation"), "getCustomer");
            }
            if (s.equals("namespace")) {
                Assert.assertEquals(vendorExtensions.get("namespace"), "http://service.test.com/");
            }
        }
    }
}