/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.common.gateway.graphql;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.QueryAnalyzerResponseDTO;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Test cases for {@link QueryAnalyzer}
 */
public class QueryAnalyzerTestCase {

    private final String subscriptionSchemaRelativePath = File.separator + "graphQL" + File.separator
            + "schema_with_subscriptions.graphql";
    private QueryAnalyzer queryAnalyzer;
    private FieldComplexityCalculatorImpl fieldComplexityCalculator;

    @Before
    public void setup() throws Exception {
        String schemaString = IOUtils.toString(this.getClass().getResourceAsStream(subscriptionSchemaRelativePath),
                StandardCharsets.UTF_8);
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry registry = schemaParser.parse(schemaString);
        GraphQLSchema graphQLSchema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
        queryAnalyzer = new QueryAnalyzer(graphQLSchema);
        String complexityPolicy = "{\"complexity\":{\"Subscription\":{\"liftStatusChange\":3},"
                + "\"Lift\":{\"night\":1,\"name\":1,\"elevationGain\":1,\"id\":1,\"capacity\":1}}}";
        fieldComplexityCalculator = new FieldComplexityCalculatorImpl(complexityPolicy);
    }

    @Test
    public void testAnalyseQueryDepthForSubscriptionsSuccess() {
        int maxDepth = 3;
        String payload = "subscription {\n" +
                "  liftStatusChange {\n" +
                "    id\n" +
                "  }\n" +
                "}\n";
        QueryAnalyzerResponseDTO queryAnalyzerResponseDTO = queryAnalyzer.analyseQueryDepth(maxDepth, payload);
        Assert.assertTrue(queryAnalyzerResponseDTO.isSuccess());
        Assert.assertTrue(queryAnalyzerResponseDTO.getErrorList().isEmpty());
        maxDepth = 0;
        queryAnalyzerResponseDTO = queryAnalyzer.analyseQueryDepth(maxDepth, payload);
        Assert.assertTrue(queryAnalyzerResponseDTO.isSuccess());
        Assert.assertTrue(queryAnalyzerResponseDTO.getErrorList().isEmpty());
    }

    @Test
    public void testAnalyseQueryDepthForSubscriptionsError() {
        int maxDepth = 1;
        String payload = "subscription {\n" +
                "  liftStatusChange {\n" +
                "    name\n" +
                "    id\n" +
                "  }\n" +
                "}\n";
        QueryAnalyzerResponseDTO queryAnalyzerResponseDTO = queryAnalyzer.analyseQueryDepth(maxDepth, payload);
        Assert.assertFalse(queryAnalyzerResponseDTO.isSuccess());
        Assert.assertTrue(queryAnalyzerResponseDTO.getErrorList().toString()
                .contains("maximum query depth exceeded 2 > 1"));
    }

    @Test
    public void testAnalyzeQueryDepthForSubscriptionsError() {
        int maxComplexity = 4;
        String payload = "subscription {\n" +
                "  liftStatusChange {\n" +
                "    name\n" +
                "    id\n" +
                "    night\n" +
                "  }\n" +
                "}\n";
        QueryAnalyzerResponseDTO queryAnalyzerResponseDTO = queryAnalyzer.analyseQueryComplexity(maxComplexity, payload,
                fieldComplexityCalculator);
        Assert.assertFalse(queryAnalyzerResponseDTO.isSuccess());
        Assert.assertTrue(queryAnalyzerResponseDTO.getErrorList().toString()
                .contains("maximum query complexity exceeded"));
    }

    @Test
    public void testAnalyzeQueryDepthForSubscriptionsSuccess() {
        int maxComplexity = 4;
        String payload = "subscription {\n" +
                "  liftStatusChange {\n" +
                "    name\n" +
                "  }\n" +
                "}\n";
        QueryAnalyzerResponseDTO queryAnalyzerResponseDTO = queryAnalyzer.analyseQueryComplexity(maxComplexity, payload,
                fieldComplexityCalculator);
        Assert.assertTrue(queryAnalyzerResponseDTO.isSuccess());
        Assert.assertTrue(queryAnalyzerResponseDTO.getErrorList().isEmpty());
        maxComplexity = 0;
        queryAnalyzerResponseDTO = queryAnalyzer.analyseQueryComplexity(maxComplexity, payload,
                fieldComplexityCalculator);
        Assert.assertTrue(queryAnalyzerResponseDTO.isSuccess());
        Assert.assertTrue(queryAnalyzerResponseDTO.getErrorList().isEmpty());
    }
}
