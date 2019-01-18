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

package org.wso2.carbon.apimgt.hostobjects.oidc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.apimgt.hostobjects.oidc.internal.*;

import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class wrap up the operations needed to authenticate with OIDC server
 */
public class OIDCRelyingPartyObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(OIDCRelyingPartyObject.class);

    //stores oidc properties like, identity server url,keystore path, alias, keystore password, issuerId
    private Properties oidcConfigProperties = new Properties();

    // issuerId, relyingPartyObject .this is to provide oidc functionality to multiple jaggery apps.
    private static Map<String, OIDCRelyingPartyObject> oidcRelyingPartyObjectMap = new HashMap<String, OIDCRelyingPartyObject>();


    // sessionId, sessionIndex. this is to map current session with session index sent from Identity server.
    // When log out request come from identity server,we need to invalidate the current session.
    private static Map<String, SessionInfo> sessionIdMap = new ConcurrentHashMap<String, SessionInfo>();


    @Override
    public String getClassName() {
        return "OIDCRelyingParty";
    }

    /**
     * @param cx        - Context
     * @param args      - args[0]-issuerId, this issuer need to be registered in Identity server.
     * @param ctorObj   - function
     * @param inNewExpr - boolean
     * @return          - host object
     * @throws Exception
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid arguments!, IssuerId is missing in parameters.");
        }

        OIDCRelyingPartyObject relyingPartyObject = oidcRelyingPartyObjectMap.get((String) args[0]);
        if (relyingPartyObject == null) {
            relyingPartyObject = new OIDCRelyingPartyObject();
            relyingPartyObject.setOIDCProperty(OIDCConstants.ISSUER_ID, (String) args[0]);
            oidcRelyingPartyObjectMap.put((String) args[0], relyingPartyObject);
        }

        return relyingPartyObject;
    }


    /**
     * Building authentication request URL. This URL allows to redirect in to OIDC server and authenticate.
     * @param cx        - Context
     * @param thisObj   - This Object
     * @param args      - takes nonce and state parameters
     * @param funObj    - Function
     * @return URL which redirects to OIDC server and allow to authenticate
     * @throws Exception
     */
    public static String jsFunction_buildAuthRequestUrl(Context cx, Scriptable thisObj, Object[] args,
                                                        Function funObj) throws Exception {

        int argLength = args.length;
        if (argLength != 2 || !(args[0] instanceof String) || !(args[1] instanceof String)) {
            throw new ScriptException("Invalid argument. Nonce or State not set properly");
        }

        String nonce = (String) args[0];
        String state = (String) args[1];

        OIDCRelyingPartyObject relyingPartyObject = (OIDCRelyingPartyObject) thisObj;

        try {
            log.debug(" Building auth request Url");

            URIBuilder uriBuilder = new URIBuilder(relyingPartyObject.getOIDCProperty(OIDCConstants.
                    AUTHORIZATION_ENDPOINT_URI));

            uriBuilder.addParameter(OIDCConstants.RESPONSE_TYPE,
                    relyingPartyObject.getOIDCProperty(OIDCConstants.RESPONSE_TYPE));
            uriBuilder.addParameter(OIDCConstants.CLIENT_ID,
                    relyingPartyObject.getOIDCProperty(OIDCConstants.CLIENT_ID));
            uriBuilder.addParameter(OIDCConstants.SCOPE,
                    relyingPartyObject.getOIDCProperty(OIDCConstants.SCOPE));
            uriBuilder.addParameter(OIDCConstants.REDIRECT_URI,
                    relyingPartyObject.getOIDCProperty(OIDCConstants.REDIRECT_URI));
            uriBuilder.addParameter(OIDCConstants.NONCE,
                    nonce);
            uriBuilder.addParameter(OIDCConstants.STATE,
                    state);

            // Optional parameters:
            //for (Map.Entry<String, String> option : options.entrySet()) {
            // uriBuilder.addParameter(option.getKey(), option.getValue());
            //}
            //uriBuilder.addParameter("requestURI", requestURI);

            return uriBuilder.build().toString();

        } catch (URISyntaxException e) {
            log.error("Build Auth Request Failed", e);
            throw new Exception("Build Auth Request Failed", e);

        }

    }


    /**
     * @param cx      - Context
     * @param thisObj - This object
     * @param args    - argument list
     * @param funObj  - function
     * @return        - boolean
     * @throws Exception
     */
    public static boolean jsFunction_validateOIDCSignature(Context cx, Scriptable thisObj, Object[] args,
                                                           Function funObj)
            throws Exception {

        int argLength = args.length;
        if (argLength != 3 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. oidcTokenReponse, Nonce value or session ID is missing.");
        }

        return validateSignature(thisObj, args) != null;
    }

    public static String jsFunction_getIdTokenResponse(Context cx, Scriptable thisObj, Object[] args,
                                                       Function funObj) throws Exception {

        log.debug("Obtaining id_token");
        OIDCRelyingPartyObject relyingPartyObject = (OIDCRelyingPartyObject) thisObj;

        ServerConfiguration serverConfiguration = getServerConfiguration(relyingPartyObject);
        AuthClient authClient = getClientConfiguration(relyingPartyObject);

        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Authorization Code is missing.");
        }

        String authorizationCode = (String) args[0];
        return getTokenFromTokenEP(serverConfiguration, authClient, authorizationCode);
    }

    /**
     * To validate the OIDC signature and return userInfo JSON.
     *
     * @param oidcObject OIDC relying part object.
     * @param args       Authorization Code, Nonce value.
     * @return user info if the signature is validated.
     * @throws Exception Exception
     */
    private static String validateSignature(Scriptable oidcObject, Object[] args) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Validating OIDC signature");
        }
        boolean isSignatureValid;
        String storedNonce = (String) args[1];
        OIDCRelyingPartyObject relyingPartyObject = (OIDCRelyingPartyObject) oidcObject;
        ServerConfiguration serverConfiguration = getServerConfiguration(relyingPartyObject);
        AuthClient authClient = getClientConfiguration(relyingPartyObject);
        AuthenticationToken oidcAuthenticationToken = getAuthenticationToken((String) args[0]);
        String userInfoJson = Util.getUserInfo(serverConfiguration, oidcAuthenticationToken);
        String userName = getUserName(userInfoJson, relyingPartyObject);

        if (StringUtils.isEmpty(userName)) {
            log.error("Authentication Request is rejected. Username is null. Please make sure the username is not "
                    + "empty");
            return null;
        }
        isSignatureValid = validateSignature(serverConfiguration, authClient, oidcAuthenticationToken, storedNonce);
        if (isSignatureValid) {
            if (log.isDebugEnabled()) {
                log.debug("OIDC signature validation succeeded for the user " + userName);
            }
            SessionInfo sessionInfo = new SessionInfo((String) args[2]);
            sessionInfo.setLoggedInUser(userName);
            relyingPartyObject.addSessionInfo(sessionInfo);
            return userInfoJson;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("OIDC signature validation failed for the user " + userName);
            }
            return null;
        }
    }

    /**
     * To validate the OIDC signature and return the user info if the validation Succeeds.
     *
     * @param cx      Context.
     * @param thisObj OIDC relying party object.
     * @param args    Arguments.
     * @param funObj  Function Objects.
     * @return user info json string if the validation succeeds, otherwise null will be returned.
     * @throws Exception Exception related with validating signature.
     */
    public static String jsFunction_validateSignature(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) throws Exception {
        if (args.length != 3 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Authorization Code, Nonce value or session ID is missing.");
        }
        return validateSignature(thisObj, args);
    }

    /**
     * Get OIDC Server Configuration
     * @return ServerConfiguration
     */
    private static ServerConfiguration getServerConfiguration(OIDCRelyingPartyObject relyingPartyObject) {

        ServerConfiguration serverConfiguration = new ServerConfiguration();

        serverConfiguration.setIssuer(relyingPartyObject.getOIDCProperty(OIDCConstants.IDP_URL));
        serverConfiguration.setJwksUri(relyingPartyObject.getOIDCProperty(OIDCConstants.JWKS_URI));
        serverConfiguration.setUserInfoUri(relyingPartyObject.getOIDCProperty(OIDCConstants.USER_INFO_URI));
        serverConfiguration.setTokenEndpointUri(relyingPartyObject.getOIDCProperty(OIDCConstants.TOKEN_ENDPOINT_URI));
        return serverConfiguration;
    }

    /**
     * Create AuthClient bean to hold client information
     * @return AuthClient
     */
    private static AuthClient getClientConfiguration(OIDCRelyingPartyObject relyingPartyObject) {

        AuthClient authClient = new AuthClient();

        authClient.setClientId(relyingPartyObject.getOIDCProperty(OIDCConstants.CLIENT_ID));
        authClient.setClientSecret(relyingPartyObject.getOIDCProperty(OIDCConstants.CLIENT_SECRET));
        authClient.setAuthorizationType(relyingPartyObject.getOIDCProperty(OIDCConstants.AUTHORIZATION_TYPE));
        authClient.setRedirectURI(relyingPartyObject.getOIDCProperty(OIDCConstants.REDIRECT_URI));
        authClient.setClientAlgorithm(relyingPartyObject.getOIDCProperty(OIDCConstants.CLIENT_ALGORITHM));
        return authClient;

    }

    /**
     * HTTP post against token endpoint of OIDC server.
     *
     * @param serverConfiguration ServerConfiguration
     * @param code                code
     * @return json String
     * @throws java.io.IOException
     */
    private static String getTokenFromTokenEP(ServerConfiguration serverConfiguration, AuthClient authClient,
                                       String code) throws IOException {

        // Client details
        String clientId = authClient.getClientId();
        String clientSecret = authClient.getClientSecret();
        String authorizationType = authClient.getAuthorizationType();
        String redirectURI = authClient.getRedirectURI();


        HttpClient client = new DefaultHttpClient();

        HttpPost post = new HttpPost(serverConfiguration.getTokenEndpointUri());

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        nvps.add(new BasicNameValuePair("grant_type", authorizationType));
        nvps.add(new BasicNameValuePair("code", code));
        nvps.add(new BasicNameValuePair("redirect_uri", redirectURI));
        post.setEntity(new UrlEncodedFormEntity(nvps));


        post.setHeader(HttpHeaders.AUTHORIZATION, String.format("Basic %s", Base64.
                encode(String.format("%s:%s", clientId, clientSecret))).trim());

        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        String jsonString = "";
        String line;
        while ((line = rd.readLine()) != null) {
            jsonString = jsonString + line;
            log.debug("Response from Token Endpoint : " + jsonString);
        }
        return jsonString;
    }

    private static AuthenticationToken getAuthenticationToken(String jsonTokenResponse)
            throws Exception {

        JsonElement jsonRoot = new JsonParser().parse(jsonTokenResponse);
        if (!jsonRoot.isJsonObject()) {
            throw new Exception("Token Endpoint did not return a JSON object: " + jsonRoot);
        }
        JsonObject tokenResponse = jsonRoot.getAsJsonObject();

        if (tokenResponse.get("error") != null) {

            // Handle error
            String error = tokenResponse.get("error").getAsString();
            log.error("Token Endpoint returned: " + error);
            throw new Exception("Unable to obtain Access Token.  Token Endpoint returned: " + error);

        } else {

            // get out all the token strings
            String accessTokenValue;
            String idTokenValue;
            String refreshTokenValue = null;

            if (tokenResponse.has("access_token")) {
                accessTokenValue = tokenResponse.get("access_token").getAsString();
            } else {
                throw new Exception("Token Endpoint did not return an access_token: " +
                        jsonTokenResponse);
            }

            if (tokenResponse.has("id_token")) {
                idTokenValue = tokenResponse.get("id_token").getAsString();
            } else {
                log.error("Token Endpoint did not return an id_token");
                throw new Exception("Token Endpoint did not return an id_token");
            }

            if (tokenResponse.has("refresh_token")) {
                refreshTokenValue = tokenResponse.get("refresh_token").getAsString();
            }

            return new AuthenticationToken(idTokenValue,
                    accessTokenValue, refreshTokenValue);

        }
    }

    /**
     * To get the username from the userinfo.
     *
     * @param userInfoJson       Relevant user information.
     * @param relyingPartyObject OIDC Relying party Object.
     * @return extracted username.
     * @throws Exception Exception.
     */
    static String getUserName(String userInfoJson, OIDCRelyingPartyObject relyingPartyObject)
            throws Exception {

        String userName;
        JsonElement jsonRoot = new JsonParser().parse(userInfoJson);

        if (!jsonRoot.isJsonObject()) {
            log.error("User Info Json did not return a JSON object: " + jsonRoot);
            throw new Exception("User Info Json did not return a JSON object: " + jsonRoot);
        }

        JsonObject jsonResponse = jsonRoot.getAsJsonObject();

        String usernameProperty = relyingPartyObject.getOIDCProperty("usernameClaim");
        if (StringUtils.isEmpty(usernameProperty)) {
            usernameProperty = "preferred_username";
        }
        if (jsonResponse.has(usernameProperty)) {
            userName = jsonResponse.get(usernameProperty).getAsString();
            log.debug("User name taken from user info endpoint : " + userName);
        } else {
            throw new Exception("User Info JSON did not return an preferred_username");
        }
        return userName;
    }

    private static boolean validateSignature(ServerConfiguration serverConfiguration, AuthClient authClient,
                                      AuthenticationToken oidcAuthenticationToken, String nonce) throws Exception {

        boolean isSignatureValid;
        JWT idToken = JWTParser.parse(oidcAuthenticationToken.getIdTokenValue());
        JWTClaimsSet idClaims = idToken.getJWTClaimsSet();

        // Supports only signedJWT
        if (idToken instanceof SignedJWT) {
            SignedJWT signedIdToken = (SignedJWT) idToken;
            isSignatureValid = Util.verifySignature(signedIdToken, serverConfiguration);

        } else if (idToken instanceof PlainJWT) {
            log.error("Plain JWT not supported");
            throw new Exception("Plain JWT not supported");

        } else {
            log.error("JWT type not supported");
            throw new Exception("JWT type not supported");
        }

        boolean isValidClaimSet = Util.validateIdClaims(serverConfiguration, authClient, idToken, nonce, idClaims);
        return  isSignatureValid && isValidClaimSet;
    }



    /**
     * Create a cryptographically random nonce and return
     *
     * @param
     * @return
     */
    public static String jsFunction_createNonce(Context cx, Scriptable thisObj,
                                                Object[] args,
                                                Function funObj) {
        return new BigInteger(50, new SecureRandom()).toString(16);
    }


    /**
     * Create a cryptographically random state and return
     *
     * @param
     * @return
     */
    public static String jsFunction_createState(Context cx, Scriptable thisObj,
                                                Object[] args,
                                                Function funObj) {
        return new BigInteger(50, new SecureRandom()).toString(16);
    }


    public static String jsFunction_getLoggedInUser(Context cx, Scriptable thisObj,
                                                    Object[] args,
                                                    Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session id is missing.");
        }
        OIDCRelyingPartyObject relyingPartyObject = (OIDCRelyingPartyObject) thisObj;
        SessionInfo sessionInfo = relyingPartyObject.getSessionInfo((String) args[0]);
        String loggedInUser = null;
        if (sessionInfo != null && sessionInfo.getLoggedInUser() != null) {
            loggedInUser = sessionInfo.getLoggedInUser();
        }

        return loggedInUser;

    }


    public static boolean jsFunction_isSessionAuthenticated(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session id is missing.");
        }
        OIDCRelyingPartyObject relyingPartyObject = (OIDCRelyingPartyObject) thisObj;

        return relyingPartyObject.isSessionIdExists((String) args[0]);

    }


    /**
     * Add current browser session with session index.
     */
    private void addSessionInfo(SessionInfo sessionInfo) {
        sessionIdMap.put(sessionInfo.getSessionId(), sessionInfo);
    }

    private SessionInfo getSessionInfo(String sessionId) {
        return sessionIdMap.get(sessionId);
    }

    private boolean isSessionIdExists(String sessionId) {
        return sessionIdMap.containsKey(sessionId);
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
        OIDCRelyingPartyObject relyingPartyObject = (OIDCRelyingPartyObject) thisObj;

        relyingPartyObject.invalidateSessionBySessionId((String) args[0]);
        // this is to invalidate relying party object after user log out. To release memory allocations.
        invalidateRelyingPartyObject("API_STORE");

    }

    private void invalidateSessionBySessionId(String sessionId) {
        sessionIdMap.remove(sessionId);
    }

    /**
     * Remove relying party object added with issuerId.
     *
     * @param issuerId
     */
    private static void invalidateRelyingPartyObject(String issuerId) {
        oidcRelyingPartyObjectMap.remove(issuerId);
    }


    public static void jsFunction_logoutUser(Context cx, Scriptable thisObj,
                                                               Object[] args,
                                                               Function funObj)
            throws Exception {

    }


    /**
     * Set OIDC Configuration key,values
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
            throw new ScriptException("Invalid arguments when setting OIDC configuration values.");
        }
        if (log.isDebugEnabled()) {
            log.debug("OIDC key values pair properties that set on relying party object is " + args[0] + " " + args[1]);
        }
        OIDCRelyingPartyObject relyingPartyObject = (OIDCRelyingPartyObject) thisObj;
        relyingPartyObject.setOIDCProperty((String) args[0], (String) args[1]);

    }


    private String getOIDCProperty(String key) {
        return oidcConfigProperties.getProperty(key);
    }

    private void setOIDCProperty(String key, String value) {
        oidcConfigProperties.put(key, value);
    }


}
