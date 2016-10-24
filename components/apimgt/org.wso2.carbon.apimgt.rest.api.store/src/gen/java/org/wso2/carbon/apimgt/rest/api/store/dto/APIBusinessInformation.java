package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * APIBusinessInformation
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T10:59:23.111+05:30")
public class APIBusinessInformation   {
  @JsonProperty("businessOwner")
  private String businessOwner = null;

  @JsonProperty("businessOwnerEmail")
  private String businessOwnerEmail = null;

  @JsonProperty("technicalOwner")
  private String technicalOwner = null;

  @JsonProperty("technicalOwnerEmail")
  private String technicalOwnerEmail = null;

  public APIBusinessInformation businessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
    return this;
  }

   /**
   * Get businessOwner
   * @return businessOwner
  **/
  @ApiModelProperty(value = "")
  public String getBusinessOwner() {
    return businessOwner;
  }

  public void setBusinessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
  }

  public APIBusinessInformation businessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
    return this;
  }

   /**
   * Get businessOwnerEmail
   * @return businessOwnerEmail
  **/
  @ApiModelProperty(value = "")
  public String getBusinessOwnerEmail() {
    return businessOwnerEmail;
  }

  public void setBusinessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
  }

  public APIBusinessInformation technicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
    return this;
  }

   /**
   * Get technicalOwner
   * @return technicalOwner
  **/
  @ApiModelProperty(value = "")
  public String getTechnicalOwner() {
    return technicalOwner;
  }

  public void setTechnicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
  }

  public APIBusinessInformation technicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
    return this;
  }

   /**
   * Get technicalOwnerEmail
   * @return technicalOwnerEmail
  **/
  @ApiModelProperty(value = "")
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
    APIBusinessInformation aPIBusinessInformation = (APIBusinessInformation) o;
    return Objects.equals(this.businessOwner, aPIBusinessInformation.businessOwner) &&
        Objects.equals(this.businessOwnerEmail, aPIBusinessInformation.businessOwnerEmail) &&
        Objects.equals(this.technicalOwner, aPIBusinessInformation.technicalOwner) &&
        Objects.equals(this.technicalOwnerEmail, aPIBusinessInformation.technicalOwnerEmail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(businessOwner, businessOwnerEmail, technicalOwner, technicalOwnerEmail);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIBusinessInformation {\n");
    
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

