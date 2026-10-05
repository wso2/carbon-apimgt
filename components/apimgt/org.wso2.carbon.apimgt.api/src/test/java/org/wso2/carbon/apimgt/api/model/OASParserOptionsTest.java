/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.api.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class OASParserOptionsTest {
    @Test
    public void testRemoteRefListsDefaultNullAndRoundTrip() {
        OASParserOptions o = new OASParserOptions();
        Assert.assertNull(o.getRemoteRefAllowList());
        Assert.assertNull(o.getRemoteRefBlockList());
        // Backwards compatibility: network access control is off unless explicitly enabled.
        Assert.assertFalse(o.isNetworkAccessControlEnabled());
        o.setRemoteRefAllowList(Arrays.asList("*.wso2.com", "api.github.com"));
        o.setRemoteRefBlockList(Arrays.asList("*.internal"));
        o.setNetworkAccessControlEnabled(true);
        Assert.assertEquals(2, o.getRemoteRefAllowList().size());
        Assert.assertEquals("*.internal", o.getRemoteRefBlockList().get(0));
        Assert.assertTrue(o.isNetworkAccessControlEnabled());
    }

    @Test
    public void testCopyConstructorCopiesFieldsRaw() {
        OASParserOptions that = new OASParserOptions();
        that.setExplicitStyleAndExplode("false");
        that.setYamlCodePointLimit("10");
        that.setRemoteRefAllowList(Arrays.asList("*.wso2.com", "api.github.com"));
        that.setRemoteRefBlockList(Arrays.asList("*.internal"));
        that.setNetworkAccessControlEnabled(true);

        OASParserOptions copy = new OASParserOptions(that);

        Assert.assertEquals(that.isExplicitStyleAndExplode(), copy.isExplicitStyleAndExplode());
        // Must be copied verbatim (already a code-point count), not re-converted as if it were MB.
        Assert.assertEquals(that.getYamlCodePointLimit(), copy.getYamlCodePointLimit());
        Assert.assertNotEquals(Integer.valueOf(Integer.MAX_VALUE), copy.getYamlCodePointLimit());
        Assert.assertEquals(that.getRemoteRefAllowList(), copy.getRemoteRefAllowList());
        Assert.assertEquals(that.getRemoteRefBlockList(), copy.getRemoteRefBlockList());
        Assert.assertTrue(copy.isNetworkAccessControlEnabled());

        OASParserOptions fromNull = new OASParserOptions(null);
        Assert.assertNull(fromNull.getRemoteRefAllowList());
        Assert.assertNull(fromNull.getRemoteRefBlockList());
        Assert.assertNull(fromNull.getYamlCodePointLimit());
        Assert.assertTrue(fromNull.isExplicitStyleAndExplode());
        Assert.assertFalse(fromNull.isNetworkAccessControlEnabled());
    }
}
