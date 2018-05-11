
import ballerina/http;
import ballerina/log;
import ballerina/auth;
import ballerina/cache;
import ballerina/config;
import ballerina/runtime;
import ballerina/time;
import ballerina/io;

// Authentication handler

@Description {value:"Representation of OAuth2 Auth handler for HTTP traffic"}
@Field {value:"oAuthAuthenticator: OAuthAuthProvider instance"}
@Field {value:"name: Authentication handler name"}
public type OAuthnHandler object {
    public {
        string name= "oauth2";
        OAuthAuthProvider oAuthAuthenticator = new;
    }

    public function canHandle (http:Request req) returns (boolean);
    public function handle (http:Request req, APIKeyValidationRequestDto apiKeyValidationRequestDto)
    returns (APIKeyValidationDto| error);

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
                                   returns (APIKeyValidationDto| error) {
    APIKeyValidationDto apiKeyValidationDto;
    try {
        apiKeyValidationDto = self.oAuthAuthenticator.authenticate(apiKeyValidationRequestDto);
    } catch (error err) {
        log:printError("Error while getting key validation info for access token" +
                apiKeyValidationRequestDto.accessToken, err = err);
        return err;
    }
    return apiKeyValidationDto;
}



function  getAccessTokenCacheKey(APIKeyValidationRequestDto dto) returns string {
    return dto.accessToken + ":" + dto.context + "/" + dto.apiVersion + dto.matchingResource + ":" + dto.httpVerb;
}



@Description {value:"Represents a OAuth2 Authenticator"}
@Field {value:"gatewayTokenCache: Authentication cache object"}
public type OAuthAuthProvider object {
    public {
        APIGatewayCache gatewayTokenCache;
    }


    public function authenticate (APIKeyValidationRequestDto apiKeyValidationRequestDto) returns (APIKeyValidationDto);

    public function doIntrospect (APIKeyValidationRequestDto apiKeyValidationRequestDto) returns (json);

};


@Description {value:"Authenticate with a oauth2 token"}
@Param {value:"apiKeyValidationRequestDto: Object containig data to call the key validation service"}
@Return {value:"boolean: true if authentication is a success, else false"}
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
            self.gatewayTokenCache.addToInvalidTokenCache(apiKeyValidationRequestDto.accessToken, true);
            apiKeyValidationDtoFromcache.authorized= "false";
            return apiKeyValidationDtoFromcache;
        }
        authorized = < boolean > apiKeyValidationDtoFromcache.authorized;
        apiKeyValidationDto = apiKeyValidationDtoFromcache;
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
    endpoint http:Client keyValidationEndpoint {
        url:getGatewayConfInstance().getKeyManagerConf().serverUrl
    };
    try {
        string base64Header = "admin:admin";
        string encodedBasicAuthHeader = check base64Header.base64Encode();

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
        var result1 = keyValidationEndpoint -> post("/services/APIKeyValidationService", request=clientRequest1);

        match result1 {
            error err => {
                io:println("Error occurred while reading locator response",err);
            }
            http:Response prod => {
                clientResponse1 = prod;
            }
        }
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
        return(j2);

    } catch (error err) {
        io:println(err);
        return {};
    }
    return {};

}



