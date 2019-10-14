package org.wso2.carbon.throttle.service.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ConditionDTO  {
  
  
  
  private String conditionType = null;
  
  
  private String conditionName = null;
  
  
  private String conditionValue = null;
  
  
  private Boolean isInverted = null;
  
  
  private String category = null;
  
  
  private String headerFieldName = null;
  
  
  private String headerFieldValue = null;
  
  
  private String startingIP = null;
  
  
  private String endingIP = null;
  
  
  private String specificIP = null;
  
  
  private String claimUri = null;
  
  
  private String claimAttrib = null;
  
  
  private String parameterName = null;
  
  
  private String parameterValue = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("conditionType")
  public String getConditionType() {
    return conditionType;
  }
  public void setConditionType(String conditionType) {
    this.conditionType = conditionType;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("conditionName")
  public String getConditionName() {
    return conditionName;
  }
  public void setConditionName(String conditionName) {
    this.conditionName = conditionName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("conditionValue")
  public String getConditionValue() {
    return conditionValue;
  }
  public void setConditionValue(String conditionValue) {
    this.conditionValue = conditionValue;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("isInverted")
  public Boolean getIsInverted() {
    return isInverted;
  }
  public void setIsInverted(Boolean isInverted) {
    this.isInverted = isInverted;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("headerFieldName")
  public String getHeaderFieldName() {
    return headerFieldName;
  }
  public void setHeaderFieldName(String headerFieldName) {
    this.headerFieldName = headerFieldName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("headerFieldValue")
  public String getHeaderFieldValue() {
    return headerFieldValue;
  }
  public void setHeaderFieldValue(String headerFieldValue) {
    this.headerFieldValue = headerFieldValue;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("startingIP")
  public String getStartingIP() {
    return startingIP;
  }
  public void setStartingIP(String startingIP) {
    this.startingIP = startingIP;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("endingIP")
  public String getEndingIP() {
    return endingIP;
  }
  public void setEndingIP(String endingIP) {
    this.endingIP = endingIP;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("specificIP")
  public String getSpecificIP() {
    return specificIP;
  }
  public void setSpecificIP(String specificIP) {
    this.specificIP = specificIP;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("claimUri")
  public String getClaimUri() {
    return claimUri;
  }
  public void setClaimUri(String claimUri) {
    this.claimUri = claimUri;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("claimAttrib")
  public String getClaimAttrib() {
    return claimAttrib;
  }
  public void setClaimAttrib(String claimAttrib) {
    this.claimAttrib = claimAttrib;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("parameterName")
  public String getParameterName() {
    return parameterName;
  }
  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("parameterValue")
  public String getParameterValue() {
    return parameterValue;
  }
  public void setParameterValue(String parameterValue) {
    this.parameterValue = parameterValue;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConditionDTO {\n");
    
    sb.append("  conditionType: ").append(conditionType).append("\n");
    sb.append("  conditionName: ").append(conditionName).append("\n");
    sb.append("  conditionValue: ").append(conditionValue).append("\n");
    sb.append("  isInverted: ").append(isInverted).append("\n");
    sb.append("  category: ").append(category).append("\n");
    sb.append("  headerFieldName: ").append(headerFieldName).append("\n");
    sb.append("  headerFieldValue: ").append(headerFieldValue).append("\n");
    sb.append("  startingIP: ").append(startingIP).append("\n");
    sb.append("  endingIP: ").append(endingIP).append("\n");
    sb.append("  specificIP: ").append(specificIP).append("\n");
    sb.append("  claimUri: ").append(claimUri).append("\n");
    sb.append("  claimAttrib: ").append(claimAttrib).append("\n");
    sb.append("  parameterName: ").append(parameterName).append("\n");
    sb.append("  parameterValue: ").append(parameterValue).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
