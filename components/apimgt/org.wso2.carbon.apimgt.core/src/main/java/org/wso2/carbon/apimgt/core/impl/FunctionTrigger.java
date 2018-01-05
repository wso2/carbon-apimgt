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
import org.wso2.carbon.apimgt.core.dao.FunctionDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Function;
import org.wso2.carbon.apimgt.core.models.HttpResponse;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * Implementation which observes any {@link org.wso2.carbon.apimgt.core.models.Event} in
 * {@link org.wso2.carbon.apimgt.core.api.APIMObservable} and trigger corresponding function that is mapped
 * to the particular {@link org.wso2.carbon.apimgt.core.models.Event} occurred.
 */
public class FunctionTrigger implements EventObserver {

    private FunctionDAO functionDAO;
    private RestCallUtil restCallUtil;

    private static final Logger log = LoggerFactory.getLogger(FunctionTrigger.class);

    /**
     * Constructor.
     *
     * @param functionDAO  To call {@link org.wso2.carbon.apimgt.core.dao.FunctionDAO} methods
     * @param restCallUtil To call {@link org.wso2.carbon.apimgt.core.api.RestCallUtil} methods
     */
    public FunctionTrigger(FunctionDAO functionDAO, RestCallUtil restCallUtil) {
        if (functionDAO == null) {
            throw new IllegalArgumentException("FunctionDAO param must not be null");
        }
        if (restCallUtil == null) {
            throw new IllegalArgumentException("RestCallUtil param must not be null");
        }
        this.functionDAO = functionDAO;
        this.restCallUtil = restCallUtil;
    }

    /**
     * Used to observe all the {@link org.wso2.carbon.apimgt.core.models.Event} occurrences
     * in an {@link org.wso2.carbon.apimgt.core.api.APIMObservable} object and trigger corresponding function that is
     * mapped to the particular {@link org.wso2.carbon.apimgt.core.models.Event} occurred.
     * <p>
     * This is a specific implementation for
     * {@link org.wso2.carbon.apimgt.core.api.EventObserver#captureEvent(Event, String, ZonedDateTime, Map)} method,
     * provided by {@link org.wso2.carbon.apimgt.core.impl.FunctionTrigger} which implements
     * {@link org.wso2.carbon.apimgt.core.api.EventObserver} interface.
     * <p>
     * {@inheritDoc}
     *
     * @see org.wso2.carbon.apimgt.core.impl.EventLogger#captureEvent(Event, String, ZonedDateTime, Map)
     */
    @Override
    public void captureEvent(Event event, String username, ZonedDateTime eventTime,
                             Map<String, String> metadata) {
        List<Function> functions = null;
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
        // Add general attributes to payload
        metadata.put(APIMgtConstants.FunctionsConstants.EVENT, event.getEventAsString());
        metadata.put(APIMgtConstants.FunctionsConstants.COMPONENT, event.getComponent().getComponentAsString());
        metadata.put(APIMgtConstants.FunctionsConstants.USERNAME, username);
        metadata.put(APIMgtConstants.FunctionsConstants.EVENT_TIME, eventTime.toString());

        try {
            functions = functionDAO.getUserFunctionsForEvent(username, event);
            jsonPayload = new Gson().toJson(metadata);
        } catch (APIMgtDAOException e) {
            String message = "Error loading functions for event from DB: -event: " + event + " -Username: " + username;
            log.error(message, new APIManagementException("Problem invoking 'getUserFunctionsForEvent' method in " +
                    "'FunctionDAO' ", e, ExceptionCodes.APIMGT_DAO_EXCEPTION));
        }

        if (functions != null && !functions.isEmpty()) {
            for (Function function : functions) {
                HttpResponse response = null;
                try {
                    response = restCallUtil.postRequest(function.getEndpointURI(), null, null, Entity.json(jsonPayload),
                            MediaType.APPLICATION_JSON_TYPE, Collections.EMPTY_MAP);
                } catch (APIManagementException e) {
                    log.error("Failed to make http request: -function: " + function.getName() + " -endpoint URI: "
                            + function.getEndpointURI() + " -event: " + event + " -Username: " + username, e);
                }

                if (response != null) {
                    int responseStatusCode = response.getResponseCode();

                    // Successful function invocation. Possible response codes: 200-299
                    // Benefit of integer division used to ensure all possible success response codes covered
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
