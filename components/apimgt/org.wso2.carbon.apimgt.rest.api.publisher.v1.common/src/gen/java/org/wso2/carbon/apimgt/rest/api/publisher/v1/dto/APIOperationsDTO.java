package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;
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
    private String authType = "Any";
    private String throttlingPolicy = null;
    private List<String> scopes = new ArrayList<String>();
    private List<String> usedProductIds = new ArrayList<String>();
    private String amznResourceName = null;
    private Integer amznResourceTimeout = null;
    private String payloadSchema = null;
    private String uriMapping = null;
    private APIOperationPoliciesDTO operationPolicies = null;

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
  public APIOperationsDTO authType(String authType) {
    this.authType = authType;
    return this;
  }

  
  @ApiModelProperty(example = "Application & Application User", value = "")
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

  
  @ApiModelProperty(example = "[]", value = "")
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

  
  @ApiModelProperty(example = "[]", value = "")
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

  /**
   **/
  public APIOperationsDTO payloadSchema(String payloadSchema) {
    this.payloadSchema = payloadSchema;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("payloadSchema")
  public String getPayloadSchema() {
    return payloadSchema;
  }
  public void setPayloadSchema(String payloadSchema) {
    this.payloadSchema = payloadSchema;
  }

  /**
   **/
  public APIOperationsDTO uriMapping(String uriMapping) {
    this.uriMapping = uriMapping;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("uriMapping")
  public String getUriMapping() {
    return uriMapping;
  }
  public void setUriMapping(String uriMapping) {
    this.uriMapping = uriMapping;
  }

  /**
   **/
  public APIOperationsDTO operationPolicies(APIOperationPoliciesDTO operationPolicies) {
    this.operationPolicies = operationPolicies;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("operationPolicies")
  public APIOperationPoliciesDTO getOperationPolicies() {
    return operationPolicies;
  }
  public void setOperationPolicies(APIOperationPoliciesDTO operationPolicies) {
    this.operationPolicies = operationPolicies;
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
        Objects.equals(amznResourceTimeout, apIOperations.amznResourceTimeout) &&
        Objects.equals(payloadSchema, apIOperations.payloadSchema) &&
        Objects.equals(uriMapping, apIOperations.uriMapping) &&
        Objects.equals(operationPolicies, apIOperations.operationPolicies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, target, verb, authType, throttlingPolicy, scopes, usedProductIds, amznResourceName, amznResourceTimeout, payloadSchema, uriMapping, operationPolicies);
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
    sb.append("    payloadSchema: ").append(toIndentedString(payloadSchema)).append("\n");
    sb.append("    uriMapping: ").append(toIndentedString(uriMapping)).append("\n");
    sb.append("    operationPolicies: ").append(toIndentedString(operationPolicies)).append("\n");
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

