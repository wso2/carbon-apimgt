import ballerina/http;
import ballerina/log;
import ballerina/auth;
import ballerina/config;
import ballerina/runtime;
import ballerina/time;
import ballerina/io;
import ballerina/reflect;
import org.wso2.carbon.apimgt.gateway.handlers as handler;
import org.wso2.carbon.apimgt.gateway.constants as constants;
import org.wso2.carbon.apimgt.gateway.utils as utils;
import org.wso2.carbon.apimgt.gateway.dto as dto;


// Authentication filter

@Description {value:"Representation of the Authentication filter"}
@Field {value:"filterRequest: request filter method which attempts to authenticated the request"}
@Field {value:"filterRequest: response filter method (not used this scenario)"}
public type OAuthnFilter object {

    public {
        handler:OAuthnHandler oauthnHandler;// Handles the oauth2 authentication;
        http:AuthnHandlerChain authnHandlerChain;
    }

    public new (oauthnHandler, authnHandlerChain) {}

    @Description {value:"filterRequest: Request filter function"}
    public function filterRequest (http:Request request, http:FilterContext context) returns http:FilterResult {
        // get auth config for this resource
        boolean authenticated;
        http:HttpServiceConfig httpServiceConfig =  utils:getServiceConfigAnnotation(reflect:getServiceAnnotations
            (context.serviceType));
        http:HttpResourceConfig httpResourceConfig =  utils:getResourceConfigAnnotation
        (reflect:getResourceAnnotations
            (context.serviceType, context
                .resourceName));
        dto:APIKeyValidationRequestDto apiKeyValidationRequestDto = utils:getKeyValidationRequestObject
        (httpServiceConfig,
            httpResourceConfig);
        var (isSecured, authProviders) = getResourceAuthConfig(context);
        dto:APIKeyValidationDto apiKeyValidationDto;
        boolean isAuthorized;
        if (isSecured) {
            // if auth providers are there, use those to authenticate
            match getAuthenticationProviderType(request.getHeader(constants:AUTH_HEADER)){
                string providerId => {
                    string[] providerIds = [providerId];
                    isAuthorized = self.authnHandlerChain.handleWithSpecificAuthnHandlers(authProviders, request);
                }
                () => {
                    apiKeyValidationDto = self.oauthnHandler.handle(request, apiKeyValidationRequestDto);
                    // set dto once ballerina supports
                    context.attributes[constants:KEY_VALIDATION_RESPONSE] = apiKeyValidationDto;
                    isAuthorized = <boolean>apiKeyValidationDto.authorized;
                }
            }

        } else {
            // not secured, no need to authenticate
            return createAuthnResult(true);
        }
        return createAuthnResult(isAuthorized);
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
    reflect:getResourceAnnotations(context.serviceType, context.resourceName));
    http:ListenerAuthConfig? serviceLevelAuthAnn = utils:getAuthAnnotation(constants:ANN_PACKAGE,
    constants:SERVICE_ANN_NAME,
    reflect:getServiceAnnotations(context.serviceType));
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

function getAuthenticationProviderType(string authHeader) returns (string|()) {
    if(authHeader.contains(constants:AUTH_SCHEME_BASIC)){
        return constants:AUTHN_SCHEME_BASIC;
    } else if (authHeader.contains(constants:AUTH_SCHEME_BEARER) && authHeader.contains(".")) {
        return constants:AUTH_SCHEME_JWT;
    } else {
        return ();
    }
}


