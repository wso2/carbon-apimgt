package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicySpecAttributeDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class OperationPolicyDataDTO   {
  
    private String category = null;
    private String id = null;
    private String name = null;
    private String displayName = null;
    private String description = null;
    private List<String> applicableFlows = new ArrayList<String>();
    private List<String> supportedGateways = new ArrayList<String>();
    private List<String> supportedApiTypes = new ArrayList<String>();
    private Boolean isAPISpecific = null;
    private String md5 = null;
    private List<OperationPolicySpecAttributeDTO> policyAttributes = new ArrayList<OperationPolicySpecAttributeDTO>();

  /**
   **/
  public OperationPolicyDataDTO category(String category) {
    this.category = category;
    return this;
  }

  
  @ApiModelProperty(example = "Mediation", value = "")
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }

  /**
   **/
  public OperationPolicyDataDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "121223q41-24141-124124124-12414", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public OperationPolicyDataDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "removeHeaderPolicy", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public OperationPolicyDataDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "Remove Header Policy", value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public OperationPolicyDataDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "With this policy, user can add a new header to the request", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public OperationPolicyDataDTO applicableFlows(List<String> applicableFlows) {
    this.applicableFlows = applicableFlows;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("applicableFlows")
  public List<String> getApplicableFlows() {
    return applicableFlows;
  }
  public void setApplicableFlows(List<String> applicableFlows) {
    this.applicableFlows = applicableFlows;
  }

  /**
   **/
  public OperationPolicyDataDTO supportedGateways(List<String> supportedGateways) {
    this.supportedGateways = supportedGateways;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("supportedGateways")
  public List<String> getSupportedGateways() {
    return supportedGateways;
  }
  public void setSupportedGateways(List<String> supportedGateways) {
    this.supportedGateways = supportedGateways;
  }

  /**
   **/
  public OperationPolicyDataDTO supportedApiTypes(List<String> supportedApiTypes) {
    this.supportedApiTypes = supportedApiTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("supportedApiTypes")
  public List<String> getSupportedApiTypes() {
    return supportedApiTypes;
  }
  public void setSupportedApiTypes(List<String> supportedApiTypes) {
    this.supportedApiTypes = supportedApiTypes;
  }

  /**
   **/
  public OperationPolicyDataDTO isAPISpecific(Boolean isAPISpecific) {
    this.isAPISpecific = isAPISpecific;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("isAPISpecific")
  public Boolean isIsAPISpecific() {
    return isAPISpecific;
  }
  public void setIsAPISpecific(Boolean isAPISpecific) {
    this.isAPISpecific = isAPISpecific;
  }

  /**
   **/
  public OperationPolicyDataDTO md5(String md5) {
    this.md5 = md5;
    return this;
  }

  
  @ApiModelProperty(example = "121223q41-24141-124124124-12414", value = "")
  @JsonProperty("md5")
  public String getMd5() {
    return md5;
  }
  public void setMd5(String md5) {
    this.md5 = md5;
  }

  /**
   **/
  public OperationPolicyDataDTO policyAttributes(List<OperationPolicySpecAttributeDTO> policyAttributes) {
    this.policyAttributes = policyAttributes;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("policyAttributes")
  public List<OperationPolicySpecAttributeDTO> getPolicyAttributes() {
    return policyAttributes;
  }
  public void setPolicyAttributes(List<OperationPolicySpecAttributeDTO> policyAttributes) {
    this.policyAttributes = policyAttributes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationPolicyDataDTO operationPolicyData = (OperationPolicyDataDTO) o;
    return Objects.equals(category, operationPolicyData.category) &&
        Objects.equals(id, operationPolicyData.id) &&
        Objects.equals(name, operationPolicyData.name) &&
        Objects.equals(displayName, operationPolicyData.displayName) &&
        Objects.equals(description, operationPolicyData.description) &&
        Objects.equals(applicableFlows, operationPolicyData.applicableFlows) &&
        Objects.equals(supportedGateways, operationPolicyData.supportedGateways) &&
        Objects.equals(supportedApiTypes, operationPolicyData.supportedApiTypes) &&
        Objects.equals(isAPISpecific, operationPolicyData.isAPISpecific) &&
        Objects.equals(md5, operationPolicyData.md5) &&
        Objects.equals(policyAttributes, operationPolicyData.policyAttributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, id, name, displayName, description, applicableFlows, supportedGateways, supportedApiTypes, isAPISpecific, md5, policyAttributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationPolicyDataDTO {\n");
    
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    applicableFlows: ").append(toIndentedString(applicableFlows)).append("\n");
    sb.append("    supportedGateways: ").append(toIndentedString(supportedGateways)).append("\n");
    sb.append("    supportedApiTypes: ").append(toIndentedString(supportedApiTypes)).append("\n");
    sb.append("    isAPISpecific: ").append(toIndentedString(isAPISpecific)).append("\n");
    sb.append("    md5: ").append(toIndentedString(md5)).append("\n");
    sb.append("    policyAttributes: ").append(toIndentedString(policyAttributes)).append("\n");
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

