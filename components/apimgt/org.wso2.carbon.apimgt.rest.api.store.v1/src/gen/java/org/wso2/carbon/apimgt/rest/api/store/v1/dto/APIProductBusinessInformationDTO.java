package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class APIProductBusinessInformationDTO   {
  
    private String businessOwner = null;
    private String businessOwnerEmail = null;

  /**
   **/
  public APIProductBusinessInformationDTO businessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
    return this;
  }

  
  @ApiModelProperty(example = "businessowner", value = "")
  @JsonProperty("businessOwner")
  public String getBusinessOwner() {
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
        Objects.equals(businessOwnerEmail, apIProductBusinessInformation.businessOwnerEmail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(businessOwner, businessOwnerEmail);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductBusinessInformationDTO {\n");
    
    sb.append("    businessOwner: ").append(toIndentedString(businessOwner)).append("\n");
    sb.append("    businessOwnerEmail: ").append(toIndentedString(businessOwnerEmail)).append("\n");
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

