/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import java.util.Objects;

public class OperationPolicyDataHolder {

    private int policyId;
    private int templateId = -1;
    private String apiId;
    private String revisionId;
    private OperationPolicySpecification specification;
    private String definition;

    public int getPolicyId() {

        return policyId;
    }

    public void setPolicyId(int policyId) {

        this.policyId = policyId;
    }

    public int getTemplateId() {

        return templateId;
    }

    public void setTemplateId(int templateId) {

        this.templateId = templateId;
    }

    public String getApiId() {

        return apiId;
    }

    public void setApiId(String apiId) {

        this.apiId = apiId;
    }

    public String getRevisionId() {

        return revisionId;
    }

    public void setRevisionId(String revisionId) {

        this.revisionId = revisionId;
    }

    public OperationPolicySpecification getSpecification() {

        return specification;
    }

    public void setSpecification(OperationPolicySpecification specification) {

        this.specification = specification;
    }

    public String getDefinition() {

        return definition;
    }

    public void setDefinition(String definition) {

        this.definition = definition;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationPolicyDataHolder that = (OperationPolicyDataHolder) o;
        return Objects.equals(templateId, that.templateId) &&
                Objects.equals(policyId, that.policyId) &&
                Objects.equals(apiId, that.apiId) &&
                Objects.equals(revisionId, that.revisionId) &&
                Objects.equals(specification, that.specification) &&
                Objects.equals(definition, that.definition);
    }

    @Override
    public int hashCode() {

        return Objects.hash(templateId, policyId, apiId, revisionId, specification, definition);
    }
}
