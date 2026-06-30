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

package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;

public class PlatformGatewayTokenUtilTest {

    @Test
    public void testParseTokenId() {
        Assert.assertEquals("abc-123", PlatformGatewayTokenUtil.parseTokenId("abc-123.plainTokenValue"));
    }

    @Test
    public void testConstantTimeEqualsMatchesSameToken() {
        Assert.assertTrue(PlatformGatewayTokenUtil.constantTimeEquals("id.plain", "id.plain"));
    }

    @Test
    public void testConstantTimeEqualsRejectsWhitespacePaddedToken() {
        Assert.assertFalse(PlatformGatewayTokenUtil.constantTimeEquals(" id.plain ", "id.plain"));
    }

    @Test
    public void testMatchesActiveTokenHash() throws Exception {
        String plainToken = "plain-token";
        String hash = PlatformGatewayTokenUtil.hashToken(plainToken);
        PlatformGatewayDAO.TokenWithGateway tokenRow =
                new PlatformGatewayDAO.TokenWithGateway(hash, "gw-1", "carbon.super", "gw");
        Assert.assertTrue(PlatformGatewayTokenUtil.matchesActiveTokenHash(tokenRow, plainToken));
        Assert.assertFalse(PlatformGatewayTokenUtil.matchesActiveTokenHash(tokenRow, "other-token"));
    }
}
