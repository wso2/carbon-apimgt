/*
 *   Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.solace;

import org.junit.Test;
import org.wso2.carbon.apimgt.solace.parser.SolaceApiParser;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for SolaceApiParser class.
 */
public class SolaceApiParserTest {

    private final SolaceApiParser parser = new SolaceApiParser();

    @Test
    public void testGetVendorFromExtension_ReturnsSolace_WhenVendorIsSolaceLowercase() throws Exception {
        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"t\",\"version\":\"1.0.0\"}," +
                "\"x-origin\":{\"vendor\":\"solace\"}}";
        String vendor = parser.getVendorFromExtensionWithError(definition);
        assertEquals("Vendor must be recognized as Solace", SolaceConstants.SOLACE_ENVIRONMENT, vendor);
    }

    @Test
    public void testGetVendorFromExtension_ReturnsSolace_WhenVendorIsSolaceUppercase() throws Exception {
        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"t\",\"version\":\"1.0.0\"}," +
                "\"x-origin\":{\"vendor\":\"SOLACE\"}}";
        String vendor = parser.getVendorFromExtensionWithError(definition);
        assertEquals("Vendor matching should be case-insensitive", SolaceConstants.SOLACE_ENVIRONMENT, vendor);
    }

    @Test
    public void testGetVendorFromExtension_ReturnsNull_WhenVendorNotPresent() throws Exception {
        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"t\",\"version\":\"1.0.0\"}}";
        String vendor = parser.getVendorFromExtensionWithError(definition);
        assertNull("When no x-origin extension exists, vendor should be null", vendor);
    }

    @Test
    public void testGetVendorFromExtension_ReturnsNull_WhenOriginHasDifferentVendor() throws Exception {
        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"t\",\"version\":\"1.0.0\"}," +
                "\"x-origin\":{\"vendor\":\"other-vendor\"}}";
        String vendor = parser.getVendorFromExtensionWithError(definition);
        assertNull("When vendor is not Solace, method should return null", vendor);
    }

    @Test
    public void testGetVendorFromExtension_vendorMatches_returnsSolace() throws Exception {
        // x-origin object contains vendor key with matching value
        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"t\",\"version\":\"1.0.0\"}," +
                "\"x-origin\":{\"vendor\":\"" + SolaceConstants.SOLACE_ENVIRONMENT + "\"}}";
        String vendor = parser.getVendorFromExtensionWithError(definition);
        assertEquals(SolaceConstants.SOLACE_ENVIRONMENT, vendor);
    }

    @Test
    public void testGetVendorFromExtension_xOriginPresent_butNoVendorKey_returnsNull() throws Exception {
        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"t\",\"version\":\"1.0.0\"}," +
                "\"x-origin\":{\"other\":\"value\"}}";
        String vendor = parser.getVendorFromExtensionWithError(definition);
        assertNull("Missing vendor key inside x-origin should return null", vendor);
    }

}
