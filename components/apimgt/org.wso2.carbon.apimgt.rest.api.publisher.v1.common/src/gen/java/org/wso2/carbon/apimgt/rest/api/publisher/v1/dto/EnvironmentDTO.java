package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AdditionalPropertyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayEnvironmentProtocolURIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.VHostDTO;
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
    private String type = null;
    private String serverUrl = null;
    private String provider = null;
    private Boolean showInApiConsole = null;
    private List<VHostDTO> vhosts = new ArrayList<VHostDTO>();
    private List<GatewayEnvironmentProtocolURIDTO> endpointURIs = new ArrayList<GatewayEnvironmentProtocolURIDTO>();
    private List<AdditionalPropertyDTO> additionalProperties = new ArrayList<AdditionalPropertyDTO>();

  /**
   **/
  public EnvironmentDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("id")
  @NotNull
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
  public EnvironmentDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "Default", value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public EnvironmentDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "hybrid", required = true, value = "")
  @JsonProperty("type")
  @NotNull
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public EnvironmentDTO serverUrl(String serverUrl) {
    this.serverUrl = serverUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9443/services/", required = true, value = "")
  @JsonProperty("serverUrl")
  @NotNull
  public String getServerUrl() {
    return serverUrl;
  }
  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
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
  public EnvironmentDTO showInApiConsole(Boolean showInApiConsole) {
    this.showInApiConsole = showInApiConsole;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "")
  @JsonProperty("showInApiConsole")
  @NotNull
  public Boolean isShowInApiConsole() {
    return showInApiConsole;
  }
  public void setShowInApiConsole(Boolean showInApiConsole) {
    this.showInApiConsole = showInApiConsole;
  }

  /**
   **/
  public EnvironmentDTO vhosts(List<VHostDTO> vhosts) {
    this.vhosts = vhosts;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("vhosts")
  public List<VHostDTO> getVhosts() {
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
        Objects.equals(type, environment.type) &&
        Objects.equals(serverUrl, environment.serverUrl) &&
        Objects.equals(provider, environment.provider) &&
        Objects.equals(showInApiConsole, environment.showInApiConsole) &&
        Objects.equals(vhosts, environment.vhosts) &&
        Objects.equals(endpointURIs, environment.endpointURIs) &&
        Objects.equals(additionalProperties, environment.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, displayName, type, serverUrl, provider, showInApiConsole, vhosts, endpointURIs, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvironmentDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    serverUrl: ").append(toIndentedString(serverUrl)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    showInApiConsole: ").append(toIndentedString(showInApiConsole)).append("\n");
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

