package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SharedScopeUsedAPIInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SharedScopeUsageDTO   {
  
    private String id = null;
    private String name = null;
    private List<SharedScopeUsedAPIInfoDTO> usedApiList = new ArrayList<SharedScopeUsedAPIInfoDTO>();

  /**
   * UUID of the Scope. Valid only for shared scopes. 
   **/
  public SharedScopeUsageDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", required = true, value = "UUID of the Scope. Valid only for shared scopes. ")
  @JsonProperty("id")
  @NotNull
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * name of Scope 
   **/
  public SharedScopeUsageDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "apim:api_view", required = true, value = "name of Scope ")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * API list which have used the shared scope 
   **/
  public SharedScopeUsageDTO usedApiList(List<SharedScopeUsedAPIInfoDTO> usedApiList) {
    this.usedApiList = usedApiList;
    return this;
  }

  
  @ApiModelProperty(value = "API list which have used the shared scope ")
      @Valid
  @JsonProperty("usedApiList")
  public List<SharedScopeUsedAPIInfoDTO> getUsedApiList() {
    return usedApiList;
  }
  public void setUsedApiList(List<SharedScopeUsedAPIInfoDTO> usedApiList) {
    this.usedApiList = usedApiList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SharedScopeUsageDTO sharedScopeUsage = (SharedScopeUsageDTO) o;
    return Objects.equals(id, sharedScopeUsage.id) &&
        Objects.equals(name, sharedScopeUsage.name) &&
        Objects.equals(usedApiList, sharedScopeUsage.usedApiList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, usedApiList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SharedScopeUsageDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    usedApiList: ").append(toIndentedString(usedApiList)).append("\n");
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

