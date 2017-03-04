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
import org.wso2.carbon.apimgt.core.models.LambdaFunction;

import java.util.List;

/**
 * DAO Interface for Lambda function.
 */
public interface LambdaFunctionDAO {

    List<LambdaFunction> getUserDeployedFunctions(String userName) throws APIMgtDAOException;

    List<LambdaFunction> getUserFunctionsForEvent(String userName, Event event) throws APIMgtDAOException;

    List<Event> getTriggersForUserFunction(String userName, String functionName) throws APIMgtDAOException;

    void addEventFunctionMapping(String userName, Event event, String functionName) throws APIMgtDAOException;

    void deleteEventFunctionMapping(String userName, Event event, String functionName) throws APIMgtDAOException;

    void updateUserDeployedFunctions(String userName, List<LambdaFunction> functions) throws APIMgtDAOException;
}
