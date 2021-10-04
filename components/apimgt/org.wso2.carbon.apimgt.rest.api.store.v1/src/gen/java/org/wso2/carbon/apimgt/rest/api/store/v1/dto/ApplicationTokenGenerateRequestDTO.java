package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApplicationTokenGenerateRequestDTO   {
  
    private String consumerSecret = null;
    private Long validityPeriod = null;
    private List<String> scopes = new ArrayList<String>();
    private String revokeToken = null;

    @XmlType(name="GrantTypeEnum")
    @XmlEnum(String.class)
    public enum GrantTypeEnum {
        CLIENT_CREDENTIALS("CLIENT_CREDENTIALS"),
        TOKEN_EXCHANGE("TOKEN_EXCHANGE");
        private String value;

        GrantTypeEnum (String v) {
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
        public static GrantTypeEnum fromValue(String v) {
            for (GrantTypeEnum b : GrantTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private GrantTypeEnum grantType = GrantTypeEnum.CLIENT_CREDENTIALS;
    private Object additionalProperties = null;

  /**
   * Consumer secret of the application
   **/
  public ApplicationTokenGenerateRequestDTO consumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
    return this;
  }

  
  @ApiModelProperty(example = "cV5pvyisxug5b5QZInq9cGZrMOMa", value = "Consumer secret of the application")
  @JsonProperty("consumerSecret")
  public String getConsumerSecret() {
    return consumerSecret;
  }
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  /**
   * Token validity period
   **/
  public ApplicationTokenGenerateRequestDTO validityPeriod(Long validityPeriod) {
    this.validityPeriod = validityPeriod;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "Token validity period")
  @JsonProperty("validityPeriod")
  public Long getValidityPeriod() {
    return validityPeriod;
  }
  public void setValidityPeriod(Long validityPeriod) {
    this.validityPeriod = validityPeriod;
  }

  /**
   * Allowed scopes (space seperated) for the access token
   **/
  public ApplicationTokenGenerateRequestDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(example = "[\"apim:subscribe\"]", value = "Allowed scopes (space seperated) for the access token")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  /**
   * Token to be revoked, if any
   **/
  public ApplicationTokenGenerateRequestDTO revokeToken(String revokeToken) {
    this.revokeToken = revokeToken;
    return this;
  }

  
  @ApiModelProperty(value = "Token to be revoked, if any")
  @JsonProperty("revokeToken")
  public String getRevokeToken() {
    return revokeToken;
  }
  public void setRevokeToken(String revokeToken) {
    this.revokeToken = revokeToken;
  }

  /**
   **/
  public ApplicationTokenGenerateRequestDTO grantType(GrantTypeEnum grantType) {
    this.grantType = grantType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("grantType")
  public GrantTypeEnum getGrantType() {
    return grantType;
  }
  public void setGrantType(GrantTypeEnum grantType) {
    this.grantType = grantType;
  }

  /**
   * Additional parameters if Authorization server needs any
   **/
  public ApplicationTokenGenerateRequestDTO additionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "Additional parameters if Authorization server needs any")
      @Valid
  @JsonProperty("additionalProperties")
  public Object getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationTokenGenerateRequestDTO applicationTokenGenerateRequest = (ApplicationTokenGenerateRequestDTO) o;
    return Objects.equals(consumerSecret, applicationTokenGenerateRequest.consumerSecret) &&
        Objects.equals(validityPeriod, applicationTokenGenerateRequest.validityPeriod) &&
        Objects.equals(scopes, applicationTokenGenerateRequest.scopes) &&
        Objects.equals(revokeToken, applicationTokenGenerateRequest.revokeToken) &&
        Objects.equals(grantType, applicationTokenGenerateRequest.grantType) &&
        Objects.equals(additionalProperties, applicationTokenGenerateRequest.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerSecret, validityPeriod, scopes, revokeToken, grantType, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationTokenGenerateRequestDTO {\n");
    
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
    sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    revokeToken: ").append(toIndentedString(revokeToken)).append("\n");
    sb.append("    grantType: ").append(toIndentedString(grantType)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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

