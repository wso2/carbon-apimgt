package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIAiConfigurationEndpointConfigurationDTO   {
  

    @XmlType(name="AuthTypeEnum")
    @XmlEnum(String.class)
    public enum AuthTypeEnum {
        HEADER("header"),
        QUERY_PARAMETER("query_parameter");
        private String value;

        AuthTypeEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static AuthTypeEnum fromValue(String v) {
            for (AuthTypeEnum b : AuthTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private AuthTypeEnum authType = null;
    private String authKey = null;
    private String sandboxAuthValue = null;
    private String productionAuthValue = null;

  /**
   **/
  public APIAiConfigurationEndpointConfigurationDTO authType(AuthTypeEnum authType) {
    this.authType = authType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("authType")
  public AuthTypeEnum getAuthType() {
    return authType;
  }
  public void setAuthType(AuthTypeEnum authType) {
    this.authType = authType;
  }

  /**
   **/
  public APIAiConfigurationEndpointConfigurationDTO authKey(String authKey) {
    this.authKey = authKey;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("authKey")
  public String getAuthKey() {
    return authKey;
  }
  public void setAuthKey(String authKey) {
    this.authKey = authKey;
  }

  /**
   **/
  public APIAiConfigurationEndpointConfigurationDTO sandboxAuthValue(String sandboxAuthValue) {
    this.sandboxAuthValue = sandboxAuthValue;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sandboxAuthValue")
  public String getSandboxAuthValue() {
    return sandboxAuthValue;
  }
  public void setSandboxAuthValue(String sandboxAuthValue) {
    this.sandboxAuthValue = sandboxAuthValue;
  }

  /**
   **/
  public APIAiConfigurationEndpointConfigurationDTO productionAuthValue(String productionAuthValue) {
    this.productionAuthValue = productionAuthValue;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("productionAuthValue")
  public String getProductionAuthValue() {
    return productionAuthValue;
  }
  public void setProductionAuthValue(String productionAuthValue) {
    this.productionAuthValue = productionAuthValue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIAiConfigurationEndpointConfigurationDTO apIAiConfigurationEndpointConfiguration = (APIAiConfigurationEndpointConfigurationDTO) o;
    return Objects.equals(authType, apIAiConfigurationEndpointConfiguration.authType) &&
        Objects.equals(authKey, apIAiConfigurationEndpointConfiguration.authKey) &&
        Objects.equals(sandboxAuthValue, apIAiConfigurationEndpointConfiguration.sandboxAuthValue) &&
        Objects.equals(productionAuthValue, apIAiConfigurationEndpointConfiguration.productionAuthValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authType, authKey, sandboxAuthValue, productionAuthValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIAiConfigurationEndpointConfigurationDTO {\n");
    
    sb.append("    authType: ").append(toIndentedString(authType)).append("\n");
    sb.append("    authKey: ").append(toIndentedString(authKey)).append("\n");
    sb.append("    sandboxAuthValue: ").append(toIndentedString(sandboxAuthValue)).append("\n");
    sb.append("    productionAuthValue: ").append(toIndentedString(productionAuthValue)).append("\n");
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

