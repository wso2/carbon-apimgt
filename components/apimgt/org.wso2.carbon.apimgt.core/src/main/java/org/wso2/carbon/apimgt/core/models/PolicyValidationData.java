/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.models;

/**
 * This class used to keep policy related details for gateway.
 */
public class PolicyValidationData {
    private String id;
    private String name;
    private boolean stopOnQuotaReach;

    public PolicyValidationData(String id, String name, boolean stopOnQuotaReach) {
        this.id = id;
        this.name = name;
        this.stopOnQuotaReach = stopOnQuotaReach;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isStopOnQuotaReach() {
        return stopOnQuotaReach;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PolicyValidationData that = (PolicyValidationData) o;

        if (stopOnQuotaReach != that.stopOnQuotaReach) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (stopOnQuotaReach ? 1 : 0);
        return result;
    }
}
