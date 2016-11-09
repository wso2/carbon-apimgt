/***********************************************************************************************************************
 *
 *  *
 *  *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *   WSO2 Inc. licenses this file to you under the Apache License,
 *  *   Version 2.0 (the "License"); you may not use this file except
 *  *   in compliance with the License.
 *  *   You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */

package org.wso2.carbon.apimgt.core.models;


/**
 * This Class represents the Business information of the API
 */
public final class BusinessInformation {
    private String businessOwner = "";
    private String businessOwnerEmail = "";
    private String technicalOwner = "";
    private String technicalOwnerEmail = "";

    public String getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public String getBusinessOwnerEmail() {
        return businessOwnerEmail;
    }

    public void setBusinessOwnerEmail(String businessOwnerEmail) {
        this.businessOwnerEmail = businessOwnerEmail;
    }

    public String getTechnicalOwner() {
        return technicalOwner;
    }

    public void setTechnicalOwner(String technicalOwner) {
        this.technicalOwner = technicalOwner;
    }

    public String getTechnicalOwnerEmail() {
        return technicalOwnerEmail;
    }

    public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
        this.technicalOwnerEmail = technicalOwnerEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BusinessInformation that = (BusinessInformation) o;

        if (!businessOwner.equals(that.businessOwner)) return false;
        if (!businessOwnerEmail.equals(that.businessOwnerEmail)) return false;
        if (!technicalOwner.equals(that.technicalOwner)) return false;
        return technicalOwnerEmail.equals(that.technicalOwnerEmail);

    }

    @Override
    public int hashCode() {
        int result = businessOwner.hashCode();
        result = 31 * result + businessOwnerEmail.hashCode();
        result = 31 * result + technicalOwner.hashCode();
        result = 31 * result + technicalOwnerEmail.hashCode();
        return result;
    }
}
