package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APISolaceURLsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APISolaceEndpointURLsDTO   {

    private String environmentName = null;
    private String environmentDisplayName = null;
    private String environmentOrganization = null;
    private List<APISolaceURLsDTO> solaceURLs = new ArrayList<APISolaceURLsDTO>();

    /**
     **/
    public APISolaceEndpointURLsDTO environmentName(String environmentName) {
        this.environmentName = environmentName;
        return this;
    }


    @ApiModelProperty(example = "Default", value = "")
    @JsonProperty("environmentName")
    public String getEnvironmentName() {
        return environmentName;
    }
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    /**
     **/
    public APISolaceEndpointURLsDTO environmentDisplayName(String environmentDisplayName) {
        this.environmentDisplayName = environmentDisplayName;
        return this;
    }


    @ApiModelProperty(example = "Default", value = "")
    @JsonProperty("environmentDisplayName")
    public String getEnvironmentDisplayName() {
        return environmentDisplayName;
    }
    public void setEnvironmentDisplayName(String environmentDisplayName) {
        this.environmentDisplayName = environmentDisplayName;
    }

    /**
     **/
    public APISolaceEndpointURLsDTO environmentOrganization(String environmentOrganization) {
        this.environmentOrganization = environmentOrganization;
        return this;
    }


    @ApiModelProperty(example = "Default", value = "")
    @JsonProperty("environmentOrganization")
    public String getEnvironmentOrganization() {
        return environmentOrganization;
    }
    public void setEnvironmentOrganization(String environmentOrganization) {
        this.environmentOrganization = environmentOrganization;
    }

    /**
     **/
    public APISolaceEndpointURLsDTO solaceURLs(List<APISolaceURLsDTO> solaceURLs) {
        this.solaceURLs = solaceURLs;
        return this;
    }


    @ApiModelProperty(value = "")
    @Valid
    @JsonProperty("solaceURLs")
    public List<APISolaceURLsDTO> getSolaceURLs() {
        return solaceURLs;
    }
    public void setSolaceURLs(List<APISolaceURLsDTO> solaceURLs) {
        this.solaceURLs = solaceURLs;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        APISolaceEndpointURLsDTO apISolaceEndpointURLs = (APISolaceEndpointURLsDTO) o;
        return Objects.equals(environmentName, apISolaceEndpointURLs.environmentName) &&
                Objects.equals(environmentDisplayName, apISolaceEndpointURLs.environmentDisplayName) &&
                Objects.equals(environmentOrganization, apISolaceEndpointURLs.environmentOrganization) &&
                Objects.equals(solaceURLs, apISolaceEndpointURLs.solaceURLs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environmentName, environmentDisplayName, environmentOrganization, solaceURLs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class APISolaceEndpointURLsDTO {\n");

        sb.append("    environmentName: ").append(toIndentedString(environmentName)).append("\n");
        sb.append("    environmentDisplayName: ").append(toIndentedString(environmentDisplayName)).append("\n");
        sb.append("    environmentOrganization: ").append(toIndentedString(environmentOrganization)).append("\n");
        sb.append("    solaceURLs: ").append(toIndentedString(solaceURLs)).append("\n");
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
