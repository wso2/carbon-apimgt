package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

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



public class InlineResponse200DTO   {
  
    private Boolean successorFound = null;
    private String successorApiId = null;
    private String successorApiName = null;
    private String successorApiVersion = null;
    private Double similarityScore = null;
    private String sunsetHeaderValue = null;
    private String linkHeaderValue = null;
    private Boolean migrationRisk = null;
    private String message = null;

  /**
   * Whether a structural successor was found
   **/
  public InlineResponse200DTO successorFound(Boolean successorFound) {
    this.successorFound = successorFound;
    return this;
  }

  
  @ApiModelProperty(value = "Whether a structural successor was found")
  @JsonProperty("successorFound")
  public Boolean isSuccessorFound() {
    return successorFound;
  }
  public void setSuccessorFound(Boolean successorFound) {
    this.successorFound = successorFound;
  }

  /**
   * UUID of the recommended successor API
   **/
  public InlineResponse200DTO successorApiId(String successorApiId) {
    this.successorApiId = successorApiId;
    return this;
  }

  
  @ApiModelProperty(value = "UUID of the recommended successor API")
  @JsonProperty("successorApiId")
  public String getSuccessorApiId() {
    return successorApiId;
  }
  public void setSuccessorApiId(String successorApiId) {
    this.successorApiId = successorApiId;
  }

  /**
   * Name of the recommended successor API
   **/
  public InlineResponse200DTO successorApiName(String successorApiName) {
    this.successorApiName = successorApiName;
    return this;
  }

  
  @ApiModelProperty(value = "Name of the recommended successor API")
  @JsonProperty("successorApiName")
  public String getSuccessorApiName() {
    return successorApiName;
  }
  public void setSuccessorApiName(String successorApiName) {
    this.successorApiName = successorApiName;
  }

  /**
   * Version of the recommended successor API
   **/
  public InlineResponse200DTO successorApiVersion(String successorApiVersion) {
    this.successorApiVersion = successorApiVersion;
    return this;
  }

  
  @ApiModelProperty(value = "Version of the recommended successor API")
  @JsonProperty("successorApiVersion")
  public String getSuccessorApiVersion() {
    return successorApiVersion;
  }
  public void setSuccessorApiVersion(String successorApiVersion) {
    this.successorApiVersion = successorApiVersion;
  }

  /**
   * Structural similarity score (0.0 to 1.0)
   **/
  public InlineResponse200DTO similarityScore(Double similarityScore) {
    this.similarityScore = similarityScore;
    return this;
  }

  
  @ApiModelProperty(value = "Structural similarity score (0.0 to 1.0)")
  @JsonProperty("similarityScore")
  public Double getSimilarityScore() {
    return similarityScore;
  }
  public void setSimilarityScore(Double similarityScore) {
    this.similarityScore = similarityScore;
  }

  /**
   * RFC 8594 Sunset header value
   **/
  public InlineResponse200DTO sunsetHeaderValue(String sunsetHeaderValue) {
    this.sunsetHeaderValue = sunsetHeaderValue;
    return this;
  }

  
  @ApiModelProperty(value = "RFC 8594 Sunset header value")
  @JsonProperty("sunsetHeaderValue")
  public String getSunsetHeaderValue() {
    return sunsetHeaderValue;
  }
  public void setSunsetHeaderValue(String sunsetHeaderValue) {
    this.sunsetHeaderValue = sunsetHeaderValue;
  }

  /**
   * RFC 8594 Link header value with successor-version relation
   **/
  public InlineResponse200DTO linkHeaderValue(String linkHeaderValue) {
    this.linkHeaderValue = linkHeaderValue;
    return this;
  }

  
  @ApiModelProperty(value = "RFC 8594 Link header value with successor-version relation")
  @JsonProperty("linkHeaderValue")
  public String getLinkHeaderValue() {
    return linkHeaderValue;
  }
  public void setLinkHeaderValue(String linkHeaderValue) {
    this.linkHeaderValue = linkHeaderValue;
  }

  /**
   * Whether deprecation poses a migration risk (no successor found)
   **/
  public InlineResponse200DTO migrationRisk(Boolean migrationRisk) {
    this.migrationRisk = migrationRisk;
    return this;
  }

  
  @ApiModelProperty(value = "Whether deprecation poses a migration risk (no successor found)")
  @JsonProperty("migrationRisk")
  public Boolean isMigrationRisk() {
    return migrationRisk;
  }
  public void setMigrationRisk(Boolean migrationRisk) {
    this.migrationRisk = migrationRisk;
  }

  /**
   * Human-readable message about the deprecation guide result
   **/
  public InlineResponse200DTO message(String message) {
    this.message = message;
    return this;
  }

  
  @ApiModelProperty(value = "Human-readable message about the deprecation guide result")
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse200DTO inlineResponse200 = (InlineResponse200DTO) o;
    return Objects.equals(successorFound, inlineResponse200.successorFound) &&
        Objects.equals(successorApiId, inlineResponse200.successorApiId) &&
        Objects.equals(successorApiName, inlineResponse200.successorApiName) &&
        Objects.equals(successorApiVersion, inlineResponse200.successorApiVersion) &&
        Objects.equals(similarityScore, inlineResponse200.similarityScore) &&
        Objects.equals(sunsetHeaderValue, inlineResponse200.sunsetHeaderValue) &&
        Objects.equals(linkHeaderValue, inlineResponse200.linkHeaderValue) &&
        Objects.equals(migrationRisk, inlineResponse200.migrationRisk) &&
        Objects.equals(message, inlineResponse200.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(successorFound, successorApiId, successorApiName, successorApiVersion, similarityScore, sunsetHeaderValue, linkHeaderValue, migrationRisk, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse200DTO {\n");
    
    sb.append("    successorFound: ").append(toIndentedString(successorFound)).append("\n");
    sb.append("    successorApiId: ").append(toIndentedString(successorApiId)).append("\n");
    sb.append("    successorApiName: ").append(toIndentedString(successorApiName)).append("\n");
    sb.append("    successorApiVersion: ").append(toIndentedString(successorApiVersion)).append("\n");
    sb.append("    similarityScore: ").append(toIndentedString(similarityScore)).append("\n");
    sb.append("    sunsetHeaderValue: ").append(toIndentedString(sunsetHeaderValue)).append("\n");
    sb.append("    linkHeaderValue: ").append(toIndentedString(linkHeaderValue)).append("\n");
    sb.append("    migrationRisk: ").append(toIndentedString(migrationRisk)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

