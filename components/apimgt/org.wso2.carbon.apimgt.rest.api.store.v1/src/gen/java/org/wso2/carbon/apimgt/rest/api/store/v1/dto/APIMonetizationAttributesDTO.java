package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIMonetizationAttributesDTO   {
  
    private String fixedPrice = null;
    private String pricePerRequest = null;
    private String currencyType = null;
    private String billingCycle = null;

  /**
   **/
  public APIMonetizationAttributesDTO fixedPrice(String fixedPrice) {
    this.fixedPrice = fixedPrice;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "")
  @JsonProperty("fixedPrice")
  public String getFixedPrice() {
    return fixedPrice;
  }
  public void setFixedPrice(String fixedPrice) {
    this.fixedPrice = fixedPrice;
  }

  /**
   **/
  public APIMonetizationAttributesDTO pricePerRequest(String pricePerRequest) {
    this.pricePerRequest = pricePerRequest;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("pricePerRequest")
  public String getPricePerRequest() {
    return pricePerRequest;
  }
  public void setPricePerRequest(String pricePerRequest) {
    this.pricePerRequest = pricePerRequest;
  }

  /**
   **/
  public APIMonetizationAttributesDTO currencyType(String currencyType) {
    this.currencyType = currencyType;
    return this;
  }

  
  @ApiModelProperty(example = "USD", value = "")
  @JsonProperty("currencyType")
  public String getCurrencyType() {
    return currencyType;
  }
  public void setCurrencyType(String currencyType) {
    this.currencyType = currencyType;
  }

  /**
   **/
  public APIMonetizationAttributesDTO billingCycle(String billingCycle) {
    this.billingCycle = billingCycle;
    return this;
  }

  
  @ApiModelProperty(example = "month", value = "")
  @JsonProperty("billingCycle")
  public String getBillingCycle() {
    return billingCycle;
  }
  public void setBillingCycle(String billingCycle) {
    this.billingCycle = billingCycle;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIMonetizationAttributesDTO apIMonetizationAttributes = (APIMonetizationAttributesDTO) o;
    return Objects.equals(fixedPrice, apIMonetizationAttributes.fixedPrice) &&
        Objects.equals(pricePerRequest, apIMonetizationAttributes.pricePerRequest) &&
        Objects.equals(currencyType, apIMonetizationAttributes.currencyType) &&
        Objects.equals(billingCycle, apIMonetizationAttributes.billingCycle);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fixedPrice, pricePerRequest, currencyType, billingCycle);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIMonetizationAttributesDTO {\n");
    
    sb.append("    fixedPrice: ").append(toIndentedString(fixedPrice)).append("\n");
    sb.append("    pricePerRequest: ").append(toIndentedString(pricePerRequest)).append("\n");
    sb.append("    currencyType: ").append(toIndentedString(currencyType)).append("\n");
    sb.append("    billingCycle: ").append(toIndentedString(billingCycle)).append("\n");
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

