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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.APIConstants.DigestAuthConstants;

import java.net.URI;
import java.security.SecureRandom;
import java.util.*;

/**
 * This mediator would set the authorization header of the request that is to be sent to the endpoint,
 * making use of the 401 Unauthorized response received from the first request.
 */

public class DigestAuthMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(DigestAuthMediator.class);

    /**
     * This method is used to split the WWW-Authenticate header sent from the backend
     *
     * @param wwwHeaderSplits The array containing 2 Strings, where the first is an empty String always and the next is
     *                        the String containing the digest challenge sent in the WWW-Authenticate header
     * @return A String array containing all the required parameters for the Authorization header processing
     */
    public String[] splitDigestHeader(String[] wwwHeaderSplits) {

        String realm = null;
        String qop = null;
        String serverNonce = null;
        String opaque = null;
        String algorithm = null;

        String wwwHeaderValueString = wwwHeaderSplits[1];

        //Should split from ", " instead of "," because this might fail when qop or algorithm has multiple values.
        String[] wwwHeaderValueArray = wwwHeaderValueString.split(", ");

        for (String keyval : wwwHeaderValueArray) {

            String key = keyval.substring(0, keyval.indexOf('='));
            String value = keyval.substring(keyval.indexOf('=') + 1);

            if (DigestAuthConstants.REALM.equals(key.trim())) {
                realm = value;
            } else if (DigestAuthConstants.QOP.equals(key.trim())) {
                qop = value;
            } else if (DigestAuthConstants.NONCE.equals(key.trim())) {
                serverNonce = value;
            } else if (DigestAuthConstants.OPAQUE.equals(key.trim())) {
                opaque = value;
            } else if (DigestAuthConstants.ALGORITHM.equals(key.trim())) {
                algorithm = value;
            }
        }

        //remove front and end double quotes.serverNonce and realm will not be null.
        serverNonce = serverNonce.substring(1, serverNonce.length() - 1);
        realm = realm.substring(1, realm.length() - 1);

        //qop can be null
        if (qop != null) {
            qop = qop.substring(1, qop.length() - 1);
        }
        //algorithm can be null
        if (algorithm != null) {
            algorithm = algorithm.substring(1, algorithm.length() - 1);
        }
        //No need to trim the opaque if present because it is not used in calculations

        //Selecting an option if multiple options are provided for qop and algorithm
        if (DigestAuthConstants.AUTH.equals(qop) || DigestAuthConstants.AUTH_INT.equals(qop) || qop == null) {
            //do nothing
        } else { //this is if qop = "auth,aut-int" or something other than "auth" or "auth-int", assume qop="auth"
            qop = DigestAuthConstants.AUTH;
        }

        if (DigestAuthConstants.MD5.equals(algorithm) || DigestAuthConstants.MD5_SESS.equals(algorithm) || algorithm == null) {
            //do nothing
        } else { //this is if algorithm = "MD5,MD5-sess", assume algorithm = "MD5"
            algorithm = DigestAuthConstants.MD5;
        }

        return new String[] { realm, serverNonce, qop, opaque, algorithm };
    }

    /**
     * This method is used to calculate hash 1 for digest authentication
     *
     * @param username    The endpoint username
     * @param realm       Sent in the WWW-Authenticate header
     * @param password    The endpoint password
     * @param algorithm   Sent in the WWW-Authenticate header. This can also be a chosen value if multiple algorithms are
     *                    sent in the WWW-Authenticate header
     * @param serverNonce Sent in the WWW-Authenticate header
     * @param clientNonce Randomly generated by the client. This is a 16 digit hexadecimal value
     * @return The hash 1 required for creating the final response hash
     */
    public String calculateHA1(String username, String realm, String password, String algorithm, String serverNonce,
            String clientNonce) {

        String ha1;

        if (DigestAuthConstants.MD5_SESS.equals(algorithm)) {

            StringBuilder tempHa1StringBuilder = new StringBuilder(username);
            tempHa1StringBuilder.append(':');
            tempHa1StringBuilder.append(realm);
            tempHa1StringBuilder.append(':');
            tempHa1StringBuilder.append(password);
            String tempHa1 = DigestUtils.md5Hex(tempHa1StringBuilder.toString());
            StringBuilder ha1StringBuilder = new StringBuilder(tempHa1);
            ha1StringBuilder.append(':');
            ha1StringBuilder.append(serverNonce);
            ha1StringBuilder.append(':');
            ha1StringBuilder.append(clientNonce);
            ha1 = DigestUtils.md5Hex(ha1StringBuilder.toString());

        } else {

            StringBuilder ha1StringBuilder = new StringBuilder(username);
            ha1StringBuilder.append(':');
            ha1StringBuilder.append(realm);
            ha1StringBuilder.append(':');
            ha1StringBuilder.append(password);
            ha1 = DigestUtils.md5Hex(ha1StringBuilder.toString());

        }
        return ha1;
    }

    /**
     * This method is used to hash the entityBody for qop = auth-int (for calculating hash2)
     *
     * @param messageContext The message context where the entity body is stored
     * @return The hash of the entity body
     */
    public String findEntityBodyHash(MessageContext messageContext) {

        String entityBody = (String) messageContext.getProperty(DigestAuthConstants.MESSAGE_BODY);

        //if the entity-body is null,take it as an empty string
        if (entityBody == null) {
            entityBody = "";
        }

        return DigestUtils.md5Hex(entityBody);
    }

    /**
     * This method is used to calculate hash 2 for digest authentication
     *
     * @param qop            Sent in the WWW-Authenticate header. This can also be a chosen value if multiple qops are
     *                       sent in the WWW-Authenticate header
     * @param httpMethod     The method of request, one of GET, POST, PUT, DELETE etc.
     * @param digestUri      The uri of the resource omitting the host name and the internet protocol
     * @param messageContext The message context to be passed into the findEntityBodyHash() method called inside this method
     * @return The hash 2 required for creating the final response hash
     */
    public String calculateHA2(String qop, String httpMethod, String digestUri, MessageContext messageContext) {

        String ha2;

        if (DigestAuthConstants.AUTH_INT.equals(qop)) {

            //Extracting the entity body for calculating hash2 for qop="auth-int"
            String entityBodyHash = findEntityBodyHash(messageContext);

            StringBuilder ha2StringBuilder = new StringBuilder(httpMethod);
            ha2StringBuilder.append(':');
            ha2StringBuilder.append(digestUri);
            ha2StringBuilder.append(':');
            ha2StringBuilder.append(entityBodyHash);
            ha2 = DigestUtils.md5Hex(ha2StringBuilder.toString());

        } else {

            StringBuilder ha2StringBuilder = new StringBuilder(httpMethod);
            ha2StringBuilder.append(':');
            ha2StringBuilder.append(digestUri);
            ha2 = DigestUtils.md5Hex(ha2StringBuilder.toString());
        }

        return ha2;
    }

    /**
     * This method is used to randomly generate the client nonce
     *
     * @return The randomly generated client nonce which is a 16 digit hexadecimal value
     */
    public String generateClientNonce() {

        SecureRandom secRandom = new SecureRandom();
        byte[] result = new byte[32];
        secRandom.nextBytes(result);
        return new String(Hex.encodeHex(result));
    }

    /**
     * This method is used to increment and return the nonce count with each request
     *
     * @param prevCount This is the previous nonce count stored in the message context
     * @return The incremented nonce count. This is an 8 digit hexadecimal value
     */
    public String incrementNonceCount(String prevCount) {
        int counter = Integer.parseInt(prevCount);
        return String.format("%08x", ++counter);
    }

    /**
     * This method is used to generate the response hash to be sent in the Authorization header
     *
     * @param ha1            The hash 1
     * @param ha2            The hash 2
     * @param serverNonce    Sent in the WWW-Authenticate header
     * @param qop            Sent in the WWW-Authenticate header. This can also be a chosen value if multiple qops are
     *                       sent in the WWW-Authenticate header
     * @param prevNonceCount The previous nonce count stored in the message context
     * @param clientNonce    Randomly generated by the client. This is a 16 digit hexadecimal value
     * @return An array containing the response hash and the nonce count if qop is not null. It will return an array
     * containing only the response hash if qop is null
     */
    public String[] generateResponseHash(String ha1, String ha2, String serverNonce, String qop, String prevNonceCount,
            String clientNonce) {

        String serverResponse;

        if (qop != null) {

            //Increment the nonceCount
            String nonceCount = incrementNonceCount(prevNonceCount);

            StringBuilder serverResponseStringBuilder = new StringBuilder(ha1);
            serverResponseStringBuilder.append(':');
            serverResponseStringBuilder.append(serverNonce);
            serverResponseStringBuilder.append(':');
            serverResponseStringBuilder.append(nonceCount);
            serverResponseStringBuilder.append(':');
            serverResponseStringBuilder.append(clientNonce);
            serverResponseStringBuilder.append(':');
            serverResponseStringBuilder.append(qop);
            serverResponseStringBuilder.append(':');
            serverResponseStringBuilder.append(ha2);

            serverResponse = DigestUtils.md5Hex(serverResponseStringBuilder.toString());
            return new String[] { serverResponse, nonceCount };

        } else {

            StringBuilder serverResponseStringBuilder = new StringBuilder(ha1);
            serverResponseStringBuilder.append(':');
            serverResponseStringBuilder.append(serverNonce);
            serverResponseStringBuilder.append(':');
            serverResponseStringBuilder.append(ha2);

            serverResponse = DigestUtils.md5Hex(serverResponseStringBuilder.toString());
            return new String[] { serverResponse };
        }
    }

    /**
     * This method is used to construct the authorization header to be sent to the backend for authentication
     *
     * @param userName            The endpoint userName
     * @param realm               Sent in the WWW-Authenticate header
     * @param serverNonce         Sent in the WWW-Authenticate header
     * @param digestUri           The uri of the resource omitting the host name and the internet protocol
     * @param serverResponseArray The array returned by the generateResponseHash() method
     * @param qop                 Sent in the WWW-Authenticate header. This can also be a chosen value if multiple qops are
     *                            sent in the WWW-Authenticate header
     * @param opaque              Sent in the WWW-Authenticate header
     * @param clientNonce         Randomly generated by the client. This is a 16 digit hexadecimal value
     * @param algorithm           Sent in the WWW-Authenticate header. This can also be a chosen value if multiple algorithms are
     *                            sent in the WWW-Authenticate header
     * @return The processed authorization header to be sent in the second request to the resource
     */
    public StringBuilder constructAuthHeader(String userName, String realm, String serverNonce, String digestUri,
            String[] serverResponseArray, String qop, String opaque, String clientNonce, String algorithm) {

        StringBuilder header = new StringBuilder("Digest ");
        header.append("username=\"").append(userName).append("\"").append( ", ");
        header.append("realm=\"" ).append(realm).append("\"").append(", ");
        header.append("nonce=\"").append(serverNonce).append("\"").append(", ");
        header.append("uri=\"").append(digestUri).append("\"").append(", ");

        if (qop != null) {

            String nonceCount = serverResponseArray[1];
            header.append("qop=").append(qop).append(", ");
            header.append("nc=").append(nonceCount).append(", ");
            header.append("cnonce=\"").append(clientNonce).append("\"").append(", ");

        }

        String serverResponse = serverResponseArray[0];

        if (algorithm != null) {
            header.append("algorithm=").append(algorithm).append(", ");
        }

        if (opaque != null) {
            header.append("response=\"").append(serverResponse).append("\"").append(", ");
            header.append("opaque=").append(opaque);
        } else {
            header.append("response=\"").append(serverResponse).append("\"");
        }
        return header;
    }

    /**
     * This method performs the overall mediation for digest authentication
     *
     * @param messageContext This message context will contain the context of the 401 response received from the
     *                       backend after the first request from the client. It also contains some properties set
     *                       from the synapse configuration of the api as well, such as POSTFIX, BACKEND_URL,
     *                       HTTP_METHOD etc.
     * @return A boolean value.True if successful and false if not.
     */
    public boolean mediate(MessageContext messageContext) {

        String realm;
        String serverNonce;
        String qop;
        String opaque;
        String algorithm;

        if (log.isDebugEnabled()) {
            log.debug("Digest authorization header creation mediator is activated...");
        }

        try {

            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                    getAxis2MessageContext();

            String postFix = (String) messageContext.getProperty(DigestAuthConstants.POSTFIX);
            String backendURL = (String) messageContext.getProperty(DigestAuthConstants.BACKEND_URL);
            URI backendUri = new URI(backendURL);
            String path = backendUri.getPath();

            if (path.endsWith("/") && postFix.startsWith("/")) {
                postFix = postFix.substring(1);
            }

            String digestUri = path + postFix;

            if (log.isDebugEnabled()) {
                log.debug("digest-uri value is : " + digestUri);
            }

            //Take the WWW-Authenticate header from the message context
            Map transportHeaders = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            String wwwHeader = (String) transportHeaders.get(HttpHeaders.WWW_AUTHENTICATE);

            if (log.isDebugEnabled()) {
                log.debug("WWW-Authentication header is :" + wwwHeader);
            }

            if (StringUtils.isEmpty(wwwHeader)) {
                String errorDesc = "Digest authentication is not supported by the backend";
                log.error(HttpHeaders.WWW_AUTHENTICATE + " header is not found. " + errorDesc);
                Utils.setFaultPayload(messageContext, getFaultPayload("Unauthenticated at backend level",
                        errorDesc));
                Utils.sendFault(messageContext, HttpStatus.SC_UNAUTHORIZED);
               return false;
            }

            //This step can throw a NullPointerException if a WWW-Authenticate header is not received.
            String[] wwwHeaderSplits = wwwHeader.split("Digest", 2);

            //Happens only if the WWW-Authenticate header supports digest authentication.
            if (wwwHeaderSplits.length > 1 && wwwHeaderSplits[1] != null) {

                //extracting required header information from the WWW-Authenticate header
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
                String userNamePassword = (String) messageContext.getProperty(DigestAuthConstants.UNAMEPASSWORD);

                byte[] valueDecoded = Base64.decodeBase64(userNamePassword.getBytes(DigestAuthConstants.CHARSET));
                String decodedString = new String(valueDecoded, DigestAuthConstants.CHARSET);
                String[] splittedArrayOfUserNamePassword = decodedString.split(":");

                String userName = splittedArrayOfUserNamePassword[0];
                String passWord = splittedArrayOfUserNamePassword[1];

                if (log.isDebugEnabled()) {
                    log.debug("Username : " + userName);
                    log.debug("Password : " + passWord);
                }

                //get the Http method (GET, POST, PUT or DELETE etc.)
                String httpMethod = (String) messageContext.getProperty(DigestAuthConstants.HTTP_METHOD);

                if (log.isDebugEnabled()) {
                    log.debug("HTTP method of request is : " + httpMethod);
                }

                //generate clientNonce
                String clientNonce = generateClientNonce();

                //calculate hash1
                String ha1 = calculateHA1(userName, realm, passWord, algorithm, serverNonce, clientNonce);

                if (log.isDebugEnabled()) {
                    log.debug("Value of hash 1 is : " + ha1);
                }

                //calculate hash2
                String ha2 = calculateHA2(qop, httpMethod, digestUri, messageContext);

                if (log.isDebugEnabled()) {
                    log.debug("Value of hash 2 is : " + ha2);
                }

                //getting the previous NonceCount
                String prevNonceCount = (String) messageContext.getProperty(DigestAuthConstants.NONCE_COUNT);

                if (prevNonceCount == null) {
                    messageContext.setProperty(DigestAuthConstants.NONCE_COUNT, DigestAuthConstants.INIT_NONCE_COUNT);
                    prevNonceCount = (String) messageContext.getProperty(DigestAuthConstants.NONCE_COUNT);
                }

                //generate the final hash (serverResponse)
                String[] serverResponseArray = generateResponseHash(ha1, ha2, serverNonce, qop, prevNonceCount,
                        clientNonce);

                //setting the NonceCount after incrementing
                messageContext.setProperty(DigestAuthConstants.NONCE_COUNT, serverResponseArray[1]);

                String serverResponse = serverResponseArray[0];

                if (log.isDebugEnabled()) {
                    log.debug("Value of server response  is : " + serverResponse);
                }

                //Construct the authorization header
                StringBuilder header = constructAuthHeader(userName, realm, serverNonce, digestUri, serverResponseArray,
                        qop, opaque, clientNonce, algorithm);

                if (log.isDebugEnabled()) {
                    log.debug("Processed Authorization header to be sent in the request is : " + header);
                }

                //set the AuthHeader field in the message context
                messageContext.setProperty(DigestAuthConstants.AUTH_HEADER, header.toString());

                return true;

            } else {
                //This is not digest auth protected api. let it go. Might be basic auth or NTLM protected.
                //Here we receive a www-authenticate header but it is not for Digest authentication.
                return true;
            }

        } catch (Exception e) {
            log.error("Exception has occurred while performing Digest Auth class mediation : " + e.getMessage(), e);
            return false;
        }

    }

    //Interface methods are being implemented here
    public void init(SynapseEnvironment synapseEnvironment) {
        //ignore
    }

    public void destroy() {
        // ignore
    }

    protected OMElement getFaultPayload(String errorMessage, String errorDesc) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac
                .createOMNamespace(APISecurityConstants.API_SECURITY_NS, APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);
        OMElement errorMessageElement = fac.createOMElement("message", ns);
        errorMessageElement.setText(errorMessage);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(errorDesc);
        payload.addChild(errorMessageElement);
        payload.addChild(errorDetail);
        return payload;
    }

}

