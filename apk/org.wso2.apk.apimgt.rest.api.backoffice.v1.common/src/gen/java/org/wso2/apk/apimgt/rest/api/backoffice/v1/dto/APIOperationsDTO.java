package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIOperationsDTO   {
  
    private String id = null;
    private String target = null;
    private String verb = null;
    private String usagePlan = null;

  /**
   **/
  public APIOperationsDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "postapiresource", value = "")
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

  
  @ApiModelProperty(example = "/order/{orderId}", value = "")
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

  
  @ApiModelProperty(example = "POST", value = "")
  @JsonProperty("verb")
  public String getVerb() {
    return verb;
  }
  public void setVerb(String verb) {
    this.verb = verb;
  }

  /**
   **/
  public APIOperationsDTO usagePlan(String usagePlan) {
    this.usagePlan = usagePlan;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "")
  @JsonProperty("usagePlan")
  public String getUsagePlan() {
    return usagePlan;
  }
  public void setUsagePlan(String usagePlan) {
    this.usagePlan = usagePlan;
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
        Objects.equals(verb, apIOperations.verb) &&
        Objects.equals(usagePlan, apIOperations.usagePlan);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, target, verb, usagePlan);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIOperationsDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    verb: ").append(toIndentedString(verb)).append("\n");
    sb.append("    usagePlan: ").append(toIndentedString(usagePlan)).append("\n");
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

