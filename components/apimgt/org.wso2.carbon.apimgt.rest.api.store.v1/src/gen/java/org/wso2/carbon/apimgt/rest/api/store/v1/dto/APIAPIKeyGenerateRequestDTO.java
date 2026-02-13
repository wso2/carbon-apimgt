package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

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



public class APIAPIKeyGenerateRequestDTO   {
  
    private String keyDisplayName = null;

    @XmlType(name="EnvironmentTypeEnum")
    @XmlEnum(String.class)
    public enum EnvironmentTypeEnum {
        PRODUCTION("PRODUCTION"),
        SANDBOX("SANDBOX");
        private String value;

        EnvironmentTypeEnum (String v) {
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
        public static EnvironmentTypeEnum fromValue(String v) {
            for (EnvironmentTypeEnum b : EnvironmentTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private EnvironmentTypeEnum environmentType = null;
    private Integer validityPeriod = null;
    private Object additionalProperties = null;

  /**
   * API Key name
   **/
  public APIAPIKeyGenerateRequestDTO keyDisplayName(String keyDisplayName) {
    this.keyDisplayName = keyDisplayName;
    return this;
  }

  
  @ApiModelProperty(example = "Test_Key", value = "API Key name")
  @JsonProperty("keyDisplayName")
  public String getKeyDisplayName() {
    return keyDisplayName;
  }
  public void setKeyDisplayName(String keyDisplayName) {
    this.keyDisplayName = keyDisplayName;
  }

  /**
   * Environment type of the API key
   **/
  public APIAPIKeyGenerateRequestDTO environmentType(EnvironmentTypeEnum environmentType) {
    this.environmentType = environmentType;
    return this;
  }

  
  @ApiModelProperty(example = "PRODUCTION", value = "Environment type of the API key")
  @JsonProperty("environmentType")
  public EnvironmentTypeEnum getEnvironmentType() {
    return environmentType;
  }
  public void setEnvironmentType(EnvironmentTypeEnum environmentType) {
    this.environmentType = environmentType;
  }

  /**
   * API key validity period
   **/
  public APIAPIKeyGenerateRequestDTO validityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "API key validity period")
  @JsonProperty("validityPeriod")
  public Integer getValidityPeriod() {
    return validityPeriod;
  }
  public void setValidityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
  }

  /**
   * Additional parameters if Authorization server needs any
   **/
  public APIAPIKeyGenerateRequestDTO additionalProperties(Object additionalProperties) {
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
    APIAPIKeyGenerateRequestDTO apIAPIKeyGenerateRequest = (APIAPIKeyGenerateRequestDTO) o;
    return Objects.equals(keyDisplayName, apIAPIKeyGenerateRequest.keyDisplayName) &&
        Objects.equals(environmentType, apIAPIKeyGenerateRequest.environmentType) &&
        Objects.equals(validityPeriod, apIAPIKeyGenerateRequest.validityPeriod) &&
        Objects.equals(additionalProperties, apIAPIKeyGenerateRequest.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyDisplayName, environmentType, validityPeriod, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIAPIKeyGenerateRequestDTO {\n");
    
    sb.append("    keyDisplayName: ").append(toIndentedString(keyDisplayName)).append("\n");
    sb.append("    environmentType: ").append(toIndentedString(environmentType)).append("\n");
    sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
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

