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



public class APIProductSearchResultAllOfDTO   {
  
    private String description = null;
    private String context = null;
    private String version = null;
    private String provider = null;
    private String status = null;
    private String thumbnailUri = null;
    private Boolean hasThumbnail = null;
    private Boolean monetizedInfo = null;
    private String businessOwner = null;
    private String businessOwnerEmail = null;
    private String technicalOwner = null;
    private String technicalOwnerEmail = null;
    private Boolean egress = null;

  /**
   * A brief description about the API
   **/
  public APIProductSearchResultAllOfDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A calculator API that supports basic operations", value = "A brief description about the API")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * A string that represents the context of the user&#39;s request
   **/
  public APIProductSearchResultAllOfDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPI", value = "A string that represents the context of the user's request")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * The version of the API Product
   **/
  public APIProductSearchResultAllOfDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "The version of the API Product")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * If the provider value is not given, the user invoking the API will be used as the provider. 
   **/
  public APIProductSearchResultAllOfDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "If the provider value is not given, the user invoking the API will be used as the provider. ")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * This describes in which status of the lifecycle the APIPRODUCT is
   **/
  public APIProductSearchResultAllOfDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "PUBLISHED", value = "This describes in which status of the lifecycle the APIPRODUCT is")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public APIProductSearchResultAllOfDTO thumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
    return this;
  }

  
  @ApiModelProperty(example = "/apis/01234567-0123-0123-0123-012345678901/thumbnail", value = "")
  @JsonProperty("thumbnailUri")
  public String getThumbnailUri() {
    return thumbnailUri;
  }
  public void setThumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
  }

  /**
   **/
  public APIProductSearchResultAllOfDTO hasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("hasThumbnail")
  public Boolean isHasThumbnail() {
    return hasThumbnail;
  }
  public void setHasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
  }

  /**
   **/
  public APIProductSearchResultAllOfDTO monetizedInfo(Boolean monetizedInfo) {
    this.monetizedInfo = monetizedInfo;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("monetizedInfo")
  public Boolean isMonetizedInfo() {
    return monetizedInfo;
  }
  public void setMonetizedInfo(Boolean monetizedInfo) {
    this.monetizedInfo = monetizedInfo;
  }

  /**
   **/
  public APIProductSearchResultAllOfDTO businessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
    return this;
  }

  
  @ApiModelProperty(example = "Business Owner", value = "")
  @JsonProperty("businessOwner")
  public String getBusinessOwner() {
    return businessOwner;
  }
  public void setBusinessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
  }

  /**
   **/
  public APIProductSearchResultAllOfDTO businessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
    return this;
  }

  
  @ApiModelProperty(example = "businessowner@abc.com", value = "")
  @JsonProperty("businessOwnerEmail")
  public String getBusinessOwnerEmail() {
    return businessOwnerEmail;
  }
  public void setBusinessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
  }

  /**
   **/
  public APIProductSearchResultAllOfDTO technicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
    return this;
  }

  
  @ApiModelProperty(example = "Technical Owner", value = "")
  @JsonProperty("TechnicalOwner")
  public String getTechnicalOwner() {
    return technicalOwner;
  }
  public void setTechnicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
  }

  /**
   **/
  public APIProductSearchResultAllOfDTO technicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
    return this;
  }

  
  @ApiModelProperty(example = "technicalowner@abc.com", value = "")
  @JsonProperty("TechnicalOwnerEmail")
  public String getTechnicalOwnerEmail() {
    return technicalOwnerEmail;
  }
  public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
  }

  /**
   * Whether the API is Egress or not
   **/
  public APIProductSearchResultAllOfDTO egress(Boolean egress) {
    this.egress = egress;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Whether the API is Egress or not")
  @JsonProperty("egress")
  public Boolean isEgress() {
    return egress;
  }
  public void setEgress(Boolean egress) {
    this.egress = egress;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIProductSearchResultAllOfDTO apIProductSearchResultAllOf = (APIProductSearchResultAllOfDTO) o;
    return Objects.equals(description, apIProductSearchResultAllOf.description) &&
        Objects.equals(context, apIProductSearchResultAllOf.context) &&
        Objects.equals(version, apIProductSearchResultAllOf.version) &&
        Objects.equals(provider, apIProductSearchResultAllOf.provider) &&
        Objects.equals(status, apIProductSearchResultAllOf.status) &&
        Objects.equals(thumbnailUri, apIProductSearchResultAllOf.thumbnailUri) &&
        Objects.equals(hasThumbnail, apIProductSearchResultAllOf.hasThumbnail) &&
        Objects.equals(monetizedInfo, apIProductSearchResultAllOf.monetizedInfo) &&
        Objects.equals(businessOwner, apIProductSearchResultAllOf.businessOwner) &&
        Objects.equals(businessOwnerEmail, apIProductSearchResultAllOf.businessOwnerEmail) &&
        Objects.equals(technicalOwner, apIProductSearchResultAllOf.technicalOwner) &&
        Objects.equals(technicalOwnerEmail, apIProductSearchResultAllOf.technicalOwnerEmail) &&
        Objects.equals(egress, apIProductSearchResultAllOf.egress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, context, version, provider, status, thumbnailUri, hasThumbnail, monetizedInfo, businessOwner, businessOwnerEmail, technicalOwner, technicalOwnerEmail, egress);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductSearchResultAllOfDTO {\n");
    
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    thumbnailUri: ").append(toIndentedString(thumbnailUri)).append("\n");
    sb.append("    hasThumbnail: ").append(toIndentedString(hasThumbnail)).append("\n");
    sb.append("    monetizedInfo: ").append(toIndentedString(monetizedInfo)).append("\n");
    sb.append("    businessOwner: ").append(toIndentedString(businessOwner)).append("\n");
    sb.append("    businessOwnerEmail: ").append(toIndentedString(businessOwnerEmail)).append("\n");
    sb.append("    technicalOwner: ").append(toIndentedString(technicalOwner)).append("\n");
    sb.append("    technicalOwnerEmail: ").append(toIndentedString(technicalOwnerEmail)).append("\n");
    sb.append("    egress: ").append(toIndentedString(egress)).append("\n");
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

