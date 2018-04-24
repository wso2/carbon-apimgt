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

public function isResourceSecured (http:ListenerAuthConfig? resourceLevelAuthAnn, http:ListenerAuthConfig?
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

@Description {value:"Tries to retrieve the annotation value for authentication hierarchically - first from the resource
level
and then from the service level, if its not there in the resource level"}
@Param {value:"annotationPackage: annotation package name"}
@Param {value:"annotationName: annotation name"}
@Param {value:"annData: array of annotationData instances"}
@Return {value:"ListenerAuthConfig: ListenerAuthConfig instance if its defined, else nil"}
public function getAuthAnnotation (string annotationPackage, string annotationName, internal:annotationData[] annData)
    returns (http:ListenerAuthConfig?) {
    if (lengthof annData == 0) {
        return ();
    }
    internal:annotationData|() authAnn;
    foreach ann in annData {
        if (ann.name == annotationName && ann.pkgName == annotationPackage) {
            authAnn = ann;
            break;
        }
    }
    match authAnn {
        internal:annotationData annData1 => {
            if (annotationName == constants:RESOURCE_ANN_NAME) {
                http:HttpResourceConfig resourceConfig = check <http:HttpResourceConfig>annData1.value;
                return resourceConfig.authConfig;
            } else if (annotationName == constants:SERVICE_ANN_NAME) {
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

