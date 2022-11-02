package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIAdditionalPropertiesDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIAdditionalPropertiesMapDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIBusinessInformationDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIMonetizationInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.apk.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ModifiableAPIDTO   {
  
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
    private List<APIAdditionalPropertiesDTO> additionalProperties = new ArrayList<APIAdditionalPropertiesDTO>();
    private Map<String, APIAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<String, APIAdditionalPropertiesMapDTO>();
    private APIMonetizationInfoDTO monetization = null;
    private APIBusinessInformationDTO businessInformation = null;
    private List<String> categories = new ArrayList<String>();

  /**
   * UUID of the API 
   **/
  public ModifiableAPIDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the API ")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the API
   **/
  public ModifiableAPIDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PizzaShackAPI", required = true, value = "Name of the API")
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
  public ModifiableAPIDTO context(String context) {
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
  public ModifiableAPIDTO description(String description) {
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
  public ModifiableAPIDTO hasThumbnail(Boolean hasThumbnail) {
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
   * State of the API. Only published APIs are visible on the Developer Portal 
   **/
  public ModifiableAPIDTO state(StateEnum state) {
    this.state = state;
    return this;
  }

  
  @ApiModelProperty(value = "State of the API. Only published APIs are visible on the Developer Portal ")
  @JsonProperty("state")
  public StateEnum getState() {
    return state;
  }
  public void setState(StateEnum state) {
    this.state = state;
  }

  /**
   **/
  public ModifiableAPIDTO tags(List<String> tags) {
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
  public ModifiableAPIDTO additionalProperties(List<APIAdditionalPropertiesDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "Map of custom properties of API")
      @Valid
  @JsonProperty("additionalProperties")
  public List<APIAdditionalPropertiesDTO> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(List<APIAdditionalPropertiesDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   **/
  public ModifiableAPIDTO additionalPropertiesMap(Map<String, APIAdditionalPropertiesMapDTO> additionalPropertiesMap) {
    this.additionalPropertiesMap = additionalPropertiesMap;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("additionalPropertiesMap")
  public Map<String, APIAdditionalPropertiesMapDTO> getAdditionalPropertiesMap() {
    return additionalPropertiesMap;
  }
  public void setAdditionalPropertiesMap(Map<String, APIAdditionalPropertiesMapDTO> additionalPropertiesMap) {
    this.additionalPropertiesMap = additionalPropertiesMap;
  }

  /**
   **/
  public ModifiableAPIDTO monetization(APIMonetizationInfoDTO monetization) {
    this.monetization = monetization;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("monetization")
  public APIMonetizationInfoDTO getMonetization() {
    return monetization;
  }
  public void setMonetization(APIMonetizationInfoDTO monetization) {
    this.monetization = monetization;
  }

  /**
   **/
  public ModifiableAPIDTO businessInformation(APIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("businessInformation")
  public APIBusinessInformationDTO getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(APIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }

  /**
   * API categories 
   **/
  public ModifiableAPIDTO categories(List<String> categories) {
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
    ModifiableAPIDTO modifiableAPI = (ModifiableAPIDTO) o;
    return Objects.equals(id, modifiableAPI.id) &&
        Objects.equals(name, modifiableAPI.name) &&
        Objects.equals(context, modifiableAPI.context) &&
        Objects.equals(description, modifiableAPI.description) &&
        Objects.equals(hasThumbnail, modifiableAPI.hasThumbnail) &&
        Objects.equals(state, modifiableAPI.state) &&
        Objects.equals(tags, modifiableAPI.tags) &&
        Objects.equals(additionalProperties, modifiableAPI.additionalProperties) &&
        Objects.equals(additionalPropertiesMap, modifiableAPI.additionalPropertiesMap) &&
        Objects.equals(monetization, modifiableAPI.monetization) &&
        Objects.equals(businessInformation, modifiableAPI.businessInformation) &&
        Objects.equals(categories, modifiableAPI.categories);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, context, description, hasThumbnail, state, tags, additionalProperties, additionalPropertiesMap, monetization, businessInformation, categories);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModifiableAPIDTO {\n");
    
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

