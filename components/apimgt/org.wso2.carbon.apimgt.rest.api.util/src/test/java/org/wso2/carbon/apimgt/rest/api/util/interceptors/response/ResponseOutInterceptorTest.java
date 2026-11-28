/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.rest.api.util.interceptors.response;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import javax.ws.rs.core.MultivaluedMap;

public class ResponseOutInterceptorTest {

    private static final String X_XSS_PROTECTION = "X-XSS-Protection";
    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String NO_SNIFF = "nosniff";

    @Test
    public void testContentTypeOptionsHeaderIsAdded() {

        Message message = new MessageImpl();

        new ResponseOutInterceptor().handleMessage(message);

        MultivaluedMap<String, Object> headers = getProtocolHeaders(message);
        Assert.assertNotNull(headers);
        Assert.assertEquals(Collections.singletonList(NO_SNIFF), headers.get(X_CONTENT_TYPE_OPTIONS));
    }

    @Test
    public void testXSSProtectionHeaderIsNotAdded() {

        Message message = new MessageImpl();

        new ResponseOutInterceptor().handleMessage(message);

        MultivaluedMap<String, Object> headers = getProtocolHeaders(message);
        Assert.assertNotNull(headers);
        Assert.assertFalse(headers.containsKey(X_XSS_PROTECTION));
    }

    @Test
    public void testExistingHeadersArePreserved() {

        Message message = new MessageImpl();
        MultivaluedMap<String, Object> existingHeaders = new MetadataMap<>();
        existingHeaders.add("Cache-Control", "no-store");
        message.put(Message.PROTOCOL_HEADERS, existingHeaders);

        new ResponseOutInterceptor().handleMessage(message);

        MultivaluedMap<String, Object> headers = getProtocolHeaders(message);
        Assert.assertEquals(Collections.singletonList("no-store"), headers.get("Cache-Control"));
        Assert.assertEquals(Collections.singletonList(NO_SNIFF), headers.get(X_CONTENT_TYPE_OPTIONS));
        Assert.assertFalse(headers.containsKey(X_XSS_PROTECTION));
    }

    @SuppressWarnings("unchecked")
    private MultivaluedMap<String, Object> getProtocolHeaders(Message message) {

        return (MultivaluedMap<String, Object>) message.get(Message.PROTOCOL_HEADERS);
    }
}
