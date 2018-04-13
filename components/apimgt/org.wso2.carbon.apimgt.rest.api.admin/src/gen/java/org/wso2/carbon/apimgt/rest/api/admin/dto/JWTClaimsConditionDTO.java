package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * JWTClaimsConditionDTO
 */
public class JWTClaimsConditionDTO   {
  @SerializedName("claimUrl")
  private String claimUrl = null;

  @SerializedName("attribute")
  private String attribute = null;

  public JWTClaimsConditionDTO claimUrl(String claimUrl) {
    this.claimUrl = claimUrl;
    return this;
  }

   /**
   * Get claimUrl
   * @return claimUrl
  **/
  @ApiModelProperty(required = true, value = "")
  public String getClaimUrl() {
    return claimUrl;
  }

  public void setClaimUrl(String claimUrl) {
    this.claimUrl = claimUrl;
  }

  public JWTClaimsConditionDTO attribute(String attribute) {
    this.attribute = attribute;
    return this;
  }

   /**
   * Get attribute
   * @return attribute
  **/
  @ApiModelProperty(required = true, value = "")
  public String getAttribute() {
    return attribute;
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JWTClaimsConditionDTO jwTClaimsCondition = (JWTClaimsConditionDTO) o;
    return Objects.equals(this.claimUrl, jwTClaimsCondition.claimUrl) &&
        Objects.equals(this.attribute, jwTClaimsCondition.attribute);
  }

  @Override
  public int hashCode() {
    return Objects.hash(claimUrl, attribute);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class JWTClaimsConditionDTO {\n");
    
    sb.append("    claimUrl: ").append(toIndentedString(claimUrl)).append("\n");
    sb.append("    attribute: ").append(toIndentedString(attribute)).append("\n");
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

