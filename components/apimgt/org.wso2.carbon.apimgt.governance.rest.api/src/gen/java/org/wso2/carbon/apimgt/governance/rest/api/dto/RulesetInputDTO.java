package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.File;
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

public class RulesetInputDTO   {
  
    private String name = null;
    private String description = null;
    private File rulesetContent = null;

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
    private RuleCategoryEnum ruleCategory = RuleCategoryEnum.SPECTRAL;

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

  /**
   * Name of the ruleset.
   **/
  public RulesetInputDTO name(String name) {
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
  public RulesetInputDTO description(String description) {
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
  public RulesetInputDTO rulesetContent(File rulesetContent) {
    this.rulesetContent = rulesetContent;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The content of the ruleset file (YAML or JSON).")
  @JsonProperty("rulesetContent")
  @NotNull
  public File getRulesetContent() {
    return rulesetContent;
  }
  public void setRulesetContent(File rulesetContent) {
    this.rulesetContent = rulesetContent;
  }

  /**
   * Category the rules included in ruleset.
   **/
  public RulesetInputDTO ruleCategory(RuleCategoryEnum ruleCategory) {
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
  public RulesetInputDTO ruleType(RuleTypeEnum ruleType) {
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
  public RulesetInputDTO artifactType(ArtifactTypeEnum artifactType) {
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
  public RulesetInputDTO documentationLink(String documentationLink) {
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
  public RulesetInputDTO provider(String provider) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RulesetInputDTO rulesetInput = (RulesetInputDTO) o;
    return Objects.equals(name, rulesetInput.name) &&
        Objects.equals(description, rulesetInput.description) &&
        Objects.equals(rulesetContent, rulesetInput.rulesetContent) &&
        Objects.equals(ruleCategory, rulesetInput.ruleCategory) &&
        Objects.equals(ruleType, rulesetInput.ruleType) &&
        Objects.equals(artifactType, rulesetInput.artifactType) &&
        Objects.equals(documentationLink, rulesetInput.documentationLink) &&
        Objects.equals(provider, rulesetInput.provider);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, rulesetContent, ruleCategory, ruleType, artifactType, documentationLink, provider);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RulesetInputDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    rulesetContent: ").append(toIndentedString(rulesetContent)).append("\n");
    sb.append("    ruleCategory: ").append(toIndentedString(ruleCategory)).append("\n");
    sb.append("    ruleType: ").append(toIndentedString(ruleType)).append("\n");
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    documentationLink: ").append(toIndentedString(documentationLink)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
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

