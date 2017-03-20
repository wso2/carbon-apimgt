package deployment.org.wso2.apim;
import ballerina.net.http;

@http:BasePath("/aaa1")
service aaa1_1489666767745 {

    @http:GET
    @http:Path("/*")
    resource get_star_ (message m) {
        http:ClientConnector productionEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        http:ClientConnector sandboxEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        message response;
        string endpointType;
        string productionType;


        endpointType = "production";
        productionType = "production";

        if (endpointType == productionType) {
            response = http:ClientConnector.execute(productionEndpoint, "get", "", m);
        } else {
            response = http:ClientConnector.execute(sandboxEndpoint, "get", "", m);
        }

        reply response;
    }
}