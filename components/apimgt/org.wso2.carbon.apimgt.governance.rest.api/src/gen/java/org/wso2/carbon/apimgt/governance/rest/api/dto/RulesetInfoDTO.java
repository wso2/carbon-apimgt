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

          @XmlType(name="RuleCategoryEnum")
    @XmlEnum(String.class)
    public enum RuleCategoryEnum {
        SPECTRAL("SPECTRAL");
        private String value;

        RuleCategoryEnum (String v) {
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
        public static RuleCategoryEnum fromValue(String v) {
            for (RuleCategoryEnum b : RuleCategoryEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private RuleCategoryEnum ruleCategory = null;

          @XmlType(name="RuleTypeEnum")
    @XmlEnum(String.class)
    public enum RuleTypeEnum {
        API_METADATA("API_METADATA"),
        API_DEFINITION("API_DEFINITION"),
        API_DOCUMENTATION("API_DOCUMENTATION");
        private String value;

        RuleTypeEnum (String v) {
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
        public static RuleTypeEnum fromValue(String v) {
            for (RuleTypeEnum b : RuleTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private RuleTypeEnum ruleType = null;

          @XmlType(name="ArtifactTypeEnum")
    @XmlEnum(String.class)
    public enum ArtifactTypeEnum {
        REST_API("REST_API"),
        ASYNC_API("ASYNC_API");
        private String value;

        ArtifactTypeEnum (String v) {
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
        public static ArtifactTypeEnum fromValue(String v) {
            for (ArtifactTypeEnum b : ArtifactTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private ArtifactTypeEnum artifactType = null;
    private String documentationLink = null;
    private String provider = null;
    private String createdBy = null;
    private String createdTime = null;
    private String updatedBy = null;
    private String updatedTime = null;

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
   * Category the rules included in ruleset.
   **/
  public RulesetInfoDTO ruleCategory(RuleCategoryEnum ruleCategory) {
    this.ruleCategory = ruleCategory;
    return this;
  }

  
  @ApiModelProperty(example = "SPECTRAL", value = "Category the rules included in ruleset.")
  @JsonProperty("ruleCategory")
  public RuleCategoryEnum getRuleCategory() {
    return ruleCategory;
  }
  public void setRuleCategory(RuleCategoryEnum ruleCategory) {
    this.ruleCategory = ruleCategory;
  }

  /**
   * Context or area to which the ruleset applies.
   **/
  public RulesetInfoDTO ruleType(RuleTypeEnum ruleType) {
    this.ruleType = ruleType;
    return this;
  }

  
  @ApiModelProperty(example = "API_DEFINITION", required = true, value = "Context or area to which the ruleset applies.")
  @JsonProperty("ruleType")
  @NotNull
  public RuleTypeEnum getRuleType() {
    return ruleType;
  }
  public void setRuleType(RuleTypeEnum ruleType) {
    this.ruleType = ruleType;
  }

  /**
   * The type of artifact that the ruleset validates.
   **/
  public RulesetInfoDTO artifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
    return this;
  }

  
  @ApiModelProperty(example = "REST_API", required = true, value = "The type of artifact that the ruleset validates.")
  @JsonProperty("artifactType")
  @NotNull
  public ArtifactTypeEnum getArtifactType() {
    return artifactType;
  }
  public void setArtifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
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
        Objects.equals(ruleCategory, rulesetInfo.ruleCategory) &&
        Objects.equals(ruleType, rulesetInfo.ruleType) &&
        Objects.equals(artifactType, rulesetInfo.artifactType) &&
        Objects.equals(documentationLink, rulesetInfo.documentationLink) &&
        Objects.equals(provider, rulesetInfo.provider) &&
        Objects.equals(createdBy, rulesetInfo.createdBy) &&
        Objects.equals(createdTime, rulesetInfo.createdTime) &&
        Objects.equals(updatedBy, rulesetInfo.updatedBy) &&
        Objects.equals(updatedTime, rulesetInfo.updatedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, ruleCategory, ruleType, artifactType, documentationLink, provider, createdBy, createdTime, updatedBy, updatedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RulesetInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    ruleCategory: ").append(toIndentedString(ruleCategory)).append("\n");
    sb.append("    ruleType: ").append(toIndentedString(ruleType)).append("\n");
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    documentationLink: ").append(toIndentedString(documentationLink)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
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

