/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.dto.devportal;

import java.util.List;

public class ApiMetaDataDTO {

    private ApiInfo apiInfo;
    private List<String> subscriptionPolicies;
    private EndPoints endPoints;

    public static class ApiInfo {
        private String referenceID;
        private String apiName;
        private String orgName;
        private String provider;
        private String apiCategory;
        private String apiDescription;
        private String visibility;
        private List<String> visibleGroups;
        private Owners owners;
        private String apiVersion;
        private String apiType;

        public String getReferenceID() {
            return referenceID;
        }

        public void setReferenceID(String referenceID) {
            this.referenceID = referenceID;
        }

        public String getApiName() {
            return apiName;
        }

        public void setApiName(String apiName) {
            this.apiName = apiName;
        }

        public String getOrgName() {
            return orgName;
        }

        public void setOrgName(String orgName) {
            this.orgName = orgName;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getApiCategory() {
            return apiCategory;
        }

        public void setApiCategory(String apiCategory) {
            this.apiCategory = apiCategory;
        }

        public String getApiDescription() {
            return apiDescription;
        }

        public void setApiDescription(String apiDescription) {
            this.apiDescription = apiDescription;
        }

        public String getVisibility() {
            return visibility;
        }

        public void setVisibility(String visibility) {
            this.visibility = visibility;
        }

        public List<String> getVisibleGroups() {
            return visibleGroups;
        }

        public void setVisibleGroups(List<String> visibleGroups) {
            this.visibleGroups = visibleGroups;
        }

        public Owners getOwners() {
            return owners;
        }

        public void setOwners(Owners owners) {
            this.owners = owners;
        }

        public String getApiVersion() {
            return apiVersion;
        }

        public void setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
        }

        public String getApiType() {
            return apiType;
        }

        public void setApiType(String apiType) {
            this.apiType = apiType;
        }

        public static class Owners {
            private String technicalOwner;
            private String technicalOwnerEmail;
            private String businessOwner;
            private String businessOwnerEmail;

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
        }
    }

    public static class EndPoints {
        private String sandboxURL;
        private String productionURL;


        public String getSandboxURL() {
            return sandboxURL;
        }

        public void setSandboxURL(String sandboxURL) {
            this.sandboxURL = sandboxURL;
        }

        public String getProductionURL() {
            return productionURL;
        }

        public void setProductionURL(String productionURL) {
            this.productionURL = productionURL;
        }
    }

    public ApiInfo getApiInfo() {
        return apiInfo;
    }

    public void setApiInfo(ApiInfo apiInfo) {
        this.apiInfo = apiInfo;
    }

    public List<String> getSubscriptionPolicies() {
        return subscriptionPolicies;
    }

    public void setSubscriptionPolicies(List<String> subscriptionPolicies) {
        this.subscriptionPolicies = subscriptionPolicies;
    }

    public EndPoints getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(EndPoints endPoints) {
        this.endPoints = endPoints;
    }
}
