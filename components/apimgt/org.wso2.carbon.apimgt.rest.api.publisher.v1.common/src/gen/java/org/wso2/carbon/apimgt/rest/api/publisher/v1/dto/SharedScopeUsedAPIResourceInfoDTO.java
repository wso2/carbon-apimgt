package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;


import java.util.Objects;




public class SharedScopeUsedAPIResourceInfoDTO   {
  
    private String target = null;
    private String verb = null;

  /**
   **/
  public SharedScopeUsedAPIResourceInfoDTO target(String target) {
    this.target = target;
    return this;
  }

  
  @ApiModelProperty(example = "/add", value = "")
  @JsonProperty("target")
  public String getTarget() {
    return target;
  }
  public void setTarget(String target) {
    this.target = target;
  }

  /**
   **/
  public SharedScopeUsedAPIResourceInfoDTO verb(String verb) {
    this.verb = verb;
    return this;
  }

  
  @ApiModelProperty(example = "POST", value = "")
  @JsonProperty("verb")
  public String getVerb() {
    return verb;
  }
  public void setVerb(String verb) {
    this.verb = verb;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SharedScopeUsedAPIResourceInfoDTO sharedScopeUsedAPIResourceInfo = (SharedScopeUsedAPIResourceInfoDTO) o;
    return Objects.equals(target, sharedScopeUsedAPIResourceInfo.target) &&
        Objects.equals(verb, sharedScopeUsedAPIResourceInfo.verb);
  }

  @Override
  public int hashCode() {
    return Objects.hash(target, verb);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SharedScopeUsedAPIResourceInfoDTO {\n");
    
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    verb: ").append(toIndentedString(verb)).append("\n");
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

