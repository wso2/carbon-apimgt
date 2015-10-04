/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axis2.context.MessageContext;
import org.junit.Test;

import static org.junit.Assert.*;

public class DigestAuthMediatorTest {

    @Test public void testSplitDigestHeader() throws Exception {
        String[] wwwHeaderSplits = { "",
                "realm=\"Vcreate\", qop=\"auth\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\"" };
        DigestAuthMediator mediator = new DigestAuthMediator();
        String[] afterSplit = mediator.splitDigestHeader(wwwHeaderSplits);
        String[] expectedArray = { "VTWRealm", "MTQ0MzY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==",
                "auth", null, null };
        assertArrayEquals(expectedArray, afterSplit);
    }

    @Test public void testCalculateHA1AlgoMD5() throws Exception {
        String username = "GarryL";
        String realm = "Vcreate";
        String password = "garry@123";
        String algorithm = "MD5";
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        DigestAuthMediator mediator = new DigestAuthMediator();
        String ha1 = mediator.calculateHA1(username, realm, password, algorithm, serverNonce, clientNonce);
        String expectedHa1 = "7eb542ec2f370e063dceca936023bb88";
        assertEquals(expectedHa1, ha1);
    }

    @Test public void testCalculateHA1AlgoMD5sess() throws Exception {
        String username = "GarryL";
        String realm = "Vcreate";
        String password = "garry@123";
        String algorithm = "MD5-sess";
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        DigestAuthMediator mediator = new DigestAuthMediator();
        String ha1 = mediator.calculateHA1(username, realm, password, algorithm, serverNonce, clientNonce);
        String expectedHa1 = "2279f75cc09856c15f1fc636f440cc79";
        assertEquals(expectedHa1, ha1);
    }

    @Test public void testCalculateHA1AlgoNone() throws Exception {
        String username = "GarryL";
        String realm = "Vcreate";
        String password = "garry@123";
        String algorithm = null;
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        DigestAuthMediator mediator = new DigestAuthMediator();
        String ha1 = mediator.calculateHA1(username, realm, password, algorithm, serverNonce, clientNonce);
        String expectedHa1 = "7eb542ec2f370e063dceca936023bb88";
        assertEquals(expectedHa1, ha1);
    }

    @Test public void testFindEntityBodyHash() throws Exception {
        //org.apache.axis2.context.MessageContext axis2MC =
    }

    @Test public void testCalculateHA2WhenQopIsAuthInt() throws Exception {
        String qop = "auth-int";
        String httpMethod = "GET";
        String postFix = "/S374453680109605K/";
        org.apache.axis2.context.MessageContext axis2MC = new MessageContext();
    }

    @Test public void testCalculateHA2WhenQopIsNotAuthInt() throws Exception {
        String qop = "auth"; //qop is not required for the calculation
        String httpMethod = "POST";
        String postFix = "/S374453680109605K/";
        org.apache.axis2.context.MessageContext axis2MC = new MessageContext(); //axis2MC is also not required here
        DigestAuthMediator mediator = new DigestAuthMediator();
        String ha2 = mediator.calculateHA2(qop, httpMethod, postFix, axis2MC);
        String expectedHa2 = "4fa6e5d1bd6f3e75c1e50c122700ae72";
        assertEquals(expectedHa2, ha2);
    }

    @Test public void testIncrementNonceCount() throws Exception {
        String prevNonceCount = "00000001";
        DigestAuthMediator mediator = new DigestAuthMediator();
        String currNonceCount = mediator.incrementNonceCount(prevNonceCount);
        String expectedNonceCount = "00000002";
        assertEquals(expectedNonceCount, currNonceCount);
    }

    @Test public void testGenerateResponseHashWhenQopNotnull() throws Exception {
        String ha1 = "7eb542ec2f370e063dceca936023bb88";
        String ha2 = "23b5493f4f370e063dc34r936023wb65";
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String qop = "auth";
        String prevNonceCount = "00000001";
        String clientNonce = "19b428e5";
        DigestAuthMediator mediator = new DigestAuthMediator();
        String[] finalHashWithNonceCount = mediator
                .generateResponseHash(ha1, ha2, serverNonce, qop, prevNonceCount, clientNonce);
        String[] expectedHashWithNonceCount = { "fc7dc9cb3e971522e129464e8f3597ba", "00000002" };
        assertArrayEquals(expectedHashWithNonceCount, finalHashWithNonceCount);
    }

    @Test public void testGenerateResponseHashWhenQopNull() throws Exception {
        String ha1 = "7eb542ec2f370e063dceca936023bb88";
        String ha2 = "23b5493f4f370e063dc34r936023wb65";
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String qop = null;
        String prevNonceCount = "00000001"; //The prevNonceCount and clientNonce will be passed no matter whether qop is null
        String clientNonce = "19b428e5";
        DigestAuthMediator mediator = new DigestAuthMediator();
        String[] finalHash = mediator.generateResponseHash(ha1, ha2, serverNonce, qop, prevNonceCount, clientNonce);
        String[] expectedHash = { "85c1b5bc7de83b75d6a592d3c7a748bb" };
        assertArrayEquals(expectedHash, finalHash);
    }

    @Test public void testConstructAuthHeaderWhenQopNull() throws Exception {
        String[] serverResponseArray = { "c42047191b9d53a208cd615b23797b15" };
        String username = "GarryL";
        String realm = "Vcreate";
        String qop = null;
        String opaque = "5ccc069c403ebaf9f0171e9517f40e41"; //Assume opaque is not null here
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        String postFix = "/S374453680109605K/";
        DigestAuthMediator mediator = new DigestAuthMediator();
        StringBuilder header = mediator
                .constructAuthHeader(username, realm, serverNonce, postFix, serverResponseArray, qop, opaque,
                        clientNonce);
        String AuthHeader = header.toString();
        String expectedHeader = "Digest username=\"GarryL\", realm=\"Vcreate\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\", uri=\"/S374453680109605K/\", response=\"c42047191b9d53a208cd615b23797b15\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        assertEquals(expectedHeader, AuthHeader);
    }

    @Test public void testConstructAuthHeaderWhenQopNotNull() throws Exception {
        String[] serverResponseArray = { "c42047191b9d53a208cd615b23797b15", "00000001" };
        String username = "GarryL";
        String realm = "Vcreate";
        String qop = "auth";
        String opaque = null; //Assume opaque is null here
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        String postFix = "/S374453680109605K/";
        DigestAuthMediator mediator = new DigestAuthMediator();
        StringBuilder header = mediator
                .constructAuthHeader(username, realm, serverNonce, postFix, serverResponseArray, qop, opaque,
                        clientNonce);
        String AuthHeader = header.toString();
        String expectedHeader = "Digest username=\"GarryL\", realm=\"Vcreate\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\", uri=\"/S374453680109605K/\", qop=auth, nc=00000001, cnonce=\"19b428e5\", response=\"c42047191b9d53a208cd615b23797b15\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        assertEquals(expectedHeader, AuthHeader);
    }

    @Test public void testMediate() throws Exception {

    }

}