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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.auth;

import feign.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*
  Test cases for class DCRErrorDecoder
 */
public class DCRErrorDecoderTest {
    @Test (description = "Test Response error decoding")
    public void testDecode() throws Exception {
        final String errorName = "Test Error";
        final String errorDescription = "The description of test error";
        DCRErrorDecoder dcrErrorDecoder = new DCRErrorDecoder();
        Map<String, Collection<String>> headers = new HashMap();
        Response response = Response.create(APIMgtConstants.HTTPStatusCodes.SC_400_BAD_REQUEST, "Reason for the error",
                headers, "{'error':'" + errorName + "', 'error_description':'" + errorDescription + "'}",
                Charset.defaultCharset());
        Exception exception = dcrErrorDecoder.decode("", response);
        String expectedErrorMessage = "Error occurred while DCR request. Error: " + errorName + ". Error Description: "
                + errorDescription;
        Assert.assertEquals(expectedErrorMessage, exception.getMessage());
    }
}

