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

import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLSOAPOperation;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPOperationBindingUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class WSDLSOAPOperationExtractorImplTestCase {

    private static Set<WSDLSOAPOperation> operations;

    @Before
    public void setup() throws Exception {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(Thread.currentThread().getContextClassLoader()
                .getResource("wsdls/phoneverify.wsdl").toExternalForm());
        byte[] wsdlContent = wsdlReader.getWSDL();
        WSDL11SOAPOperationExtractor processor = SOAPOperationBindingUtils.getWSDL11SOAPOperationExtractor(wsdlContent,
                wsdlReader);

        operations = processor.getWsdlInfo().getSoapBindingOperations();
    }
    @Test
    public void testGetWsdlDefinition() throws Exception {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(Thread.currentThread().getContextClassLoader()
                .getResource("wsdls/phoneverify.wsdl").toExternalForm());
        byte[] wsdlContent = wsdlReader.getWSDL();
        WSDL11SOAPOperationExtractor processor = new WSDL11SOAPOperationExtractor(wsdlReader);
        Assert.assertTrue("WSDL definition parsing failed", processor.init(wsdlContent));
    }

    @Test
    public void testReadSoapBindingOperations() throws Exception {
        Assert.assertTrue("WSDL operation processing failed", operations.iterator().hasNext());
        Assert.assertTrue("Incorrect wsdl namespace",
                operations.iterator().next().getTargetNamespace().equals("http://ws.cdyne.com/PhoneVerify/query"));
    }

    @Test
    public void testParseOperationInputParameters() throws Exception {
        Assert.assertTrue("WSDL operation processing failed", operations.iterator().hasNext());
        Assert.assertTrue("WSDL operation parameters are not set",
                operations.iterator().next().getInputParameterModel().size() > 0);
    }

    @Test
    public void testParseOperationOutputParameters() throws Exception {
        Assert.assertTrue("WSDL operation processing failed", operations.iterator().hasNext());
        Assert.assertTrue("WSDL operation output parameters are not set",
                operations.iterator().next().getOutputParameterModel().size() > 0);
    }

    @Test
    public void testGetSwaggerModelElementForWSDLOperationElement() throws Exception {
        List<ModelImpl> inputParameterModel = operations.iterator().next().getInputParameterModel();
        Assert.assertEquals(1, operations.iterator().next().getOutputParameterModel().size());
        for (ModelImpl model : inputParameterModel) {
            Assert.assertTrue(
                    "CheckPhoneNumbers".equals(model.getName()) || "CheckPhoneNumber".equals(model.getName()));
        }
    }

    @Test
    public void testGetSwaggerModelForWSDLComplexTypeElement() throws Exception {
        for (WSDLSOAPOperation operation : operations) {
            List<ModelImpl> inputParameterModel = operation.getInputParameterModel();
            for (ModelImpl model : inputParameterModel) {
                Map<String, Property> properties = model.getProperties();
                for (String property : properties.keySet()) {
                    Property currentProp = properties.get(property);
                    if ("CheckPhoneNumber".equals(model.getName())) {
                        Assert.assertTrue("PhoneNumber".equals(currentProp.getName()) || "LicenseKey"
                                .equals(currentProp.getName()));
                        Assert.assertEquals("string", currentProp.getType());
                    }
                    if ("CheckPhoneNumbers".equals(model.getName())) {
                        Assert.assertTrue("PhoneNumbers".equals(currentProp.getName()) || "LicenseKey"
                                .equals(currentProp.getName()));
                        if ("PhoneNumbers".equals(currentProp.getName())) {
                            Assert.assertEquals("ref", currentProp.getType());
                        } else if ("LicenceKey".equals(currentProp.getName())) {
                            Assert.assertEquals("string", currentProp.getType());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testGetSwaggerModelForCompositeComplexType() throws Exception {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(
                Thread.currentThread().getContextClassLoader().getResource("wsdls/sample-service.wsdl")
                        .toExternalForm());
        byte[] wsdlContent = wsdlReader.getWSDL();
        WSDL11SOAPOperationExtractor processor = SOAPOperationBindingUtils.getWSDL11SOAPOperationExtractor(wsdlContent,
                wsdlReader);
        Map<String, ModelImpl> parameterModelMap = processor.getWsdlInfo().getParameterModelMap();
        Assert.assertNotNull(parameterModelMap);
        Assert.assertTrue("wsdl complex types has not been properly parsed",
                parameterModelMap.size() == 12);
        //composite complex type
        Assert.assertNotNull(parameterModelMap.get("ItemSearchRequest"));
        Assert.assertEquals(7, parameterModelMap.get("ItemSearchRequest").getProperties().size());
        Assert.assertNotNull(parameterModelMap.get("ItemSearchRequest").getProperties().get("Tracks"));
        Assert.assertNotNull(parameterModelMap.get("ItemSearchRequest").getProperties().get("Tracks"));
        Assert.assertEquals(ArrayProperty.TYPE,
                parameterModelMap.get("ItemSearchRequest").getProperties().get("Tracks").getType());
        Assert.assertNotNull(((ArrayProperty) parameterModelMap.get("ItemSearchRequest").getProperties().get("Tracks"))
                .getItems());
    }

    @Test
    public void testGetSwaggerModelForSimpleType() throws Exception {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(
                Thread.currentThread().getContextClassLoader().getResource("wsdls/sample-service.wsdl")
                        .toExternalForm());
        byte[] wsdlContent = wsdlReader.getWSDL();
        WSDL11SOAPOperationExtractor processor = SOAPOperationBindingUtils.getWSDL11SOAPOperationExtractor(wsdlContent,
                wsdlReader);
        Map<String, ModelImpl> parameterModelMap = processor.getWsdlInfo().getParameterModelMap();
        Assert.assertNotNull(parameterModelMap);
        //get simple type
        Assert.assertNotNull(parameterModelMap.get("Condition"));
        Assert.assertEquals("string", parameterModelMap.get("Condition").getType());
        //get simple type inside complex type
        Assert.assertNotNull(parameterModelMap.get("ItemSearchRequest").getProperties().get("Availability"));
        Assert.assertEquals("string",
                parameterModelMap.get("ItemSearchRequest").getProperties().get("Availability").getType());
    }

    @Test
    public void testGetSwaggerModelForWSDLsWithCompositeBindings() throws Exception {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(Thread.currentThread().getContextClassLoader()
                .getResource("wsdls/wsdl-with-composite-bindings/sampleservice.wsdl").toExternalForm());
        byte[] wsdlContent = wsdlReader.getWSDL();
        WSDL11SOAPOperationExtractor processor = SOAPOperationBindingUtils.getWSDL11SOAPOperationExtractor(wsdlContent,
                wsdlReader);
        Set<WSDLSOAPOperation> operations = processor.getWsdlInfo().getSoapBindingOperations();
        Assert.assertNotNull(operations);
        Map<String, ModelImpl> parameterModelMap = processor.getWsdlInfo().getParameterModelMap();
        Assert.assertNotNull(parameterModelMap);
    }

    @Test
    public void testGetSwaggerModelForImportedSchemas() throws Exception {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(
                Thread.currentThread().getContextClassLoader().getResource("wsdls/import-schemas/sampleservice.wsdl")
                        .toExternalForm());
        byte[] wsdlContent = wsdlReader.getWSDL();
        WSDL11SOAPOperationExtractor processor = SOAPOperationBindingUtils.getWSDL11SOAPOperationExtractor(wsdlContent,
                wsdlReader);
        Set<WSDLSOAPOperation> operations = processor.getWsdlInfo().getSoapBindingOperations();
        Assert.assertNotNull(operations);
        Map<String, ModelImpl> parameterModelMap = processor.getWsdlInfo().getParameterModelMap();
        Assert.assertNotNull(parameterModelMap);
    }

    public static API getAPIForTesting() {
        API api = new API(new APIIdentifier("admin", "api1", "1.0.0"));
        api.setTransports("https");
        api.setContext("/weather");
        return api;
    }
}
