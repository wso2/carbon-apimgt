package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleConditionBaseDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class JWTClaimsConditionDTO   {
  
    private Boolean invertCondition = false;
    private String claimUrl = null;
    private String attribute = null;

  /**
   * Specifies whether inversion of the condition to be matched against the request.  **Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value (&#39;true&#39; or &#39;false&#39;) for the same type of conditional group. 
   **/
  public JWTClaimsConditionDTO invertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies whether inversion of the condition to be matched against the request.  **Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value ('true' or 'false') for the same type of conditional group. ")
  @JsonProperty("invertCondition")
  public Boolean isInvertCondition() {
    return invertCondition;
  }
  public void setInvertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
  }

  /**
   * JWT claim URL
   **/
  public JWTClaimsConditionDTO claimUrl(String claimUrl) {
    this.claimUrl = claimUrl;
    return this;
  }

  
  @ApiModelProperty(value = "JWT claim URL")
  @JsonProperty("claimUrl")
  public String getClaimUrl() {
    return claimUrl;
  }
  public void setClaimUrl(String claimUrl) {
    this.claimUrl = claimUrl;
  }

  /**
   * Attribute to be matched
   **/
  public JWTClaimsConditionDTO attribute(String attribute) {
    this.attribute = attribute;
    return this;
  }

  
  @ApiModelProperty(value = "Attribute to be matched")
  @JsonProperty("attribute")
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
    return Objects.equals(invertCondition, jwTClaimsCondition.invertCondition) &&
        Objects.equals(claimUrl, jwTClaimsCondition.claimUrl) &&
        Objects.equals(attribute, jwTClaimsCondition.attribute);
  }

  @Override
  public int hashCode() {
    return Objects.hash(invertCondition, claimUrl, attribute);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class JWTClaimsConditionDTO {\n");
    
    sb.append("    invertCondition: ").append(toIndentedString(invertCondition)).append("\n");
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

