/*
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import graphql.language.Field;
import graphql.language.FragmentSpread;
import graphql.language.Selection;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test cases related GraphQLAPIHandler.
 */
public class GraphQLAPIHandlerTest {

    @Test
    public void testGetNestedLevelOperations() throws Exception {
        GraphQLAPIHandler graphQLAPIHandler = new GraphQLAPIHandler();
        ArrayList<String> operationArray = new ArrayList<>();
        ArrayList<String> supportedFields = new ArrayList<>();
        supportedFields.add("search");
        supportedFields.add("addItem");
        List<Selection> selectionList = new ArrayList<>();
        Selection searchField = new Field("search");
        Selection addItemField = new Field("addItem");
        FragmentSpread newSearchField = new FragmentSpread("customFragment");
        selectionList.add(searchField);
        selectionList.add(addItemField);
        selectionList.add(newSearchField);

        graphQLAPIHandler.getNestedLevelOperations(selectionList, supportedFields, operationArray);
        Assert.assertEquals("Different no of operations are found", 2, operationArray.size());
        Assert.assertFalse("Fragment should not in the operation list",
                operationArray.contains(newSearchField.getName()));
    }
}
