package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class KeyManagerDTO   {
  
    private String name = null;
    private String type = null;
    private Boolean enabled = null;
    private String tenantDomain = null;
    private Object _configuration = null;

    @XmlType(name="TokenTypeEnum")
    @XmlEnum(String.class)
    public enum TokenTypeEnum {
        EXCHANGED("EXCHANGED"),
        DIRECT("DIRECT"),
        BOTH("BOTH");
        private String value;

        TokenTypeEnum (String v) {
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
        public static TokenTypeEnum fromValue(String v) {
            for (TokenTypeEnum b : TokenTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private TokenTypeEnum tokenType = TokenTypeEnum.DIRECT;

  /**
   **/
  public KeyManagerDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public KeyManagerDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public KeyManagerDTO enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("enabled")
  public Boolean isEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   **/
  public KeyManagerDTO tenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  /**
   **/
  public KeyManagerDTO _configuration(Object _configuration) {
    this._configuration = _configuration;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("configuration")
  public Object getConfiguration() {
    return _configuration;
  }
  public void setConfiguration(Object _configuration) {
    this._configuration = _configuration;
  }

  /**
   * The type of the tokens to be used (exchanged or without exchanged). Accepted values are EXCHANGED, DIRECT or BOTH.
   **/
  public KeyManagerDTO tokenType(TokenTypeEnum tokenType) {
    this.tokenType = tokenType;
    return this;
  }

  
  @ApiModelProperty(example = "EXCHANGED", value = "The type of the tokens to be used (exchanged or without exchanged). Accepted values are EXCHANGED, DIRECT or BOTH.")
  @JsonProperty("tokenType")
  public TokenTypeEnum getTokenType() {
    return tokenType;
  }
  public void setTokenType(TokenTypeEnum tokenType) {
    this.tokenType = tokenType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyManagerDTO keyManager = (KeyManagerDTO) o;
    return Objects.equals(name, keyManager.name) &&
        Objects.equals(type, keyManager.type) &&
        Objects.equals(enabled, keyManager.enabled) &&
        Objects.equals(tenantDomain, keyManager.tenantDomain) &&
        Objects.equals(_configuration, keyManager._configuration) &&
        Objects.equals(tokenType, keyManager.tokenType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, enabled, tenantDomain, _configuration, tokenType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
    sb.append("    _configuration: ").append(toIndentedString(_configuration)).append("\n");
    sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
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

