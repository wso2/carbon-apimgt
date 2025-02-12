package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.File;
import javax.validation.constraints.*;

/**
 * Detailed information about a policy.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Detailed information about a policy.")

public class PolicyInputDTO   {
  
    private String name = null;
    private String description = null;
    private File policyContent = null;

          @XmlType(name="PolicyCategoryEnum")
    @XmlEnum(String.class)
    public enum PolicyCategoryEnum {
        SPECTRAL("SPECTRAL");
        private String value;

        PolicyCategoryEnum (String v) {
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
        public static PolicyCategoryEnum fromValue(String v) {
            for (PolicyCategoryEnum b : PolicyCategoryEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private PolicyCategoryEnum policyCategory = PolicyCategoryEnum.SPECTRAL;

          @XmlType(name="PolicyTypeEnum")
    @XmlEnum(String.class)
    public enum PolicyTypeEnum {
        API_METADATA("API_METADATA"),
        API_DEFINITION("API_DEFINITION"),
        API_DOCUMENTATION("API_DOCUMENTATION");
        private String value;

        PolicyTypeEnum (String v) {
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
        public static PolicyTypeEnum fromValue(String v) {
            for (PolicyTypeEnum b : PolicyTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private PolicyTypeEnum policyType = null;

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
   * Name of the policy.
   **/
  public PolicyInputDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "API Security Policy", required = true, value = "Name of the policy.")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * A brief description of the policy.
   **/
  public PolicyInputDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A policy designed to enforce security standards for APIs.", value = "A brief description of the policy.")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The content of the policy file (YAML or JSON).
   **/
  public PolicyInputDTO policyContent(File policyContent) {
    this.policyContent = policyContent;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The content of the policy file (YAML or JSON).")
  @JsonProperty("policyContent")
  @NotNull
  public File getPolicyContent() {
    return policyContent;
  }
  public void setPolicyContent(File policyContent) {
    this.policyContent = policyContent;
  }

  /**
   * Category the of the policy based on the rules included.
   **/
  public PolicyInputDTO policyCategory(PolicyCategoryEnum policyCategory) {
    this.policyCategory = policyCategory;
    return this;
  }

  
  @ApiModelProperty(example = "SPECTRAL", value = "Category the of the policy based on the rules included.")
  @JsonProperty("policyCategory")
  public PolicyCategoryEnum getPolicyCategory() {
    return policyCategory;
  }
  public void setPolicyCategory(PolicyCategoryEnum policyCategory) {
    this.policyCategory = policyCategory;
  }

  /**
   * Context or area to which the policy applies.
   **/
  public PolicyInputDTO policyType(PolicyTypeEnum policyType) {
    this.policyType = policyType;
    return this;
  }

  
  @ApiModelProperty(example = "API_DEFINITION", required = true, value = "Context or area to which the policy applies.")
  @JsonProperty("policyType")
  @NotNull
  public PolicyTypeEnum getPolicyType() {
    return policyType;
  }
  public void setPolicyType(PolicyTypeEnum policyType) {
    this.policyType = policyType;
  }

  /**
   * The type of artifact that the policy validates.
   **/
  public PolicyInputDTO artifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
    return this;
  }

  
  @ApiModelProperty(example = "REST_API", required = true, value = "The type of artifact that the policy validates.")
  @JsonProperty("artifactType")
  @NotNull
  public ArtifactTypeEnum getArtifactType() {
    return artifactType;
  }
  public void setArtifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
  }

  /**
   * URL to the documentation related to the policy.
   **/
  public PolicyInputDTO documentationLink(String documentationLink) {
    this.documentationLink = documentationLink;
    return this;
  }

  
  @ApiModelProperty(example = "https://example.com/docs/api-security-policy", value = "URL to the documentation related to the policy.")
  @JsonProperty("documentationLink")
  public String getDocumentationLink() {
    return documentationLink;
  }
  public void setDocumentationLink(String documentationLink) {
    this.documentationLink = documentationLink;
  }

  /**
   * Entity or individual providing the policy.
   **/
  public PolicyInputDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "TechWave", value = "Entity or individual providing the policy.")
  @JsonProperty("provider")
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
    PolicyInputDTO policyInput = (PolicyInputDTO) o;
    return Objects.equals(name, policyInput.name) &&
        Objects.equals(description, policyInput.description) &&
        Objects.equals(policyContent, policyInput.policyContent) &&
        Objects.equals(policyCategory, policyInput.policyCategory) &&
        Objects.equals(policyType, policyInput.policyType) &&
        Objects.equals(artifactType, policyInput.artifactType) &&
        Objects.equals(documentationLink, policyInput.documentationLink) &&
        Objects.equals(provider, policyInput.provider);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, policyContent, policyCategory, policyType, artifactType, documentationLink, provider);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyInputDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    policyContent: ").append(toIndentedString(policyContent)).append("\n");
    sb.append("    policyCategory: ").append(toIndentedString(policyCategory)).append("\n");
    sb.append("    policyType: ").append(toIndentedString(policyType)).append("\n");
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

