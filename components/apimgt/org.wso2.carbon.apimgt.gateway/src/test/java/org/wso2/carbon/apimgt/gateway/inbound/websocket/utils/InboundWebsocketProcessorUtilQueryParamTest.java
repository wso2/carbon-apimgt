/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.inbound.websocket.utils;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Tests removal of gateway credentials from the websocket handshake query string.
 */
public class InboundWebsocketProcessorUtilQueryParamTest {

    private String strip(String path) {
        return strip(path, APIConstants.AUTHORIZATION_QUERY_PARAM_DEFAULT);
    }

    private String strip(String path, String name) {
        InboundMessageContext ctx = new InboundMessageContext();
        ctx.setFullRequestPath(path);
        // null proves the parameter map is not read; reintroducing a use of it would fail here
        InboundWebsocketProcessorUtil.removeTokenFromQuery(null, ctx, name);
        return ctx.getFullRequestPath();
    }

    @Test
    public void testRemovesNamedParam() {
        Assert.assertEquals("/ws?authToken=X", strip("/ws?authToken=X&access_token=GW"));
    }

    @Test
    public void testRemovesFirstParam() {
        Assert.assertEquals("/ws?authToken=X", strip("/ws?access_token=GW&authToken=X"));
    }

    @Test
    public void testOnlyParamLeavesNoQuestionMark() {
        Assert.assertEquals("/ws", strip("/ws?access_token=GW"));
    }

    @Test
    public void testNoQueryStringUnchanged() {
        Assert.assertEquals("/ws", strip("/ws"));
    }

    @Test
    public void testParamNotPresentUnchanged() {
        Assert.assertEquals("/ws?a=1&b=2", strip("/ws?a=1&b=2"));
    }

    @Test
    public void testNullPathUnchanged() {
        Assert.assertNull(strip(null));
    }

    // The old implementation decoded the query and rebuilt it from a map, which destroyed
    // percent encoding and collapsed repeated parameters. Both must now survive.

    @Test
    public void testEncodingPreserved() {
        Assert.assertEquals("/ws?authToken=abc%2Bdef",
                strip("/ws?authToken=abc%2Bdef&access_token=GW"));
    }

    @Test
    public void testSpaceEncodingPreserved() {
        Assert.assertEquals("/ws?authToken=hello%20world",
                strip("/ws?authToken=hello%20world&access_token=GW"));
    }

    @Test
    public void testSlashAndEqualsEncodingPreserved() {
        Assert.assertEquals("/ws?authToken=a%2Fb%3Dc",
                strip("/ws?authToken=a%2Fb%3Dc&access_token=GW"));
    }

    @Test
    public void testRepeatedParamsPreserved() {
        Assert.assertEquals("/ws?tag=x&tag=y&tag=z",
                strip("/ws?tag=x&tag=y&tag=z&access_token=GW"));
    }

    @Test
    public void testValuelessParamPreserved() {
        Assert.assertEquals("/ws?debug", strip("/ws?debug&access_token=GW"));
    }

    @Test
    public void testOrderPreserved() {
        Assert.assertEquals("/ws?a=1&b=2&c=3", strip("/ws?a=1&b=2&access_token=GW&c=3"));
    }

    @Test
    public void testMatchIsExactNotPrefix() {
        Assert.assertEquals("/ws?access_token_hint=X",
                strip("/ws?access_token_hint=X&access_token=GW"));
    }

    // QueryStringDecoder, which located the credential, decodes names and treats ';' as a separator.
    // Removal must match that view or an encoded or ';' separated credential survives in the query.

    @Test
    public void testEncodedCredentialNameRemoved() {
        Assert.assertEquals("/ws?authToken=X", strip("/ws?%61ccess_token=GW&authToken=X"));
    }

    @Test
    public void testUnderscoreEncodedCredentialNameRemoved() {
        Assert.assertEquals("/ws?authToken=X", strip("/ws?access%5Ftoken=GW&authToken=X"));
    }

    @Test
    public void testSemicolonSeparatedCredentialRemoved() {
        Assert.assertEquals("/ws?authToken=X", strip("/ws?access_token=GW;authToken=X"));
    }

    @Test
    public void testSemicolonSeparatedCredentialLastRemoved() {
        Assert.assertEquals("/ws?authToken=X", strip("/ws?authToken=X;access_token=GW"));
    }

    @Test
    public void testEncodedNonCredentialNameKeptVerbatim() {
        Assert.assertEquals("/ws?%61uthToken=X", strip("/ws?%61uthToken=X&access_token=GW"));
    }
}
