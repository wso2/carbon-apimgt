/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.solace.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is th DTO of Solace Deployed  Environments
 */
public class SolaceDeployedEnvironmentDTO {
    private String environmentName = null;
    private String environmentDisplayName = null;
    private String organizationName = null;
    private List<SolaceURLsDTO> solaceURLs = new ArrayList<SolaceURLsDTO>();
    private SolaceTopicsObjectDTO solaceTopicsObject = null;

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getEnvironmentDisplayName() {
        return environmentDisplayName;
    }

    public void setEnvironmentDisplayName(String environmentDisplayName) {
        this.environmentDisplayName = environmentDisplayName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public List<SolaceURLsDTO> getSolaceURLs() {
        return solaceURLs;
    }

    public void setSolaceURLs(List<SolaceURLsDTO> solaceURLs) {
        this.solaceURLs = solaceURLs;
    }

    public SolaceTopicsObjectDTO getSolaceTopicsObject() {
        return solaceTopicsObject;
    }

    public void setSolaceTopicsObject(SolaceTopicsObjectDTO solaceTopicsObject) {
        this.solaceTopicsObject = solaceTopicsObject;
    }
}
