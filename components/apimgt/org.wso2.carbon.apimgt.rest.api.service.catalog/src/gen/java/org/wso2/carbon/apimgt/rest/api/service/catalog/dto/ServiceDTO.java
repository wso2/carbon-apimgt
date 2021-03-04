package org.wso2.carbon.apimgt.rest.api.service.catalog.dto;

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



public class ServiceDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private String version = null;
    private String serviceKey = null;
    private String serviceUrl = null;

    @XmlType(name="DefinitionTypeEnum")
    @XmlEnum(String.class)
    public enum DefinitionTypeEnum {
        OAS2("OAS2"),
        OAS3("OAS3"),
        WSDL1("WSDL1"),
        WSDL2("WSDL2"),
        GRAPHQL_SDL("GRAPHQL_SDL"),
        ASYNC_API("ASYNC_API");
        private String value;

        DefinitionTypeEnum (String v) {
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
        public static DefinitionTypeEnum fromValue(String v) {
            for (DefinitionTypeEnum b : DefinitionTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private DefinitionTypeEnum definitionType = null;

    @XmlType(name="SecurityTypeEnum")
    @XmlEnum(String.class)
    public enum SecurityTypeEnum {
        BASIC("BASIC"),
        DIGEST("DIGEST"),
        OAUTH2("OAUTH2"),
        X509("X509"),
        API_KEY("API_KEY"),
        NONE("NONE");
        private String value;

        SecurityTypeEnum (String v) {
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
        public static SecurityTypeEnum fromValue(String v) {
            for (SecurityTypeEnum b : SecurityTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private SecurityTypeEnum securityType = SecurityTypeEnum.NONE;
    private Boolean mutualSSLEnabled = false;
    private Integer usage = null;
    private String createdTime = null;
    private String lastUpdatedTime = null;
    private String md5 = null;
    private String definitionUrl = null;

  /**
   **/
  public ServiceDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public ServiceDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Pizzashack-Endpoint", required = true, value = "")
  @JsonProperty("name")
  @NotNull
 @Pattern(regexp="^[^\\*]+$")  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ServiceDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A Catalog Entry that exposes a REST endpoint", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public ServiceDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "v1", required = true, value = "")
  @JsonProperty("version")
  @NotNull
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public ServiceDTO serviceKey(String serviceKey) {
    this.serviceKey = serviceKey;
    return this;
  }

  
  @ApiModelProperty(example = "Pizzashack-Endpoint-1.0.0", value = "")
  @JsonProperty("serviceKey")
  public String getServiceKey() {
    return serviceKey;
  }
  public void setServiceKey(String serviceKey) {
    this.serviceKey = serviceKey;
  }

  /**
   **/
  public ServiceDTO serviceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    return this;
  }

  
  @ApiModelProperty(example = "http://localhost/pizzashack", required = true, value = "")
  @JsonProperty("serviceUrl")
  @NotNull
  public String getServiceUrl() {
    return serviceUrl;
  }
  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  /**
   * The type of the provided API definition
   **/
  public ServiceDTO definitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
    return this;
  }

  
  @ApiModelProperty(example = "OAS3", required = true, value = "The type of the provided API definition")
  @JsonProperty("definitionType")
  @NotNull
  public DefinitionTypeEnum getDefinitionType() {
    return definitionType;
  }
  public void setDefinitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
  }

  /**
   * The security type of the endpoint
   **/
  public ServiceDTO securityType(SecurityTypeEnum securityType) {
    this.securityType = securityType;
    return this;
  }

  
  @ApiModelProperty(example = "BASIC", value = "The security type of the endpoint")
  @JsonProperty("securityType")
  public SecurityTypeEnum getSecurityType() {
    return securityType;
  }
  public void setSecurityType(SecurityTypeEnum securityType) {
    this.securityType = securityType;
  }

  /**
   * Whether Mutual SSL is enabled for the endpoint
   **/
  public ServiceDTO mutualSSLEnabled(Boolean mutualSSLEnabled) {
    this.mutualSSLEnabled = mutualSSLEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "Whether Mutual SSL is enabled for the endpoint")
  @JsonProperty("mutualSSLEnabled")
  public Boolean isMutualSSLEnabled() {
    return mutualSSLEnabled;
  }
  public void setMutualSSLEnabled(Boolean mutualSSLEnabled) {
    this.mutualSSLEnabled = mutualSSLEnabled;
  }

  /**
   * Number of usages of the service in APIs
   **/
  public ServiceDTO usage(Integer usage) {
    this.usage = usage;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of usages of the service in APIs")
  @JsonProperty("usage")
  public Integer getUsage() {
    return usage;
  }
  public void setUsage(Integer usage) {
    this.usage = usage;
  }

  /**
   **/
  public ServiceDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(example = "2020-02-20T13:57:16.229", value = "")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public ServiceDTO lastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
    return this;
  }

  
  @ApiModelProperty(example = "2020-02-20T13:57:16.229", value = "")
  @JsonProperty("lastUpdatedTime")
  public String getLastUpdatedTime() {
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  /**
   **/
  public ServiceDTO md5(String md5) {
    this.md5 = md5;
    return this;
  }

  
  @ApiModelProperty(example = "36583a6a249b410e7fc4f892029709cac09763ddb230e1a829d5f9134d1abd07", value = "")
  @JsonProperty("md5")
  public String getMd5() {
    return md5;
  }
  public void setMd5(String md5) {
    this.md5 = md5;
  }

  /**
   **/
  public ServiceDTO definitionUrl(String definitionUrl) {
    this.definitionUrl = definitionUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://petstore.swagger.io/v2/swagger.json", value = "")
  @JsonProperty("definitionUrl")
  public String getDefinitionUrl() {
    return definitionUrl;
  }
  public void setDefinitionUrl(String definitionUrl) {
    this.definitionUrl = definitionUrl;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceDTO service = (ServiceDTO) o;
    return Objects.equals(id, service.id) &&
        Objects.equals(name, service.name) &&
        Objects.equals(description, service.description) &&
        Objects.equals(version, service.version) &&
        Objects.equals(serviceKey, service.serviceKey) &&
        Objects.equals(serviceUrl, service.serviceUrl) &&
        Objects.equals(definitionType, service.definitionType) &&
        Objects.equals(securityType, service.securityType) &&
        Objects.equals(mutualSSLEnabled, service.mutualSSLEnabled) &&
        Objects.equals(usage, service.usage) &&
        Objects.equals(createdTime, service.createdTime) &&
        Objects.equals(lastUpdatedTime, service.lastUpdatedTime) &&
        Objects.equals(md5, service.md5) &&
        Objects.equals(definitionUrl, service.definitionUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, version, serviceKey, serviceUrl, definitionType, securityType, mutualSSLEnabled, usage, createdTime, lastUpdatedTime, md5, definitionUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    serviceKey: ").append(toIndentedString(serviceKey)).append("\n");
    sb.append("    serviceUrl: ").append(toIndentedString(serviceUrl)).append("\n");
    sb.append("    definitionType: ").append(toIndentedString(definitionType)).append("\n");
    sb.append("    securityType: ").append(toIndentedString(securityType)).append("\n");
    sb.append("    mutualSSLEnabled: ").append(toIndentedString(mutualSSLEnabled)).append("\n");
    sb.append("    usage: ").append(toIndentedString(usage)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    lastUpdatedTime: ").append(toIndentedString(lastUpdatedTime)).append("\n");
    sb.append("    md5: ").append(toIndentedString(md5)).append("\n");
    sb.append("    definitionUrl: ").append(toIndentedString(definitionUrl)).append("\n");
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

