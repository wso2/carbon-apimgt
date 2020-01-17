package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class APIOperationsDTO   {
  
    private String id = null;
    private String target = null;
    private String verb = null;
    private String authType = "Any";
    private String throttlingPolicy = null;
    private List<String> scopes = new ArrayList<>();
    private List<String> usedProductIds = new ArrayList<>();
    private String amznResourceName = null;
    private Integer amznResourceTimeout = null;

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

  /**
   **/
  public APIOperationsDTO authType(String authType) {
    this.authType = authType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("authType")
  public String getAuthType() {
    return authType;
  }
  public void setAuthType(String authType) {
    this.authType = authType;
  }

  /**
   **/
  public APIOperationsDTO throttlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "")
  @JsonProperty("throttlingPolicy")
  public String getThrottlingPolicy() {
    return throttlingPolicy;
  }
  public void setThrottlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
  }

  /**
   **/
  public APIOperationsDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  /**
   **/
  public APIOperationsDTO usedProductIds(List<String> usedProductIds) {
    this.usedProductIds = usedProductIds;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("usedProductIds")
  public List<String> getUsedProductIds() {
    return usedProductIds;
  }
  public void setUsedProductIds(List<String> usedProductIds) {
    this.usedProductIds = usedProductIds;
  }

  /**
   **/
  public APIOperationsDTO amznResourceName(String amznResourceName) {
    this.amznResourceName = amznResourceName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("amznResourceName")
  public String getAmznResourceName() {
    return amznResourceName;
  }
  public void setAmznResourceName(String amznResourceName) {
    this.amznResourceName = amznResourceName;
  }

  /**
   **/
  public APIOperationsDTO amznResourceTimeout(Integer amznResourceTimeout) {
    this.amznResourceTimeout = amznResourceTimeout;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("amznResourceTimeout")
  public Integer getAmznResourceTimeout() {
    return amznResourceTimeout;
  }
  public void setAmznResourceTimeout(Integer amznResourceTimeout) {
    this.amznResourceTimeout = amznResourceTimeout;
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
        Objects.equals(authType, apIOperations.authType) &&
        Objects.equals(throttlingPolicy, apIOperations.throttlingPolicy) &&
        Objects.equals(scopes, apIOperations.scopes) &&
        Objects.equals(usedProductIds, apIOperations.usedProductIds) &&
        Objects.equals(amznResourceName, apIOperations.amznResourceName) &&
        Objects.equals(amznResourceTimeout, apIOperations.amznResourceTimeout);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, target, verb, authType, throttlingPolicy, scopes, usedProductIds, amznResourceName, amznResourceTimeout);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIOperationsDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    verb: ").append(toIndentedString(verb)).append("\n");
    sb.append("    authType: ").append(toIndentedString(authType)).append("\n");
    sb.append("    throttlingPolicy: ").append(toIndentedString(throttlingPolicy)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    usedProductIds: ").append(toIndentedString(usedProductIds)).append("\n");
    sb.append("    amznResourceName: ").append(toIndentedString(amznResourceName)).append("\n");
    sb.append("    amznResourceTimeout: ").append(toIndentedString(amznResourceTimeout)).append("\n");
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

