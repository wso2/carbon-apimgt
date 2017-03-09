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

package org.wso2.carbon.apimgt.core.dao;

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Function;

import java.util.List;

/**
 * DAO interface for Functions. This provides methods to deal with DAO operations needed.
 */
public interface FunctionDAO {

    /**
     * To get all the functions deployed by the current user.
     *
     * @param userName Logged in user's username
     * @return List of Functions
     * @throws APIMgtDAOException In case of any failures, when trying to get Functions from DB
     */
    List<Function> getUserDeployedFunctions(String userName) throws APIMgtDAOException;

    /**
     * To get all the user functions mapped for a particular event.
     *
     * @param userName Logged in user's username
     * @param event    Event to which we need to get the mapped functions
     * @return List of Functions
     * @throws APIMgtDAOException In case of any failures, when trying to get Functions for an Event from DB
     */
    List<Function> getUserFunctionsForEvent(String userName, Event event) throws APIMgtDAOException;

    /**
     * To get all the triggers(registered events) for a particular function.
     *
     * @param userName     Logged in user's username
     * @param functionName Name of the Function to which we need to get the triggers
     * @return List of Events
     * @throws APIMgtDAOException In case of any failures, when trying to get Events for a function from DB
     */
    List<Event> getTriggersForUserFunction(String userName, String functionName) throws APIMgtDAOException;

    /**
     * To add a new Event-Function mapping.
     *
     * @param userName     Logged in user's username
     * @param event        Event involved in the mapping
     * @param functionName Name of the Function, which is involved in the mapping
     * @throws APIMgtDAOException In case of any failures, when trying to add a new Event-Function mapping
     */
    void addEventFunctionMapping(String userName, Event event, String functionName) throws APIMgtDAOException;

    /**
     * To delete an existing Event-Function mapping.
     *
     * @param userName     Logged in user's username
     * @param event        Event involved in the mapping
     * @param functionName Name of the Function, which is involved in the mapping
     * @throws APIMgtDAOException In case of any failures, when trying to delete an Event-Function mapping
     */
    void deleteEventFunctionMapping(String userName, Event event, String functionName) throws APIMgtDAOException;

    /**
     * To update all the Functions in the storage(Add new Functions + delete missing Functions and its mappings).
     * For this project, this should be called whenever we get user deployed Functions list from app cloud.
     *
     * @param userName  Logged in user's username
     * @param functions Correct list of Functions
     * @throws APIMgtDAOException In case of any failures, when trying to update all the Functions
     */
    void updateUserDeployedFunctions(String userName, List<Function> functions) throws APIMgtDAOException;
}
