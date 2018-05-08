import ballerina/http;
import ballerina/log;
import ballerina/auth;
import ballerina/config;
import ballerina/runtime;
import ballerina/time;
import ballerina/io;
import ballerina/reflect;

public function isResourceSecured(http:ListenerAuthConfig? resourceLevelAuthAnn, http:ListenerAuthConfig?
    serviceLevelAuthAnn)
                    returns boolean {
    boolean isSecured;
    match resourceLevelAuthAnn.authentication {
        http:Authentication authn => {
            isSecured = authn.enabled;
        }
        () => {
            // if not found at resource level, check in the service level
            match serviceLevelAuthAnn.authentication {
                http:Authentication authn => {
                    isSecured = authn.enabled;
                }
                () => {
                    // if still authentication annotation is nil, means the user has not specified that the service
                    // should be secured. However since the authn filter has been engaged, need to authenticate.
                    isSecured = true;
                }
            }
        }
    }
    return isSecured;
}

@Description { value: "Tries to retrieve the annotation value for authentication hierarchically - first from the resource
level
and then from the service level, if its not there in the resource level" }
@Param { value: "annotationPackage: annotation package name" }
@Param { value: "annotationName: annotation name" }
@Param { value: "annData: array of annotationData instances" }
@Return { value: "ListenerAuthConfig: ListenerAuthConfig instance if its defined, else nil" }
public function getAuthAnnotation(string annotationPackage, string annotationName, reflect:annotationData[] annData)
                    returns (http:ListenerAuthConfig?) {
    if (lengthof annData == 0) {
        return ();
    }
    reflect:annotationData|() authAnn;
    foreach ann in annData {
        if (ann.name == annotationName && ann.pkgName == annotationPackage) {
            authAnn = ann;
            break;
        }
    }
    match authAnn {
        reflect:annotationData annData1 => {
            if (annotationName == RESOURCE_ANN_NAME) {
                http:HttpResourceConfig resourceConfig = check <http:HttpResourceConfig>annData1.value;
                return resourceConfig.authConfig;
            } else if (annotationName == SERVICE_ANN_NAME) {
                http:HttpServiceConfig serviceConfig = check <http:HttpServiceConfig>annData1.value;
                return serviceConfig.authConfig;
            } else {
                return ();
            }
        }
        () => {
            return ();
        }
    }
}


@Description { value: "Retrieve the annotation related to resources" }
@Return { value: "HttpResourceConfig: HttpResourceConfig instance if its defined, else nil" }
public function getResourceConfigAnnotation(reflect:annotationData[] annData)
                    returns (http:HttpResourceConfig) {
    if (lengthof annData == 0) {
        return {};
    }
    reflect:annotationData|() authAnn;
    foreach ann in annData {
        if (ann.name == RESOURCE_ANN_NAME && ann.pkgName == ANN_PACKAGE) {
            authAnn = ann;
            break;
        }
    }
    match authAnn {
        reflect:annotationData annData1 => {
            http:HttpResourceConfig resourceConfig = check <http:HttpResourceConfig>annData1.value;
            return resourceConfig;
        }
        () => {
            return {};
        }
    }
}

@Description { value: "Retrieve the annotation related to service" }
@Return { value: "HttpServiceConfig: HttpResourceConfig instance if its defined, else nil" }
public function getServiceConfigAnnotation(reflect:annotationData[] annData)
                    returns (http:HttpServiceConfig) {
    if (lengthof annData == 0) {
        return {};
    }
    reflect:annotationData|() authAnn;
    foreach ann in annData {
        if (ann.name == SERVICE_ANN_NAME && ann.pkgName == ANN_PACKAGE) {
            authAnn = ann;
            break;
        }
    }
    match authAnn {
        reflect:annotationData annData1 => {
            http:HttpServiceConfig serviceConfig = check <http:HttpServiceConfig>annData1.value;
            return serviceConfig;
        }
        () => {
            return {};
        }
    }
}

@Description { value: "Retrieve the key validation request dto from filter context" }
@Return { value: "api key validation request dto" }
public function getKeyValidationRequestObject(http:FilterContext context) returns APIKeyValidationRequestDto {
    APIKeyValidationRequestDto apiKeyValidationRequest = {};
    http:HttpServiceConfig httpServiceConfig = getServiceConfigAnnotation(reflect:getServiceAnnotations
        (context.serviceType));
    http:HttpResourceConfig httpResourceConfig = getResourceConfigAnnotation
    (reflect:getResourceAnnotations(context.serviceType, context.resourceName));
    apiKeyValidationRequest.context = httpServiceConfig.basePath;
    apiKeyValidationRequest.apiVersion = getVersionFromServiceAnnotation(reflect:getServiceAnnotations
        (context.serviceType)).apiVersion;
    // TODO set correct version
    apiKeyValidationRequest.requiredAuthenticationLevel = "Any";
    apiKeyValidationRequest.clientDomain = "*";
    apiKeyValidationRequest.matchingResource = httpResourceConfig.path;
    apiKeyValidationRequest.httpVerb = httpResourceConfig.methods[0];
    // TODO get correct verb
    return apiKeyValidationRequest;

}

public function getVersionFromServiceAnnotation(reflect:annotationData[] annData) returns VersionConfiguration {
    if (lengthof annData == 0) {
        return {};
    }
    reflect:annotationData|() versionAnn;
    foreach ann in annData {
        if (ann.name == VERSION_ANN_NAME && ann.pkgName == GATEWAY_ANN_PACKAGE) {
            versionAnn = ann;
            break;
        }
    }
    match versionAnn {
        reflect:annotationData annData1 => {
            VersionConfiguration versionConfig = check <VersionConfiguration>annData1.value;
            return versionConfig;
        }
        () => {
            return {};
        }
    }
}

public function getTenantFromBasePath(string basePath) returns string {
    string[] splittedArray = basePath.split("/");
    return splittedArray[lengthof splittedArray - 1];
}


public function isAccessTokenExpired(APIKeyValidationDto apiKeyValidationDto) returns boolean {
    int validityPeriod = check <int>apiKeyValidationDto.validityPeriod;
    int issuedTime = check <int>apiKeyValidationDto.issuedTime;
    int timestampSkew = 5000;
    // TODO : make this configurable;
    int currentTime = time:currentTime().time;
    int intMaxValue = 9223372036854775807;
    if (validityPeriod != intMaxValue &&
            // For cases where validityPeriod is closer to int.MAX_VALUE (then issuedTime + validityPeriod would spill
            // over and would produce a negative value)
            (currentTime - timestampSkew) > validityPeriod) {
        if ((currentTime - timestampSkew) > (issuedTime + validityPeriod)) {
            apiKeyValidationDto.validationStatus = API_AUTH_INVALID_CREDENTIALS;
            return true;
        }
    }
    return false;
}
public function getContext(http:FilterContext context) returns (string) {
    http:HttpServiceConfig httpServiceConfig = getServiceConfigAnnotation(reflect:getServiceAnnotations
        (context.serviceType));
    return httpServiceConfig.basePath;

}

