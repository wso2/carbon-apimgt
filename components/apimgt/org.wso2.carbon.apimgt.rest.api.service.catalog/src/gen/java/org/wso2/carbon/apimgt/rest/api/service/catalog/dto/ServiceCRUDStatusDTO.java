package org.wso2.carbon.apimgt.rest.api.service.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;


public class ServiceCRUDStatusDTO {

    private String id = null;
    private String name = null;
    private String displayName = null;
    private String version = null;
    private String serviceUrl = null;
    private String createdTime = null;
    private String lastUpdatedTime = null;

    /**
     *
     **/
    public ServiceCRUDStatusDTO id(String id) {
        this.id = id;
        return this;
    }


    @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     **/
    public ServiceCRUDStatusDTO name(String name) {
        this.name = name;
        return this;
    }


    @ApiModelProperty(example = "Pizzashack-Endpoint", value = "")
    @JsonProperty("name")
    @Pattern(regexp = "^[^\\*]+$")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     **/
    public ServiceCRUDStatusDTO displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }


    @ApiModelProperty(example = "Pizzashack-Endpoint", value = "")
    @JsonProperty("displayName")
    @Pattern(regexp = "^[^\\*]+$")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     *
     **/
    public ServiceCRUDStatusDTO version(String version) {
        this.version = version;
        return this;
    }


    @ApiModelProperty(example = "v1", required = true, value = "")
    @JsonProperty("version")
    @NotNull
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     *
     **/
    public ServiceCRUDStatusDTO serviceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        return this;
    }


    @ApiModelProperty(example = "http://localhost/pizzashack", value = "")
    @JsonProperty("serviceUrl")
    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     *
     **/
    public ServiceCRUDStatusDTO createdTime(String createdTime) {
        this.createdTime = createdTime;
        return this;
    }


    @ApiModelProperty(example = "2020-02-20T13:57:16.229", value = "")
    @JsonProperty("createdTime")
    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    /**
     *
     **/
    public ServiceCRUDStatusDTO lastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
        return this;
    }


    @ApiModelProperty(example = "2020-02-20T13:57:16.229", value = "")
    @JsonProperty("lastUpdatedTime")
    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceCRUDStatusDTO serviceCRUDStatus = (ServiceCRUDStatusDTO) o;
        return Objects.equals(id, serviceCRUDStatus.id) &&
                Objects.equals(name, serviceCRUDStatus.name) &&
                Objects.equals(displayName, serviceCRUDStatus.displayName) &&
                Objects.equals(version, serviceCRUDStatus.version) &&
                Objects.equals(serviceUrl, serviceCRUDStatus.serviceUrl) &&
                Objects.equals(createdTime, serviceCRUDStatus.createdTime) &&
                Objects.equals(lastUpdatedTime, serviceCRUDStatus.lastUpdatedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, displayName, version, serviceUrl, createdTime, lastUpdatedTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceCRUDStatusDTO {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    serviceUrl: ").append(toIndentedString(serviceUrl)).append("\n");
        sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
        sb.append("    lastUpdatedTime: ").append(toIndentedString(lastUpdatedTime)).append("\n");
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

