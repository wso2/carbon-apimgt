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

public class RulesetDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private String rulesetContent = null;

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
    private Boolean isDefault = false;
    private String createdBy = null;
    private String createdTime = null;
    private String updatedBy = null;
    private String updatedTime = null;

  /**
   * UUID of the ruleset.
   **/
  public RulesetDTO id(String id) {
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
  public RulesetDTO name(String name) {
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
  public RulesetDTO description(String description) {
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
   * The content of the ruleset file (YAML or JSON).
   **/
  public RulesetDTO rulesetContent(String rulesetContent) {
    this.rulesetContent = rulesetContent;
    return this;
  }

  
  @ApiModelProperty(example = "rules:   oas2-always-use-https:     given:       - '$.schemes[*]'     severity: error     then:       function: enumeration       functionOptions:         values:           - https     description: >-       Host schemes must use the HTTPS protocol. Applies to: OpenAPI 2.0`     message: API host schemes must use the HTTPS protocol.     formats:       - oas2 ", required = true, value = "The content of the ruleset file (YAML or JSON).")
  @JsonProperty("rulesetContent")
  @NotNull
  public String getRulesetContent() {
    return rulesetContent;
  }
  public void setRulesetContent(String rulesetContent) {
    this.rulesetContent = rulesetContent;
  }

  /**
   * Context or area to which the ruleset applies.
   **/
  public RulesetDTO appliesTo(AppliesToEnum appliesTo) {
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
  public RulesetDTO documentationLink(String documentationLink) {
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
  public RulesetDTO provider(String provider) {
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
   * Whether the ruleset is a default one or not.
   **/
  public RulesetDTO isDefault(Boolean isDefault) {
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

  /**
   * Identifier of the user who created the ruleset.
   **/
  public RulesetDTO createdBy(String createdBy) {
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
  public RulesetDTO createdTime(String createdTime) {
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
  public RulesetDTO updatedBy(String updatedBy) {
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
  public RulesetDTO updatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

  
  @ApiModelProperty(example = "2024-08-10T12:00:00Z", value = "Timestamp when the ruleset was last updated.")
  @JsonProperty("updatedTime")
  public String getUpdatedTime() {
    return updatedTime;
  }
  public void setUpdatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RulesetDTO ruleset = (RulesetDTO) o;
    return Objects.equals(id, ruleset.id) &&
        Objects.equals(name, ruleset.name) &&
        Objects.equals(description, ruleset.description) &&
        Objects.equals(rulesetContent, ruleset.rulesetContent) &&
        Objects.equals(appliesTo, ruleset.appliesTo) &&
        Objects.equals(documentationLink, ruleset.documentationLink) &&
        Objects.equals(provider, ruleset.provider) &&
        Objects.equals(isDefault, ruleset.isDefault) &&
        Objects.equals(createdBy, ruleset.createdBy) &&
        Objects.equals(createdTime, ruleset.createdTime) &&
        Objects.equals(updatedBy, ruleset.updatedBy) &&
        Objects.equals(updatedTime, ruleset.updatedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, rulesetContent, appliesTo, documentationLink, provider, isDefault, createdBy, createdTime, updatedBy, updatedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RulesetDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    rulesetContent: ").append(toIndentedString(rulesetContent)).append("\n");
    sb.append("    appliesTo: ").append(toIndentedString(appliesTo)).append("\n");
    sb.append("    documentationLink: ").append(toIndentedString(documentationLink)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    isDefault: ").append(toIndentedString(isDefault)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    updatedBy: ").append(toIndentedString(updatedBy)).append("\n");
    sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
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

