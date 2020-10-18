package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIMonetizationAttributesDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class APITiersDTO   {
  
    private String tierName = null;
    private String tierPlan = null;
    private APIMonetizationAttributesDTO monetizationAttributes = null;

  /**
   **/
  public APITiersDTO tierName(String tierName) {
    this.tierName = tierName;
    return this;
  }

  
  @ApiModelProperty(example = "Gold", value = "")
  @JsonProperty("tierName")
  public String getTierName() {
    return tierName;
  }
  public void setTierName(String tierName) {
    this.tierName = tierName;
  }

  /**
   **/
  public APITiersDTO tierPlan(String tierPlan) {
    this.tierPlan = tierPlan;
    return this;
  }

  
  @ApiModelProperty(example = "COMMERCIAL", value = "")
  @JsonProperty("tierPlan")
  public String getTierPlan() {
    return tierPlan;
  }
  public void setTierPlan(String tierPlan) {
    this.tierPlan = tierPlan;
  }

  /**
   **/
  public APITiersDTO monetizationAttributes(APIMonetizationAttributesDTO monetizationAttributes) {
    this.monetizationAttributes = monetizationAttributes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("monetizationAttributes")
  public APIMonetizationAttributesDTO getMonetizationAttributes() {
    return monetizationAttributes;
  }
  public void setMonetizationAttributes(APIMonetizationAttributesDTO monetizationAttributes) {
    this.monetizationAttributes = monetizationAttributes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APITiersDTO apITiers = (APITiersDTO) o;
    return Objects.equals(tierName, apITiers.tierName) &&
        Objects.equals(tierPlan, apITiers.tierPlan) &&
        Objects.equals(monetizationAttributes, apITiers.monetizationAttributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tierName, tierPlan, monetizationAttributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APITiersDTO {\n");
    
    sb.append("    tierName: ").append(toIndentedString(tierName)).append("\n");
    sb.append("    tierPlan: ").append(toIndentedString(tierPlan)).append("\n");
    sb.append("    monetizationAttributes: ").append(toIndentedString(monetizationAttributes)).append("\n");
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

