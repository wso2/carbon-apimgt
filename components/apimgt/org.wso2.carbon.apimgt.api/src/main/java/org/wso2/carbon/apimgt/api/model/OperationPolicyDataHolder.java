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
    private String definition;
    private String organization;
    private String md5Hash;
    private String apiUUID;                 // Null for common policies
    private String revisionUUID;            // Null for common policies and API specific policies that are not revisioned yet
    private String clonedCommonPolicyId;    // Null for common policies and API specific policies that are not cloned.
    private OperationPolicySpecification specification;


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

    public String getOrganization() {

        return organization;
    }

    public void setOrganization(String organization) {

        this.organization = organization;
    }

    public boolean isApiSpecificPolicy() {

        return apiUUID != null;
    }

    public String getMd5Hash() {

        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {

        this.md5Hash = md5Hash;
    }

    public String getApiUUID() {

        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {

        this.apiUUID = apiUUID;
    }

    public String getRevisionUUID() {

        return revisionUUID;
    }

    public void setRevisionUUID(String revisionUUID) {

        this.revisionUUID = revisionUUID;
    }

    public boolean isClonedPolicy() {

        return clonedCommonPolicyId != null;
    }

    public String getClonedCommonPolicyId() {

        return clonedCommonPolicyId;
    }

    public void setClonedCommonPolicyId(String clonedCommonPolicyId) {

        this.clonedCommonPolicyId = clonedCommonPolicyId;
    }

    public boolean isARevision() {
        return revisionUUID != null;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationPolicyDataHolder that = (OperationPolicyDataHolder) o;
        return Objects.equals(policyId, that.policyId) &&
                Objects.equals(specification, that.specification) &&
                Objects.equals(definition, that.definition) &&
                Objects.equals(md5Hash, that.md5Hash) &&
                Objects.equals(organization, that.organization);
    }

    @Override
    public int hashCode() {

        return Objects.hash(policyId, specification, definition, md5Hash, organization);
    }
}
