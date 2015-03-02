/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.apache.synapse.commons.json;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;

import java.io.InputStream;
import java.io.OutputStream;

public class JsonStreamingFormatterTest extends TestCase {

    private static final String obj_1 = "{\n" +
                                        "  \"name\" : \"John\",\n" +
                                        "  \"email\" : \"john@synapse.org\",\n" +
                                        "  \"id\" : 33\n" +
                                        "}\n";

    public void testCase() {
        try {
            InputStream inputStream = Util.newInputStream(obj_1.getBytes());
            MessageFormatter formatter = Util.newJsonStreamFormatter();
            MessageContext messageContext = Util.newMessageContext();
            JsonUtil.newJsonPayload(messageContext, inputStream, true, true);
            OutputStream out = Util.newOutputStream();
            formatter.writeTo(messageContext, null, out, false);
            assertTrue(obj_1.equals(out.toString()));
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            assertTrue(false);
        }
    }
}
