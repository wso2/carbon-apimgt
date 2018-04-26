
import ballerina/http;
import ballerina/log;
import ballerina/auth;
import ballerina/cache;
import ballerina/config;
import ballerina/runtime;
import ballerina/time;
import ballerina/io;
import org.wso2.carbon.apimgt.gateway.constants as constants;
import org.wso2.carbon.apimgt.gateway.dto as dto;

// Authorization handler

endpoint http:Client introspectEndpoint {
    url:"https://localhost:9443"
};

endpoint http:Client keyValidationEndpoint {
    url:"https://localhost:9443"
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
        cache:Cache authCache;
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
    dto:APIKeyValidationDto apiKeyValidationDto = check <dto:APIKeyValidationDto>self.doIntrospect(authToken);
    io:println(apiKeyValidationDto);
    boolean authorized = <boolean>apiKeyValidationDto.authorized;
    if (authorized) {
        // set username
        runtime:getInvocationContext().userPrincipal.username = apiKeyValidationDto.endUserName;
        string[] scopes = apiKeyValidationDto.scopes;//.split(",");
        io:println("#####################################################################");
        io:println(scopes);
        if (lengthof scopes > 0) {
            runtime:getInvocationContext().userPrincipal.scopes = scopes;
        }
        // read scopes and set to the invocation context

    }
    return authorized;
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
        string base64Header = "admin:admin";
        string encodedBasicAuthHeader = check base64Header.base64Encode();
        io:println(encodedBasicAuthHeader);

        // Intorospect related changes
        //http:Request clientRequest = new;
        //
        //http:Response clientResponse = new;
        //
        //clientRequest.setTextPayload("token=" + authToken, contentType=constants:X_WWW_FORM_URLENCODED);
        //clientRequest.setHeader(constants:CONTENT_TYPE_HEADER, constants:X_WWW_FORM_URLENCODED);
        //clientRequest.setHeader(constants:AUTHORIZATION_HEADER, constants:BASIC_PREFIX_WITH_SPACE +
        //        encodedBasicAuthHeader);
        ////var result = introspectEndpoint -> post("/api/identity/oauth2/introspect/v1.0/introspect", clientRequest);
        //var result = introspectEndpoint -> post("/oauth2/introspect", request=clientRequest);
        //
        //match result {
        //    http:HttpConnectorError err => {
        //        io:println("Error occurred while reading locator response",err);
        //    }
        //    http:Response prod => {
        //        clientResponse = prod;
        //    }
        //}
        //io:println("\nPOST request:");
        //io:println(clientResponse.getJsonPayload());


        ////////////////////////////////////////////////////////////////////
        http:Request clientRequest1 = new;

        http:Response clientResponse1 = new;
        string xmlString = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"
            xmlns:xsd=\"http://org.apache.axis2/xsd\">
            <soapenv:Header/>
            <soapenv:Body>
            <xsd:validateKey>
            <!--Optional:-->
            <xsd:context>/pizzashack/1.0.0</xsd:context>
            <!--Optional:-->
            <xsd:version>1.0.0</xsd:version>
            <!--Optional:-->
            <xsd:accessToken>" + authToken + "</xsd:accessToken>
            <!--Optional:-->
            <xsd:requiredAuthenticationLevel>Any</xsd:requiredAuthenticationLevel>
            <!--Optional:-->
            <xsd:clientDomain>*</xsd:clientDomain>
            <!--Optional:-->
            <xsd:matchingResource>/menu</xsd:matchingResource>
            <!--Optional:-->
            <xsd:httpVerb>GET</xsd:httpVerb>
            </xsd:validateKey>
            </soapenv:Body>
            </soapenv:Envelope>";

        clientRequest1.setTextPayload(xmlString,contentType="text/xml");
        clientRequest1.setHeader(constants:CONTENT_TYPE_HEADER, "text/xml");
        clientRequest1.setHeader(constants:AUTHORIZATION_HEADER, constants:BASIC_PREFIX_WITH_SPACE +
                encodedBasicAuthHeader);
        clientRequest1.setHeader("SOAPAction", "urn:validateKey");
        //var result = introspectEndpoint -> post("/api/identity/oauth2/introspect/v1.0/introspect", clientRequest);
        var result1 = keyValidationEndpoint -> post("/services/APIKeyValidationService", request=clientRequest1);

        match result1 {
            http:HttpConnectorError err => {
                io:println("Error occurred while reading locator response",err);
            }
            http:Response prod => {
                clientResponse1 = prod;
            }
        }
        io:println("\nPOST request:");
        io:println(clientResponse1.getXmlPayload());
        xml responsepayload = check clientResponse1.getXmlPayload();
        json j2 = responsepayload.toJSON({attributePrefix: "", preserveNamespaces: false});
        j2 = j2["Envelope"]["Body"]["validateKeyResponse"]["return"];
        io:println(j2);
        return(j2);
        //return check clientResponse.getJsonPayload();

    } catch (error err) {
        io:println(err);
        return {};
    }
    return {};

}



