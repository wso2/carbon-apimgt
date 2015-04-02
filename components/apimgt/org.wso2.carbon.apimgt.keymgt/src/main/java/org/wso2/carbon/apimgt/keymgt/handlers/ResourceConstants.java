package org.wso2.carbon.apimgt.keymgt.handlers;


public final class ResourceConstants {

    public static final String RESOURCE_CONTEXT = "context";
    public static final String RESOURCE_VERSION = "apiVersion";
    public static final String RESOURCE_AUTH_LEVEL = "authLevel";
    public static final String RESOURCE_PATH = "resource";
    public static final String RESOURCE_VERB = "httpVerb";
    public static final String KEY_DOMAIN = "clientDomain";

    public static final String RESOURCE_PARAMS = "keymgt_resource_params";
    public static final String INTROSPECTURI = "http://localhost:8080/openid-connect-server-webapp/introspect";
    public static final String AUTH_TOKEN_PARAM_NAME = "token";
    public static final String AUTHORIZATION_PARAM_NAME = "Authorization";
    public static final String BASIC_TOKEN_NAME = "Basic";
    public static final String INTROSPECTION_TOKEN = "Y2xpZW50OnNlY3JldA=="; //base64_encode(clientid:secret)
    public static final String UTF8_PARAM_NAME = "UTF-8";
    public static final String ACTIVE_PARAM_NAME = "active";
    public static final String IAT_PARAM_NAME = "iat";
    public static final String EXP_PARAM_NAME = "exp";
    public static final String CLIENT_ID_PARAM_NAME = "client_id";


}
