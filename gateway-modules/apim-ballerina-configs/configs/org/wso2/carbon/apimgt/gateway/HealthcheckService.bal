package org.wso2.carbon.apimgt.gateway;
import ballerina.net.http;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;
@http:BasePath{value: "/healthCheck"}
service Service1 {
    @http:GET{}
    @http:Path{value: "/"}
    resource Resource1( message m) {
        gatewayUtil:retrieveSubscriptions("/api1","1.0.0");
        gatewayUtil:retrieveResources("/api1","1.0.0");
        reply m;
    }
}

