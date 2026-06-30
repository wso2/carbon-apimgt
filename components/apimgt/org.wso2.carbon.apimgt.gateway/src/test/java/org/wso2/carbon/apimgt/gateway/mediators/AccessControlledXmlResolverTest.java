/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.mediators;

import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.util.ArrayList;
import java.util.List;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for the validation/blocking logic of {@link AccessControlledXmlResolver}. The actual
 * (redirect-safe) fetching of a permitted reference and the LSInput contract are exercised against a
 * real HTTP server in {@link RedirectSafeXsdFetcherTest}.
 */
public class AccessControlledXmlResolverTest {

    @Test(expected = XsdRefBlockedException.class)
    public void testBlocksDeniedHost() {
        AccessControlledXmlResolver resolver = new AccessControlledXmlResolver(url -> {
            throw new APIManagementException("blocked");
        });
        resolver.resolveResource(W3C_XML_SCHEMA_NS_URI, null, null,
                "http://10.0.0.5/internal.xsd", "http://schemas.example.com/main.xsd");
    }

    @Test(expected = XsdRefBlockedException.class)
    public void testBlocksNonHttpScheme() {
        AccessControlledXmlResolver resolver = new AccessControlledXmlResolver(url -> { });
        resolver.resolveResource(W3C_XML_SCHEMA_NS_URI, null, null, "file:///etc/passwd", null);
    }

    @Test(expected = XsdRefBlockedException.class)
    public void testBlocksUnresolvableSystemId() {
        AccessControlledXmlResolver resolver = new AccessControlledXmlResolver(url -> { });
        resolver.resolveResource(W3C_XML_SCHEMA_NS_URI, null, null, "relative/types.xsd", null);
    }

    @Test
    public void testResolvesRelativeAgainstBaseBeforeValidating() {
        List<String> validated = new ArrayList<>();
        AccessControlledXmlResolver resolver = new AccessControlledXmlResolver(url -> {
            validated.add(url);
            throw new APIManagementException("stop before fetch");
        });
        try {
            resolver.resolveResource(W3C_XML_SCHEMA_NS_URI, null, null,
                    "common/types.xsd", "http://schemas.example.com/dir/main.xsd");
            fail("expected XsdRefBlockedException");
        } catch (XsdRefBlockedException expected) {
        }
        assertEquals("http://schemas.example.com/dir/common/types.xsd", validated.get(0));
    }
}
