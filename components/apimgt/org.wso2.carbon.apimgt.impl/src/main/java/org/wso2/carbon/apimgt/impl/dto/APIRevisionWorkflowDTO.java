/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.api.model.APIRevision;

/**
 * APIRevision DTO class for workflow
 */
public class APIRevisionWorkflowDTO extends WorkflowDTO {
    private APIRevision revision;
    private String userName;
    private String environment;
    private String revisionId;
    private String apiName;
    private String apiProvider;
    private String apiVersion;

    public APIRevision getAPIRevision() {
        return revision;
    }

    public void setAPIRevision(APIRevision revision) {
        this.revision = revision;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    public String getApiProvider() {
        return apiProvider;
    }

    public void setApiProvider(String apiProvider) {
        this.apiProvider = apiProvider;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
