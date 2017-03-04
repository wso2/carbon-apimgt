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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.api.RestCallUtil;
import org.wso2.carbon.apimgt.core.dao.LambdaFunctionDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.LambdaFunction;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * Implementation which observes to the API Manager events and trigger corresponding lambda function that belongs to
 * the user and particular event occurred.
 */
public class LambdaFunctionTrigger implements EventObserver {

    private LambdaFunctionDAO lambdaFunctionDAO;
    private RestCallUtil restCallUtil;

    private static final Logger log = LoggerFactory.getLogger(LambdaFunctionTrigger.class);

    public LambdaFunctionTrigger(LambdaFunctionDAO lambdaFunctionDAO, RestCallUtil restCallUtil) {
        if (lambdaFunctionDAO == null) {
            throw new IllegalArgumentException("LambdaFunctionDAO param must not be null");
        }
        if (restCallUtil == null) {
            throw new IllegalArgumentException("RestCallUtil param must not be null");
        }
        this.lambdaFunctionDAO = lambdaFunctionDAO;
        this.restCallUtil = restCallUtil;
    }

    @Override
    public void captureEvent(Event event, String username, ZonedDateTime eventTime,
                             Map<String, String> metadata) {
        List<LambdaFunction> functions = null;
        String jsonPayload = null;

        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (eventTime == null) {
            throw new IllegalArgumentException("Event_time must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Payload must not be null");
        }

        metadata.put(APIMgtConstants.FunctionsConstants.EVENT, event.getEventAsString());
        metadata.put(APIMgtConstants.FunctionsConstants.COMPONENT, event.getComponent().getComponentAsString());
        metadata.put(APIMgtConstants.FunctionsConstants.USERNAME, username);
        metadata.put(APIMgtConstants.FunctionsConstants.EVENT_TIME, eventTime.toString());

        try {
            functions = lambdaFunctionDAO.getUserFunctionsForEvent(username, event);
            jsonPayload = new Gson().toJson(metadata);
        } catch (APIMgtDAOException e) {
            String message = "Error loading functions for event from DB: -event: " + event + " -Username: " + username;
            log.error(message, new APIManagementException("Problem invoking 'getUserFunctionsForEvent' method in " +
                    "'LambdaFunctionDAO' ", e, ExceptionCodes.APIMGT_DAO_EXCEPTION));
        }

        if (functions != null && !functions.isEmpty()) {
            for (LambdaFunction function : functions) {
                HttpResponse response = null;
                try {
                    response = restCallUtil.postRequest(function.getEndpointURI(), null, null,
                            Entity.json(jsonPayload), MediaType.APPLICATION_JSON_TYPE);
                } catch (APIManagementException e) {
                    log.error("Failed to make http request: -function: " + function.getName() + " -endpoint URI: "
                            + function.getEndpointURI() + " -event: " + event + " -Username: " + username, e);
                }

                if (response != null) {
                    int responseStatusCode = response.getResponseCode();

                    if (responseStatusCode / 100 == 2) {
                        log.info("Function successfully invoked: " + function.getName() + " -event: " + event +
                                " -Username: " + username + " -Response code: " + responseStatusCode);
                    } else {
                        log.error("Problem invoking function: " + function.getName() + " -event: " + event +
                                " -Username: " + username + " -Response code: " + responseStatusCode);
                    }
                }
            }
        }
    }
}
