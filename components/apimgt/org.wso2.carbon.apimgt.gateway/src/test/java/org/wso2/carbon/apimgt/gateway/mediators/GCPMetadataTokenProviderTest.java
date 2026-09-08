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
 * Unit tests for {@link GCPMetadataTokenProvider} focused on the metadata token-URL construction - the
 * comma-separated {@code scopes} query parameter (as the GCE metadata server expects, unlike the
 * space-separated JWT-bearer form) and the {@code GCE_METADATA_HOST} host override.
 * <p>
 * The scope tests use the host-explicit {@code buildTokenUrl(scope, host)} overload so they are independent of
 * any {@code GCE_METADATA_HOST} set in the build environment.
 */
public class GCPMetadataTokenProviderTest {

    private static final String DEFAULT_HOST = "metadata.google.internal";
    private static final String BASE_URL =
            "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token";

    @Test
    public void testNoScopeOmitsScopesParameter() {

        Assert.assertEquals(BASE_URL, GCPMetadataTokenProvider.buildTokenUrl("", DEFAULT_HOST));
        Assert.assertEquals(BASE_URL, GCPMetadataTokenProvider.buildTokenUrl(null, DEFAULT_HOST));
    }

    @Test
    public void testSingleScopeIsAppendedUnchanged() {

        Assert.assertEquals(
                BASE_URL + "?scopes=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fcloud-platform",
                GCPMetadataTokenProvider.buildTokenUrl(
                        "https://www.googleapis.com/auth/cloud-platform", DEFAULT_HOST));
    }

    @Test
    public void testSpaceSeparatedScopesAreJoinedWithCommas() {

        // The stored/JWT form is space-separated; the metadata server requires commas.
        String url = GCPMetadataTokenProvider.buildTokenUrl(
                "https://www.googleapis.com/auth/cloud-platform https://www.googleapis.com/auth/userinfo.email",
                DEFAULT_HOST);
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
                GCPMetadataTokenProvider.buildTokenUrl("  a   b\tc  ", DEFAULT_HOST));
    }

    @Test
    public void testCustomHostIsUsedInTheUrl() {

        // GCE_METADATA_HOST override: only the host changes, the path stays fixed.
        Assert.assertEquals(
                "http://metadata.internal.example/computeMetadata/v1/instance/service-accounts/default/token",
                GCPMetadataTokenProvider.buildTokenUrl("", "metadata.internal.example"));
        Assert.assertEquals(
                "http://metadata.internal.example/computeMetadata/v1/instance/service-accounts/default/token"
                        + "?scopes=a%2Cb",
                GCPMetadataTokenProvider.buildTokenUrl("a b", "metadata.internal.example"));
    }

    @Test
    public void testResolveMetadataHostDefaultsWhenEnvUnset() {

        // In the build environment GCE_METADATA_HOST is not set, so the default host is used. When it is set
        // on the VM, resolveMetadataHost() returns that value (exercised on the workload, not portably in a
        // unit test since the JVM cannot set its own environment).
        if (System.getenv(GCPMetadataTokenProvider.GCE_METADATA_HOST_ENV_VAR) == null) {
            Assert.assertEquals(DEFAULT_HOST, GCPMetadataTokenProvider.resolveMetadataHost());
        }
    }
}
