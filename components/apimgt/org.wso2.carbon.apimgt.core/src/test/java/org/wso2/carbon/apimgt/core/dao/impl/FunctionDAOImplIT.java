/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.dao.FunctionDAO;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Function;

import java.util.Arrays;
import java.util.List;

public class FunctionDAOImplIT extends DAOIntegrationTestBase {
    private static final String ADMIN = "admin";

    @Test
    public void testUpdateGetUserDeployedFunctions() throws Exception {
        FunctionDAO functionDAO = DAOFactory.getFunctionDAO();

        //validate getUserDeployedFunctions() args
        try {
            functionDAO.getUserDeployedFunctions(null);
            Assert.fail("Expected IllegalArgumentException when username is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Username must not be null"));
        }

        Function function1 = SampleTestObjectCreator.createDefaultFunction();
        Function function2 = SampleTestObjectCreator.createAlternativeFunction();
        Function function3 = SampleTestObjectCreator.createAlternativeFunction2();

        //Validate updateUserDeployedFunctions() args
        try {
            functionDAO.updateUserDeployedFunctions(null, Arrays.asList(function1, function2));
            Assert.fail("Expected IllegalArgumentException when username is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Username must not be null"));
        }

        try {
            functionDAO.updateUserDeployedFunctions(ADMIN, null);
            Assert.fail("Expected IllegalArgumentException when function list is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Functions(List) must not be null"));
        }

        functionDAO.updateUserDeployedFunctions(ADMIN, Arrays.asList(function1, function2));

        List<Function> functionListFromDB = functionDAO.getUserDeployedFunctions(ADMIN);
        Assert.assertEquals(functionListFromDB.size(), 2);
        Assert.assertTrue(functionListFromDB.contains(function1));
        Assert.assertTrue(functionListFromDB.contains(function2));

        functionDAO.updateUserDeployedFunctions(ADMIN, Arrays.asList(function1, function3));
        List<Function> functionListFromDBUpdated = functionDAO.getUserDeployedFunctions(ADMIN);
        Assert.assertEquals(functionListFromDBUpdated.size(), 2);
        Assert.assertTrue(functionListFromDBUpdated.contains(function1));
        Assert.assertTrue(functionListFromDBUpdated.contains(function3));
        Assert.assertFalse(functionListFromDBUpdated.contains(function2));
    }
    
    @Test
    public void testAddGetDeleteEventFunctionMapping() throws Exception {
        FunctionDAO functionDAO = DAOFactory.getFunctionDAO();
        Function function1 = SampleTestObjectCreator.createDefaultFunction();
        Function function2 = SampleTestObjectCreator.createAlternativeFunction();
        Function function3 = SampleTestObjectCreator.createAlternativeFunction2();

        functionDAO.updateUserDeployedFunctions(ADMIN, Arrays.asList(function1, function2, function3));

        try {
            functionDAO.addEventFunctionMapping(null, Event.API_CREATION, function1.getName());
            Assert.fail("Expected IllegalArgumentException when username is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Username must not be null"));
        }

        try {
            functionDAO.addEventFunctionMapping(ADMIN, null, function1.getName());
            Assert.fail("Expected IllegalArgumentException when Event is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Event must not be null"));
        }

        try {
            functionDAO.addEventFunctionMapping(ADMIN, Event.API_CREATION, null);
            Assert.fail("Expected IllegalArgumentException when function name is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Function name must not be null"));
        }

        functionDAO.addEventFunctionMapping(ADMIN, Event.API_CREATION, function1.getName());
        functionDAO.addEventFunctionMapping(ADMIN, Event.API_UPDATE, function1.getName());
        functionDAO.addEventFunctionMapping(ADMIN, Event.APP_CREATION, function2.getName());
        functionDAO.addEventFunctionMapping(ADMIN, Event.APP_MODIFICATION, function2.getName());
        functionDAO.addEventFunctionMapping(ADMIN, Event.API_CREATION, function3.getName());
        functionDAO.addEventFunctionMapping(ADMIN, Event.APP_CREATION, function3.getName());

        //Validate getTriggersForUserFunction() args
        try {
            functionDAO.getTriggersForUserFunction(null, function3.getName());
            Assert.fail("Expected IllegalArgumentException when username is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Username must not be null"));
        }

        try {
            functionDAO.getTriggersForUserFunction(ADMIN, null);
            Assert.fail("Expected IllegalArgumentException when function name is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Function name must not be null"));
        }

        //Check events for functions
        List<Event> eventsFromDB = functionDAO.getTriggersForUserFunction(ADMIN, function3.getName());
        Assert.assertEquals(eventsFromDB.size(), 2);
        Assert.assertTrue(
                eventsFromDB.get(0).equals(Event.API_CREATION) || eventsFromDB.get(0).equals(Event.APP_CREATION));
        Assert.assertTrue(
                eventsFromDB.get(1).equals(Event.API_CREATION) || eventsFromDB.get(1).equals(Event.APP_CREATION));

        //Validate getUserFunctionsForEvent() args
        try {
            functionDAO.getUserFunctionsForEvent(null, Event.API_UPDATE);
            Assert.fail("Expected IllegalArgumentException when username is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Username must not be null"));
        }

        try {
            functionDAO.getUserFunctionsForEvent(ADMIN, null);
            Assert.fail("Expected IllegalArgumentException when Event is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Event must not be null"));
        }

        //Check functions for events
        List<Function> functionsForAPIUpdateFromDB = functionDAO.getUserFunctionsForEvent(ADMIN, Event.API_UPDATE);
        Assert.assertEquals(functionsForAPIUpdateFromDB.size(), 1);
        Assert.assertTrue(functionsForAPIUpdateFromDB.get(0).equals(function1));

        List<Function> functionsForAPICreateFromDB = functionDAO.getUserFunctionsForEvent(ADMIN, Event.API_CREATION);
        Assert.assertEquals(functionsForAPICreateFromDB.size(), 2);
        Assert.assertTrue(functionsForAPICreateFromDB.get(0).equals(function1) || functionsForAPICreateFromDB.get(0)
                .equals(function3));
        Assert.assertTrue(functionsForAPICreateFromDB.get(1).equals(function1) || functionsForAPICreateFromDB.get(1)
                .equals(function3));

        //Validate deleteEventFunctionMapping() args
        try {
            functionDAO.deleteEventFunctionMapping(null, Event.API_CREATION, function1.getName());
            Assert.fail("Expected IllegalArgumentException when username is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Username must not be null"));
        }

        try {
            functionDAO.deleteEventFunctionMapping(ADMIN, null, function1.getName());
            Assert.fail("Expected IllegalArgumentException when Event is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Event must not be null"));
        }

        try {
            functionDAO.deleteEventFunctionMapping(ADMIN, Event.API_CREATION, null);
            Assert.fail("Expected IllegalArgumentException when function name is null.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Function name must not be null"));
        }

        //Check deletion
        functionDAO.deleteEventFunctionMapping(ADMIN, Event.API_CREATION, function3.getName());

        List<Function> functionsForAPICreateFromDBAfterDeletion = functionDAO
                .getUserFunctionsForEvent(ADMIN, Event.API_CREATION);
        Assert.assertEquals(functionsForAPICreateFromDBAfterDeletion.size(), 1);
        Assert.assertEquals(functionsForAPICreateFromDBAfterDeletion.get(0), function1);
    }
}
