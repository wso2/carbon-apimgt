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
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PlatformGatewayDAO.class})
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
    public void testConstantTimeEqualsRejectsDifferentLengthWithoutEarlyReturn() {
        Assert.assertFalse(PlatformGatewayTokenUtil.constantTimeEquals("short", "much-longer-value"));
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

    @Test
    public void testMatchesConnectConfigRegistrationTokenAllowsPreBootstrap() throws Exception {
        PowerMockito.mockStatic(PlatformGatewayDAO.class);
        PlatformGatewayDAO dao = Mockito.mock(PlatformGatewayDAO.class);
        PowerMockito.when(PlatformGatewayDAO.getInstance()).thenReturn(dao);
        Mockito.when(dao.getActiveTokenById("token-id")).thenReturn(null);

        String apiKey = "token-id.plain-token";
        Assert.assertTrue(PlatformGatewayTokenUtil.matchesConnectConfigRegistrationToken(apiKey, apiKey));
    }

    @Test
    public void testMatchesConnectConfigRegistrationTokenRejectsStaleTomlAfterRotation() throws Exception {
        PowerMockito.mockStatic(PlatformGatewayDAO.class);
        PlatformGatewayDAO dao = Mockito.mock(PlatformGatewayDAO.class);
        PowerMockito.when(PlatformGatewayDAO.getInstance()).thenReturn(dao);

        String stalePlain = "old-plain-token";
        String newPlain = "new-plain-token";
        String tokenId = "token-id";
        String staleApiKey = tokenId + "." + stalePlain;
        String newHash = PlatformGatewayTokenUtil.hashToken(newPlain);
        PlatformGatewayDAO.TokenWithGateway activeRow =
                new PlatformGatewayDAO.TokenWithGateway(newHash, "gw-1", "carbon.super", "gw");
        Mockito.when(dao.getActiveTokenById(tokenId)).thenReturn(activeRow);

        Assert.assertFalse(PlatformGatewayTokenUtil.matchesConnectConfigRegistrationToken(staleApiKey, staleApiKey));
        Assert.assertTrue(PlatformGatewayTokenUtil.matchesConnectConfigRegistrationToken(
                tokenId + "." + newPlain, tokenId + "." + newPlain));
    }

    @Test
    public void testMatchesConnectConfigRegistrationTokenRejectsMismatchedConfig() throws Exception {
        PowerMockito.mockStatic(PlatformGatewayDAO.class);
        PlatformGatewayDAO dao = Mockito.mock(PlatformGatewayDAO.class);
        PowerMockito.when(PlatformGatewayDAO.getInstance()).thenReturn(dao);
        Mockito.when(dao.getActiveTokenById("token-id")).thenReturn(null);

        Assert.assertFalse(PlatformGatewayTokenUtil.matchesConnectConfigRegistrationToken(
                "token-id.plain-a", "token-id.plain-b"));
    }
}
