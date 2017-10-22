/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.mediators;

import junit.framework.TestCase;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

public class DigestAuthMediatorTest extends TestCase {

    DigestAuthMediator mediator;

    public void testSplitDigestHeader() throws Exception {
        String[] wwwHeaderSplits = { "",
                "realm=\"Vcreate\", qop=\"auth\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\"" };
        mediator = new DigestAuthMediator();
        String[] afterSplit = mediator.splitDigestHeader(wwwHeaderSplits);
        String[] expectedArray = { "Vcreate", "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==",
                "auth", null, null };
        assertArrayEquals(expectedArray, afterSplit);
    }

    public void testSplitDigestHeaderQopNull() throws Exception {
        String[] wwwHeaderSplits = { "",
                "realm=\"Vcreate\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\"" };
        mediator = new DigestAuthMediator();
        String[] afterSplit = mediator.splitDigestHeader(wwwHeaderSplits);
        String[] expectedArray = { "Vcreate", "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==", null,
                null, null };
        assertArrayEquals(expectedArray, afterSplit);
    }

    public void testSplitDigestHeaderWithQopMultiple() throws Exception {
        String[] wwwHeaderSplits = { "",
                "realm=\"Vcreate\", qop=\"auth,auth-int\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\"" };
        mediator = new DigestAuthMediator();
        String[] afterSplit = mediator.splitDigestHeader(wwwHeaderSplits);
        String[] expectedArray = { "Vcreate", "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==",
                "auth", null, null };
        assertArrayEquals(expectedArray, afterSplit);
    }

    public void testSplitDigestHeaderWithAlgorithmMultiple() throws Exception {
        String[] wwwHeaderSplits = { "",
                "realm=\"Vcreate\", qop=\"auth-int\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\", algorithm=\"MD5,MD5-sess\"" };
        mediator = new DigestAuthMediator();
        String[] afterSplit = mediator.splitDigestHeader(wwwHeaderSplits);
        String[] expectedArray = { "Vcreate", "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==",
                "auth-int", null, "MD5" };
        assertArrayEquals(expectedArray, afterSplit);
    }

    public void testCalculateHA1AlgoMD5() throws Exception {
        String username = "GarryL";
        String realm = "Vcreate";
        String password = "garry@123";
        String algorithm = "MD5";
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        mediator = new DigestAuthMediator();
        String ha1 = mediator.calculateHA1(username, realm, password, algorithm, serverNonce, clientNonce);
        String expectedHa1 = "7eb542ec2f370e063dceca936023bb88";
        assertEquals(expectedHa1, ha1);
    }

    public void testCalculateHA1AlgoMD5sess() throws Exception {
        String username = "GarryL";
        String realm = "Vcreate";
        String password = "garry@123";
        String algorithm = "MD5-sess";
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        mediator = new DigestAuthMediator();
        String ha1 = mediator.calculateHA1(username, realm, password, algorithm, serverNonce, clientNonce);
        String expectedHa1 = "2279f75cc09856c15f1fc636f440cc79";
        assertEquals(expectedHa1, ha1);
    }

    public void testCalculateHA1AlgoNone() throws Exception {
        String username = "GarryL";
        String realm = "Vcreate";
        String password = "garry@123";
        String algorithm = null; //algorithm is taken as not specified here
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        mediator = new DigestAuthMediator();
        String ha1 = mediator.calculateHA1(username, realm, password, algorithm, serverNonce, clientNonce);
        String expectedHa1 = "7eb542ec2f370e063dceca936023bb88";
        assertEquals(expectedHa1, ha1);
    }

    public void testIncrementNonceCount() throws Exception {
        String prevNonceCount = "00000001";
        mediator = new DigestAuthMediator();
        String currNonceCount = mediator.incrementNonceCount(prevNonceCount);
        String expectedNonceCount = "00000002";
        assertEquals(expectedNonceCount, currNonceCount);
    }

    public void testGenerateResponseHashWhenQopNotnull() throws Exception {
        String ha1 = "7eb542ec2f370e063dceca936023bb88";
        String ha2 = "23b5493f4f370e063dc34r936023wb65";
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String qop = "auth";
        String prevNonceCount = "00000001";
        String clientNonce = "19b428e5";
        mediator = new DigestAuthMediator();
        String[] finalHashWithNonceCount = mediator
                .generateResponseHash(ha1, ha2, serverNonce, qop, prevNonceCount, clientNonce);
        String[] expectedHashWithNonceCount = { "fc7dc9cb3e971522e129464e8f3597ba", "00000002" };
        assertArrayEquals(expectedHashWithNonceCount, finalHashWithNonceCount);
    }

    public void testGenerateResponseHashWhenQopNull() throws Exception {
        String ha1 = "7eb542ec2f370e063dceca936023bb88";
        String ha2 = "23b5493f4f370e063dc34r936023wb65";
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String qop = null; //qop is taken to be null here
        String prevNonceCount = "00000001"; //The prevNonceCount and clientNonce will be passed no matter whether qop is null
        String clientNonce = "19b428e5";
        mediator = new DigestAuthMediator();
        String[] finalHash = mediator.generateResponseHash(ha1, ha2, serverNonce, qop, prevNonceCount, clientNonce);
        String[] expectedHash = { "85c1b5bc7de83b75d6a592d3c7a748bb" };
        assertArrayEquals(expectedHash, finalHash);
    }

    public void testConstructAuthHeaderWhenQopNull() throws Exception {
        String[] serverResponseArray = { "c42047191b9d53a208cd615b23797b15" };
        String username = "GarryL";
        String realm = "Vcreate";
        String qop = null; //qop is taken to be null here
        String opaque = "\"5ccc069c403ebaf9f0171e9517f40e41\""; //Opaque is not null here
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        String digestUri = "/service/path/S374453680109605K";
        String algorithm = "MD5";
        mediator = new DigestAuthMediator();
        StringBuilder header = mediator
                .constructAuthHeader(username, realm, serverNonce, digestUri, serverResponseArray, qop, opaque,
                        clientNonce, algorithm);
        String AuthHeader = header.toString();
        String expectedHeader = "Digest username=\"GarryL\", realm=\"Vcreate\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\", uri=\"/service/path/S374453680109605K\", algorithm=MD5, response=\"c42047191b9d53a208cd615b23797b15\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        assertEquals(expectedHeader, AuthHeader);
    }

    public void testConstructAuthHeaderWhenQopNotNull() throws Exception {
        String[] serverResponseArray = { "c42047191b9d53a208cd615b23797b15", "00000001" };
        String username = "GarryL";
        String realm = "Vcreate";
        String qop = "auth";
        String opaque = null; //Opaque is taken to be null here
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        String digestUri = "/service/path/S374453680109605K";
        String algorithm = "MD5"; //Algorithm is not null here
        mediator = new DigestAuthMediator();
        StringBuilder header = mediator
                .constructAuthHeader(username, realm, serverNonce, digestUri, serverResponseArray, qop, opaque,
                        clientNonce, algorithm);
        String AuthHeader = header.toString();
        String expectedHeader = "Digest username=\"GarryL\", realm=\"Vcreate\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\", uri=\"/service/path/S374453680109605K\", qop=auth, nc=00000001, cnonce=\"19b428e5\", algorithm=MD5, response=\"c42047191b9d53a208cd615b23797b15\"";
        assertEquals(expectedHeader, AuthHeader);
    }

    public void testConstructAuthHeaderWhenAlgoNull() throws Exception {
        String[] serverResponseArray = { "c42047191b9d53a208cd615b23797b15", "00000001" };
        String username = "GarryL";
        String realm = "Vcreate";
        String qop = "auth";
        String opaque = "\"5ccc069c403ebaf9f0171e9517f40e41\"";
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        String digestUri = "/service/path/S374453680109605K";
        String algorithm = null; //Algorithm is null
        mediator = new DigestAuthMediator();
        StringBuilder header = mediator
                .constructAuthHeader(username, realm, serverNonce, digestUri, serverResponseArray, qop, opaque,
                        clientNonce, algorithm);
        String AuthHeader = header.toString();
        String expectedHeader = "Digest username=\"GarryL\", realm=\"Vcreate\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\", uri=\"/service/path/S374453680109605K\", qop=auth, nc=00000001, cnonce=\"19b428e5\", response=\"c42047191b9d53a208cd615b23797b15\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        assertEquals(expectedHeader, AuthHeader);
    }

    @Test
    public void testMediate() {
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Map transportHeaders = new HashMap();
        String expectedHeader = "Digest username=\"GarryL\", realm=\"Vcreate\", " +
                "nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\", " +
                "uri=\"/service/path/S374453680109605K\", qop=auth, nc=00000001, cnonce=\"19b428e5\", " +
                "response=\"c42047191b9d53a208cd615b23797b15\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        transportHeaders.put(HttpHeaders.WWW_AUTHENTICATE, expectedHeader);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn
                (transportHeaders);
        Mockito.when(messageContext.getProperty(APIConstants.DigestAuthConstants.POSTFIX)).thenReturn("/abc");
        Mockito.when(messageContext.getProperty(APIConstants.DigestAuthConstants.UNAMEPASSWORD)).thenReturn
                ("YWRtaW46YWRtaW4x");
        Mockito.when(messageContext.getProperty(APIConstants.DigestAuthConstants.HTTP_METHOD)).thenReturn("GET");

        Mockito.when(messageContext.getProperty(APIConstants.DigestAuthConstants.BACKEND_URL)).thenReturn
                ("https://localhost/cde");
        Mockito.when(messageContext.getProperty(APIConstants.DigestAuthConstants.NONCE_COUNT)).thenReturn
                (APIConstants.DigestAuthConstants.INIT_NONCE_COUNT);
        DigestAuthMediator digestAuthMediator = new DigestAuthMediator();
        digestAuthMediator.mediate(messageContext);
    }
}