package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.BackOfficeAPIBusinessInformationDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.BackOfficeAPIMonetizationInfoDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.ProductAPIDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.RuntimeAPIAdditionalPropertiesDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.RuntimeAPIAdditionalPropertiesMapDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class BackOfficeAPIDTO   {
  
    private String id = null;
    private String name = null;
    private String context = null;
    private String description = null;
    private Boolean hasThumbnail = null;

    @XmlType(name="StateEnum")
    @XmlEnum(String.class)
    public enum StateEnum {
        CREATED("CREATED"),
        PUBLISHED("PUBLISHED");
        private String value;

        StateEnum (String v) {
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
        public static StateEnum fromValue(String v) {
            for (StateEnum b : StateEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private StateEnum state = StateEnum.CREATED;
    private List<String> tags = new ArrayList<String>();
    private List<RuntimeAPIAdditionalPropertiesDTO> additionalProperties = new ArrayList<RuntimeAPIAdditionalPropertiesDTO>();
    private Map<String, RuntimeAPIAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<String, RuntimeAPIAdditionalPropertiesMapDTO>();
    private BackOfficeAPIMonetizationInfoDTO monetization = null;
    private BackOfficeAPIBusinessInformationDTO businessInformation = null;
    private String createdTime = null;
    private String lastUpdatedTime = null;
    private List<ProductAPIDTO> runtimeApis = new ArrayList<ProductAPIDTO>();
    private List<String> categories = new ArrayList<String>();

  /**
   * UUID of the Back Office API 
   **/
  public BackOfficeAPIDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the Back Office API ")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the Back Office API
   **/
  public BackOfficeAPIDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PizzaShackBackOfficeAPI", required = true, value = "Name of the Back Office API")
  @JsonProperty("name")
  @NotNull
 @Size(min=1,max=50)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public BackOfficeAPIDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "pizzaproduct", value = "")
  @JsonProperty("context")
 @Size(min=1,max=60)  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * A brief description about the API
   **/
  public BackOfficeAPIDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "This is a simple API for Pizza Shack online pizza delivery store", value = "A brief description about the API")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public BackOfficeAPIDTO hasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("hasThumbnail")
  public Boolean isHasThumbnail() {
    return hasThumbnail;
  }
  public void setHasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
  }

  /**
   * State of the Back Office API. Only published Back Office APIs are visible on the Developer Portal 
   **/
  public BackOfficeAPIDTO state(StateEnum state) {
    this.state = state;
    return this;
  }

  
  @ApiModelProperty(value = "State of the Back Office API. Only published Back Office APIs are visible on the Developer Portal ")
  @JsonProperty("state")
  public StateEnum getState() {
    return state;
  }
  public void setState(StateEnum state) {
    this.state = state;
  }

  /**
   **/
  public BackOfficeAPIDTO tags(List<String> tags) {
    this.tags = tags;
    return this;
  }

  
  @ApiModelProperty(example = "[\"pizza\",\"food\"]", value = "")
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  /**
   * Map of custom properties of API
   **/
  public BackOfficeAPIDTO additionalProperties(List<RuntimeAPIAdditionalPropertiesDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "Map of custom properties of API")
      @Valid
  @JsonProperty("additionalProperties")
  public List<RuntimeAPIAdditionalPropertiesDTO> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(List<RuntimeAPIAdditionalPropertiesDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   **/
  public BackOfficeAPIDTO additionalPropertiesMap(Map<String, RuntimeAPIAdditionalPropertiesMapDTO> additionalPropertiesMap) {
    this.additionalPropertiesMap = additionalPropertiesMap;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("additionalPropertiesMap")
  public Map<String, RuntimeAPIAdditionalPropertiesMapDTO> getAdditionalPropertiesMap() {
    return additionalPropertiesMap;
  }
  public void setAdditionalPropertiesMap(Map<String, RuntimeAPIAdditionalPropertiesMapDTO> additionalPropertiesMap) {
    this.additionalPropertiesMap = additionalPropertiesMap;
  }

  /**
   **/
  public BackOfficeAPIDTO monetization(BackOfficeAPIMonetizationInfoDTO monetization) {
    this.monetization = monetization;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("monetization")
  public BackOfficeAPIMonetizationInfoDTO getMonetization() {
    return monetization;
  }
  public void setMonetization(BackOfficeAPIMonetizationInfoDTO monetization) {
    this.monetization = monetization;
  }

  /**
   **/
  public BackOfficeAPIDTO businessInformation(BackOfficeAPIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("businessInformation")
  public BackOfficeAPIBusinessInformationDTO getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(BackOfficeAPIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }

  /**
   **/
  public BackOfficeAPIDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public BackOfficeAPIDTO lastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastUpdatedTime")
  public String getLastUpdatedTime() {
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  /**
   * Runtime APIs and resources in the Back Office API. 
   **/
  public BackOfficeAPIDTO runtimeApis(List<ProductAPIDTO> runtimeApis) {
    this.runtimeApis = runtimeApis;
    return this;
  }

  
  @ApiModelProperty(example = "[{\"apiId\":\"01234567-0123-0123-0123-012345678901\",\"runtimeId\":\"01234567-765-0765-0123-012345678901\"}]", value = "Runtime APIs and resources in the Back Office API. ")
      @Valid
  @JsonProperty("runtimeApis")
  public List<ProductAPIDTO> getRuntimeApis() {
    return runtimeApis;
  }
  public void setRuntimeApis(List<ProductAPIDTO> runtimeApis) {
    this.runtimeApis = runtimeApis;
  }

  /**
   * API categories 
   **/
  public BackOfficeAPIDTO categories(List<String> categories) {
    this.categories = categories;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "API categories ")
  @JsonProperty("categories")
  public List<String> getCategories() {
    return categories;
  }
  public void setCategories(List<String> categories) {
    this.categories = categories;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BackOfficeAPIDTO backOfficeAPI = (BackOfficeAPIDTO) o;
    return Objects.equals(id, backOfficeAPI.id) &&
        Objects.equals(name, backOfficeAPI.name) &&
        Objects.equals(context, backOfficeAPI.context) &&
        Objects.equals(description, backOfficeAPI.description) &&
        Objects.equals(hasThumbnail, backOfficeAPI.hasThumbnail) &&
        Objects.equals(state, backOfficeAPI.state) &&
        Objects.equals(tags, backOfficeAPI.tags) &&
        Objects.equals(additionalProperties, backOfficeAPI.additionalProperties) &&
        Objects.equals(additionalPropertiesMap, backOfficeAPI.additionalPropertiesMap) &&
        Objects.equals(monetization, backOfficeAPI.monetization) &&
        Objects.equals(businessInformation, backOfficeAPI.businessInformation) &&
        Objects.equals(createdTime, backOfficeAPI.createdTime) &&
        Objects.equals(lastUpdatedTime, backOfficeAPI.lastUpdatedTime) &&
        Objects.equals(runtimeApis, backOfficeAPI.runtimeApis) &&
        Objects.equals(categories, backOfficeAPI.categories);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, context, description, hasThumbnail, state, tags, additionalProperties, additionalPropertiesMap, monetization, businessInformation, createdTime, lastUpdatedTime, runtimeApis, categories);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BackOfficeAPIDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    hasThumbnail: ").append(toIndentedString(hasThumbnail)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    additionalPropertiesMap: ").append(toIndentedString(additionalPropertiesMap)).append("\n");
    sb.append("    monetization: ").append(toIndentedString(monetization)).append("\n");
    sb.append("    businessInformation: ").append(toIndentedString(businessInformation)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    lastUpdatedTime: ").append(toIndentedString(lastUpdatedTime)).append("\n");
    sb.append("    runtimeApis: ").append(toIndentedString(runtimeApis)).append("\n");
    sb.append("    categories: ").append(toIndentedString(categories)).append("\n");
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

