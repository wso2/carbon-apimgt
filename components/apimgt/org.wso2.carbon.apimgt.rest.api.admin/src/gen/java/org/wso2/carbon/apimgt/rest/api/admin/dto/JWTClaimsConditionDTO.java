package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleConditionDTO;

/**
 * JWTClaimsConditionDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-05-04T16:42:24.822+05:30")
public class JWTClaimsConditionDTO extends ThrottleConditionDTO  {
  @JsonProperty("claimUrl")
  private String claimUrl = null;

  @JsonProperty("attribute")
  private String attribute = null;

  public JWTClaimsConditionDTO claimUrl(String claimUrl) {
    this.claimUrl = claimUrl;
    return this;
  }

   /**
   * Get claimUrl
   * @return claimUrl
  **/
  @ApiModelProperty(value = "")
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
  @ApiModelProperty(value = "")
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
        Objects.equals(this.attribute, jwTClaimsCondition.attribute) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(claimUrl, attribute, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class JWTClaimsConditionDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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

