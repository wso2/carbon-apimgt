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

    private String policyId;
    private String templateName;
    private OperationPolicySpecification specification;
    private String definition;

    public String getPolicyId() {

        return policyId;
    }

    public void setPolicyId(String policyId) {

        this.policyId = policyId;
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

    public String getTemplateName() {

        return templateName;
    }

    public void setTemplateName(String templateName) {

        this.templateName = templateName;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationPolicyDataHolder that = (OperationPolicyDataHolder) o;
        return Objects.equals(policyId, that.policyId) &&
                Objects.equals(templateName, that.templateName) &&
                Objects.equals(specification, that.specification) &&
                Objects.equals(definition, that.definition);
    }

    @Override
    public int hashCode() {

        return Objects.hash(policyId, templateName, specification, definition);
    }
}
