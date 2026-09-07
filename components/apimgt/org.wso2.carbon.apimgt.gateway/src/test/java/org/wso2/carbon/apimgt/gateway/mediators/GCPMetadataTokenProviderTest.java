/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.mediators;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link GCPMetadataTokenProvider} focused on the metadata token-URL construction - in
 * particular that the {@code scopes} query parameter is delivered comma-separated (as the GCE metadata
 * server expects), unlike the space-separated form used for the JWT-bearer assertion.
 */
public class GCPMetadataTokenProviderTest {

    private static final String BASE_URL =
            "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token";

    @Test
    public void testNoScopeOmitsScopesParameter() {

        Assert.assertEquals(BASE_URL, GCPMetadataTokenProvider.buildTokenUrl(""));
        Assert.assertEquals(BASE_URL, GCPMetadataTokenProvider.buildTokenUrl(null));
    }

    @Test
    public void testSingleScopeIsAppendedUnchanged() {

        Assert.assertEquals(
                BASE_URL + "?scopes=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fcloud-platform",
                GCPMetadataTokenProvider.buildTokenUrl("https://www.googleapis.com/auth/cloud-platform"));
    }

    @Test
    public void testSpaceSeparatedScopesAreJoinedWithCommas() {

        // The stored/JWT form is space-separated; the metadata server requires commas.
        String url = GCPMetadataTokenProvider.buildTokenUrl(
                "https://www.googleapis.com/auth/cloud-platform https://www.googleapis.com/auth/userinfo.email");
        Assert.assertEquals(
                BASE_URL + "?scopes=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fcloud-platform"
                        + "%2Chttps%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email",
                url);
        // No raw space (encoded as '+' or %20) survives into the scopes value.
        Assert.assertFalse(url.contains("+"));
        Assert.assertFalse(url.contains("%20"));
    }

    @Test
    public void testCollapsesRunsOfWhitespaceAndTrims() {

        // Leading/trailing whitespace and multi-space/tab runs between scopes collapse to single commas.
        Assert.assertEquals(
                BASE_URL + "?scopes=a%2Cb%2Cc",
                GCPMetadataTokenProvider.buildTokenUrl("  a   b\tc  "));
    }
}
