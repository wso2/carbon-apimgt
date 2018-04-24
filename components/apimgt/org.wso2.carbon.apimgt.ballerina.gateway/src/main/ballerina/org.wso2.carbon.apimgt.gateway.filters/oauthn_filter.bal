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


// Authentication filter



@Description {value:"Representation of the Authentication filter"}
@Field {value:"filterRequest: request filter method which attempts to authenticated the request"}
@Field {value:"filterRequest: response filter method (not used this scenario)"}
public type OAuthnFilter object {

    public {
        handler:OAuthnHandler oauthnHandler;
    }

    new (oauthnHandler) {}

    @Description {value:"filterRequest: Request filter function"}
    public function filterRequest (http:Request request, http:FilterContext context) returns http:FilterResult {
        // get auth config for this resource
        boolean authenticated;
        var (isSecured, authProviders) = getResourceAuthConfig(context);
        if (isSecured) {
            // if auth providers are there, use those to authenticate
            authenticated = self.oauthnHandler.handle(request);
        } else {
            // not secured, no need to authenticate
            return createAuthnResult(true);
        }
        return createAuthnResult(authenticated);
    }
};

@Description {value:"Creates an instance of FilterResult"}
@Param {value:"authorized: authorization status for the request"}
@Return {value:"FilterResult: Authorization result to indicate if the request can proceed or not"}
function createAuthnResult (boolean authenticated) returns (http:FilterResult) {
    http:FilterResult requestFilterResult = {};
    if (authenticated) {
        requestFilterResult = {canProceed:true, statusCode:200, message:"Successfully authenticated"};
    } else {
        requestFilterResult = {canProceed:false, statusCode:401, message:"Authentication failure"};
    }
    return requestFilterResult;
}

@Description {value:"Checks if the resource is secured"}
@Param {value:"context: FilterContext object"}
@Return {value:"boolean, string[]: tuple of whether the resource is secured and the list of auth provider ids "}
function getResourceAuthConfig (http:FilterContext context) returns (boolean, string[]) {
    boolean resourceSecured;
    string[] authProviderIds = [];
    // get authn details from the resource level
    http:ListenerAuthConfig? resourceLevelAuthAnn = utils:getAuthAnnotation(constants:ANN_PACKAGE,
    constants:RESOURCE_ANN_NAME,
        internal:getResourceAnnotations(context.serviceType, context.resourceName));
    http:ListenerAuthConfig? serviceLevelAuthAnn = utils:getAuthAnnotation(constants:ANN_PACKAGE,
    constants:SERVICE_ANN_NAME,
        internal:getServiceAnnotations(context.serviceType));
    // check if authentication is enabled
    resourceSecured = utils:isResourceSecured(resourceLevelAuthAnn, serviceLevelAuthAnn);
    // if resource is not secured, no need to check further
    if (!resourceSecured) {
        return (resourceSecured, authProviderIds);
    }
    // check if auth providers are given at resource level
    match resourceLevelAuthAnn.authProviders {
        string[] providers => {
            authProviderIds = providers;
        }
        () => {
            // no auth providers found in resource level, try in service level
            match serviceLevelAuthAnn.authProviders {
                string[] providers => {
                    authProviderIds = providers;
                }
                () => {
                    // no auth providers found
                }
            }
        }
    }
    return (resourceSecured, authProviderIds);
}


