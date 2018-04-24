
import ballerina/http;
import ballerina/log;
import ballerina/auth;
import ballerina/caching;
import ballerina/config;
import ballerina/runtime;
import ballerina/time;
import ballerina/util;
import ballerina/io;
import ballerina/internal;
import org.wso2.carbon.apimgt.gateway.constants as constants;
import org.wso2.carbon.apimgt.gateway.dto as dto;

// Authorization handler

endpoint http:Client introspectEndpoint {
    targets:[{url:"https://localhost:9443"}]
};

@Description {value:"Representation of JWT Auth handler for HTTP traffic"}
@Field {value:"jwtAuthenticator: JWTAuthenticator instance"}
@Field {value:"name: Authentication handler name"}
public type OAuthnHandler object {
    public {
        string name= "oauth2";
        OAuthAuthProvider oAuthAuthenticator = new;
    }

    public function canHandle (http:Request req) returns (boolean);
    public function handle (http:Request req) returns (boolean);
};

@Description {value:"Intercepts a HTTP request for authentication"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if authentication is a success, else false"}
public function OAuthnHandler::canHandle (http:Request req) returns (boolean) {
    string authHeader;
    try {
        authHeader = req.getHeader(constants:AUTH_HEADER);
        io:println("auth header" + authHeader);
    } catch (error e) {
        log:printDebug("Error in retrieving header " + constants:AUTH_HEADER + ": " + e.message);
        return false;
    }
    if (authHeader != null && authHeader.hasPrefix(constants:AUTH_SCHEME_BEARER)) {
        string[] authHeaderComponents = authHeader.split(" ");
        if (lengthof authHeaderComponents == 2) {
            return true;
        }
    }
    return false;
}

@Description {value:"Checks if the provided HTTP request can be authenticated with JWT authentication"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if its possible to authenticate with JWT auth, else false"}
public function OAuthnHandler::handle (http:Request req) returns (boolean) {
    string accessToken = extractAccessToken(req);
    io:println("token " + accessToken);
    var isAuthenticated = self.oAuthAuthenticator.authenticate(accessToken);
    io:println("isAuthenticated " + isAuthenticated);
    return isAuthenticated;
    //match isAuthenticated {
    //    boolean authenticated => {
    //        return authenticated;
    //
    //    error err => {
    //        log:printErrorCause("Error while validating JWT token ", err);
    //        return false;
    //    }
    //}
}

function extractAccessToken (http:Request req) returns (string) {
    string authHeader = req.getHeader(constants:AUTH_HEADER);
    string[] authHeaderComponents = authHeader.split(" ");
    return authHeaderComponents[1];
}



@Description {value:"Represents a JWT Authenticator"}
@Field {value:"jwtAuthProviderConfig: JWTAuthProviderConfig object"}
@Field {value:"authCache: Authentication cache object"}
public type OAuthAuthProvider object {
    public {
        JWTAuthProviderConfig jwtAuthProviderConfig;
        caching:Cache authCache;
    }


    public function authenticate (string authToken) returns (boolean);

    public function doIntrospect (string authToken) returns (json);
};

@Description {value:"Represents JWT validator configurations"}
public type JWTAuthProviderConfig {
    string issuer,
    string audience,
    int timeSkew,
    string certificateAlias,
    string trustStoreFilePath,
    string trustStorePassword,
};

@final string JWT_AUTH_CACHE = "jwt_auth_cache";
@final string SCOPES = "scope";
@final string GROUPS = "groups";
@final string USERNAME = "name";
@final string AUTH_TYPE_JWT = "jwt";




@Description {value:"Authenticate with a jwt token"}
@Param {value:"jwtToken: Jwt token extracted from the authentication header"}
@Return {value:"boolean: true if authentication is a success, else false"}
@Return {value:"error: If error occured in authentication"}
public function OAuthAuthProvider::authenticate (string authToken) returns (boolean) {
    dto:IntrospectDto introspectDto = check <dto:IntrospectDto>self.doIntrospect(authToken);
    io:println(introspectDto);
    if (introspectDto.active) {
        // set username
        runtime:getInvocationContext().authenticationContext.username = introspectDto.username;
        string[] scopes = introspectDto.scope.split(" ");
        if (lengthof scopes > 0) {
            runtime:getInvocationContext().authenticationContext.scopes = scopes;
        }
        // read scopes and set to the invocation context

    }
    return introspectDto.active;
    //match introspectDto.active {
    //    string active => {
    //        return  <boolean>active but {string => false};
    //    }
    //    error err => {
    //        log:printErrorCause("Error in invoking introspect endpoint", err);
    //        return false;
    //    }
    //}
}


public function OAuthAuthProvider::doIntrospect (string authToken) returns (json) {
    try {
        string encodedBasicAuthHeader = check util:base64EncodeString("admin:admin");
        http:Request clientRequest = new;

        http:Response clientResponse = new;

        clientRequest.setStringPayload("token=" + authToken);
        clientRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
        clientRequest.setHeader("Authorization", "Basic " + encodedBasicAuthHeader);
        //var result = introspectEndpoint -> post("/api/identity/oauth2/introspect/v1.0/introspect", clientRequest);
        var result = introspectEndpoint -> post("/oauth2/introspect", clientRequest);

        match result {
            http:HttpConnectorError err => {
                io:println("Error occurred while reading locator response");
            }
            http:Response prod => {
                clientResponse = prod;
            }
        }
        io:println("\nPOST request:");
        io:println(clientResponse.getJsonPayload());
        return check clientResponse.getJsonPayload();

    } catch (error err) {
        io:println(err);
        return {};
    }
    return {};

}



