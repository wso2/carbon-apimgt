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
package org.wso2.carbon.apimgt.impl.soaptorest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class SOAPOperationBindingTestCase {

    @Test
    public void testGetSoapOperationMapping() throws Exception {
        String mapping = SOAPOperationBindingUtils.getSoapOperationMapping(Thread.currentThread().getContextClassLoader()
                .getResource("wsdls/phoneverify.wsdl").toExternalForm());
        Assert.assertTrue("Failed getting soap operation mapping from the WSDL", !mapping.isEmpty());
    }

    @Test
    public void testGetWSDLProcessor() throws Exception {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(Thread.currentThread().getContextClassLoader()
                .getResource("wsdls/phoneverify.wsdl").toExternalForm());
        byte[] wsdlContent = wsdlReader.getWSDL();
        WSDLSOAPOperationExtractor processor = SOAPOperationBindingUtils.getWSDLProcessor(wsdlContent, wsdlReader);
        Assert.assertNotNull(processor);
        Assert.assertTrue("Failed to get soap binding operations from the WSDL", processor.getWsdlInfo().getSoapBindingOperations().size() > 0);
    }

}
