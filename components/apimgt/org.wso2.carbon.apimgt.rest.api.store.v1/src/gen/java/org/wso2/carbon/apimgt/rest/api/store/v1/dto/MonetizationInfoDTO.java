package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class MonetizationInfoDTO   {
  

@XmlType(name="BillingTypeEnum")
@XmlEnum(String.class)
public enum BillingTypeEnum {

    @XmlEnumValue("fixedPrice") FIXEDPRICE(String.valueOf("fixedPrice")), @XmlEnumValue("dynamicRate") DYNAMICRATE(String.valueOf("dynamicRate"));


    private String value;

    BillingTypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static BillingTypeEnum fromValue(String v) {
        for (BillingTypeEnum b : BillingTypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private BillingTypeEnum billingType = null;
    private String billingCycle = null;
    private String fixedPrice = null;
    private String pricePerRequest = null;
    private String currencyType = null;

  /**
   **/
  public MonetizationInfoDTO billingType(BillingTypeEnum billingType) {
    this.billingType = billingType;
    return this;
  }

  
  @ApiModelProperty(example = "fixedPrice", value = "")
  @JsonProperty("billingType")
  public BillingTypeEnum getBillingType() {
    return billingType;
  }
  public void setBillingType(BillingTypeEnum billingType) {
    this.billingType = billingType;
  }

  /**
   **/
  public MonetizationInfoDTO billingCycle(String billingCycle) {
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

  /**
   **/
  public MonetizationInfoDTO fixedPrice(String fixedPrice) {
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
  public MonetizationInfoDTO pricePerRequest(String pricePerRequest) {
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
  public MonetizationInfoDTO currencyType(String currencyType) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MonetizationInfoDTO monetizationInfo = (MonetizationInfoDTO) o;
    return Objects.equals(billingType, monetizationInfo.billingType) &&
        Objects.equals(billingCycle, monetizationInfo.billingCycle) &&
        Objects.equals(fixedPrice, monetizationInfo.fixedPrice) &&
        Objects.equals(pricePerRequest, monetizationInfo.pricePerRequest) &&
        Objects.equals(currencyType, monetizationInfo.currencyType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(billingType, billingCycle, fixedPrice, pricePerRequest, currencyType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MonetizationInfoDTO {\n");
    
    sb.append("    billingType: ").append(toIndentedString(billingType)).append("\n");
    sb.append("    billingCycle: ").append(toIndentedString(billingCycle)).append("\n");
    sb.append("    fixedPrice: ").append(toIndentedString(fixedPrice)).append("\n");
    sb.append("    pricePerRequest: ").append(toIndentedString(pricePerRequest)).append("\n");
    sb.append("    currencyType: ").append(toIndentedString(currencyType)).append("\n");
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

