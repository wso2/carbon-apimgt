/*
 *
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.ContentType;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.LambdaFunction;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;

/**
 * Implementation which observes to the API Manager events and trigger corresponding lambda function that belongs to
 * the user and particular event occurred.
 */
public class LambdaFunctionTrigger implements EventObserver {

    private static final Logger log = LoggerFactory.getLogger(EventObserver.class);

    private LambdaFunctionTrigger() {

    }

    private static class SingletonHelper {
        private static final LambdaFunctionTrigger instance = new LambdaFunctionTrigger();
    }

    public static LambdaFunctionTrigger getInstance() {
        return SingletonHelper.instance;
    }

    @Override
    public void captureEvent(Event event, String username, ZonedDateTime eventTime,
                             Map<String, String> payload) {
        List<LambdaFunction> functions = null;
        String jsonPayload = null;

        payload.put("Event", event.getEventAsString());
        payload.put("Component", event.getComponent().getComponentAsString());
        payload.put("Username", username);
        payload.put("Event_Time", eventTime.toString());

        try {
            functions = DAOFactory.getLambdaFunctionDAO().getUserFunctionsForEvent(username, event);
            jsonPayload = new Gson().toJson(payload);
        } catch (APIMgtDAOException e) {
            log.error("Error loading functions for event from DB: -event: " + event + " -Username: " + username, e);
        }

        if (functions != null && !functions.isEmpty()) {
            for (LambdaFunction function : functions) {
                HttpResponse response = null;
                try {
                    response = RestCallUtil.postRequest(function.getEndpointURI(), null, null,
                            Entity.json(jsonPayload), ContentType.APPLICATION_JSON);
                } catch (IOException e) {
                    log.error("Error making connection", e);
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
