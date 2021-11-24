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
 *
 */
package org.wso2.carbon.apimgt.impl.definitions;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.testng.Assert;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class GraphQLSchemaDefinitionTest {

    private final GraphQLSchemaDefinition graphQLSchemaDefinition = new GraphQLSchemaDefinition();

    @Test
    public void testSubscriptionAvailability() throws Exception {
        String graphqlDirPath = "definitions" + File.separator + "graphql" + File.separator;
        String relativePath = graphqlDirPath + "schema_with_subscriptions.graphql";
        String schema = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath),
                StandardCharsets.UTF_8);
        Assert.assertTrue(graphQLSchemaDefinition.isSubscriptionAvailable(schema));
        relativePath = graphqlDirPath + "schema_without_subscriptions.graphql";
        schema = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath),
                StandardCharsets.UTF_8);
        Assert.assertFalse(graphQLSchemaDefinition.isSubscriptionAvailable(schema));
    }
}
