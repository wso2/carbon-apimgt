package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class APIOperationsDTO   {
  
    private String id = null;
    private String target = null;
    private String verb = null;

  /**
   **/
  public APIOperationsDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "apioperation", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public APIOperationsDTO target(String target) {
    this.target = target;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("target")
  public String getTarget() {
    return target;
  }
  public void setTarget(String target) {
    this.target = target;
  }

  /**
   **/
  public APIOperationsDTO verb(String verb) {
    this.verb = verb;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
    APIOperationsDTO apIOperations = (APIOperationsDTO) o;
    return Objects.equals(id, apIOperations.id) &&
        Objects.equals(target, apIOperations.target) &&
        Objects.equals(verb, apIOperations.verb);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, target, verb);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIOperationsDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

