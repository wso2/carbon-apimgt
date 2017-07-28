/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.models.policy;

import java.util.Arrays;

/**
 * contains {@link ApplicationPolicy} attributes
 */
public class ApplicationPolicy extends Policy {

    private byte[] customAttributes;

    public ApplicationPolicy(String name) {
        super(name);
    }

    public ApplicationPolicy(String uuid, String policyName) {
        super(uuid, policyName);
    }

    public byte[] getCustomAttributes() {
        return customAttributes != null ? Arrays.copyOf(customAttributes, customAttributes.length) : new byte[0];
    }

    public void setCustomAttributes(byte[] customAttributes) {
        this.customAttributes = Arrays.copyOf(customAttributes, customAttributes.length);
    }
    @Override
    public String toString() {
        return "ApplicationPolicy [policyName=" + getPolicyName()
                + ", policyUUID =" + getUuid() + ", description=" + getDescription() + ", defaultQuotaPolicy="
                + getDefaultQuotaPolicy() + "]";
    }
}
