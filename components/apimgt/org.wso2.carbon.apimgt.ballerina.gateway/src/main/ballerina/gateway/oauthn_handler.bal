
import ballerina/http;
import ballerina/log;
import ballerina/auth;
import ballerina/cache;
import ballerina/config;
import ballerina/runtime;
import ballerina/time;
import ballerina/io;

// Authentication handler

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
    public function handle (http:Request req, APIKeyValidationRequestDto apiKeyValidationRequestDto)
    returns (APIKeyValidationDto);

};

@Description {value:"Intercepts a HTTP request for authentication"}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if authentication is a success, else false"}
public function OAuthnHandler::canHandle (http:Request req) returns (boolean) {
    string authHeader;
    try {
        authHeader = req.getHeader(AUTH_HEADER);
        io:println("auth header" + authHeader);
    } catch (error e) {
        log:printDebug("Error in retrieving header " + AUTH_HEADER + ": " + e.message);
        return false;
    }
    if (authHeader != null && authHeader.hasPrefix(AUTH_SCHEME_BEARER)) {
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
public function OAuthnHandler::handle (http:Request req, APIKeyValidationRequestDto apiKeyValidationRequestDto)
                                   returns (APIKeyValidationDto) {
    APIKeyValidationDto apiKeyValidationDto;
    string accessToken = extractAccessToken(req);
    try {
        apiKeyValidationRequestDto.accessToken = accessToken;
        io:println("token " + accessToken);
        apiKeyValidationDto = self.oAuthAuthenticator.authenticate(apiKeyValidationRequestDto);
        io:println("isAuthenticated " + apiKeyValidationDto.authorized);
    } catch (error err) {
        io:println("Error while getting key validation info for access token" + accessToken);
        io:print(error);
    }
    return apiKeyValidationDto;
}


function extractAccessToken (http:Request req) returns (string) {
    string authHeader = req.getHeader(AUTH_HEADER);
    string[] authHeaderComponents = authHeader.split(" ");
    return authHeaderComponents[1];
}

function  getAccessTokenCacheKey(APIKeyValidationRequestDto dto) returns string {
    return dto.accessToken + ":" + dto.context + "/" + dto.apiVersion + dto.matchingResource + ":" + dto.httpVerb;
}



@Description {value:"Represents a JWT Authenticator"}
@Field {value:"jwtAuthProviderConfig: JWTAuthProviderConfig object"}
@Field {value:"authCache: Authentication cache object"}
public type OAuthAuthProvider object {
    public {
        JWTAuthProviderConfig jwtAuthProviderConfig;
        APIGatewayCache gatewayTokenCache;
    }


    public function authenticate (APIKeyValidationRequestDto apiKeyValidationRequestDto) returns (APIKeyValidationDto);

    public function doIntrospect (APIKeyValidationRequestDto apiKeyValidationRequestDto) returns (json);

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
public function OAuthAuthProvider::authenticate (APIKeyValidationRequestDto apiKeyValidationRequestDto) returns
                                                                                                              (APIKeyValidationDto) {
    string cacheKey = getAccessTokenCacheKey(apiKeyValidationRequestDto);
    io:println("cacheKey " + cacheKey);
    boolean authorized;
    APIKeyValidationDto apiKeyValidationDto;
    match self.gatewayTokenCache.authenticateFromGatewayKeyValidationCache(cacheKey) {
        APIKeyValidationDto apiKeyValidationDtoFromcache => {
        if(isAccessTokenExpired(apiKeyValidationDtoFromcache)) {
            self.gatewayTokenCache.removeFromGatewayKeyValidationCache(cacheKey);
            io:println("Token expired, removed from the cache");
            self.gatewayTokenCache.addToInvalidTokenCache(apiKeyValidationRequestDto.accessToken, true);
            apiKeyValidationDtoFromcache.authorized= "false";
            return apiKeyValidationDtoFromcache;
        }
        authorized = < boolean > apiKeyValidationDtoFromcache.authorized;
        apiKeyValidationDto = apiKeyValidationDtoFromcache;
            io:println("value returned from cache");
        }
        () => {
            match self.gatewayTokenCache.retrieveFromInvalidTokenCache(apiKeyValidationRequestDto.accessToken) {
                boolean cacheAuthorizedValue => {
                    APIKeyValidationDto apiKeyValidationInfoDTO = { authorized: "false", validationStatus:API_AUTH_INVALID_CREDENTIALS };
                    return apiKeyValidationInfoDTO;
                }
                () => {
                    json keyValidationInfoJson = self.doIntrospect(apiKeyValidationRequestDto);
                    match <string>keyValidationInfoJson.authorized {
                        string authorizeValue => {
                            boolean auth = <boolean>authorizeValue;
                            if (auth) {
                                match <APIKeyValidationDto>keyValidationInfoJson {
                                    APIKeyValidationDto dto => {
                                        apiKeyValidationDto = dto;
                                    }
                                    error err => {
                                        io:println(err);
                                        throw err;
                                    }
                                }
                                io:println(apiKeyValidationDto);
                                authorized = auth;
                                self.gatewayTokenCache.addToGatewayKeyValidationCache(cacheKey, apiKeyValidationDto);
                                io:println("value not returned from cache");
                            } else {
                                self.gatewayTokenCache.addToInvalidTokenCache(apiKeyValidationRequestDto.accessToken, true);
                                apiKeyValidationDto.authorized="false";
                                apiKeyValidationDto.validationStatus = check <string>keyValidationInfoJson
                                    .validationStatus;
                            }
                        }
                        error err => {
                            io:println(err);
                            throw err;
                        }
                    }
                }
            }

        }
    }
    if (authorized) {
        // set username
        runtime:getInvocationContext().userPrincipal.username = apiKeyValidationDto.endUserName;
        string[] scopes = apiKeyValidationDto.scopes;//.split(",");
        if (lengthof scopes > 0) {
            runtime:getInvocationContext().userPrincipal.scopes = scopes;
        }
        // read scopes and set to the invocation context

    }
    return apiKeyValidationDto;
}


public function OAuthAuthProvider::doIntrospect (APIKeyValidationRequestDto apiKeyValidationRequestDto) returns (json) {
    try {
        string base64Header = "admin:admin";
        string encodedBasicAuthHeader = check base64Header.base64Encode();
        io:println(encodedBasicAuthHeader);

        // Intorospect related changes
        //http:Request clientRequest = new;
        //
        //http:Response clientResponse = new;
        //
        //clientRequest.setTextPayload("token=" + authToken, contentType=X_WWW_FORM_URLENCODED);
        //clientRequest.setHeader(CONTENT_TYPE_HEADER, X_WWW_FORM_URLENCODED);
        //clientRequest.setHeader(AUTHORIZATION_HEADER, BASIC_PREFIX_WITH_SPACE +
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
            <xsd:context>" + apiKeyValidationRequestDto.context + "</xsd:context>
            <!--Optional:-->
            <xsd:version>" + apiKeyValidationRequestDto.apiVersion + "</xsd:version>
            <!--Optional:-->
            <xsd:accessToken>" + apiKeyValidationRequestDto.accessToken + "</xsd:accessToken>
            <!--Optional:-->
            <xsd:requiredAuthenticationLevel>" + apiKeyValidationRequestDto.requiredAuthenticationLevel + "</xsd:requiredAuthenticationLevel>
            <!--Optional:-->
            <xsd:clientDomain>" + apiKeyValidationRequestDto.clientDomain + "</xsd:clientDomain>
            <!--Optional:-->
            <xsd:matchingResource>" + apiKeyValidationRequestDto.matchingResource + "</xsd:matchingResource>
            <!--Optional:-->
            <xsd:httpVerb>" + apiKeyValidationRequestDto.httpVerb + "</xsd:httpVerb>
            </xsd:validateKey>
            </soapenv:Body>
            </soapenv:Envelope>";

        clientRequest1.setTextPayload(xmlString,contentType="text/xml");
        clientRequest1.setHeader(CONTENT_TYPE_HEADER, "text/xml");
        clientRequest1.setHeader(AUTHORIZATION_HEADER, BASIC_PREFIX_WITH_SPACE +
                encodedBasicAuthHeader);
        clientRequest1.setHeader("SOAPAction", "urn:validateKey");
        //var result = introspectEndpoint -> post("/api/identity/oauth2/introspect/v1.0/introspect", clientRequest);
        var result1 = keyValidationEndpoint -> post("/services/APIKeyValidationService", request=clientRequest1);

        match result1 {
            error err => {
                io:println("Error occurred while reading locator response",err);
            }
            http:Response prod => {
                clientResponse1 = prod;
            }
        }
        io:println("\nPOST request:");
        io:println(clientResponse1.getXmlPayload());
        xml responsepayload;
        match clientResponse1.getXmlPayload() {
            error err => {
                io:println("Error occurred while getting key validation service response ",err);
                return {};
            }
            xml responseXml => {
                responsepayload = responseXml;
            }
        }
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



