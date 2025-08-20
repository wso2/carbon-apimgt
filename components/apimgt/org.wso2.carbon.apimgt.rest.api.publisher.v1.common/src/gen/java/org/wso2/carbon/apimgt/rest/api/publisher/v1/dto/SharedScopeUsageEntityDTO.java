package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SharedScopeUsedResourceInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SharedScopeUsageEntityDTO   {
  
    private String name = null;
    private String context = null;
    private String version = null;
    private String provider = null;
    private String revisionId = null;
    private String type = null;
    private List<SharedScopeUsedResourceInfoDTO> usedResourceList = new ArrayList<SharedScopeUsedResourceInfoDTO>();

  /**
   **/
  public SharedScopeUsageEntityDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPI", required = true, value = "")
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
  public SharedScopeUsageEntityDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPI", required = true, value = "")
  @JsonProperty("context")
  @NotNull
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   **/
  public SharedScopeUsageEntityDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", required = true, value = "")
  @JsonProperty("version")
  @NotNull
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * If the provider value is not given user invoking the api will be used as the provider. 
   **/
  public SharedScopeUsageEntityDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "If the provider value is not given user invoking the api will be used as the provider. ")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   **/
  public SharedScopeUsageEntityDTO revisionId(String revisionId) {
    this.revisionId = revisionId;
    return this;
  }

  
  @ApiModelProperty(example = "Revision 1", value = "")
  @JsonProperty("revisionId")
  public String getRevisionId() {
    return revisionId;
  }
  public void setRevisionId(String revisionId) {
    this.revisionId = revisionId;
  }

  /**
   * Entity type. 
   **/
  public SharedScopeUsageEntityDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "MCP", value = "Entity type. ")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Resource list which have used the shared scope. 
   **/
  public SharedScopeUsageEntityDTO usedResourceList(List<SharedScopeUsedResourceInfoDTO> usedResourceList) {
    this.usedResourceList = usedResourceList;
    return this;
  }

  
  @ApiModelProperty(value = "Resource list which have used the shared scope. ")
      @Valid
  @JsonProperty("usedResourceList")
  public List<SharedScopeUsedResourceInfoDTO> getUsedResourceList() {
    return usedResourceList;
  }
  public void setUsedResourceList(List<SharedScopeUsedResourceInfoDTO> usedResourceList) {
    this.usedResourceList = usedResourceList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SharedScopeUsageEntityDTO sharedScopeUsageEntity = (SharedScopeUsageEntityDTO) o;
    return Objects.equals(name, sharedScopeUsageEntity.name) &&
        Objects.equals(context, sharedScopeUsageEntity.context) &&
        Objects.equals(version, sharedScopeUsageEntity.version) &&
        Objects.equals(provider, sharedScopeUsageEntity.provider) &&
        Objects.equals(revisionId, sharedScopeUsageEntity.revisionId) &&
        Objects.equals(type, sharedScopeUsageEntity.type) &&
        Objects.equals(usedResourceList, sharedScopeUsageEntity.usedResourceList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, context, version, provider, revisionId, type, usedResourceList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SharedScopeUsageEntityDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    revisionId: ").append(toIndentedString(revisionId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    usedResourceList: ").append(toIndentedString(usedResourceList)).append("\n");
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

