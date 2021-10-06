package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APISolaceURLsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationSolaceTopicsObjectDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApplicationSolaceDeployedEnvironmentsDTO   {

    private String environmentName = null;
    private String environmentDisplayName = null;
    private String organizationName = null;
    private ApplicationSolaceTopicsObjectDTO solaceTopicsObject = null;
    private List<String> publishTopics = new ArrayList<String>();
    private List<String> subscribeTopics = new ArrayList<String>();
    private List<APISolaceURLsDTO> solaceURLs = new ArrayList<APISolaceURLsDTO>();

    /**
     **/
    public ApplicationSolaceDeployedEnvironmentsDTO environmentName(String environmentName) {
        this.environmentName = environmentName;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("environmentName")
    public String getEnvironmentName() {
        return environmentName;
    }
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    /**
     **/
    public ApplicationSolaceDeployedEnvironmentsDTO environmentDisplayName(String environmentDisplayName) {
        this.environmentDisplayName = environmentDisplayName;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("environmentDisplayName")
    public String getEnvironmentDisplayName() {
        return environmentDisplayName;
    }
    public void setEnvironmentDisplayName(String environmentDisplayName) {
        this.environmentDisplayName = environmentDisplayName;
    }

    /**
     **/
    public ApplicationSolaceDeployedEnvironmentsDTO organizationName(String organizationName) {
        this.organizationName = organizationName;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("organizationName")
    public String getOrganizationName() {
        return organizationName;
    }
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     **/
    public ApplicationSolaceDeployedEnvironmentsDTO solaceTopicsObject(ApplicationSolaceTopicsObjectDTO solaceTopicsObject) {
        this.solaceTopicsObject = solaceTopicsObject;
        return this;
    }


    @ApiModelProperty(value = "")
    @Valid
    @JsonProperty("SolaceTopicsObject")
    public ApplicationSolaceTopicsObjectDTO getSolaceTopicsObject() {
        return solaceTopicsObject;
    }
    public void setSolaceTopicsObject(ApplicationSolaceTopicsObjectDTO solaceTopicsObject) {
        this.solaceTopicsObject = solaceTopicsObject;
    }

    /**
     **/
    public ApplicationSolaceDeployedEnvironmentsDTO publishTopics(List<String> publishTopics) {
        this.publishTopics = publishTopics;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("publishTopics")
    public List<String> getPublishTopics() {
        return publishTopics;
    }
    public void setPublishTopics(List<String> publishTopics) {
        this.publishTopics = publishTopics;
    }

    /**
     **/
    public ApplicationSolaceDeployedEnvironmentsDTO subscribeTopics(List<String> subscribeTopics) {
        this.subscribeTopics = subscribeTopics;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("subscribeTopics")
    public List<String> getSubscribeTopics() {
        return subscribeTopics;
    }
    public void setSubscribeTopics(List<String> subscribeTopics) {
        this.subscribeTopics = subscribeTopics;
    }

    /**
     **/
    public ApplicationSolaceDeployedEnvironmentsDTO solaceURLs(List<APISolaceURLsDTO> solaceURLs) {
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
        ApplicationSolaceDeployedEnvironmentsDTO applicationSolaceDeployedEnvironments = (ApplicationSolaceDeployedEnvironmentsDTO) o;
        return Objects.equals(environmentName, applicationSolaceDeployedEnvironments.environmentName) &&
                Objects.equals(environmentDisplayName, applicationSolaceDeployedEnvironments.environmentDisplayName) &&
                Objects.equals(organizationName, applicationSolaceDeployedEnvironments.organizationName) &&
                Objects.equals(solaceTopicsObject, applicationSolaceDeployedEnvironments.solaceTopicsObject) &&
                Objects.equals(publishTopics, applicationSolaceDeployedEnvironments.publishTopics) &&
                Objects.equals(subscribeTopics, applicationSolaceDeployedEnvironments.subscribeTopics) &&
                Objects.equals(solaceURLs, applicationSolaceDeployedEnvironments.solaceURLs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environmentName, environmentDisplayName, organizationName, solaceTopicsObject, publishTopics, subscribeTopics, solaceURLs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ApplicationSolaceDeployedEnvironmentsDTO {\n");

        sb.append("    environmentName: ").append(toIndentedString(environmentName)).append("\n");
        sb.append("    environmentDisplayName: ").append(toIndentedString(environmentDisplayName)).append("\n");
        sb.append("    organizationName: ").append(toIndentedString(organizationName)).append("\n");
        sb.append("    solaceTopicsObject: ").append(toIndentedString(solaceTopicsObject)).append("\n");
        sb.append("    publishTopics: ").append(toIndentedString(publishTopics)).append("\n");
        sb.append("    subscribeTopics: ").append(toIndentedString(subscribeTopics)).append("\n");
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
