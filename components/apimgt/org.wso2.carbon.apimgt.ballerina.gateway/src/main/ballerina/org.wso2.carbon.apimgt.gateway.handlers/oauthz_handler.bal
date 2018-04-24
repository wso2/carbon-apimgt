
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
@Description {value:"Representation of AuthzHandler"}
@Field {value:"authzCache: authorization cache instance"}
public type OAuthAuthzHandler object {
    public {
        caching:Cache? authzCache;
    }
    new (authzCache) {
    }
    public function canHandle(http:Request req) returns (boolean);
    public function handle(string username, string serviceName, string resourceName, string method,
    string[] scopes) returns (boolean);
    function authorizeFromCache(string authzCacheKey) returns (boolean|());
    function cacheAuthzResult (string authzCacheKey, boolean isAuthorized);
};

@Description {value:"Performs a authorization check, by comparing the groups of the user and the groups of the scope"}
@Param {value:"username: user name"}
@Param {value:"serviceName: service name"}
@Param {value:"resourceName: resource name"}
@Param {value:"method: method names"}
@Param {value:"scopes: array of scope names"}
@Return {value:"boolean: true if authorization check is a success, else false"}
public function OAuthAuthzHandler::handle (string username, string serviceName, string resourceName, string method,
string[] scopes) returns (boolean) {
    // first, check in the cache. cache key is <username>-<resource>-<http method>,
    // since different resources can have different scopes
    string authzCacheKey = runtime:getInvocationContext().authenticationContext.username +
        "-" + serviceName +  "-" + resourceName + "-" + method;
    match self.authorizeFromCache(authzCacheKey) {
        boolean isAuthorized => {
            return isAuthorized;
        }
        () => {
            // if there are scopes set in the AuthenticationContext already from a previous authentication phase, try to
            // match against those.
            string[] authCtxtScopes = runtime:getInvocationContext().authenticationContext.scopes;
            if (lengthof authCtxtScopes > 0) {
                boolean authorized = matchScopes(scopes, authCtxtScopes);
                if (authorized) {
                    log:printInfo("Successfully authorized to access resource: " + resourceName + ", method: " +
                            method);
                } else {
                    log:printInfo("Authorization failure for resource: " + resourceName + ", method: " + method);
                }
                // cache authz result
                self.cacheAuthzResult(authzCacheKey, authorized);
                return authorized;
            } else {
                // no scopes found for user, authorization failure
                log:printDebug("No scopes found for user: " + username + " to access resource: " + resourceName + ",
                                                                                                    method:" + method);
                return false;
            }
        }
    }
}

@Description {value:"Retrieves the cached authorization result if any, for the given basic auth header value"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
@Return {value:"boolean|(): cached entry, or nill in a cache miss"}
function OAuthAuthzHandler::authorizeFromCache(string authzCacheKey) returns (boolean|()) {
    try {
        match self.authzCache {
            caching:Cache cache => {
                return check <boolean> cache.get(authzCacheKey);
            }
            () => {
                return ();
            }
        }
    } catch (error e) {
        // do nothing
    }
    return ();
}

@Description {value:"Caches the authorization result"}
@Param {value:"authzCacheKey: cache key - <username>-<resource>"}
@Param {value:"isAuthorized: authorization decision"}
function OAuthAuthzHandler::cacheAuthzResult (string authzCacheKey, boolean isAuthorized) {
    match self.authzCache {
        caching:Cache cache => {
            cache.put(authzCacheKey, isAuthorized);
        }
        () => {
            return;
        }
    }
}

@Description {value:"Matches the scopes"}
@Param {value:"scopesOfResource: array of scopes for the resource"}
@Param {value:"scopesForRequest: array of scopes relevant to this request"}
@Return {value:"boolean: true if two arrays have at least one match"}
function matchScopes (string[] scopesOfResource, string[] scopesForRequest) returns (boolean) {
    foreach scopeForRequest in scopesForRequest {
        foreach scopeOfResource in scopesOfResource {
            io:println("scope = " + scopeOfResource);
            if (scopeForRequest == scopeOfResource) {
                // if  that is equal to a group of a scope, authorization passes
                return true;
            }
        }
    }
    return false;
}

@Description {value:"Checks if the provided request can be authorized. This method will validate if the username is
already set in the authentication context. If not, the flow cannot continue."}
@Param {value:"req: Request object"}
@Return {value:"boolean: true if its possible authorize, else false"}
public function OAuthAuthzHandler::canHandle (http:Request req) returns (boolean) {
    if (runtime:getInvocationContext().authenticationContext.username.length() == 0) {
        log:printError("Username not set in auth context. Unable to authorize");
        return false;
    }
    return true;
}