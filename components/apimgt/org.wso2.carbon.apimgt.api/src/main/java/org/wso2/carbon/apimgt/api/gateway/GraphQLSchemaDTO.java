/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.api.gateway;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;

/**
 * This contains GraphQL Schema related data to deploy in Gateway.
 */
public class GraphQLSchemaDTO {

    private GraphQLSchema graphQLSchema;
    private TypeDefinitionRegistry typeDefinitionRegistry;

    public GraphQLSchemaDTO(GraphQLSchema graphQLSchema, TypeDefinitionRegistry typeDefinitionRegistry) {
        this.graphQLSchema = graphQLSchema;
        this.typeDefinitionRegistry = typeDefinitionRegistry;
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    public void setGraphQLSchema(GraphQLSchema graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    public TypeDefinitionRegistry getTypeDefinitionRegistry() {
        return typeDefinitionRegistry;
    }

    public void setTypeDefinitionRegistry(TypeDefinitionRegistry typeDefinitionRegistry) {
        this.typeDefinitionRegistry = typeDefinitionRegistry;
    }
}
