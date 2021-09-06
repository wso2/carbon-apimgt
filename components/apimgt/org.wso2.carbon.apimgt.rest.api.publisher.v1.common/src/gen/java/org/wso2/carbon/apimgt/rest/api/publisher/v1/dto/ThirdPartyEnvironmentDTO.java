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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThirdPartyEnvironmentDTO;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ThirdPartyEnvironmentDTO   {

    private String name = null;
    private String organization = null;
    private String provider = null;
    private String displayName = null;
    private List<ThirdPartyEnvironmentProtocolURIDTO> endpointURIs = new ArrayList<ThirdPartyEnvironmentProtocolURIDTO>();

    /**
     **/
    public ThirdPartyEnvironmentDTO name(String name) {
        this.name = name;
        return this;
    }


    @ApiModelProperty(example = "default", required = true, value = "")
    @JsonProperty("name")
    @NotNull
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     **/
    public ThirdPartyEnvironmentDTO organization(String organization) {
        this.organization = organization;
        return this;
    }


    @ApiModelProperty(example = "default", required = true, value = "")
    @JsonProperty("organization")
    @NotNull
    public String getOrganization() {
        return organization;
    }
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     **/
    public ThirdPartyEnvironmentDTO provider(String provider) {
        this.provider = provider;
        return this;
    }


    @ApiModelProperty(example = "default", required = true, value = "")
    @JsonProperty("provider")
    @NotNull
    public String getProvider() {
        return provider;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     **/
    public ThirdPartyEnvironmentDTO displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }


    @ApiModelProperty(example = "default", value = "")
    @JsonProperty("displayName")
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     **/
    public ThirdPartyEnvironmentDTO endpointURIs(List<ThirdPartyEnvironmentProtocolURIDTO> endpointURIs) {
        this.endpointURIs = endpointURIs;
        return this;
    }


    @ApiModelProperty(value = "")
    @Valid
    @JsonProperty("endpointURIs")
    public List<ThirdPartyEnvironmentProtocolURIDTO> getEndpointURIs() {
        return endpointURIs;
    }
    public void setEndpointURIs(List<ThirdPartyEnvironmentProtocolURIDTO> endpointURIs) {
        this.endpointURIs = endpointURIs;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThirdPartyEnvironmentDTO thirdPartyEnvironment = (ThirdPartyEnvironmentDTO) o;
        return Objects.equals(name, thirdPartyEnvironment.name) &&
                Objects.equals(organization, thirdPartyEnvironment.organization) &&
                Objects.equals(provider, thirdPartyEnvironment.provider) &&
                Objects.equals(displayName, thirdPartyEnvironment.displayName) &&
                Objects.equals(endpointURIs, thirdPartyEnvironment.endpointURIs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, organization, provider, displayName, endpointURIs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ThirdPartyEnvironmentDTO {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    organization: ").append(toIndentedString(organization)).append("\n");
        sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    endpointURIs: ").append(toIndentedString(endpointURIs)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
