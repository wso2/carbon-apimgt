package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SharedScopeUsedAPIResourceInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SharedScopeUsedAPIInfoDTO   {
  
    private String name = null;
    private String context = null;
    private String version = null;
    private String provider = null;
    private List<SharedScopeUsedAPIResourceInfoDTO> usedResourceList = new ArrayList<SharedScopeUsedAPIResourceInfoDTO>();

  /**
   **/
  public SharedScopeUsedAPIInfoDTO name(String name) {
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
  public SharedScopeUsedAPIInfoDTO context(String context) {
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
  public SharedScopeUsedAPIInfoDTO version(String version) {
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
  public SharedScopeUsedAPIInfoDTO provider(String provider) {
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
   * Resource list which have used the shared scope within this API 
   **/
  public SharedScopeUsedAPIInfoDTO usedResourceList(List<SharedScopeUsedAPIResourceInfoDTO> usedResourceList) {
    this.usedResourceList = usedResourceList;
    return this;
  }

  
  @ApiModelProperty(value = "Resource list which have used the shared scope within this API ")
      @Valid
  @JsonProperty("usedResourceList")
  public List<SharedScopeUsedAPIResourceInfoDTO> getUsedResourceList() {
    return usedResourceList;
  }
  public void setUsedResourceList(List<SharedScopeUsedAPIResourceInfoDTO> usedResourceList) {
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
    SharedScopeUsedAPIInfoDTO sharedScopeUsedAPIInfo = (SharedScopeUsedAPIInfoDTO) o;
    return Objects.equals(name, sharedScopeUsedAPIInfo.name) &&
        Objects.equals(context, sharedScopeUsedAPIInfo.context) &&
        Objects.equals(version, sharedScopeUsedAPIInfo.version) &&
        Objects.equals(provider, sharedScopeUsedAPIInfo.provider) &&
        Objects.equals(usedResourceList, sharedScopeUsedAPIInfo.usedResourceList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, context, version, provider, usedResourceList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SharedScopeUsedAPIInfoDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
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

