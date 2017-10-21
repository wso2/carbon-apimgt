package org.wso2.carbon.apimgt.gateway.handlers.security.keys;


import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;

public class APIKeyValidatorClientWrapper extends APIKeyValidatorClient {

    public APIKeyValidatorClientWrapper() throws APISecurityException {
    }

    @Override
    protected String getAxis2ClientXmlLocation() {
        return "src/test/resources/axis2_client.xml";
    }

    @Override
    protected String getClientRepoLocation() {
        return "src/test/resources/client";
    }
}
