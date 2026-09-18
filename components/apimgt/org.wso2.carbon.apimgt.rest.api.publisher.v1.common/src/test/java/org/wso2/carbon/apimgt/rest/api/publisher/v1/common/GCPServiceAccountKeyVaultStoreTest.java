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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Unit tests for the GCP secure-vault key-lookup expression built by
 * {@link GCPServiceAccountKeyVaultStore#buildVaultLookupExpression(java.util.List)}. The expression is what the
 * endpoint sequence emits in secure-vault mode; the gateway mediator splits its resolved value on {@code '|'}
 * before base64-decoding the key.
 */
public class GCPServiceAccountKeyVaultStoreTest {

    @Test
    public void testSingleAliasIsABareLookup() {

        // XPath concat() requires >= 2 args, so one chunk must be a bare vault-lookup (not concat).
        Assert.assertEquals("wso2:vault-lookup('a0')",
                GCPServiceAccountKeyVaultStore.buildVaultLookupExpression(Collections.singletonList("a0")));
    }

    @Test
    public void testTwoAliasesAreConcatenatedWithPipe() {

        Assert.assertEquals(
                "concat(wso2:vault-lookup('a0'), '|', wso2:vault-lookup('a1'))",
                GCPServiceAccountKeyVaultStore.buildVaultLookupExpression(Arrays.asList("a0", "a1")));
    }

    @Test
    public void testManyAliasesInterleavePipeDelimiters() {

        String expression = GCPServiceAccountKeyVaultStore.buildVaultLookupExpression(Arrays.asList("a0", "a1", "a2"));

        Assert.assertEquals(
                "concat(wso2:vault-lookup('a0'), '|', wso2:vault-lookup('a1'), '|', wso2:vault-lookup('a2'))",
                expression);
        // One fewer delimiter than lookups (n lookups -> n-1 pipes).
        Assert.assertEquals(3, countOccurrences(expression, "vault-lookup"));
        Assert.assertEquals(2, countOccurrences(expression, "'|'"));
    }

    private static int countOccurrences(String haystack, String needle) {

        int count = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) {
            count++;
        }
        return count;
    }
}
