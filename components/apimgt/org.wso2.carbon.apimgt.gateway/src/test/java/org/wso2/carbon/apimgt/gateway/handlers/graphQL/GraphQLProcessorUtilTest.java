/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import graphql.language.Field;
import graphql.language.FragmentSpread;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.utils.GraphQLProcessorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GraphQLProcessorUtilTest {

    @Test
    public void testGetNestedLevelOperations() {
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

        GraphQLProcessorUtil.getNestedLevelOperations(selectionList, supportedFields, operationArray);
        Assert.assertEquals("Different no of operations are found", 2, operationArray.size());
        Assert.assertFalse("Fragment should not in the operation list",
                operationArray.contains(newSearchField.getName()));
    }

    @Test
    public void testGetSubscriptionOperation() throws Exception {

        // Build subscription operation definition for:
        // subscription checkLiftStatusChange {liftStatusChange { name id } }
        Field subField1 = new Field("name");
        Field subField2 = new Field("id");
        List<Selection> subSelections = new ArrayList<>();
        subSelections.add(subField1);
        subSelections.add(subField2);
        SelectionSet subSelectionSet = new SelectionSet(subSelections);
        Field operationField = new Field("liftStatusChange", subSelectionSet);
        List<Selection> operationSelections = new ArrayList<>();
        operationSelections.add(operationField);
        SelectionSet selectionSet = new SelectionSet(operationSelections);
        OperationDefinition.Builder builder = OperationDefinition.newOperationDefinition();
        builder.name("checkLiftStatusChange");
        builder.operation(OperationDefinition.Operation.SUBSCRIPTION);
        builder.selectionSet(selectionSet);
        OperationDefinition operation = builder.build();
        // Get schema and parse
        String graphqlDirPath = "graphQL" + File.separator;
        String relativePath = graphqlDirPath + "schema_with_subscriptions.graphql";
        String schema = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath));
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry registry = schemaParser.parse(schema);
        String subscriptionOperation = GraphQLProcessorUtil.getOperationList(operation, registry);
        Assert.assertEquals(subscriptionOperation, "liftStatusChange");
    }
}
