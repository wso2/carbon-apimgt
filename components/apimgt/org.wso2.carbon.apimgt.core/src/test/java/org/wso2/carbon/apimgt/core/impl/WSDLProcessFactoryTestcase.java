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
import org.wso2.carbon.apimgt.core.api.WSDLProcessor;

public class WSDLProcessFactoryTestcase {

    @Test
    public void testGetWSDLProcessorForPath() throws Exception {
        String extractedLocation = SampleTestObjectCreator.createDefaultWSDL11Archive();
        WSDLProcessor processor = WSDLProcessFactory.getInstance().getWSDLProcessorForPath(extractedLocation);
        Assert.assertTrue(processor instanceof WSDL11ProcessorImpl);

        extractedLocation = SampleTestObjectCreator.createDefaultWSDL20Archive();
        processor = WSDLProcessFactory.getInstance().getWSDLProcessorForPath(extractedLocation);
        Assert.assertTrue(processor instanceof WSDL20ProcessorImpl);
    }

    @Test
    public void testGetWSDLProcessorForContent() throws Exception {
        byte[] wsdl11Content = SampleTestObjectCreator.createDefaultWSDL11Content();
        WSDLProcessor processor = WSDLProcessFactory.getInstance().getWSDLProcessor(wsdl11Content);
        Assert.assertTrue(processor instanceof WSDL11ProcessorImpl);

        byte[] wsdl20Content = SampleTestObjectCreator.createDefaultWSDL20Content();
        processor = WSDLProcessFactory.getInstance().getWSDLProcessor(wsdl20Content);
        Assert.assertTrue(processor instanceof WSDL20ProcessorImpl);
    }
}
