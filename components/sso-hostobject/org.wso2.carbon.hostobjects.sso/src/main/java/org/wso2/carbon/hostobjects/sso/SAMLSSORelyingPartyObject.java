/*
* Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.hostobjects.sso;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.web.SessionHostObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.XMLObject;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.hostobjects.sso.internal.SSOConstants;
import org.wso2.carbon.hostobjects.sso.internal.SSOHostObjectDataHolder;
import org.wso2.carbon.hostobjects.sso.internal.SessionInfo;
import org.wso2.carbon.hostobjects.sso.internal.builder.AuthReqBuilder;
import org.wso2.carbon.hostobjects.sso.internal.builder.LogoutRequestBuilder;
import org.wso2.carbon.hostobjects.sso.internal.util.Util;

import javax.script.ScriptException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class wrap up the operations needed to write a saml relying party for sso use case.
 */
public class SAMLSSORelyingPartyObject extends ScriptableObject {
    private static final Log log = LogFactory.getLog(SAMLSSORelyingPartyObject.class);
    //stores sso properties like, identity server url,keystore path, alias, keystore password, issuerId
    private Properties ssoConfigProperties = new Properties();
    protected static SAMLSSORelyingPartyObject ssho;

    // relay state,requested uri
    private static Map<String, String> relayStateMap = new HashMap<String, String>();

    // issuerId, relyingPartyObject .this is to provide sso functionality to multiple jaggery apps.
    private static Map<String, SAMLSSORelyingPartyObject> ssoRelyingPartyMap = new HashMap<String, SAMLSSORelyingPartyObject>();

    // sessionId, sessionIndex. this is to map current session with session index sent from Identity server.
    // When log out request come from identity server,we need to invalidate the current session.
    private static Map<String, SessionInfo> sessionIdMap = new ConcurrentHashMap<String, SessionInfo>();
    private static Map<String, Set<SessionHostObject>> sessionIndexMap = new ConcurrentHashMap<String, Set<SessionHostObject>>();
    private static Set<SessionHostObject> sho = new HashSet<SessionHostObject>();
    //used store logged in user name until put into jaggery session
    private String loggedInUserName;


    @Override
    public String getClassName() {
        return "SSORelyingParty";
    }

    public static void jsFunction_addSession(Context cx, Scriptable thisObj, Object[] args,
                                             Function funObj) throws Exception {

        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof SessionHostObject)) {
            throw new ScriptException("Invalid argument. Session is missing.");
        }
        SessionHostObject sho = (SessionHostObject) args[0];
        SAMLSSORelyingPartyObject.sho.add(sho);
        SAMLSSORelyingPartyObject.ssho = (SAMLSSORelyingPartyObject) thisObj;
    }

    /**
     * @param cx
     * @param args      - args[0]-issuerId, this issuer need to be registered in Identity server.
     * @param ctorObj
     * @param inNewExpr
     * @return
     * @throws ScriptException
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid arguments!, IssuerId is missing in parameters.");
        }

        SAMLSSORelyingPartyObject relyingPartyObject = ssoRelyingPartyMap.get((String) args[0]);
        if (relyingPartyObject == null) {
            relyingPartyObject = new SAMLSSORelyingPartyObject();
            relyingPartyObject.setSSOProperty(SSOConstants.ISSUER_ID, (String) args[0]);
            ssoRelyingPartyMap.put((String) args[0], relyingPartyObject);
        }
        return relyingPartyObject;
    }

    /**
     * @param cx
     * @param thisObj
     * @param args    -args[0]- SAML response xml
     * @param funObj
     * @return
     * @throws Exception
     */
    public static boolean jsFunction_validateSignature(Context cx, Scriptable thisObj,
                                                       Object[] args,
                                                       Function funObj)
            throws Exception {

        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. SAML response is missing.");
        }

        String decodedString = Util.decode((String) args[0]);

        XMLObject samlObject = Util.unmarshall(decodedString);
        String tenantDomain = Util.getDomainName(samlObject);

        int tenantId = Util.getRealmService().getTenantManager().getTenantId(tenantDomain);

        if (samlObject instanceof Response) {
            Response samlResponse = (Response) samlObject;
            SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;

            //Try and validate the signature using the super tenant key store.
            boolean sigValid = Util.validateSignature(samlResponse,
                    relyingPartyObject.getSSOProperty(SSOConstants.KEY_STORE_NAME),
                    relyingPartyObject.getSSOProperty(SSOConstants.KEY_STORE_PASSWORD),
                    relyingPartyObject.getSSOProperty(SSOConstants.IDP_ALIAS),
                    MultitenantConstants.SUPER_TENANT_ID, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            //If not success, try and validate the signature using tenant key store.
            if(!sigValid && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                sigValid = Util.validateSignature(samlResponse,
                        relyingPartyObject.getSSOProperty(SSOConstants.KEY_STORE_NAME),
                        relyingPartyObject.getSSOProperty(SSOConstants.KEY_STORE_PASSWORD),
                        relyingPartyObject.getSSOProperty(SSOConstants.IDP_ALIAS),
                        tenantId, tenantDomain);
            }
            return sigValid;
        }
        if (log.isWarnEnabled()) {
            log.warn("SAML response in signature validation is not a SAML Response.");
        }
        return false;
    }

    /**
     * @param cx
     * @param thisObj
     * @param args    -args[0]-Logout request xml as a string.
     * @param funObj
     * @return
     * @throws Exception
     */
    public static boolean jsFunction_isLogoutRequest(Context cx, Scriptable thisObj, Object[] args,
                                                     Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Logout request xml is missing.");
        }

        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        String encoded = getSSOSamlEncodingProperty(relyingPartyObject);
        boolean isEncoded = true;
        if (encoded != null) {
            try {
                isEncoded = Boolean.parseBoolean(encoded);
            } catch (Exception e) {
                throw new ScriptException("Invalid property value found for " +
                                          "" + SSOConstants.SAML_ENCODED + " " + encoded);
            }
        }

        String logoutRequest = StringEscapeUtils.unescapeXml((String) args[0]);
        String decodedString = isEncoded ? Util.decode(logoutRequest) : logoutRequest;
        XMLObject samlObject = Util.unmarshall(decodedString);
		if (log.isDebugEnabled() && samlObject instanceof LogoutRequest) {
			log.debug("Request is a logout request and request is " + args[0]);
		}
        return samlObject instanceof LogoutRequest;

    }

    /**
     * @param cx
     * @param thisObj
     * @param args-args[0]- Logout response xml as a string
     * @param funObj
     * @return
     * @throws Exception
     */
    public static boolean jsFunction_isLogoutResponse(Context cx, Scriptable thisObj, Object[] args,
                                                      Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Logout response xml is missing.");
        }

        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        String encoded = getSSOSamlEncodingProperty(relyingPartyObject);
        boolean isEncoded = true;
        if (encoded != null) {
            try {
                isEncoded = Boolean.parseBoolean(encoded);
            } catch (Exception e) {
                throw new ScriptException("Invalid property value found for " +
                                          "" + SSOConstants.SAML_ENCODED + " " + encoded);
            }
        }

        String decodedString = isEncoded ? Util.decode((String) args[0]) : (String) args[0];
        XMLObject samlObject = Util.unmarshall(decodedString);
		if (log.isDebugEnabled() && samlObject instanceof LogoutResponse) {
			log.debug("Response is a logout response and response is " + args[0]);
		}
        return samlObject instanceof LogoutResponse;

    }

    /**
     * Checking whether the response is for passiveAuth SAML request or not.
     *
     * @param cx
     * @param thisObj
     * @param args - args[0] response for passiveAuth required as XML
     * @param funObj
     * @return
     * @throws Exception
     */

    public static boolean jsFunction_isPassiveAuthResponse(Context cx, Scriptable thisObj, Object[] args,
                                                          Function funObj) throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Logout response xml is missing.");
        }

        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        String encoded = getSSOSamlEncodingProperty(relyingPartyObject);
        boolean isEncoded = true;
        if (encoded != null) {
            try {
                isEncoded = Boolean.parseBoolean(encoded);
            } catch (Exception e) {
                throw new ScriptException("Invalid property value found for " +
                                          "" + SSOConstants.SAML_ENCODED + " " + encoded);
            }
        }

        String decodedString = isEncoded ? Util.decode((String) args[0]) : (String) args[0];
        XMLObject samlObject = Util.unmarshall(decodedString);

        if(samlObject instanceof Response)  {
            Response samlResponse = (Response) samlObject;

            if (samlResponse.getStatus() != null &&
                samlResponse.getStatus().getStatusCode() != null &&
                samlResponse.getStatus().getStatusCode().getValue().equals("urn:oasis:names:tc:SAML:2.0:status:Responder") &&
                samlResponse.getStatus().getStatusCode().getStatusCode() != null &&
                samlResponse.getStatus().getStatusCode().getStatusCode().getValue().equals("urn:oasis:names:tc:SAML:2.0:status:NoPassive")) {
                return true;
            }

        }
        return false;

    }

    /**
     * Compressing and Encoding the response
     *
     * @param cx
     * @param thisObj
     * @param args    -args[0]- string to be encoded.
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_encode(Context cx, Scriptable thisObj, Object[] args,
                                           Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. String to be encoded is missing.");
        }

        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        String deflate = getSSOSamlDeflateProperty(relyingPartyObject);
        boolean shouldDeflate = true;
        if (deflate != null) {
            try {
                shouldDeflate = Boolean.parseBoolean(deflate);
            } catch (Exception e) {
                throw new ScriptException("Invalid property value found for " +
                                          "" + SSOConstants.SAML_DEFLATE + " " + deflate);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Response string to be encoded is " + args[0]);
        }

        return shouldDeflate ? Util.deflateAndEncode((String) args[0]) : Util.encode((String) args[0]);

    }

    public static String jsFunction_getSAMLToken(Context cx, Scriptable thisObj, Object[] args,
                                                 Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session Id is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        SessionInfo sessionInfo = relyingPartyObject.getSessionInfo((String) args[0]);
        if (sessionInfo != null) {
            if (log.isDebugEnabled()) {
                log.debug("SAML token of relying party object is " + sessionInfo.getSamlToken());
            }
            //Here the samlToken is encoded. So no need to encode that again
            return sessionInfo.getSamlToken();
        }
        return null;
    }

    /**
     * Decoding and deflating the encoded AuthReq
     *
     * @param cx
     * @param thisObj
     * @param args    -args[0]-String to be decoded
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_decode(Context cx, Scriptable thisObj, Object[] args,
                                           Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. String to be decoded is missing.");
        }

        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        String encoded = getSSOSamlEncodingProperty(relyingPartyObject);
        boolean isEncoded = true;
        if (encoded != null) {
            try {
                isEncoded = Boolean.parseBoolean(encoded);
            } catch (Exception e) {
                throw new ScriptException("Invalid property value found for " +
                                          "" + SSOConstants.SAML_ENCODED + " " + encoded);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("AuthReq string to be decoded is " + args[0]);
        }

        return isEncoded ? Util.decode((String) args[0]) : (String) args[0];
    }

    /**
     * generate a UUID
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_getUUID(Context cx, Scriptable thisObj,
                                            Object[] args,
                                            Function funObj)
            throws Exception {
        return UUID.randomUUID().toString();

    }

    /**
     * Get SAML authentication request build with given issuer
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_getSAMLAuthRequest(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws Exception {
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        //ADDED
        if (!Boolean.valueOf(relyingPartyObject.getSSOProperty(SSOConstants.SIGN_REQUESTS))) {
            return Util.marshall(new AuthReqBuilder().buildAuthenticationRequest(
                    relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID)));
        } else {
            int argLength = args.length;
            if (argLength == 0) {
                return Util.marshall(new AuthReqBuilder().buildSignedAuthRequest(
                        relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID), MultitenantConstants.SUPER_TENANT_ID,
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
            } else {
                String consumerUrl = (String) args[0];
                return Util.marshall(new AuthReqBuilder().buildSignedAuthRequestWithConsumerUrl(
                        relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID),
                        relyingPartyObject.getSSOProperty(SSOConstants.IDP_URL), consumerUrl,
                        MultitenantConstants.SUPER_TENANT_ID, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
            }
        }
        //END
    }

    /**
     * Provides a way to includes passiveAuth and assertionConsumerServiceURL parameters to SAML Auth request
     *
     * @param cx
     * @param thisObj
     * @param args - args[0] indicates passiveAuth required or not args[1] indicates assertionConsumerServiceURL
     * @param funObj
     * @return
     * @throws Exception
     */

    public static String jsFunction_getSAMLPassiveAuthRequest(Context cx, Scriptable thisObj,
                                                              Object[] args,
                                                              Function funObj) throws Exception {
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;

        if ((Boolean) args[0])  {
            String acsUrl = (String) args[1];
            return Util.marshall(new AuthReqBuilder().
                    buildPassiveAuthenticationRequest(relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID), acsUrl));
        } else {
            return Util.marshall(new AuthReqBuilder().
                    buildAuthenticationRequest(relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID)));
        }
    }

    /**
     * Get SAML logout request build.
     *
     * @param cx
     * @param thisObj
     * @param args-args[0]-the user to be logout
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_getSAMLLogoutRequest(Context cx, Scriptable thisObj,
                                                         Object[] args,
                                                         Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 2 || !(args[0] instanceof String) && (args[1] instanceof String)) {
            throw new ScriptException("Invalid argument. The user to be logout is missing.");
        }
        if (log.isDebugEnabled()) {
            log.debug("The user to be logged out is " + args[0]);
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        log.debug("SAMLLogoutRequest is going to get session details");
        if (relyingPartyObject.getSessionInfo((String) args[1]) != null) {
            String sessionIndexId = relyingPartyObject.getSessionInfo((String) args[1]).getSessionIndex();
            if (sessionIndexId != null && sessionIndexId.length() > 0) {

                return Util.marshall(new LogoutRequestBuilder().
                        buildLogoutRequest((String) args[0], sessionIndexId,
                                           SSOConstants.LOGOUT_USER,
                                           relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID)));
            } else {
                return Util.marshall(new LogoutRequestBuilder().
                        buildLogoutRequest((String) args[0], SSOConstants.LOGOUT_USER,
                                           relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID)));
            }
        }
        log.debug("Session Information not found");
        return null;
    }

    /**
     * Extract the name of authenticated user from SAML response.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_getSAMLResponseNameId(Context cx, Scriptable thisObj,
                                                          Object[] args,
                                                          Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. The SAML response is missing.");
        }

        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        String encoded = getSSOSamlEncodingProperty(relyingPartyObject);
        boolean isEncoded = true;
        if (encoded != null) {
            try {
                isEncoded = Boolean.parseBoolean(encoded);
            } catch (Exception e) {
                throw new ScriptException("Invalid property value found for " +
                                          "" + SSOConstants.SAML_ENCODED + " " + encoded);
            }
        }

        String decodedString = isEncoded ? Util.decode((String) args[0]) : (String) args[0];
        XMLObject samlObject = Util.unmarshall(decodedString);
        String username = null;

        if (samlObject instanceof Response) {
            Response samlResponse = (Response) samlObject;
            List<Assertion> assertions = samlResponse.getAssertions();

            // extract the username
            if (assertions != null && assertions.size() > 0) {
                Subject subject = assertions.get(0).getSubject();
                if (subject != null) {
                    if (subject.getNameID() != null) {
                        username = subject.getNameID().getValue();
                        if (log.isDebugEnabled()) {
                            log.debug("Name of authenticated user from SAML response " + username);
                        }
                    }
                }
            }
        }
        if (username == null) {
            throw new Exception("Failed to get subject assertion from SAML response.");
        }
        return username;
    }

    /**
     * Set SSO Configuration key,values
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @throws ScriptException
     */
    public static void jsFunction_setProperty(Context cx, Scriptable thisObj, Object[] args,
                                              Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 2 || !(args[0] instanceof String) || !(args[1] instanceof String)) {
            throw new ScriptException("Invalid arguments when setting sso configuration values.");
        }
        if (log.isDebugEnabled()) {
            log.debug("SSO key values pair properties that set on relying party object is " + args[0] + " " + args[1]);
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        relyingPartyObject.setSSOProperty((String) args[0], (String) args[1]);

    }

    /**
     * Check if the browser session is valid. If user is log out from any sso service provider,
     * user session is invalidated.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static boolean jsFunction_isSessionAuthenticated(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session id is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        if (relyingPartyObject.isSessionIdExists((String) args[0])) {
            if (log.isDebugEnabled()) {
                log.debug("Browser session is valid..");
            }
            return true;
        }
        return false;

    }

    public static String jsFunction_getIdentitySessionId(Context cx, Scriptable thisObj,
                                                         Object[] args,
                                                         Function funObj) throws ScriptException {
        String identitySession = null;
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session id is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        SessionInfo sessionInfo = relyingPartyObject.getSessionInfo((String) args[0]);
        if (sessionInfo != null) {
            identitySession = sessionInfo.getSessionId();
        }
        return identitySession;
    }

    public static String jsFunction_getLoggedInUser(Context cx, Scriptable thisObj,
                                                    Object[] args,
                                                    Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session id is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        SessionInfo sessionInfo = relyingPartyObject.getSessionInfo((String) args[0]);
        String loggedInUser = null;
        if (sessionInfo != null && sessionInfo.getLoggedInUser() != null) {
            loggedInUser = sessionInfo.getLoggedInUser();
        }
        if (log.isDebugEnabled()) {
            log.debug("Logged in user is" + loggedInUser);
        }
        return loggedInUser;

    }

    /**
     * Invalidate current browser authenticated session based on SAML log out request session index value.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @throws Exception
     */
    public static void jsFunction_invalidateSessionBySAMLResponse(Context cx, Scriptable thisObj,
                                                                  Object[] args,
                                                                  Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. SAML log out request is missing.");
        }
        if (log.isDebugEnabled()) {
            log.debug("jsFunction_invalidateSessionBySAMLResponse===================Invalidating the authenticated session ");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        String encoded = getSSOSamlEncodingProperty(relyingPartyObject);
        boolean isEncoded = true;
        if (encoded != null) {
            try {
                isEncoded = Boolean.parseBoolean(encoded);
            } catch (Exception e) {
                throw new ScriptException("Invalid property value found for " +
                                          "" + SSOConstants.SAML_ENCODED + " " + encoded);
            }
        }

        String logoutRequest = StringEscapeUtils.unescapeXml((String) args[0]);
        String decodedString = isEncoded ? Util.decode(logoutRequest) : logoutRequest;
        XMLObject samlObject = Util.unmarshall(decodedString);
        String sessionIndex = null;
        if (samlObject instanceof LogoutRequest) {
            // if log out request
            LogoutRequest samlLogoutRequest = (LogoutRequest) samlObject;
            List<SessionIndex> sessionIndexes = samlLogoutRequest.getSessionIndexes();
            if (sessionIndexes != null && sessionIndexes.size() > 0) {
                sessionIndex = sessionIndexes.get(0).getSessionIndex();
            }
        }

        if (sessionIndex == null) {
            log.debug("No session index found in authentication statement in SAML response.");
        } else {
            relyingPartyObject.handleLogout(sessionIndex);
        }


    }

    /**
     * Invalidate current browser authenticated session based on session id.
     * Session will be invalidated after user log out request get succeeded.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @throws Exception
     */
    public static void jsFunction_invalidateSessionBySessionId(Context cx, Scriptable thisObj,
                                                               Object[] args,
                                                               Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session id is missing.");
        }
        if (log.isDebugEnabled()) {
            log.debug("jsFunction_invalidateSessionBySessionId===================Invalidating the authenticated session ");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        String sessionId = (String) args[0];
        String sessionIndex = relyingPartyObject.getSessionIndex(sessionId);
        relyingPartyObject.handleLogout(sessionIndex);


    }

    /**
     * Set the current session as authenticated by mapping with current session id to session index.
     *
     * @param cx
     * @param thisObj
     * @param args    -args[0]- current session id, args[1]-SAML response
     * @param funObj
     * @throws Exception
     */
    public static void jsFunction_setSessionAuthenticated(Context cx, Scriptable thisObj,
                                                          Object[] args,
                                                          Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 2 || !(args[0] instanceof String) || !(args[1] instanceof String)) {
            throw new ScriptException("Invalid argument. Current session id and SAML response are missing.");
        }

        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        String encoded = getSSOSamlEncodingProperty(relyingPartyObject);
        boolean isEncoded = true;
        if (encoded != null) {
            try {
                isEncoded = Boolean.parseBoolean(encoded);
            } catch (Exception e) {
                throw new ScriptException("Invalid property value found for " +
                                          "" + SSOConstants.SAML_ENCODED + " " + encoded);
            }
        }

        String decodedString = isEncoded ? Util.decode((String) args[1]) : (String) args[1];
        XMLObject samlObject = Util.unmarshall(decodedString);
        String sessionIndex = null;
        String username = null;
        if (samlObject instanceof Response) {
            Response samlResponse = (Response) samlObject;
            List<Assertion> assertions = samlResponse.getAssertions();

            // extract the session index
            if (assertions != null && assertions.size() > 0) {
                List<AuthnStatement> authenticationStatements = assertions.get(0).getAuthnStatements();
                AuthnStatement authnStatement = authenticationStatements.get(0);
                if (authnStatement != null) {
                    if (authnStatement.getSessionIndex() != null) {
                        sessionIndex = authnStatement.getSessionIndex();
                    }
                }
            }

            // extract the username
            if (assertions != null && assertions.size() > 0) {
                Subject subject = assertions.get(0).getSubject();
                if (subject != null) {
                    if (subject.getNameID() != null) {
                        username = subject.getNameID().getValue();
                    }
                }
            }
        }
        if (sessionIndex == null) {
            log.debug("No session index found in authentication statement in SAML response.");
        }
        if (username == null) {
            throw new Exception("Failed to get subject assertion from SAML response.");
        }

        SessionInfo sessionInfo = new SessionInfo((String) args[0]);
        if (sessionIndex != null) {
            sessionInfo.setSessionIndex(sessionIndex);
        }
        sessionInfo.setLoggedInUser(username);
        if (log.isDebugEnabled()) {
            log.debug("Encoded SAML token that is set on session info is " + args[1]);
        }
        sessionInfo.setSamlToken((String) args[1]);//We expect an encoded SamlToken here.
        relyingPartyObject.addSessionInfo(sessionInfo);
        relyingPartyObject.addSessionInfo(sessionIndex, SAMLSSORelyingPartyObject.sho);

    }

    /**
     * Get SSO configuration properties.
     *
     * @param cx
     * @param thisObj
     * @param args    -args[0]-configuration key
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static String jsFunction_getProperty(Context cx, Scriptable thisObj, Object[] args,
                                                Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. SSO configuratin key is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        return relyingPartyObject.getSSOProperty((String) args[0]);

    }

    /**
     * Set relay state property with requested uri.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @throws ScriptException
     */
    public static void jsFunction_setRelayStateProperty(Context cx, Scriptable thisObj,
                                                        Object[] args,
                                                        Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 2 || !(args[0] instanceof String) || !(args[1] instanceof String)) {
            throw new ScriptException("Invalid argument. RelayState and requested URI are missing.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Added relay state properties" + (String) args[0] + ":" + (String) args[1]);
        }
        relayStateMap.put((String) args[0], (String) args[1]);

    }

    /**
     * Get requested URI for relay state. And relay state value is removed, as relay state is unique and onetime value.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static String jsFunction_getRelayStateProperty(Context cx, Scriptable thisObj,
                                                          Object[] args,
                                                          Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Relay state value is missing.");
        }
        String requestedURI = relayStateMap.get((String) args[0]);
        if (log.isDebugEnabled()) {
            log.debug("Requested URI:" + relayStateMap.get((String) args[0]));
        }
        relayStateMap.remove((String) args[0]);
        return requestedURI;

    }

    public static String jsFunction_xmlDecode(Context cx, Scriptable thisObj,
                                              Object[] args,
                                              Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Relay state value is missing.");
        }
        String xmlString = (String) args[0];
        xmlString = xmlString.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
//                .replaceAll("&apos;", "'").replaceAll("&quot;", "\"").replaceAll("&amp;", "&");

        return xmlString;

    }

    public static String jsFunction_xmlEncode(Context cx, Scriptable thisObj,
                                              Object[] args,
                                              Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Relay state value is missing.");
        }
        String xmlString = (String) args[0];
        xmlString = xmlString.replaceAll(">", "&gt;").replaceAll("<", "&lt;");
//                .replaceAll("'","&apos;").replaceAll("\"","&quot;").replaceAll("&","&amp;");

        return xmlString;

    }

    private String getSSOProperty(String key) {
        return ssoConfigProperties.getProperty(key);
    }

    private void setSSOProperty(String key, String value) {

        if (key.equalsIgnoreCase(SSOConstants.SAML_DEFLATE) && value == null) {
            ssoConfigProperties.put(key, SSOConstants.DEFAULT_DEFLATE_VALUE);
        } else if (key.equalsIgnoreCase(SSOConstants.SAML_ENCODED) && value == null) {
            ssoConfigProperties.put(key, SSOConstants.DEFAULT_ENCODED_VALUE);
        } else {
            ssoConfigProperties.put(key, value);
        }
    }

    /**
     * Decode xml
     *
     * @param xmlString
     * @return
     */
    public static String decode(String xmlString) {
        xmlString = xmlString.replaceAll("&gt;", ">").replaceAll("&lt;", "<").
                replaceAll("&apos;", "'").replaceAll("&quot;", "\"").replaceAll("&amp;", "&");

        return xmlString;
    }

    /**
     * Add current browser session with session index.
     */
    private void addSessionInfo(SessionInfo sessionInfo) {
        sessionIdMap.put(sessionInfo.getSessionId(), sessionInfo);
    }

    /**
     * Remove current browser session(s) mapped with session index given.
     *
     * @param sessionIndex
     */
    private void invalidateSessionBySessionIndex(String sessionIndex) {
        if (sessionIndex != null) {
            for (Map.Entry entry : sessionIdMap.entrySet()) {
                if (entry.getValue() instanceof SessionInfo) {
                    SessionInfo sessionInfo = (SessionInfo) entry.getValue();
                    if (sessionInfo != null && sessionIndex.equals(sessionInfo.getSessionIndex())) {
                        if (log.isDebugEnabled()) {
                            log.debug("Session Index exists,thus invalidating session by session index" + sessionInfo.getSessionIndex());
                        }
                        sessionIdMap.remove(entry.getKey());
                    }
                }
            }
        }
    }

    private void invalidateSessionBySessionId(String sessionId) {
        if (log.isDebugEnabled()) {
            log.debug("Session Id exists,thus invalidating it " + sessionId);
        }
        sessionIdMap.remove(sessionId);
    }

    private boolean isSessionIdExists(String sessionId) throws Exception {
        Iterator<Map.Entry<String, Set<SessionHostObject>>> iterator = sessionIndexMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<SessionHostObject>> entry = iterator.next();
            Set<SessionHostObject> sessions = new HashSet<SessionHostObject>(entry.getValue());
            for (SessionHostObject session : sessions) {
                Object[] args = new Object[0];
                if (session != null && sessionId.equals(SessionHostObject.jsFunction_getId(null, session, args, null))) {
                    if (log.isDebugEnabled()) {
                        log.debug("Session Id exists:" + SessionHostObject.jsFunction_getId(null, session, args, null));
                    }
                    return true;
                }
            }
            //}
        }
        return false;
    }

    private String getSessionIndex(String sessionId) throws Exception {
        Iterator<Map.Entry<String, Set<SessionHostObject>>> iterator = sessionIndexMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<SessionHostObject>> entry = iterator.next();
            Set<SessionHostObject> sessions = new HashSet<SessionHostObject>(entry.getValue());
            for (SessionHostObject session : sessions) {
                Object[] args = new Object[0];
                if (session != null && sessionId.equals(SessionHostObject.jsFunction_getId(null, session, args, null))) {
                    if (log.isDebugEnabled()) {
                        log.debug("Get corresponding session index " + entry.getKey() + "by passing session id" + sessionId);
                    }
                    return entry.getKey();
                }
            }
            //}
        }
        return null;
    }

    protected SessionInfo getSessionInfo(String sessionId) {
        if (sessionIdMap != null) {
            return sessionIdMap.get(sessionId);
        }
        return null;
    }

    /**
     * Remove relying party object added with issuerId.
     *
     * @param issuerId
     */
    private static void invalidateRelyingPartyObject(String issuerId) {
        ssoRelyingPartyMap.remove(issuerId);
    }

    private static String getSSOSamlEncodingProperty(SAMLSSORelyingPartyObject relyingPartyObject) {

        if (relyingPartyObject.getSSOProperty(SSOConstants.SAML_ENCODED) == null) {
            return SSOConstants.DEFAULT_ENCODED_VALUE;
        } else {
            return relyingPartyObject.getSSOProperty(SSOConstants.SAML_ENCODED);
        }
    }

    private static String getSSOSamlDeflateProperty(SAMLSSORelyingPartyObject relyingPartyObject) {

        if (relyingPartyObject.getSSOProperty(SSOConstants.SAML_DEFLATE) == null) {
            return SSOConstants.DEFAULT_DEFLATE_VALUE;
        } else {
            return relyingPartyObject.getSSOProperty(SSOConstants.SAML_DEFLATE);
        }
    }

    /**
     * Add current browser session with session index.
     */
    private void addSessionInfo(String sessionIndex, Set<SessionHostObject> sho) {
        if (log.isDebugEnabled()) {
            log.debug("Added session index:" + sessionIndex);
        }
        sessionIndexMap.put(sessionIndex, sho);
    }

    /**
     * Remove current browser session(s) mapped with session index given.
     *
     * @param sessionId
     */
    private void invalidateSessionById(String sessionId) throws Exception {
        Iterator<Map.Entry<String, Set<SessionHostObject>>> iterator = sessionIndexMap.entrySet().iterator();
        while (iterator.hasNext()) {
            boolean remove = false;
            Map.Entry<String, Set<SessionHostObject>> entry = iterator.next();
            Set<SessionHostObject> sessions = new HashSet<SessionHostObject>(entry.getValue());
            for (SessionHostObject session : sessions) {
                Object[] args = new Object[0];
                if (session != null && sessionId.equals(SessionHostObject.jsFunction_getId(null, session, args, null))) {
                    if (log.isDebugEnabled()) {
                        log.debug("Session Id exists.Invalidating that value" + SessionHostObject.jsFunction_getId(null, session, args, null));
                    }
                    remove = true;
                }
                if (remove) {
                    sessionIndexMap.remove(entry.getKey());
                    remove = false;
                }
            }
        }
    }

    public void removeSession(String sessionIndex) {
        if (sessionIndexMap != null && sessionIndex != null) {
            if (sessionIndexMap.containsKey(sessionIndex)) {
                if (log.isDebugEnabled()) {
                    log.debug("Remove session index from SessionIndexMap:" + sessionIndex);
                }
                sessionIndexMap.remove(sessionIndex);
            }
        }
    }

    public void clearSessionsSet() {
        if (SAMLSSORelyingPartyObject.sho != null) {
            if (log.isDebugEnabled()) {
                log.debug("Removing set of session hostobjects");
            }
            SAMLSSORelyingPartyObject.sho = new HashSet<SessionHostObject>();
        }
        if (SAMLSSORelyingPartyObject.ssho != null) {
            if (log.isDebugEnabled()) {
                log.debug("Removing the session hostobject");
            }
            SAMLSSORelyingPartyObject.ssho = null;
        }
    }

    public SessionHostObject getSession(String sessionIndex) {
        if (sessionIndexMap != null) {
            return (SessionHostObject) sessionIndexMap.get(sessionIndex);
        }
        return null;
    }

    public void handleLogout(String sessionIndex) {
        if (log.isDebugEnabled()) {
            log.debug("session index map value:" + sessionIndexMap);
            if (sessionIndexMap != null) {
                log.debug("session index map size:" + sessionIndexMap.size());
                log.debug("session index map :" + sessionIndexMap);
                log.debug("session index :" + sessionIndex);
                // log.debug("Is session index map contains session index:"+sessionIndexMap.containsKey(sessionIndex));
            }
        }
        if ((sessionIndexMap != null && sessionIndex != null) && (!sessionIndexMap.containsKey(sessionIndex))) {
            //send cluster message
            sendSessionInvalidationClusterMessage(sessionIndex);
            return;
        }
        try {
            ssho.invalidateSessionBySessionIndex(sessionIndex);
            // this is to invalidate relying party object after user log out. To release memory allocations.
            invalidateRelyingPartyObject(ssho.getSSOProperty(SSOConstants.ISSUER_ID));
            if (sessionIndexMap != null && sessionIndex != null) {
                Set<SessionHostObject> sessionList = sessionIndexMap.get(sessionIndex);
                Object[] args = new Object[0];
                if (sessionList != null) {
                    for (SessionHostObject session : sessionList) {
                        if (SessionHostObject.jsFunction_getId(null, session, args, null) != null) {
                            SessionHostObject.jsFunction_invalidate(null, session, args, null);
                        }
                    }
                }
            }
            removeSession(sessionIndex);
            clearSessionsSet();
        } catch (Exception ignored) {
            removeSession(sessionIndex);
            clearSessionsSet();
        }
        if (log.isDebugEnabled()) {
            log.debug("Cleared authenticated session index:" + sessionIndex + "in handle logout method");
        }
    }


    public void sendSessionInvalidationClusterMessage(String sessionIndex) {

        SessionClusterMessage clusterMessage = new SessionClusterMessage();
        clusterMessage.setMessageId(UUID.randomUUID());
        clusterMessage.setSessionIndex(sessionIndex);
        ClusteringAgent clusteringAgent = SSOHostObjectDataHolder.getInstance()
                .getConfigurationContextService().getServerConfigContext().getAxisConfiguration()
                .getClusteringAgent();
        if (clusteringAgent != null) {
            int numberOfRetries = 0;
            while (numberOfRetries < 60) {
                try {
                    clusteringAgent.sendMessage(clusterMessage, true);
                    log.info("Sent [" + clusterMessage + "]");
                    break;
                } catch (ClusteringFault e) {
                    numberOfRetries++;
                    if (numberOfRetries < 60) {
                        log.warn(
                                "Could not send SSOSessionInvalidationClusterMessage. Retry will be attempted in 2s. Request: "
                                + clusterMessage, e);
                    } else {
                        log.error("Could not send SSOSessionInvalidationClusterMessage. Several retries failed. Request:"
                                  + clusterMessage, e);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }


}
