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



public class ServiceMetadataDTO   {
  
    private String key = null;
    private String name = null;
    private String displayName = null;
    private String description = null;
    private String version = null;
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
    private SecurityTypeEnum securityType = null;
    private Boolean mutualSSLEnabled = false;

  /**
   **/
  public ServiceMetadataDTO key(String key) {
    this.key = key;
    return this;
  }

  
  @ApiModelProperty(example = "PizzashackEndpoint-1.0.0", value = "")
  @JsonProperty("key")
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }

  /**
   **/
  public ServiceMetadataDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Pizzashack-Endpoint", value = "")
  @JsonProperty("name")
 @Pattern(regexp="^[^\\*]+$")  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ServiceMetadataDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "Pizzashack-Endpoint", value = "")
  @JsonProperty("displayName")
 @Pattern(regexp="^[^\\*]+$")  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public ServiceMetadataDTO description(String description) {
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
  public ServiceMetadataDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "v1", value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public ServiceMetadataDTO serviceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    return this;
  }

  
  @ApiModelProperty(example = "http://localhost/pizzashack", value = "")
  @JsonProperty("serviceUrl")
  public String getServiceUrl() {
    return serviceUrl;
  }
  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  /**
   * The type of the provided API definition
   **/
  public ServiceMetadataDTO definitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
    return this;
  }

  
  @ApiModelProperty(example = "OAS3", value = "The type of the provided API definition")
  @JsonProperty("definitionType")
  public DefinitionTypeEnum getDefinitionType() {
    return definitionType;
  }
  public void setDefinitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
  }

  /**
   * The security type of the endpoint
   **/
  public ServiceMetadataDTO securityType(SecurityTypeEnum securityType) {
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
  public ServiceMetadataDTO mutualSSLEnabled(Boolean mutualSSLEnabled) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceMetadataDTO serviceMetadata = (ServiceMetadataDTO) o;
    return Objects.equals(key, serviceMetadata.key) &&
        Objects.equals(name, serviceMetadata.name) &&
        Objects.equals(displayName, serviceMetadata.displayName) &&
        Objects.equals(description, serviceMetadata.description) &&
        Objects.equals(version, serviceMetadata.version) &&
        Objects.equals(serviceUrl, serviceMetadata.serviceUrl) &&
        Objects.equals(definitionType, serviceMetadata.definitionType) &&
        Objects.equals(securityType, serviceMetadata.securityType) &&
        Objects.equals(mutualSSLEnabled, serviceMetadata.mutualSSLEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, name, displayName, description, version, serviceUrl, definitionType, securityType, mutualSSLEnabled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceMetadataDTO {\n");
    
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    serviceUrl: ").append(toIndentedString(serviceUrl)).append("\n");
    sb.append("    definitionType: ").append(toIndentedString(definitionType)).append("\n");
    sb.append("    securityType: ").append(toIndentedString(securityType)).append("\n");
    sb.append("    mutualSSLEnabled: ").append(toIndentedString(mutualSSLEnabled)).append("\n");
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

