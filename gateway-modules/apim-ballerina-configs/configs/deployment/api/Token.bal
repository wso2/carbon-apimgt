package deployment.api;

import ballerina.net.http;
import org.wso2.carbon.apimgt.gateway.holders;
import org.wso2.carbon.apimgt.gateway.dto;
@http:BasePath {value:"/token"}
service Service1 {
    dto:KeyManagerInfoDTO keyManagerConf = holders:keyManagerConf;
    http:ClientConnector client = create http:ClientConnector(keyManagerConf:tokenEndpoint);

    @http:POST {}
    @http:Path {value:"/"}
    resource Resource1 (message m) {
        message response = http:ClientConnector.post (client, "/", m);
        reply response;
    }
}