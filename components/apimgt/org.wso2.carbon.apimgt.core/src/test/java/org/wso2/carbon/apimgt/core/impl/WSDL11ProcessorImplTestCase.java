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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.WSDLInfo;

public class WSDL11ProcessorImplTestCase {
    private static final String UPDATED_ENDPOINT_API_LABEL = "https://test.SampleLabel/weather";
    private static final String SAMPLE_LABEL_NAME = "SampleLabel";
    private static final String WSDL_VERSION_11 = "1.1";

    @Test
    public void testSingleWSDL() throws Exception {
        WSDL11ProcessorImpl wsdl11Processor = new WSDL11ProcessorImpl();
        byte[] wsdlContentBytes = SampleTestObjectCreator.createDefaultWSDL11Content();
        wsdl11Processor.init(wsdlContentBytes);
        Assert.assertTrue(wsdl11Processor.canProcess());
        assertDefaultSingleWSDLContent(wsdl11Processor.getWsdlInfo(), SampleTestObjectCreator.ORIGINAL_ENDPOINT_WEATHER,
                null);

        //validate the content from getWSDL() after initializing the WSDL processor
        byte[] originalWsdlContentFromProcessor = wsdl11Processor.getWSDL();
        WSDL11ProcessorImpl wsdl11Processor2 = new WSDL11ProcessorImpl();
        wsdl11Processor2.init(originalWsdlContentFromProcessor);
        Assert.assertTrue(wsdl11Processor2.canProcess());
        assertDefaultSingleWSDLContent(wsdl11Processor2.getWsdlInfo(),
                SampleTestObjectCreator.ORIGINAL_ENDPOINT_WEATHER, null);

        //validate the content after updating endpoints using an API and Label
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Label label = SampleTestObjectCreator.createLabel(SAMPLE_LABEL_NAME).build();
        byte[] updatedWSDLWithEndpoint = wsdl11Processor.getUpdatedWSDL(api, label);
        //validate the content of wsdl11Processor's WSDL Info content after updating its endpoints
        assertDefaultSingleWSDLContent(wsdl11Processor.getWsdlInfo(), UPDATED_ENDPOINT_API_LABEL,
                SampleTestObjectCreator.ORIGINAL_ENDPOINT_WEATHER);

        //validate the content of wsdl11Processor's returned WSDL content after updating its endpoints
        WSDL11ProcessorImpl wsdl11Processor3 = new WSDL11ProcessorImpl();
        wsdl11Processor3.init(updatedWSDLWithEndpoint);
        Assert.assertTrue(wsdl11Processor3.canProcess());
        assertDefaultSingleWSDLContent(wsdl11Processor3.getWsdlInfo(), UPDATED_ENDPOINT_API_LABEL,
                SampleTestObjectCreator.ORIGINAL_ENDPOINT_WEATHER);
    }

    @Test
    public void testWSDLArchive() throws Exception {
        String extractedLocation = SampleTestObjectCreator.createDefaultWSDL11Archive();
        WSDL11ProcessorImpl wsdl11Processor = new WSDL11ProcessorImpl();
        wsdl11Processor.initPath(extractedLocation);
        Assert.assertTrue(wsdl11Processor.canProcess());
        assertDefaultArchivedWSDLContent(wsdl11Processor.getWsdlInfo(),
                new String[] { SampleTestObjectCreator.ORIGINAL_ENDPOINT_WEATHER,
                        SampleTestObjectCreator.ORIGINAL_ENDPOINT_STOCK_QUOTE }, null);

        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Label label = SampleTestObjectCreator.createLabel(SAMPLE_LABEL_NAME).build();
        String updatedPath = wsdl11Processor.getUpdatedWSDLPath(api, label);

        WSDL11ProcessorImpl wsdl11Processor2 = new WSDL11ProcessorImpl();
        wsdl11Processor2.initPath(updatedPath);
        Assert.assertTrue(wsdl11Processor2.canProcess());
        assertDefaultArchivedWSDLContent(wsdl11Processor2.getWsdlInfo(), new String[] { UPDATED_ENDPOINT_API_LABEL },
                new String[] { SampleTestObjectCreator.ORIGINAL_ENDPOINT_STOCK_QUOTE,
                        SampleTestObjectCreator.ORIGINAL_ENDPOINT_WEATHER });
    }

    private void assertDefaultSingleWSDLContent(WSDLInfo wsdlInfo, String endpointURIToAssertContains,
            String endpointURIToAssertNotContains) throws Exception {
        //Assert wsdlInfo fields
        Assert.assertEquals(wsdlInfo.getVersion(), WSDL_VERSION_11);
        Assert.assertEquals(wsdlInfo.getEndpoints().size(), 4);
        if (endpointURIToAssertContains != null) {
            Assert.assertTrue(wsdlInfo.getEndpoints().containsValue(endpointURIToAssertContains));
        }
        if (endpointURIToAssertNotContains != null) {
            Assert.assertFalse(wsdlInfo.getEndpoints().containsValue(endpointURIToAssertNotContains));
        }
        Assert.assertTrue(wsdlInfo.hasHttpBindingOperations());
        Assert.assertTrue(wsdlInfo.hasSoapBindingOperations());
        Assert.assertEquals(wsdlInfo.getHttpBindingOperations().size(), 4);
    }

    private void assertDefaultArchivedWSDLContent(WSDLInfo wsdlInfo, String[] endpointURIsToAssertContains,
            String[] endpointURIsToAssertNotContains) throws Exception {
        //Assert wsdlInfo fields
        Assert.assertEquals(wsdlInfo.getVersion(), WSDL_VERSION_11);
        Assert.assertEquals(wsdlInfo.getEndpoints().size(), 8);
        if (endpointURIsToAssertNotContains != null) {
            for (String endpoint : endpointURIsToAssertNotContains) {
                Assert.assertFalse(wsdlInfo.getEndpoints().containsValue(endpoint));
            }
        }
        if (endpointURIsToAssertContains != null) {
            for (String endpoint : endpointURIsToAssertContains) {
                Assert.assertTrue(wsdlInfo.getEndpoints().containsValue(endpoint));
            }
        }
        Assert.assertTrue(wsdlInfo.hasHttpBindingOperations());
        Assert.assertTrue(wsdlInfo.hasSoapBindingOperations());
        Assert.assertEquals(wsdlInfo.getHttpBindingOperations().size(), 6);
    }
}
