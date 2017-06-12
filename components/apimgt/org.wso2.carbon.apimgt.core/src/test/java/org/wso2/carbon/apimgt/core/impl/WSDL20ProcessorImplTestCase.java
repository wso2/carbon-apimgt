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

public class WSDL20ProcessorImplTestCase {
    private static final String ORIGINAL_ENDPOINT_MY_SERVICE = "http://yoursite.com/MyService";
    private static final String ORIGINAL_ENDPOINT_PURCHASE_ORDER = "http://actioncon.com/services/soap12/purchaseOrder";
    private static final String UPDATED_ENDPOINT_API_LABEL = "https://test.SampleLabel/weather";
    private static final String SAMPLE_LABEL_NAME = "SampleLabel";
    private static final String WSDL_VERSION_20 = "2.0";

    @Test
    public void testSingleWSDL() throws Exception {
        WSDL20ProcessorImpl wsdl20Processor = new WSDL20ProcessorImpl();
        byte[] wsdlContentBytes = SampleTestObjectCreator.createDefaultWSDL20Content();
        wsdl20Processor.init(wsdlContentBytes);
        Assert.assertTrue(wsdl20Processor.canProcess());
        assertDefaultSingleWSDLContent(wsdl20Processor.getWsdlInfo(), ORIGINAL_ENDPOINT_MY_SERVICE, null);

        //validate the content from getWSDL() after initializing the WSDL processor
        byte[] originalWsdlContentFromProcessor = wsdl20Processor.getWSDL();
        WSDL20ProcessorImpl wsdl20Processor2 = new WSDL20ProcessorImpl();
        wsdl20Processor2.init(originalWsdlContentFromProcessor);
        Assert.assertTrue(wsdl20Processor2.canProcess());
        assertDefaultSingleWSDLContent(wsdl20Processor2.getWsdlInfo(), ORIGINAL_ENDPOINT_MY_SERVICE, null);

        //validate the content after updating endpoints using an API and Label
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Label label = SampleTestObjectCreator.createLabel(SAMPLE_LABEL_NAME).build();
        byte[] updatedWSDLWithEndpoint = wsdl20Processor.getUpdatedWSDL(api, label);
        //validate the content of wsdl20Processor's WSDL Info content after updating its endpoints
        assertDefaultSingleWSDLContent(wsdl20Processor.getWsdlInfo(), UPDATED_ENDPOINT_API_LABEL,
                ORIGINAL_ENDPOINT_MY_SERVICE);

        //validate the content of wsdl20Processor's returned WSDL content after updating its endpoints
        WSDL20ProcessorImpl wsdl20Processor3 = new WSDL20ProcessorImpl();
        wsdl20Processor3.init(updatedWSDLWithEndpoint);
        Assert.assertTrue(wsdl20Processor3.canProcess());
        assertDefaultSingleWSDLContent(wsdl20Processor3.getWsdlInfo(), UPDATED_ENDPOINT_API_LABEL,
                ORIGINAL_ENDPOINT_MY_SERVICE);
    }

    @Test
    public void testWSDLArchive() throws Exception {
        String extractedLocation = SampleTestObjectCreator.createDefaultWSDL20Archive();
        WSDL20ProcessorImpl wsdl20Processor = new WSDL20ProcessorImpl();
        wsdl20Processor.initPath(extractedLocation);
        Assert.assertTrue(wsdl20Processor.canProcess());
        assertDefaultArchivedWSDLContent(wsdl20Processor.getWsdlInfo(),
                new String[] { ORIGINAL_ENDPOINT_MY_SERVICE, ORIGINAL_ENDPOINT_PURCHASE_ORDER }, null);

        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Label label = SampleTestObjectCreator.createLabel(SAMPLE_LABEL_NAME).build();
        String updatedPath = wsdl20Processor.getUpdatedWSDLPath(api, label);

        WSDL20ProcessorImpl wsdl20Processor2 = new WSDL20ProcessorImpl();
        wsdl20Processor2.initPath(updatedPath);
        Assert.assertTrue(wsdl20Processor2.canProcess());
        assertDefaultArchivedWSDLContent(wsdl20Processor2.getWsdlInfo(), new String[] { UPDATED_ENDPOINT_API_LABEL },
                new String[] { ORIGINAL_ENDPOINT_MY_SERVICE, ORIGINAL_ENDPOINT_PURCHASE_ORDER });
    }

    private void assertDefaultSingleWSDLContent(WSDLInfo wsdlInfo, String endpointURIToAssertContains,
            String endpointURIToAssertNotContains) throws Exception {
        //Assert wsdlInfo fields
        Assert.assertEquals(wsdlInfo.getVersion(), WSDL_VERSION_20);
        Assert.assertEquals(wsdlInfo.getEndpoints().size(), 1);
        if (endpointURIToAssertContains != null) {
            Assert.assertTrue(wsdlInfo.getEndpoints().containsValue(endpointURIToAssertContains));
        }
        if (endpointURIToAssertNotContains != null) {
            Assert.assertFalse(wsdlInfo.getEndpoints().containsValue(endpointURIToAssertNotContains));
        }
    }

    private void assertDefaultArchivedWSDLContent(WSDLInfo wsdlInfo, String[] endpointURIsToAssertContains,
            String[] endpointURIsToAssertNotContains) throws Exception {
        //Assert wsdlInfo fields
        Assert.assertEquals(wsdlInfo.getVersion(), WSDL_VERSION_20);
        Assert.assertEquals(wsdlInfo.getEndpoints().size(), 2);
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
    }
}
