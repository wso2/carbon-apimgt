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



public class APIProductBusinessInformationDTO   {
  
    private String businessOwner = null;
    private String businessOwnerEmail = null;
    private String technicalOwner = null;
    private String technicalOwnerEmail = null;

  /**
   **/
  public APIProductBusinessInformationDTO businessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
    return this;
  }

  
  @ApiModelProperty(example = "businessowner", value = "")
  @JsonProperty("businessOwner")
 @Size(max=120)  public String getBusinessOwner() {
    return businessOwner;
  }
  public void setBusinessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
  }

  /**
   **/
  public APIProductBusinessInformationDTO businessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
    return this;
  }

  
  @ApiModelProperty(example = "businessowner@wso2.com", value = "")
  @JsonProperty("businessOwnerEmail")
  public String getBusinessOwnerEmail() {
    return businessOwnerEmail;
  }
  public void setBusinessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
  }

  /**
   **/
  public APIProductBusinessInformationDTO technicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
    return this;
  }

  
  @ApiModelProperty(example = "technicalowner", value = "")
  @JsonProperty("technicalOwner")
 @Size(max=120)  public String getTechnicalOwner() {
    return technicalOwner;
  }
  public void setTechnicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
  }

  /**
   **/
  public APIProductBusinessInformationDTO technicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
    return this;
  }

  
  @ApiModelProperty(example = "technicalowner@wso2.com", value = "")
  @JsonProperty("technicalOwnerEmail")
  public String getTechnicalOwnerEmail() {
    return technicalOwnerEmail;
  }
  public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIProductBusinessInformationDTO apIProductBusinessInformation = (APIProductBusinessInformationDTO) o;
    return Objects.equals(businessOwner, apIProductBusinessInformation.businessOwner) &&
        Objects.equals(businessOwnerEmail, apIProductBusinessInformation.businessOwnerEmail) &&
        Objects.equals(technicalOwner, apIProductBusinessInformation.technicalOwner) &&
        Objects.equals(technicalOwnerEmail, apIProductBusinessInformation.technicalOwnerEmail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(businessOwner, businessOwnerEmail, technicalOwner, technicalOwnerEmail);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductBusinessInformationDTO {\n");
    
    sb.append("    businessOwner: ").append(toIndentedString(businessOwner)).append("\n");
    sb.append("    businessOwnerEmail: ").append(toIndentedString(businessOwnerEmail)).append("\n");
    sb.append("    technicalOwner: ").append(toIndentedString(technicalOwner)).append("\n");
    sb.append("    technicalOwnerEmail: ").append(toIndentedString(technicalOwnerEmail)).append("\n");
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

