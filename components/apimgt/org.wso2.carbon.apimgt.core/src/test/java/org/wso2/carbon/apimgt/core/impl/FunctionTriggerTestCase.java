/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.RestCallUtil;
import org.wso2.carbon.apimgt.core.dao.FunctionDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Function;
import org.wso2.carbon.apimgt.core.models.HttpResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.MediaType;

public class FunctionTriggerTestCase {

    private static final String USER_NAME = "userName";
    private static final String FUNCTION_NAME = "functionName";

    @Test(description = "Test method for capturing event")
    public void testCaptureEvent() throws APIManagementException, URISyntaxException {
        FunctionDAO functionDAO = Mockito.mock(FunctionDAO.class);
        RestCallUtil restCallUtil = Mockito.mock(RestCallUtil.class);
        FunctionTrigger functionTrigger = new FunctionTrigger(functionDAO, restCallUtil);

        URI testUri = new URI("http://testEndpointUri");
        List<Function> functions = new ArrayList<>();
        Function function = new Function(FUNCTION_NAME, testUri);
        functions.add(function);

        HttpResponse response = new HttpResponse();
        response.setResponseCode(200);

        Event event = Event.API_CREATION;
        ZonedDateTime eventTime = ZonedDateTime.now();

        Mockito.when(functionDAO.getUserFunctionsForEvent(USER_NAME, event)).thenReturn(functions);
        Mockito.when(restCallUtil.postRequest(Mockito.eq(function.getEndpointURI()), Mockito.eq(null), Mockito.eq(null),
                Mockito.any(), Mockito.eq(MediaType.APPLICATION_JSON_TYPE))).thenReturn(response);
        functionTrigger.captureEvent(event, USER_NAME, eventTime, new HashMap<>());

        //Error path
        //Illegal argument exceptions

        //When the event parameter is null
        try {
            functionTrigger.captureEvent (null, USER_NAME, eventTime, new HashMap<>());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Event must not be null");
        }

        //When the username parameter is null
        try {
            functionTrigger.captureEvent (event, null, eventTime, new HashMap<>());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Username must not be null");
        }

        //When the eventTime parameter is null
        try {
            functionTrigger.captureEvent (event, USER_NAME, null, new HashMap<>());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Event_time must not be null");
        }

        //When the metadata parameter is null
        try {
            functionTrigger.captureEvent (event, USER_NAME, eventTime, null);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Payload must not be null");
        }
    }

    @Test(description = "Exception thrown from the FunctionTrigger constructor")
    public void testConstructorException() {
        FunctionDAO functionDAO = Mockito.mock(FunctionDAO.class);
        RestCallUtil restCallUtil = Mockito.mock(RestCallUtil.class);

        //When functionDAO is null
        try {
            FunctionTrigger functionTrigger = new FunctionTrigger(null, restCallUtil);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "FunctionDAO param must not be null");
        }

        //When restCallUtil is null
        try {
            FunctionTrigger functionTrigger = new FunctionTrigger(functionDAO, null);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "RestCallUtil param must not be null");
        }
    }
}
