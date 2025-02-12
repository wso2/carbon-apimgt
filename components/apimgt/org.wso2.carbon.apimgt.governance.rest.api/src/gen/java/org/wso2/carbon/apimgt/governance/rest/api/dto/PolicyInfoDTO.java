package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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

public class PolicyInfoDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;

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
    private PolicyCategoryEnum policyCategory = null;

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
    private String createdBy = null;
    private String createdTime = null;
    private String updatedBy = null;
    private String updatedTime = null;

  /**
   * UUID of the policy.
   **/
  public PolicyInfoDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the policy.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the policy.
   **/
  public PolicyInfoDTO name(String name) {
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
  public PolicyInfoDTO description(String description) {
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
   * Category of the policy based on the rules included.
   **/
  public PolicyInfoDTO policyCategory(PolicyCategoryEnum policyCategory) {
    this.policyCategory = policyCategory;
    return this;
  }

  
  @ApiModelProperty(example = "SPECTRAL", value = "Category of the policy based on the rules included.")
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
  public PolicyInfoDTO policyType(PolicyTypeEnum policyType) {
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
  public PolicyInfoDTO artifactType(ArtifactTypeEnum artifactType) {
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
  public PolicyInfoDTO documentationLink(String documentationLink) {
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
  public PolicyInfoDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "TechWave", required = true, value = "Entity or individual providing the policy.")
  @JsonProperty("provider")
  @NotNull
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * Identifier of the user who created the policy.
   **/
  public PolicyInfoDTO createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  
  @ApiModelProperty(example = "admin@gmail.com", value = "Identifier of the user who created the policy.")
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * Timestamp when the policy was created.
   **/
  public PolicyInfoDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(example = "2024-08-01T12:00:00Z", value = "Timestamp when the policy was created.")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   * Identifier of the user who last updated the policy.
   **/
  public PolicyInfoDTO updatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  
  @ApiModelProperty(example = "admin@gmail.com", value = "Identifier of the user who last updated the policy.")
  @JsonProperty("updatedBy")
  public String getUpdatedBy() {
    return updatedBy;
  }
  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  /**
   * Timestamp when the policy was last updated.
   **/
  public PolicyInfoDTO updatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

  
  @ApiModelProperty(example = "2024-08-02T12:00:00Z", value = "Timestamp when the policy was last updated.")
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
    PolicyInfoDTO policyInfo = (PolicyInfoDTO) o;
    return Objects.equals(id, policyInfo.id) &&
        Objects.equals(name, policyInfo.name) &&
        Objects.equals(description, policyInfo.description) &&
        Objects.equals(policyCategory, policyInfo.policyCategory) &&
        Objects.equals(policyType, policyInfo.policyType) &&
        Objects.equals(artifactType, policyInfo.artifactType) &&
        Objects.equals(documentationLink, policyInfo.documentationLink) &&
        Objects.equals(provider, policyInfo.provider) &&
        Objects.equals(createdBy, policyInfo.createdBy) &&
        Objects.equals(createdTime, policyInfo.createdTime) &&
        Objects.equals(updatedBy, policyInfo.updatedBy) &&
        Objects.equals(updatedTime, policyInfo.updatedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, policyCategory, policyType, artifactType, documentationLink, provider, createdBy, createdTime, updatedBy, updatedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    policyCategory: ").append(toIndentedString(policyCategory)).append("\n");
    sb.append("    policyType: ").append(toIndentedString(policyType)).append("\n");
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

