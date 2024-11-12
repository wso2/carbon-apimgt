package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Detailed information about a ruleset.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Detailed information about a ruleset.")

public class RulesetInfoDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;

    @XmlType(name="AppliesToEnum")
    @XmlEnum(String.class)
    public enum AppliesToEnum {
        API_METADATA("API_METADATA"),
        API_DEFINITION("API_DEFINITION"),
        DOCUMENTATION("DOCUMENTATION");
        private String value;

        AppliesToEnum (String v) {
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
        public static AppliesToEnum fromValue(String v) {
            for (AppliesToEnum b : AppliesToEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private AppliesToEnum appliesTo = null;
    private String documentationLink = null;
    private String provider = null;
    private String createdBy = null;
    private String createdTime = null;
    private String updatedBy = null;
    private String updatedTime = null;
    private Boolean isDefault = false;

  /**
   * UUID of the ruleset.
   **/
  public RulesetInfoDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the ruleset.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the ruleset.
   **/
  public RulesetInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "API Security Ruleset", required = true, value = "Name of the ruleset.")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * A brief description of the ruleset.
   **/
  public RulesetInfoDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A ruleset designed to enforce security standards for APIs.", value = "A brief description of the ruleset.")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Context or area to which the ruleset applies.
   **/
  public RulesetInfoDTO appliesTo(AppliesToEnum appliesTo) {
    this.appliesTo = appliesTo;
    return this;
  }

  
  @ApiModelProperty(example = "API_DEFINITION", required = true, value = "Context or area to which the ruleset applies.")
  @JsonProperty("appliesTo")
  @NotNull
  public AppliesToEnum getAppliesTo() {
    return appliesTo;
  }
  public void setAppliesTo(AppliesToEnum appliesTo) {
    this.appliesTo = appliesTo;
  }

  /**
   * URL to the documentation related to the ruleset.
   **/
  public RulesetInfoDTO documentationLink(String documentationLink) {
    this.documentationLink = documentationLink;
    return this;
  }

  
  @ApiModelProperty(example = "https://example.com/docs/api-security-ruleset", value = "URL to the documentation related to the ruleset.")
  @JsonProperty("documentationLink")
  public String getDocumentationLink() {
    return documentationLink;
  }
  public void setDocumentationLink(String documentationLink) {
    this.documentationLink = documentationLink;
  }

  /**
   * Entity or individual providing the ruleset.
   **/
  public RulesetInfoDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "TechWave", required = true, value = "Entity or individual providing the ruleset.")
  @JsonProperty("provider")
  @NotNull
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * Identifier of the user who created the ruleset.
   **/
  public RulesetInfoDTO createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  
  @ApiModelProperty(example = "admin@gmail.com", value = "Identifier of the user who created the ruleset.")
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * Timestamp when the ruleset was created.
   **/
  public RulesetInfoDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(example = "2024-08-01T12:00:00Z", value = "Timestamp when the ruleset was created.")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   * Identifier of the user who last updated the ruleset.
   **/
  public RulesetInfoDTO updatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  
  @ApiModelProperty(example = "admin@gmail.com", value = "Identifier of the user who last updated the ruleset.")
  @JsonProperty("updatedBy")
  public String getUpdatedBy() {
    return updatedBy;
  }
  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  /**
   * Timestamp when the ruleset was last updated.
   **/
  public RulesetInfoDTO updatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

  
  @ApiModelProperty(example = "2024-08-02T12:00:00Z", value = "Timestamp when the ruleset was last updated.")
  @JsonProperty("updatedTime")
  public String getUpdatedTime() {
    return updatedTime;
  }
  public void setUpdatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
  }

  /**
   * Whether the ruleset is a default one or not.
   **/
  public RulesetInfoDTO isDefault(Boolean isDefault) {
    this.isDefault = isDefault;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Whether the ruleset is a default one or not.")
  @JsonProperty("isDefault")
  public Boolean isIsDefault() {
    return isDefault;
  }
  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RulesetInfoDTO rulesetInfo = (RulesetInfoDTO) o;
    return Objects.equals(id, rulesetInfo.id) &&
        Objects.equals(name, rulesetInfo.name) &&
        Objects.equals(description, rulesetInfo.description) &&
        Objects.equals(appliesTo, rulesetInfo.appliesTo) &&
        Objects.equals(documentationLink, rulesetInfo.documentationLink) &&
        Objects.equals(provider, rulesetInfo.provider) &&
        Objects.equals(createdBy, rulesetInfo.createdBy) &&
        Objects.equals(createdTime, rulesetInfo.createdTime) &&
        Objects.equals(updatedBy, rulesetInfo.updatedBy) &&
        Objects.equals(updatedTime, rulesetInfo.updatedTime) &&
        Objects.equals(isDefault, rulesetInfo.isDefault);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, appliesTo, documentationLink, provider, createdBy, createdTime, updatedBy, updatedTime, isDefault);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RulesetInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    appliesTo: ").append(toIndentedString(appliesTo)).append("\n");
    sb.append("    documentationLink: ").append(toIndentedString(documentationLink)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    updatedBy: ").append(toIndentedString(updatedBy)).append("\n");
    sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
    sb.append("    isDefault: ").append(toIndentedString(isDefault)).append("\n");
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

