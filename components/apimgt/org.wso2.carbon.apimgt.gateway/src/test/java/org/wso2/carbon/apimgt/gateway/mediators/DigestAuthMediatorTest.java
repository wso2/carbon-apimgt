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
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.TestUtils;

import static org.junit.Assert.*;

public class DigestAuthMediatorTest extends TestCase {

    DigestAuthMediator mediator;

    public void testSplitDigestHeader() throws Exception {
        String[] wwwHeaderSplits = { "",
                "realm=\"Vcreate\", qop=\"auth\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\"" };
        mediator = new DigestAuthMediator();
        String[] afterSplit = mediator.splitDigestHeader(wwwHeaderSplits);
        String[] expectedArray = { "VTWRealm", "MTQ0MzY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==",
                "auth", null, null };
        assertArrayEquals(expectedArray, afterSplit);
    }

    public void testSplitDigestHeaderWithQopMultiple() throws Exception {
        String[] wwwHeaderSplits = { "",
                "realm=\"Vcreate\", qop=\"auth,auth-int\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\"" };
        mediator = new DigestAuthMediator();
        String[] afterSplit = mediator.splitDigestHeader(wwwHeaderSplits);
        String[] expectedArray = { "VTWRealm", "MTQ0MzY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==",
                "auth", null, null };
        assertArrayEquals(expectedArray, afterSplit);
    }

    public void testSplitDigestHeaderWithAlgorithmMultiple() throws Exception {
        String[] wwwHeaderSplits = { "",
                "realm=\"Vcreate\", qop=\"auth\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\" algorithm=\"MD5,MD5-sess\"" };
        mediator = new DigestAuthMediator();
        String[] afterSplit = mediator.splitDigestHeader(wwwHeaderSplits);
        String[] expectedArray = { "VTWRealm", "MTQ0MzY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==",
                "auth", null, "MD5" };
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
        String algorithm = null;
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        mediator = new DigestAuthMediator();
        String ha1 = mediator.calculateHA1(username, realm, password, algorithm, serverNonce, clientNonce);
        String expectedHa1 = "7eb542ec2f370e063dceca936023bb88";
        assertEquals(expectedHa1, ha1);
    }

    public void testFindEntityBodyHash() throws Exception {

        //Setting the MessageBody property in the current message context
        MessageContext messageContext = TestUtils.getMessageContext("/digestAuth", "1.0.0");
        messageContext.setProperty("MessageBody", null);
        mediator = new DigestAuthMediator();
        String actulaHash = mediator.findEntityBodyHash(messageContext);
        String expectedEntityBodyHash = "d41d8cd98f00b204e9800998ecf8427e";
        assertEquals(expectedEntityBodyHash, actulaHash);
    }

    public void testCalculateHA2WhenQopIsAuthInt() throws Exception {

        MessageContext messageContext = TestUtils.getMessageContext("/digestAuth", "1.0.0");
        messageContext.setProperty("MessageBody", null);
        String qop = "auth-int";
        String httpMethod = "GET";
        String postFix = "/S374453680109605K/";
        mediator = new DigestAuthMediator();
        String actulaHash2 = mediator.calculateHA2(qop, httpMethod, postFix, messageContext);
        String expectedHash2 = "d41d8cd98f00b204e9800998ecf8427e";
        assertEquals(expectedHash2, actulaHash2);

    }

    public void testCalculateHA2WhenQopIsNotAuthInt() throws Exception {

        MessageContext messageContext = TestUtils.getMessageContext("/digestAuth", "1.0.0");
        messageContext.setProperty("MessageBody", null);
        String qop = "auth";
        String httpMethod = "GET";
        String postFix = "/S374453680109605K/";
        mediator = new DigestAuthMediator();
        String actulaHash2 = mediator.calculateHA2(qop, httpMethod, postFix, messageContext);
        String expectedHash2 = "d41d8cd98f00b204e9800998ecf8427e";
        assertEquals(expectedHash2, actulaHash2);

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
        String qop = null;
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
        String qop = null;
        String opaque = "5ccc069c403ebaf9f0171e9517f40e41"; //Assume opaque is not null here
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        String postFix = "/S374453680109605K/";
        mediator = new DigestAuthMediator();
        StringBuilder header = mediator
                .constructAuthHeader(username, realm, serverNonce, postFix, serverResponseArray, qop, opaque,
                        clientNonce);
        String AuthHeader = header.toString();
        String expectedHeader = "Digest username=\"GarryL\", realm=\"Vcreate\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\", uri=\"/S374453680109605K/\", response=\"c42047191b9d53a208cd615b23797b15\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        assertEquals(expectedHeader, AuthHeader);
    }

    public void testConstructAuthHeaderWhenQopNotNull() throws Exception {
        String[] serverResponseArray = { "c42047191b9d53a208cd615b23797b15", "00000001" };
        String username = "GarryL";
        String realm = "Vcreate";
        String qop = "auth";
        String opaque = null; //Assume opaque is null here
        String serverNonce = "PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==";
        String clientNonce = "19b428e5";
        String postFix = "/S374453680109605K/";
        mediator = new DigestAuthMediator();
        StringBuilder header = mediator
                .constructAuthHeader(username, realm, serverNonce, postFix, serverResponseArray, qop, opaque,
                        clientNonce);
        String AuthHeader = header.toString();
        String expectedHeader = "Digest username=\"GarryL\", realm=\"Vcreate\", nonce=\"PwQ0MxY3OPW3MDI3NTo1NmU2M09hNzJmDsI1NWFlZik5ZWRwMjdjYWViZjcxZQ==\", uri=\"/S374453680109605K/\", qop=auth, nc=00000001, cnonce=\"19b428e5\", response=\"c42047191b9d53a208cd615b23797b15\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        assertEquals(expectedHeader, AuthHeader);
    }

    /*public void testMediate() throws Exception {

    }*/

}