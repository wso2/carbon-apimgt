

import ballerina/http;
import ballerina/log;
import ballerina/auth;
import ballerina/config;
import ballerina/runtime;
import ballerina/time;
import ballerina/io;
import org.wso2.carbon.apimgt.gateway.handlers as handler;
import org.wso2.carbon.apimgt.gateway.filters as filter;
import org.wso2.carbon.apimgt.gateway.constants as constants;
import org.wso2.carbon.apimgt.gateway.utils as utils;


AuthProvider basicAuthProvider = {id: "oauth2", scheme:"oauth2", authProvider:"config"};
endpoint APIGatewayListener listener {
    port:9091
    //authProviders:[basicAuthProvider]
};


@http:ServiceConfig {
    basePath:"/echo",
    authConfig:{
        authProviders:["oauth2"],
        authentication:{enabled:true},
        scopes:["default"]
    }
}
service<http:Service> echo bind listener {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    echo (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> respond(res);
    }
}


@Description {value:"Representation of an API gateway listener"}
@Field {value:"config: EndpointConfiguration instance"}
@Field {value:"secureListener: Secure HTTP Listener instance"}
public type APIGatewayListener object {
    public {
        EndpointConfiguration config;
        http:Listener httpListener;
    }

    new () {
        httpListener = new;
    }

    public function init(EndpointConfiguration config);

    @Description {value:"Gets called when the endpoint is being initialize during package init time"}
    @Return {value:"Error occured during initialization"}
    public function initEndpoint() returns (error);

    @Description {value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
    @Param {value:"ep: The endpoint to which the service should be registered to"}
    @Param {value:"serviceType: The type of the service to be registered"}
    public function register(typedesc serviceType);

    @Description {value:"Starts the registered service"}
    public function start();

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    public function getCallerActions() returns (http:Connection);

    @Description {value:"Stops the registered service"}
    public function stop();
};

@Description {value:"Configuration for secure HTTP service endpoint"}
@Field {value:"host: Host of the service"}
@Field {value:"port: Port number of the service"}
@Field {value:"keepAlive: The keepAlive behaviour of the connection for a particular port"}
@Field {value:"transferEncoding: The types of encoding applied to the response"}
@Field {value:"chunking: The chunking behaviour of the response"}
@Field {value:"secureSocket: The SSL configurations for the service endpoint"}
@Field {value:"httpVersion: Highest HTTP version supported"}
@Field {value:"requestLimits: Request validation limits configuration"}
@Field {value:"filters: Filters to be applied to the request before dispatched to the actual resource"}
@Field {value:"authProviders: The array of AuthProviders which are used to authenticate the users"}
public type EndpointConfiguration {
    string host,
    int port =9090,
    http:KeepAlive keepAlive = "AUTO",
    http:TransferEncoding transferEncoding = http:TRANSFERENCODE_CHUNKING,
    http:ServiceSecureSocket? secureSocket,
    string httpVersion = "1.1",
    http:RequestLimits? requestLimits,
    http:Filter[] filters,
    AuthProvider[]? authProviders,
};

@Description {value:"Configuration for authentication providers"}
@Field {value:"scheme: Authentication schem"}
@Field {value:"id: Authention provider instance id"}
public type AuthProvider {
    string scheme,
    string id,
    string authProvider,
    string filePath,
    string issuer,
    string audience,
    http:TrustStore? trustStore,
    string certificateAlias,
    int timeSkew,
};

public function APIGatewayListener::init (EndpointConfiguration config) {
    addAuthFiltersForAPIGatewayListener(config);
    self.httpListener.init(config);
}

@Description {value:"Add authn and authz filters"}
@Param {value:"config: EndpointConfiguration instance"}
function addAuthFiltersForAPIGatewayListener (EndpointConfiguration config) {
    // add authentication and authorization filters as the first two filters.
    // if there are any other filters specified, those should be added after the authn and authz filters.
    if (config.filters == null) {
        // can add authn and authz filters directly
        config.filters = createAuthFiltersForSecureListener(config);
    } else {
        http:Filter[] newFilters = createAuthFiltersForSecureListener(config);
        // add existing filters next
        int i = 0;
        while (i < lengthof config.filters) {
            newFilters[i + (lengthof newFilters)] = config.filters[i];
            i = i + 1;
        }
        config.filters = newFilters;
    }
}

@Description {value:"Create an array of auth and authz filters"}
@Param {value:"config: EndpointConfiguration instance"}
@Return {value:"Array of Filters comprising of authn and authz Filters"}
function createAuthFiltersForSecureListener (EndpointConfiguration config) returns (http:Filter[]) {
    // parse and create authentication handlers
    //http:AuthHandlerRegistry registry;
    //match config.authProviders {
    //    AuthProvider[] providers => {
    //        int i = 1;
    //        foreach provider in providers {
    //            if (lengthof provider.id > 0) {
    //                registry.add(provider.id, createAuthHandler(provider));
    //            } else {
    //                registry.add(provider.scheme + "-" + i, createAuthHandler(provider));
    //            }
    //            i++;
    //        }
    //    }
    //    () => {
    //        // if no auth providers are specified, add basic authn handler with config based auth provider
    //        log:printInfo("No Authenticator found");
    //    }
    //
    //}
    http:Filter[] authFilters = [];
    //http:AuthnHandlerChain authnHandlerChain = new(registry);
    //http:AuthnFilter authnFilter = new(authnHandlerChain);
    handler:OAuthnHandler oauthnHandler = new;
    filter:OAuthnFilter authnFilter = new(oauthnHandler);

    filter:OAuthzFilter authzFilter = new;
    authFilters[0] = <http:Filter> authnFilter;
    authFilters[1] = <http:Filter> authzFilter;
    return authFilters;
}

function createAuthHandler (AuthProvider authProvider) returns http:HttpAuthnHandler {
    if (authProvider.scheme == constants:AUTHN_SCHEME_BASIC) {
        auth:AuthProvider authProvider1;
        if (authProvider.authProvider == constants:AUTH_PROVIDER_CONFIG) {
            auth:ConfigAuthProvider configAuthProvider = new;
            authProvider1 = <auth:AuthProvider> configAuthProvider;
        } else {
            // other auth providers are unsupported yet
            error e = {message:"Invalid auth provider: " + authProvider.authProvider };
            throw e;
        }
        http:HttpBasicAuthnHandler basicAuthHandler = new(authProvider1);
        return <http:HttpAuthnHandler> basicAuthHandler;
    } else if(authProvider.scheme == constants:AUTH_SCHEME_JWT){
        auth:JWTAuthProviderConfig jwtConfig = {};
        jwtConfig.issuer = authProvider.issuer;
        jwtConfig.audience = authProvider.audience;
        jwtConfig.certificateAlias = authProvider.certificateAlias;
        jwtConfig.trustStoreFilePath = authProvider.trustStore.path but {() => ""};
        jwtConfig.trustStorePassword = authProvider.trustStore.password but {() => ""};
        auth:JWTAuthProvider jwtAuthProvider = new (jwtConfig);
        http:HttpJwtAuthnHandler jwtAuthnHandler = new(jwtAuthProvider);
        return <http:HttpAuthnHandler> jwtAuthnHandler;
    } else if (authProvider.scheme == constants:AUTH_SCHEME_OAUTH2){
        io:println("Added to registry");
        handler:OAuthnHandler oAuthnHandler = new;
        return  <http:HttpAuthnHandler> oAuthnHandler;
    } else {
        // TODO: create other HttpAuthnHandlers
        error e = {message:"Invalid auth scheme: " + authProvider.scheme };
        throw e;
    }
}

@Description {value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
@Param {value:"ep: The endpoint to which the service should be registered to"}
@Param {value:"serviceType: The type of the service to be registered"}
public function APIGatewayListener::register (typedesc serviceType) {
    self.httpListener.register(serviceType);
}

@Description {value:"Gets called when the endpoint is being initialize during package init time"}
@Return {value:"Error occured during initialization"}
public function APIGatewayListener::initEndpoint() returns (error) {
    return self.httpListener.initEndpoint();
}

@Description {value:"Starts the registered service"}
public function APIGatewayListener::start () {
    self.httpListener.start();
}

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
public function APIGatewayListener::getCallerActions () returns (http:Connection) {
    return self.httpListener.getCallerActions();
}

@Description {value:"Stops the registered service"}
public function APIGatewayListener::stop () {
    self.httpListener.stop();
}






