package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class JWTClaimsConditionDTO   {
  
    private String claimUrl = null;
    private String attribute = null;

  /**
   * JWT claim URL
   **/
  public JWTClaimsConditionDTO claimUrl(String claimUrl) {
    this.claimUrl = claimUrl;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "JWT claim URL")
  @JsonProperty("claimUrl")
  @NotNull
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

  
  @ApiModelProperty(required = true, value = "Attribute to be matched")
  @JsonProperty("attribute")
  @NotNull
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
    return Objects.equals(claimUrl, jwTClaimsCondition.claimUrl) &&
        Objects.equals(attribute, jwTClaimsCondition.attribute);
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

