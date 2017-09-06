package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

@RunWith(PowerMockRunner.class)
public class SdkGenLanguagesApiServiceImplTestCase {
    private final static Logger logger = LoggerFactory.getLogger(SdkGenLanguagesApiServiceImplTestCase.class);
    private static final String USER = "admin";
    @Test
    public void sdkGenLanguagesGet() throws APIManagementException, NotFoundException {
        SdkGenLanguagesApiServiceImpl sdkGenLanguagesApiService = new SdkGenLanguagesApiServiceImpl();
        Request request = getRequest();
        Response response = sdkGenLanguagesApiService.sdkGenLanguagesGet(request);

        Assert.assertEquals(200, response.getStatus());
    }

    // Sample request to be used by tests
    private Request getRequest() throws APIMgtSecurityException {
        CarbonMessage carbonMessage = new HTTPCarbonMessage();
        carbonMessage.setProperty("LOGGED_IN_USER", USER);
        Request request = new Request(carbonMessage);
        return request;
    }
}

