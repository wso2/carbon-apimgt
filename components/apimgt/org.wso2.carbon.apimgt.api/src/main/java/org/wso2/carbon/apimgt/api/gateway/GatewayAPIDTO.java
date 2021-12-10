/*
 *
 *  * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.apimgt.api.gateway;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This Contains the API Related data to deploy in Gateway.
 */
public class GatewayAPIDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String version;
    private String provider;
    private String tenantDomain;
    private String apiId;
    private String apiDefinition;
    private String graphQLSchema;
    private GatewayContentDTO[] sequenceToBeAdd;
    private String[] sequencesToBeRemove;
    private GatewayContentDTO[] localEntriesToBeAdd;
    private String[] localEntriesToBeRemove;
    private GatewayContentDTO[] clientCertificatesToBeAdd;
    private String[] clientCertificatesToBeRemove;
    private GatewayContentDTO[] endpointEntriesToBeAdd;
    private String[] endpointEntriesToBeRemove;
    private CredentialDto[] credentialsToBeAdd ;
    private String[] credentialsToBeRemove;
    private List<String> keyManagers = new ArrayList<>();
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public String getProvider() {

        return provider;
    }

    public void setProvider(String provider) {

        this.provider = provider;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public String getApiId() {

        return apiId;
    }

    public void setApiId(String apiId) {

        this.apiId = apiId;
    }

    public String getApiDefinition() {

        return apiDefinition;
    }

    public void setApiDefinition(String apiDefinition) {

        this.apiDefinition = apiDefinition;
    }

    public GatewayContentDTO[] getSequenceToBeAdd() {

        return sequenceToBeAdd;
    }

    public void setSequenceToBeAdd(GatewayContentDTO[] sequenceToBeAdd) {

        this.sequenceToBeAdd = sequenceToBeAdd;
    }

    public String[] getSequencesToBeRemove() {

        return sequencesToBeRemove;
    }

    public void setSequencesToBeRemove(String[] sequencesToBeRemove) {

        this.sequencesToBeRemove = sequencesToBeRemove;
    }

    public GatewayContentDTO[] getLocalEntriesToBeAdd() {

        return localEntriesToBeAdd;
    }

    public void setLocalEntriesToBeAdd(GatewayContentDTO[] localEntriesToBeAdd) {

        this.localEntriesToBeAdd = localEntriesToBeAdd;
    }

    public String[] getLocalEntriesToBeRemove() {

        return localEntriesToBeRemove;
    }

    public void setLocalEntriesToBeRemove(String[] localEntriesToBeRemove) {

        this.localEntriesToBeRemove = localEntriesToBeRemove;
    }

    public GatewayContentDTO[] getClientCertificatesToBeAdd() {

        return clientCertificatesToBeAdd;
    }

    public void setClientCertificatesToBeAdd(GatewayContentDTO[] clientCertificatesToBeAdd) {

        this.clientCertificatesToBeAdd = clientCertificatesToBeAdd;
    }

    public String[] getClientCertificatesToBeRemove() {

        return clientCertificatesToBeRemove;
    }

    public void setClientCertificatesToBeRemove(String[] clientCertificatesToBeRemove) {

        this.clientCertificatesToBeRemove = clientCertificatesToBeRemove;
    }

    public GatewayContentDTO[] getEndpointEntriesToBeAdd() {

        return endpointEntriesToBeAdd;
    }

    public void setEndpointEntriesToBeAdd(GatewayContentDTO[] endpointEntriesToBeAdd) {

        this.endpointEntriesToBeAdd = endpointEntriesToBeAdd;
    }

    public String[] getEndpointEntriesToBeRemove() {

        return endpointEntriesToBeRemove;
    }

    public void setEndpointEntriesToBeRemove(String[] endpointEntriesToBeRemove) {

        this.endpointEntriesToBeRemove = endpointEntriesToBeRemove;
    }

    public CredentialDto[] getCredentialsToBeAdd() {

        return credentialsToBeAdd;
    }

    public void setCredentialsToBeAdd(CredentialDto[] credentialsToBeAdd) {

        this.credentialsToBeAdd = credentialsToBeAdd;
    }

    public String[] getCredentialsToBeRemove() {

        return credentialsToBeRemove;
    }

    public void setCredentialsToBeRemove(String[] credentialsToBeRemove) {

        this.credentialsToBeRemove = credentialsToBeRemove;
    }

    public String getGraphQLSchema() {
        return graphQLSchema;
    }

    public void setGraphQLSchema(String graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    public List<String> getKeyManagers() {

        return keyManagers;
    }

    public void setKeyManagers(List<String> keyManagers) {

        this.keyManagers = keyManagers;
    }
}
