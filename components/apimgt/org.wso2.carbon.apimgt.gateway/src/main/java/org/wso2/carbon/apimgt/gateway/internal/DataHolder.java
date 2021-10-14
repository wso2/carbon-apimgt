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

package org.wso2.carbon.apimgt.gateway.internal;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class DataHolder {

    private static final DataHolder Instance = new DataHolder();
    private Map<String, List<String>> apiToCertificatesMap = new HashMap();
    private Map<String, Pair<GraphQLSchema, TypeDefinitionRegistry>> apiToGraphQLSchemaMap = new HashMap<>();

    private DataHolder() {

    }

    public Map<String, List<String>> getApiToCertificatesMap() {

        return apiToCertificatesMap;
    }

    public void setApiToCertificatesMap(Map<String, List<String>> apiToCertificatesMap) {

        this.apiToCertificatesMap = apiToCertificatesMap;
    }

    public static DataHolder getInstance() {

        return Instance;
    }

    public void addApiToAliasList(String apiId, List<String> aliasList) {

        apiToCertificatesMap.put(apiId, aliasList);
    }

    public List<String> getCertificateAliasListForAPI(String apiId) {

        return apiToCertificatesMap.getOrDefault(apiId, Collections.emptyList());
    }

    public Map<String, Pair<GraphQLSchema, TypeDefinitionRegistry>> getApiToGraphQLSchemaMap() {
        return apiToGraphQLSchemaMap;
    }

    public void addApiToGraphQLSchema(String apiId, Pair<GraphQLSchema, TypeDefinitionRegistry> schema) {
        apiToGraphQLSchemaMap.put(apiId, schema);
    }

    public Pair<GraphQLSchema, TypeDefinitionRegistry> getGraphQLSchemaForAPI(String apiId) {

        return apiToGraphQLSchemaMap.get(apiId);
    }
}
