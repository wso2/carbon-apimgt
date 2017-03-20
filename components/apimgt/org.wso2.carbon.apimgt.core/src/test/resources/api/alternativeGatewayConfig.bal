package deployment.org.wso2.apim;
import ballerina.net.http;

@http:BasePath("/aaa1")
service aaa1_1489666767745 {


    @http:HEAD
        	@http:Path("/*")
    resource head_star_ (message m) {
        http:ClientConnector productionEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        http:ClientConnector sandboxEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        message response;
        string endpointType;
        string productionType;


        endpointType = "production";
        productionType = "production";

        if (endpointType == productionType) {
            response = http:ClientConnector.execute(productionEndpoint, "head", "", m);
        } else {
            response = http:ClientConnector.execute(sandboxEndpoint, "head", "", m);
        }

        reply response;
    }


    @http:POST
        	@http:Path("/*")
    resource post_star_ (message m) {
        http:ClientConnector productionEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        http:ClientConnector sandboxEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        message response;
        string endpointType;
        string productionType;


        endpointType = "production";
        productionType = "production";

        if (endpointType == productionType) {
            response = http:ClientConnector.execute(productionEndpoint, "post", "", m);
        } else {
            response = http:ClientConnector.execute(sandboxEndpoint, "post", "", m);
        }

        reply response;
    }


    @http:PATCH
        	@http:Path("/*")
    resource patch_star_ (message m) {
        http:ClientConnector productionEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        http:ClientConnector sandboxEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        message response;
        string endpointType;
        string productionType;


        endpointType = "production";
        productionType = "production";

        if (endpointType == productionType) {
            response = http:ClientConnector.execute(productionEndpoint, "patch", "", m);
        } else {
            response = http:ClientConnector.execute(sandboxEndpoint, "patch", "", m);
        }

        reply response;
    }


    @http:DELETE
        	@http:Path("/*")
    resource delete_star_ (message m) {
        http:ClientConnector productionEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        http:ClientConnector sandboxEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        message response;
        string endpointType;
        string productionType;


        endpointType = "production";
        productionType = "production";

        if (endpointType == productionType) {
            response = http:ClientConnector.execute(productionEndpoint, "delete", "", m);
        } else {
            response = http:ClientConnector.execute(sandboxEndpoint, "delete", "", m);
        }

        reply response;
    }


    @http:PUT
        	@http:Path("/*")
    resource put_star_ (message m) {
        http:ClientConnector productionEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        http:ClientConnector sandboxEndpoint = create http:ClientConnector(getUrlFromKey("aaa1_1.0.0__ep"));
        message response;
        string endpointType;
        string productionType;


        endpointType = "production";
        productionType = "production";

        if (endpointType == productionType) {
            response = http:ClientConnector.execute(productionEndpoint, "put", "", m);
        } else {
            response = http:ClientConnector.execute(sandboxEndpoint, "put", "", m);
        }

        reply response;
    }


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