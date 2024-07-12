package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AdditionalPropertyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayEnvironmentProtocolURIDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.VHostDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class EnvironmentDTO   {
  
    private String id = null;
    private String name = null;
    private String displayName = null;
    private String provider = null;
    private String type = "hybrid";
    private String gatewayType = "Regular";
    private String description = null;
    private Boolean isReadOnly = null;
    private List<VHostDTO> vhosts = new ArrayList<VHostDTO>();
    private List<GatewayEnvironmentProtocolURIDTO> endpointURIs = new ArrayList<GatewayEnvironmentProtocolURIDTO>();
    private List<AdditionalPropertyDTO> additionalProperties = new ArrayList<AdditionalPropertyDTO>();

  /**
   **/
  public EnvironmentDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "ece92bdc-e1e6-325c-b6f4-656208a041e9", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public EnvironmentDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "us-region", required = true, value = "")
  @JsonProperty("name")
  @NotNull
 @Pattern(regexp="^[a-zA-Z0-9_-]+$") @Size(min=1,max=255)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public EnvironmentDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "US Region", value = "")
  @JsonProperty("displayName")
 @Size(min=1,max=255)  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public EnvironmentDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "wso2", value = "")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   **/
  public EnvironmentDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "hybrid", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public EnvironmentDTO gatewayType(String gatewayType) {
    this.gatewayType = gatewayType;
    return this;
  }

  
  @ApiModelProperty(example = "Regular", value = "")
  @JsonProperty("gatewayType")
  public String getGatewayType() {
    return gatewayType;
  }
  public void setGatewayType(String gatewayType) {
    this.gatewayType = gatewayType;
  }

  /**
   **/
  public EnvironmentDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Gateway environment in US Region", value = "")
  @JsonProperty("description")
 @Size(max=1023)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public EnvironmentDTO isReadOnly(Boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("isReadOnly")
  public Boolean isIsReadOnly() {
    return isReadOnly;
  }
  public void setIsReadOnly(Boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
  }

  /**
   **/
  public EnvironmentDTO vhosts(List<VHostDTO> vhosts) {
    this.vhosts = vhosts;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("vhosts")
  @NotNull
 @Size(min=1)  public List<VHostDTO> getVhosts() {
    return vhosts;
  }
  public void setVhosts(List<VHostDTO> vhosts) {
    this.vhosts = vhosts;
  }

  /**
   **/
  public EnvironmentDTO endpointURIs(List<GatewayEnvironmentProtocolURIDTO> endpointURIs) {
    this.endpointURIs = endpointURIs;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("endpointURIs")
  public List<GatewayEnvironmentProtocolURIDTO> getEndpointURIs() {
    return endpointURIs;
  }
  public void setEndpointURIs(List<GatewayEnvironmentProtocolURIDTO> endpointURIs) {
    this.endpointURIs = endpointURIs;
  }

  /**
   **/
  public EnvironmentDTO additionalProperties(List<AdditionalPropertyDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("additionalProperties")
  public List<AdditionalPropertyDTO> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(List<AdditionalPropertyDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnvironmentDTO environment = (EnvironmentDTO) o;
    return Objects.equals(id, environment.id) &&
        Objects.equals(name, environment.name) &&
        Objects.equals(displayName, environment.displayName) &&
        Objects.equals(provider, environment.provider) &&
        Objects.equals(type, environment.type) &&
        Objects.equals(gatewayType, environment.gatewayType) &&
        Objects.equals(description, environment.description) &&
        Objects.equals(isReadOnly, environment.isReadOnly) &&
        Objects.equals(vhosts, environment.vhosts) &&
        Objects.equals(endpointURIs, environment.endpointURIs) &&
        Objects.equals(additionalProperties, environment.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, displayName, provider, type, gatewayType, description, isReadOnly, vhosts, endpointURIs, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvironmentDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    gatewayType: ").append(toIndentedString(gatewayType)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    isReadOnly: ").append(toIndentedString(isReadOnly)).append("\n");
    sb.append("    vhosts: ").append(toIndentedString(vhosts)).append("\n");
    sb.append("    endpointURIs: ").append(toIndentedString(endpointURIs)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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

