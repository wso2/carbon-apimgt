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
import org.wso2.carbon.apimgt.gateway.handlers as handler;
import org.wso2.carbon.apimgt.gateway.constants as constants;
import org.wso2.carbon.apimgt.gateway.utils as utils;

// authorization filter
caching:Cache authzCache = new(expiryTimeMillis = 300000);
@Description {value:"Authz handler instance"}
public handler:OAuthAuthzHandler authzHandler = new(authzCache);

@Description {value:"Representation of the Authorization filter"}
@Field {value:"filterRequest: request filter method which attempts to authorize the request"}
@Field {value:"filterRequest: response filter method (not used this scenario)"}
public type OAuthzFilter object {

    @Description {value:"Filter function implementation which tries to authorize the request"}
    @Param {value:"request: Request instance"}
    @Param {value:"context: FilterContext instance"}
    @Return {value:"FilterResult: Authorization result to indicate if the request can proceed or not"}
    public function filterRequest (http:Request request, http:FilterContext context) returns http:FilterResult {
        // first check if the resource is marked to be authenticated. If not, no need to authorize.
        http:ListenerAuthConfig? resourceLevelAuthAnn = utils:getAuthAnnotation(constants:ANN_PACKAGE,
        constants:RESOURCE_ANN_NAME,
            internal:getResourceAnnotations(context.serviceType, context.resourceName));
        http:ListenerAuthConfig? serviceLevelAuthAnn = utils:getAuthAnnotation(constants:ANN_PACKAGE,
        constants:SERVICE_ANN_NAME,
            internal:getServiceAnnotations(context.serviceType));
        if (!utils:isResourceSecured(resourceLevelAuthAnn, serviceLevelAuthAnn)) {
            // not secured, no need to authorize
            return createAuthzResult(true);
        }

        string[]? scopes = getScopesForResource(resourceLevelAuthAnn, serviceLevelAuthAnn);
        boolean authorized;
        match scopes {
            string[] scopeNames => {
                if (authzHandler.canHandle(request)) {
                    authorized = authzHandler.handle(runtime:getInvocationContext().authenticationContext.username,
                        context.serviceName, context.resourceName, request.method, scopeNames);
                } else {
                    authorized = false;
                }
            }
            () => {
                // scopes are not defined, no need to authorize
                authorized = true;
            }
        }
        return createAuthzResult(authorized);
    }
};

@Description {value:"Creates an instance of FilterResult"}
@Param {value:"authorized: authorization status for the request"}
@Return {value:"FilterResult: Authorization result to indicate if the request can proceed or not"}
function createAuthzResult (boolean authorized) returns (http:FilterResult) {
    http:FilterResult requestFilterResult = {};
    if (authorized) {
        requestFilterResult = {canProceed:true, statusCode:200, message:"Successfully authorized"};
    } else {
        requestFilterResult = {canProceed:false, statusCode:403, message:"Authorization failure"};
    }
    return requestFilterResult;
}

@Description {value:"Retrieves the scope for the resource, if any"}
@Param {value:"resourceLevelAuthAnn: Resource level auth annotation"}
@Param {value:"serviceLevelAuthAnn: service level auth annotation"}
@Return {value:"string: Scope name if defined, else nil"}
function getScopesForResource (http:ListenerAuthConfig? resourceLevelAuthAnn, http:ListenerAuthConfig?
serviceLevelAuthAnn)
    returns (string[]|()) {
    match resourceLevelAuthAnn.scopes {
        string[] scopes => {
            return scopes;
        }
        () => {
            match serviceLevelAuthAnn.scopes {
                string[] scopes => {
                    return scopes;
                }
                () => {
                    return ();
                }
            }
        }
    }
}

