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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.*;

/**
 * This mediator would set username and password in to http client request.
 * Then we will send the http request and get the json payload, then we pass it to the api manager for mediation.
 */
public class DigestAuthMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(DigestAuthMediator.class);

    //method to split digest auth header sent from the backend
    public String[] splitDigestHeader(String[] wwwHeaderSplits) {

        String realm = null;
        String qop = null;
        String serverNonce = null;
        String opaque = null;
        String algorithm = null;

        String wwwHeaderValueString = wwwHeaderSplits[1];
        String wwwHeaderValueArray[] = wwwHeaderValueString.split(",");

        for (String keyval : wwwHeaderValueArray) {

            String key = keyval.substring(0, keyval.indexOf("="));
            String value = keyval.substring(keyval.indexOf("=") + 1);

            if ("realm".equals(key.trim())) {
                realm = value;
            } else if ("qop".equals(key.trim())) {
                qop = value;
            } else if ("nonce".equals(key.trim())) {
                serverNonce = value;
            } else if ("opaque".equals(key.trim())) {
                opaque = value;
            } else if ("algorithm".equals(key.trim())) {
                algorithm = value;
            }
        }

        //remove front and end double quotes.
        serverNonce = serverNonce.substring(1, serverNonce.length() - 1);
        realm = realm.substring(1, realm.length() - 1);
        qop = qop.substring(1, qop.length() - 1);

        String[] headerAttributes = { realm, serverNonce, qop, opaque, algorithm };
        return headerAttributes;
    }

    //method to calculate hash1 for digest authentication
    public String calculateHA1(String username, String realm, String password, String algorithm, String serverNonce,
            String clientNonce) {

        String ha1;

        if ("MD5-sess".equals(algorithm)) {

            StringBuilder tempHa1StringBuilder = new StringBuilder(username);
            tempHa1StringBuilder.append(":");
            tempHa1StringBuilder.append(realm);
            tempHa1StringBuilder.append(":");
            tempHa1StringBuilder.append(password);
            String tempHa1 = DigestUtils.md5Hex(tempHa1StringBuilder.toString());
            StringBuilder ha1StringBuilder = new StringBuilder(tempHa1);
            ha1StringBuilder.append(":");
            ha1StringBuilder.append(serverNonce);
            ha1StringBuilder.append(":");
            ha1StringBuilder.append(clientNonce);
            ha1 = DigestUtils.md5Hex(ha1StringBuilder.toString());

        } else {
            StringBuilder ha1StringBuilder = new StringBuilder(username);
            ha1StringBuilder.append(":");
            ha1StringBuilder.append(realm);
            ha1StringBuilder.append(":");
            ha1StringBuilder.append(password);
            ha1 = DigestUtils.md5Hex(ha1StringBuilder.toString());
        }
        return ha1;
    }

    //hashing the entityBody for qop = auth-int (for calculating ha2)
    public String findEntityBodyHash(
            org.apache.axis2.context.MessageContext axis2MC) { //Take MessageContext messageContext //an auth-int is normally returned from a POST request

        //Get the hash of the entity-body
        String entityBody = axis2MC.getEnvelope().getText(); //Any transfer encoding applied might have to be reversed
        // - deferred, will need to check the Transfer-encoding header from the message context. This header value can be taken from
        //passing the transportHeaders map and extract transportHeaders.get("Transfer-encoding"); If null, transfer encoding is not applied.
        //Therefore no need to reverse. Enrich might handle this so I don't need to see it here. Check it.
        String hash = DigestUtils.md5Hex(entityBody);
        return hash;
    }

    //method to calculate hash 2 for digest authentication
    public String calculateHA2(String qop, String httpMethod, String postFix,
            org.apache.axis2.context.MessageContext axis2MC) {

        String ha2;

        //Consider all qop types other than "auth-int" as auth and compute ha2 here
        if ("auth-int".equals(qop)) {

            //Extracting the entity body for calculating ha2 for qop="auth-int"
            String entityBodyHash = findEntityBodyHash(axis2MC);

            StringBuilder ha2StringBuilder = new StringBuilder(httpMethod);
            ha2StringBuilder.append(":");
            ha2StringBuilder.append(postFix);
            ha2StringBuilder.append(":");
            ha2StringBuilder.append(entityBodyHash);
            ha2 = DigestUtils.md5Hex(ha2StringBuilder.toString());

        } else {

            StringBuilder ha2StringBuilder = new StringBuilder(httpMethod);
            ha2StringBuilder.append(":");
            ha2StringBuilder.append(postFix);
            ha2 = DigestUtils.md5Hex(ha2StringBuilder.toString());
        }

        return ha2;
    }

    //method to randomly generate the client nonce
    public String generateClientNonce() {

        Random rand = new Random();
        int num = rand.nextInt();
        String clientNonce = String.format("%08x", num);

        return clientNonce;
    }

    //method to increment and return the nonce count with each request
    public String incrementNonceCount(String prevCount) {
        int counter = Integer.parseInt(prevCount);
        String nonceCount = String.format("%08x", ++counter);
        return nonceCount;
    }

    //generate response hash
    public String[] generateResponseHash(String ha1, String ha2, String serverNonce, String qop, String prevNonceCount,
            String clientNonce) {

        String serverResponse;

        if (qop != null) {

            //Increment the nonceCount
            String nonceCount = incrementNonceCount(prevNonceCount);

            StringBuilder serverResponseStringBuilder = new StringBuilder(ha1);
            serverResponseStringBuilder.append(":");
            serverResponseStringBuilder.append(serverNonce);
            serverResponseStringBuilder.append(":");
            serverResponseStringBuilder.append(nonceCount);
            serverResponseStringBuilder.append(":");
            serverResponseStringBuilder.append(clientNonce);
            serverResponseStringBuilder.append(":");
            serverResponseStringBuilder.append(qop);
            serverResponseStringBuilder.append(":");
            serverResponseStringBuilder.append(ha2);

            serverResponse = DigestUtils.md5Hex(serverResponseStringBuilder.toString());
            String[] responseParams = { serverResponse, nonceCount };
            return responseParams;
        } else {

            StringBuilder serverResponseStringBuilder = new StringBuilder(ha1);
            serverResponseStringBuilder.append(":");
            serverResponseStringBuilder.append(serverNonce);
            serverResponseStringBuilder.append(":");
            serverResponseStringBuilder.append(ha2);

            serverResponse = DigestUtils.md5Hex(serverResponseStringBuilder.toString());
            String[] responseParams = { serverResponse };
            return responseParams;

        }
    }

    //method to construct the authorization header
    public StringBuilder constructAuthHeader(String userName, String realm, String serverNonce, String postFix,
            String[] serverResponseArray, String qop, String opaque, String clientNonce) {

        StringBuilder header = new StringBuilder("Digest ");
        header.append("username=\"" + userName + "\"" + ", ");
        header.append("realm=\"" + realm + "\"" + ", ");
        header.append("nonce=\"" + serverNonce + "\"" + ", ");
        header.append("uri=\"" + postFix + "\"" + ", ");

        if (qop != null) {

            String nonceCount = serverResponseArray[1];
            header.append("qop=" + qop + ", ");
            header.append("nc=" + nonceCount + ", ");
            header.append("cnonce=\"" + clientNonce + "\"" + ", ");

        }

        String serverResponse = serverResponseArray[0];

        if (opaque != null) {
            header.append("response=\"" + serverResponse + "\"" + ", ");
            header.append("opaque=" + opaque);
        } else {
            header.append("response=\"" + serverResponse + "\"");
        }
        return header;
    }

    //method performing overall mediation for digest authentication
    public boolean mediate(MessageContext messageContext) {

        String realm;
        String serverNonce;
        String qop;
        String opaque;
        String algorithm;

        if (log.isDebugEnabled()) {
            log.debug("Digest auth header creation mediator is activated..");
        }

        try {

            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                    getAxis2MessageContext();

            String postFix = (String) messageContext.getProperty("POSTFIX");

            //appending forward slash.
            StringBuilder postFixStringBuilder = new StringBuilder(postFix);
            postFixStringBuilder.append("/");
            postFix = postFixStringBuilder.toString();

            if (log.isDebugEnabled()) {
                log.debug("Post Fix value is : " + postFix);
            }

            //Take the WWW-Authenticate header from the message context
            Map transportHeaders = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            String wwwHeader = (String) transportHeaders.get(HttpHeaders.WWW_AUTHENTICATE);

            if (log.isDebugEnabled()) {
                log.debug("WWW-Auth header response ..." + wwwHeader);
            }
            //This step can throw a NullPointerException if a WWW-Authenticate header is not received
            String wwwHeaderSplits[] = wwwHeader.split("Digest");

            //Happens only if the WWW-Authenticate header supports digest authentication.
            if (wwwHeaderSplits.length > 1 && wwwHeaderSplits[1] != null) {

                //extracting required header information
                String[] headerAttributes = splitDigestHeader(wwwHeaderSplits);
                realm = headerAttributes[0];
                serverNonce = headerAttributes[1];
                qop = headerAttributes[2];
                opaque = headerAttributes[3];
                algorithm = headerAttributes[4];

                if (log.isDebugEnabled()) {
                    log.debug("Server nonce value : " + serverNonce);
                    log.debug("realm : " + realm);
                }

                //get username password given by the client
                String userNamePassword = (String) messageContext.getProperty("UNAMEPASSWORD");

                byte[] valueDecoded = Base64.decodeBase64(userNamePassword.getBytes());
                String decodedString = new String(valueDecoded);
                String[] splittedArrayOfUserNamePassword = decodedString.split(":");

                String userName = splittedArrayOfUserNamePassword[0];
                String passWord = splittedArrayOfUserNamePassword[1];

                if (log.isDebugEnabled()) {
                    log.debug("User name  : " + userName);
                    log.debug("Password : " + passWord);
                }

                //get the Http method (GET, POST, PUT or DELETE)
                String httpMethod = (String) messageContext.getProperty("HTTP_METHOD");

                if (log.isDebugEnabled()) {
                    log.debug("HTTP method of request is : " + httpMethod);
                }

                //generate clientNonce
                String clientNonce = generateClientNonce();

                //calculate ha1
                String ha1 = calculateHA1(userName, realm, passWord, algorithm, serverNonce, clientNonce);

                if (log.isDebugEnabled()) {
                    log.debug("MD5 value of ha1 is : " + ha1);
                }

                //calculate ha2
                String ha2 = calculateHA2(qop, httpMethod, postFix, axis2MC);

                if (log.isDebugEnabled()) {
                    log.debug("MD5 value of ha2 is : " + ha2);
                }

                //getting the previous NonceCount
                String prevNonceCount = (String) messageContext.getProperty("NonceCount");

                if (prevNonceCount == null) {
                    messageContext.setProperty("NonceCount", "00000000");
                    prevNonceCount = (String) messageContext.getProperty("NonceCount");
                }

                //generate the final hash (serverResponse)
                String[] serverResponseArray = generateResponseHash(ha1, ha2, serverNonce, qop, prevNonceCount,
                        clientNonce);

                //setting the NonceCount after incrementing
                messageContext.setProperty("NonceCount", serverResponseArray[1]);

                String serverResponse = serverResponseArray[0];

                if (log.isDebugEnabled()) {
                    log.debug("MD5 value of server response  is : " + serverResponse);
                }

                //Construct the authorization header
                StringBuilder header = constructAuthHeader(userName, realm, serverNonce, postFix, serverResponseArray,
                        qop, opaque, clientNonce);

                if (log.isDebugEnabled()) {
                    log.debug("Processed www-header to be sent is : " + header);
                }

                //set the wwwHeader field
                messageContext.setProperty("wwwHeader", header.toString());

                return true;
            } else {
                //This is not digest auth protected api. let it go. Might be basic auth or NTLM protected.
                //We receive a www-authenticate header but it is not for Digest auth.
                return true;
            }
        }catch(NullPointerException ex){
            log.error("The endpoint does not support digest authentication : " + ex.getMessage());
            return false;
        }catch (Exception e) {
            log.error("Exception has occurred while performing class mediation : " + e.getMessage());
            return false;
        }

    }

    public void init(SynapseEnvironment synapseEnvironment) {

    }

    public void destroy() {
        // ignore
    }

}


