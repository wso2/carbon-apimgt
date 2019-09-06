package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SubscriptionDTO   {
  
    private String subscriptionId = null;
    private String applicationId = null;
    private String apiId = null;
    private APIInfoDTO apiInfo = null;
    private String apiProductId = null;
    private APIProductInfoDTO apiProductInfo = null;
    private ApplicationInfoDTO applicationInfo = null;
    private String throttlingPolicy = null;

@XmlType(name="TypeEnum")
@XmlEnum(String.class)
public enum TypeEnum {

    @XmlEnumValue("API") API(String.valueOf("API")), @XmlEnumValue("API_PRODUCT") API_PRODUCT(String.valueOf("API_PRODUCT"));


    private String value;

    TypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TypeEnum fromValue(String v) {
        for (TypeEnum b : TypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private TypeEnum type = null;

@XmlType(name="StatusEnum")
@XmlEnum(String.class)
public enum StatusEnum {

    @XmlEnumValue("BLOCKED") BLOCKED(String.valueOf("BLOCKED")), @XmlEnumValue("PROD_ONLY_BLOCKED") PROD_ONLY_BLOCKED(String.valueOf("PROD_ONLY_BLOCKED")), @XmlEnumValue("UNBLOCKED") UNBLOCKED(String.valueOf("UNBLOCKED")), @XmlEnumValue("ON_HOLD") ON_HOLD(String.valueOf("ON_HOLD")), @XmlEnumValue("REJECTED") REJECTED(String.valueOf("REJECTED"));


    private String value;

    StatusEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static StatusEnum fromValue(String v) {
        for (StatusEnum b : StatusEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private StatusEnum status = null;

  /**
   * The UUID of the subscription
   **/
  public SubscriptionDTO subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

  
  @ApiModelProperty(example = "faae5fcc-cbae-40c4-bf43-89931630d313", value = "The UUID of the subscription")
  @JsonProperty("subscriptionId")
  public String getSubscriptionId() {
    return subscriptionId;
  }
  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  /**
   * The UUID of the application
   **/
  public SubscriptionDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(example = "b3ade481-30b0-4b38-9a67-498a40873a6d", required = true, value = "The UUID of the application")
  @JsonProperty("applicationId")
  @NotNull
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   * The unique identifier of the API.
   **/
  public SubscriptionDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(value = "The unique identifier of the API.")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public SubscriptionDTO apiInfo(APIInfoDTO apiInfo) {
    this.apiInfo = apiInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiInfo")
  public APIInfoDTO getApiInfo() {
    return apiInfo;
  }
  public void setApiInfo(APIInfoDTO apiInfo) {
    this.apiInfo = apiInfo;
  }

  /**
   * The unique identifier of the API Product.
   **/
  public SubscriptionDTO apiProductId(String apiProductId) {
    this.apiProductId = apiProductId;
    return this;
  }

  
  @ApiModelProperty(value = "The unique identifier of the API Product.")
  @JsonProperty("apiProductId")
  public String getApiProductId() {
    return apiProductId;
  }
  public void setApiProductId(String apiProductId) {
    this.apiProductId = apiProductId;
  }

  /**
   **/
  public SubscriptionDTO apiProductInfo(APIProductInfoDTO apiProductInfo) {
    this.apiProductInfo = apiProductInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiProductInfo")
  public APIProductInfoDTO getApiProductInfo() {
    return apiProductInfo;
  }
  public void setApiProductInfo(APIProductInfoDTO apiProductInfo) {
    this.apiProductInfo = apiProductInfo;
  }

  /**
   **/
  public SubscriptionDTO applicationInfo(ApplicationInfoDTO applicationInfo) {
    this.applicationInfo = applicationInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("applicationInfo")
  public ApplicationInfoDTO getApplicationInfo() {
    return applicationInfo;
  }
  public void setApplicationInfo(ApplicationInfoDTO applicationInfo) {
    this.applicationInfo = applicationInfo;
  }

  /**
   **/
  public SubscriptionDTO throttlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", required = true, value = "")
  @JsonProperty("throttlingPolicy")
  @NotNull
  public String getThrottlingPolicy() {
    return throttlingPolicy;
  }
  public void setThrottlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
  }

  /**
   **/
  public SubscriptionDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   **/
  public SubscriptionDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "UNBLOCKED", value = "")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionDTO subscription = (SubscriptionDTO) o;
    return Objects.equals(subscriptionId, subscription.subscriptionId) &&
        Objects.equals(applicationId, subscription.applicationId) &&
        Objects.equals(apiId, subscription.apiId) &&
        Objects.equals(apiInfo, subscription.apiInfo) &&
        Objects.equals(apiProductId, subscription.apiProductId) &&
        Objects.equals(apiProductInfo, subscription.apiProductInfo) &&
        Objects.equals(applicationInfo, subscription.applicationInfo) &&
        Objects.equals(throttlingPolicy, subscription.throttlingPolicy) &&
        Objects.equals(type, subscription.type) &&
        Objects.equals(status, subscription.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionId, applicationId, apiId, apiInfo, apiProductId, apiProductInfo, applicationInfo, throttlingPolicy, type, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionDTO {\n");
    
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    apiInfo: ").append(toIndentedString(apiInfo)).append("\n");
    sb.append("    apiProductId: ").append(toIndentedString(apiProductId)).append("\n");
    sb.append("    apiProductInfo: ").append(toIndentedString(apiProductInfo)).append("\n");
    sb.append("    applicationInfo: ").append(toIndentedString(applicationInfo)).append("\n");
    sb.append("    throttlingPolicy: ").append(toIndentedString(throttlingPolicy)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

