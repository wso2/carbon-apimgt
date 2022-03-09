package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesMapDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIInfoDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private String context = null;
    private List<APIInfoAdditionalPropertiesDTO> additionalProperties = new ArrayList<APIInfoAdditionalPropertiesDTO>();
    private Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<String, APIInfoAdditionalPropertiesMapDTO>();
    private String version = null;
    private String provider = null;
    private String type = null;

    @XmlType(name="AudienceEnum")
    @XmlEnum(String.class)
    public enum AudienceEnum {
        PUBLIC("PUBLIC"),
        SINGLE("SINGLE");
        private String value;

        AudienceEnum (String v) {
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
        public static AudienceEnum fromValue(String v) {
            for (AudienceEnum b : AudienceEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private AudienceEnum audience = null;
    private String lifeCycleStatus = null;
    private String workflowStatus = null;
    private Boolean hasThumbnail = null;
    private List<String> securityScheme = new ArrayList<String>();
    private String createdTime = null;
    private String updatedTime = null;
    private String gatewayVendor = null;
    private Boolean advertiseOnly = null;

  /**
   **/
  public APIInfoDTO id(String id) {
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
  public APIInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPI", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public APIInfoDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A calculator API that supports basic operations", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public APIInfoDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPI", value = "")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * Map of custom properties of API
   **/
  public APIInfoDTO additionalProperties(List<APIInfoAdditionalPropertiesDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "Map of custom properties of API")
      @Valid
  @JsonProperty("additionalProperties")
  public List<APIInfoAdditionalPropertiesDTO> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(List<APIInfoAdditionalPropertiesDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   **/
  public APIInfoDTO additionalPropertiesMap(Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap) {
    this.additionalPropertiesMap = additionalPropertiesMap;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("additionalPropertiesMap")
  public Map<String, APIInfoAdditionalPropertiesMapDTO> getAdditionalPropertiesMap() {
    return additionalPropertiesMap;
  }
  public void setAdditionalPropertiesMap(Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap) {
    this.additionalPropertiesMap = additionalPropertiesMap;
  }

  /**
   **/
  public APIInfoDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * If the provider value is not given, the user invoking the API will be used as the provider. 
   **/
  public APIInfoDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "If the provider value is not given, the user invoking the API will be used as the provider. ")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   **/
  public APIInfoDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "HTTP", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * The audience of the API. Accepted values are PUBLIC, SINGLE
   **/
  public APIInfoDTO audience(AudienceEnum audience) {
    this.audience = audience;
    return this;
  }

  
  @ApiModelProperty(example = "PUBLIC", value = "The audience of the API. Accepted values are PUBLIC, SINGLE")
  @JsonProperty("audience")
  public AudienceEnum getAudience() {
    return audience;
  }
  public void setAudience(AudienceEnum audience) {
    this.audience = audience;
  }

  /**
   **/
  public APIInfoDTO lifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
    return this;
  }

  
  @ApiModelProperty(example = "CREATED", value = "")
  @JsonProperty("lifeCycleStatus")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }
  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  /**
   **/
  public APIInfoDTO workflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
    return this;
  }

  
  @ApiModelProperty(example = "APPROVED", value = "")
  @JsonProperty("workflowStatus")
  public String getWorkflowStatus() {
    return workflowStatus;
  }
  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  /**
   **/
  public APIInfoDTO hasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("hasThumbnail")
  public Boolean isHasThumbnail() {
    return hasThumbnail;
  }
  public void setHasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
  }

  /**
   **/
  public APIInfoDTO securityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("securityScheme")
  public List<String> getSecurityScheme() {
    return securityScheme;
  }
  public void setSecurityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
  }

  /**
   **/
  public APIInfoDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(example = "2021-02-11 09:57:25", value = "")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public APIInfoDTO updatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

  
  @ApiModelProperty(example = "2021-02-11 09:57:25", value = "")
  @JsonProperty("updatedTime")
  public String getUpdatedTime() {
    return updatedTime;
  }
  public void setUpdatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
  }

  /**
   **/
  public APIInfoDTO gatewayVendor(String gatewayVendor) {
    this.gatewayVendor = gatewayVendor;
    return this;
  }

  
  @ApiModelProperty(example = "wso2", value = "")
  @JsonProperty("gatewayVendor")
  public String getGatewayVendor() {
    return gatewayVendor;
  }
  public void setGatewayVendor(String gatewayVendor) {
    this.gatewayVendor = gatewayVendor;
  }

  /**
   **/
  public APIInfoDTO advertiseOnly(Boolean advertiseOnly) {
    this.advertiseOnly = advertiseOnly;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("advertiseOnly")
  public Boolean isAdvertiseOnly() {
    return advertiseOnly;
  }
  public void setAdvertiseOnly(Boolean advertiseOnly) {
    this.advertiseOnly = advertiseOnly;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIInfoDTO apIInfo = (APIInfoDTO) o;
    return Objects.equals(id, apIInfo.id) &&
        Objects.equals(name, apIInfo.name) &&
        Objects.equals(description, apIInfo.description) &&
        Objects.equals(context, apIInfo.context) &&
        Objects.equals(additionalProperties, apIInfo.additionalProperties) &&
        Objects.equals(additionalPropertiesMap, apIInfo.additionalPropertiesMap) &&
        Objects.equals(version, apIInfo.version) &&
        Objects.equals(provider, apIInfo.provider) &&
        Objects.equals(type, apIInfo.type) &&
        Objects.equals(audience, apIInfo.audience) &&
        Objects.equals(lifeCycleStatus, apIInfo.lifeCycleStatus) &&
        Objects.equals(workflowStatus, apIInfo.workflowStatus) &&
        Objects.equals(hasThumbnail, apIInfo.hasThumbnail) &&
        Objects.equals(securityScheme, apIInfo.securityScheme) &&
        Objects.equals(createdTime, apIInfo.createdTime) &&
        Objects.equals(updatedTime, apIInfo.updatedTime) &&
        Objects.equals(gatewayVendor, apIInfo.gatewayVendor) &&
        Objects.equals(advertiseOnly, apIInfo.advertiseOnly);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, context, additionalProperties, additionalPropertiesMap, version, provider, type, audience, lifeCycleStatus, workflowStatus, hasThumbnail, securityScheme, createdTime, updatedTime, gatewayVendor, advertiseOnly);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    additionalPropertiesMap: ").append(toIndentedString(additionalPropertiesMap)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    audience: ").append(toIndentedString(audience)).append("\n");
    sb.append("    lifeCycleStatus: ").append(toIndentedString(lifeCycleStatus)).append("\n");
    sb.append("    workflowStatus: ").append(toIndentedString(workflowStatus)).append("\n");
    sb.append("    hasThumbnail: ").append(toIndentedString(hasThumbnail)).append("\n");
    sb.append("    securityScheme: ").append(toIndentedString(securityScheme)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
    sb.append("    gatewayVendor: ").append(toIndentedString(gatewayVendor)).append("\n");
    sb.append("    advertiseOnly: ").append(toIndentedString(advertiseOnly)).append("\n");
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

