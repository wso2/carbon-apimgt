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
package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link GatewayUtils#retrieveGCPServiceAccountKeyChunkAlias}. The alias is stored in the
 * registry {@code REG_PROPERTY.REG_NAME} column ({@code VARCHAR(100)}), so the primary guarantee is that it
 * always fits regardless of how long the API name / version / endpoint UUID are, while remaining unique and
 * deterministic per (api, version, endpoint, stage, chunk index).
 */
public class GatewayUtilsGCPAliasTest {

    private static final int REG_NAME_LIMIT = 100;
    private static final String PREFIX = "gcp--serviceAccountKey--";

    @Test
    public void testAliasFitsRegNameColumnForLongInputs() {

        // A long API name, a long version, and a full 36-char endpoint UUID with a 3-digit chunk index.
        String alias = GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias(
                "GoogleVertexAI-AnthropicClaudeAPI-With-A-Very-Long-Descriptive-Name",
                "vertex-2023-10-16-preview",
                "a1b2c3d4-1111-2222-3333-444455556666",
                "PRODUCTION", 999);

        Assert.assertTrue("Alias must fit the VARCHAR(100) REG_NAME column, was " + alias.length(),
                alias.length() <= REG_NAME_LIMIT);
        Assert.assertTrue("Alias must carry the readable gcp service-account-key prefix",
                alias.startsWith(PREFIX));
        Assert.assertTrue("The chunk index must stay outside the hash (visible/greppable)",
                alias.endsWith("--999"));
    }

    @Test
    public void testRegressionOriginalOverflowingCaseNowFits() {

        // The exact case that overflowed REG_NAME at 105 chars before the fix.
        String alias = GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias(
                "GoogleVertexAI-AnthropicClaudeAPI", "vertex-2023-10-16",
                "default_production_endpoint", "production", 0);

        Assert.assertTrue("Previously-overflowing alias must now fit, was " + alias.length(),
                alias.length() <= REG_NAME_LIMIT);
    }

    @Test
    public void testAliasIsDeterministic() {

        String a = GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias("API", "1.0.0", "ep-uuid", "PRODUCTION", 3);
        String b = GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias("API", "1.0.0", "ep-uuid", "PRODUCTION", 3);

        Assert.assertEquals("Same inputs must yield the same alias (store side == lookup side)", a, b);
    }

    @Test
    public void testAliasIsUniquePerDimension() {

        String base = GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias("API", "1.0.0", "ep-1", "PRODUCTION", 0);

        // Each dimension (chunk index, endpoint, stage, version, name) must change the alias.
        Assert.assertNotEquals(base,
                GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias("API", "1.0.0", "ep-1", "PRODUCTION", 1));
        Assert.assertNotEquals(base,
                GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias("API", "1.0.0", "ep-2", "PRODUCTION", 0));
        Assert.assertNotEquals(base,
                GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias("API", "1.0.0", "ep-1", "SANDBOX", 0));
        Assert.assertNotEquals(base,
                GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias("API", "2.0.0", "ep-1", "PRODUCTION", 0));
        Assert.assertNotEquals(base,
                GatewayUtils.retrieveGCPServiceAccountKeyChunkAlias("OTHER", "1.0.0", "ep-1", "PRODUCTION", 0));
    }
}
