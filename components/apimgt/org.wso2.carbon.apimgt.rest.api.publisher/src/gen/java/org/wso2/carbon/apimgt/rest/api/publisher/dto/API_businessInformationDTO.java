package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * API_businessInformationDTO
 */
public class API_businessInformationDTO   {
  @SerializedName("businessOwner")
  private String businessOwner = null;

  @SerializedName("businessOwnerEmail")
  private String businessOwnerEmail = null;

  @SerializedName("technicalOwner")
  private String technicalOwner = null;

  @SerializedName("technicalOwnerEmail")
  private String technicalOwnerEmail = null;

  public API_businessInformationDTO businessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
    return this;
  }

   /**
   * Get businessOwner
   * @return businessOwner
  **/
  @ApiModelProperty(example = "businessowner", value = "")
  public String getBusinessOwner() {
    return businessOwner;
  }

  public void setBusinessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
  }

  public API_businessInformationDTO businessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
    return this;
  }

   /**
   * Get businessOwnerEmail
   * @return businessOwnerEmail
  **/
  @ApiModelProperty(example = "businessowner@wso2.com", value = "")
  public String getBusinessOwnerEmail() {
    return businessOwnerEmail;
  }

  public void setBusinessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
  }

  public API_businessInformationDTO technicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
    return this;
  }

   /**
   * Get technicalOwner
   * @return technicalOwner
  **/
  @ApiModelProperty(example = "technicalowner", value = "")
  public String getTechnicalOwner() {
    return technicalOwner;
  }

  public void setTechnicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
  }

  public API_businessInformationDTO technicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
    return this;
  }

   /**
   * Get technicalOwnerEmail
   * @return technicalOwnerEmail
  **/
  @ApiModelProperty(example = "technicalowner@wso2.com", value = "")
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
    API_businessInformationDTO apIBusinessInformation = (API_businessInformationDTO) o;
    return Objects.equals(this.businessOwner, apIBusinessInformation.businessOwner) &&
        Objects.equals(this.businessOwnerEmail, apIBusinessInformation.businessOwnerEmail) &&
        Objects.equals(this.technicalOwner, apIBusinessInformation.technicalOwner) &&
        Objects.equals(this.technicalOwnerEmail, apIBusinessInformation.technicalOwnerEmail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(businessOwner, businessOwnerEmail, technicalOwner, technicalOwnerEmail);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class API_businessInformationDTO {\n");
    
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

