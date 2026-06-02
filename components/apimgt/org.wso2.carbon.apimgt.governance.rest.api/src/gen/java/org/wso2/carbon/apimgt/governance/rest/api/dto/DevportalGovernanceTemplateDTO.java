package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceRulesetBindingDTO;
import javax.validation.constraints.*;

/**
 * Detailed information about a Devportal Governance template.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Detailed information about a Devportal Governance template.")

public class DevportalGovernanceTemplateDTO   {

    private String id = null;
    private String name = null;
    private String description = null;
    private List<String> tags = new ArrayList<String>();
    private String icon = null;
    private Map<String, Object> formConfig = new HashMap<String, Object>();
    private String formConfigHash = null;

          @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        DRAFT("DRAFT"),
        PUBLISHED("PUBLISHED");
        private String value;

        StatusEnum (String v) {
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
        public static StatusEnum fromValue(String v) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private StatusEnum status = StatusEnum.DRAFT;
    private Boolean isDefault = false;
    private Boolean isGlobal = false;
    private String createdBy = null;
    private String createdTime = null;
    private String updatedBy = null;
    private String updatedTime = null;
    private List<DevportalGovernanceRulesetBindingDTO> rulesetBindings = new ArrayList<DevportalGovernanceRulesetBindingDTO>();

  /**
   * UUID of the template.
   **/
  public DevportalGovernanceTemplateDTO id(String id) {
    this.id = id;
    return this;
  }


  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the template.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the template.
   **/
  public DevportalGovernanceTemplateDTO name(String name) {
    this.name = name;
    return this;
  }


  @ApiModelProperty(example = "Partner App Template", required = true, value = "Name of the template.")
  @JsonProperty("name")
  @NotNull
 @Size(max=256)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * A brief description of the template.
   **/
  public DevportalGovernanceTemplateDTO description(String description) {
    this.description = description;
    return this;
  }


  @ApiModelProperty(example = "Template used for partner-facing Devportal applications.", value = "A brief description of the template.")
  @JsonProperty("description")
 @Size(max=1024)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Tags for categorising the template in the Devportal gallery.
   **/
  public DevportalGovernanceTemplateDTO tags(List<String> tags) {
    this.tags = tags;
    return this;
  }


  @ApiModelProperty(example = "[\"internal\",\"partner\"]", value = "Tags for categorising the template in the Devportal gallery.")
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  /**
   * Base64-encoded icon image for the template card (JPEG, PNG, SVG, or WebP; max 200 KB).
   **/
  public DevportalGovernanceTemplateDTO icon(String icon) {
    this.icon = icon;
    return this;
  }


  @ApiModelProperty(example = "data:image/png;base64,iVBOR...", value = "Base64-encoded icon image for the template card (JPEG, PNG, SVG, or WebP; max 200 KB).")
  @JsonProperty("icon")
  public String getIcon() {
    return icon;
  }
  public void setIcon(String icon) {
    this.icon = icon;
  }

  /**
   * Nested UI form configuration used by the Devportal application, subscription, and key generation flows.
   **/
  public DevportalGovernanceTemplateDTO formConfig(Map<String, Object> formConfig) {
    this.formConfig = formConfig;
    return this;
  }


  @ApiModelProperty(example = "{\"application\":{\"throttlingPolicy\":{\"hidden\":true,\"defaultValue\":\"Unlimited\"},\"description\":{\"hidden\":false,\"defaultValue\":\"\"},\"groups\":{\"hidden\":false,\"defaultValue\":\"\"}},\"subscription\":{\"throttlingPolicy\":{\"hidden\":true,\"defaultValue\":\"Gold\"}},\"keyGeneration\":{\"allowedKeyManagers\":{\"defaultValue\":[\"Resident Key Manager\"]},\"grantTypes\":{\"hidden\":true,\"defaultValue\":[\"client_credentials\"]},\"callbackUrl\":{\"hidden\":true,\"defaultValue\":\"\"}},\"developerExperience\":{\"summary\":\"Partner applications use locked subscription and OAuth defaults.\",\"limitations\":\"Application throttling policy is fixed to Unlimited.\\nSubscription throttling policy is fixed to Gold.\\nOAuth grant types are fixed to Client Credentials.\\n\"}}", required = true, value = "Nested UI form configuration used by the Devportal application, subscription, and key generation flows.")
  @JsonProperty("formConfig")
  @NotNull
  public Map<String, Object> getFormConfig() {
    return formConfig;
  }
  public void setFormConfig(Map<String, Object> formConfig) {
    this.formConfig = formConfig;
  }

  /**
   * SHA-256 hash of the serialized form configuration.
   **/
  public DevportalGovernanceTemplateDTO formConfigHash(String formConfigHash) {
    this.formConfigHash = formConfigHash;
    return this;
  }


  @ApiModelProperty(example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", value = "SHA-256 hash of the serialized form configuration.")
  @JsonProperty("formConfigHash")
  public String getFormConfigHash() {
    return formConfigHash;
  }
  public void setFormConfigHash(String formConfigHash) {
    this.formConfigHash = formConfigHash;
  }

  /**
   * Template lifecycle status.
   **/
  public DevportalGovernanceTemplateDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }


  @ApiModelProperty(example = "DRAFT", value = "Template lifecycle status.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * Whether this template is the organization fallback template.
   **/
  public DevportalGovernanceTemplateDTO isDefault(Boolean isDefault) {
    this.isDefault = isDefault;
    return this;
  }


  @ApiModelProperty(example = "false", value = "Whether this template is the organization fallback template.")
  @JsonProperty("isDefault")
  public Boolean isIsDefault() {
    return isDefault;
  }
  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }

  /**
   * Whether this template is a super-tenant fallback visible to all organizations.
   **/
  public DevportalGovernanceTemplateDTO isGlobal(Boolean isGlobal) {
    this.isGlobal = isGlobal;
    return this;
  }


  @ApiModelProperty(example = "false", value = "Whether this template is a super-tenant fallback visible to all organizations.")
  @JsonProperty("isGlobal")
  public Boolean isIsGlobal() {
    return isGlobal;
  }
  public void setIsGlobal(Boolean isGlobal) {
    this.isGlobal = isGlobal;
  }

  /**
   * Identifier of the user who created the template.
   **/
  public DevportalGovernanceTemplateDTO createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }


  @ApiModelProperty(example = "admin@gmail.com", value = "Identifier of the user who created the template.")
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * Timestamp when the template was created.
   **/
  public DevportalGovernanceTemplateDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }


  @ApiModelProperty(example = "2026-04-24T12:00:00Z", value = "Timestamp when the template was created.")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   * Identifier of the user who last updated the template.
   **/
  public DevportalGovernanceTemplateDTO updatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }


  @ApiModelProperty(example = "admin@gmail.com", value = "Identifier of the user who last updated the template.")
  @JsonProperty("updatedBy")
  public String getUpdatedBy() {
    return updatedBy;
  }
  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  /**
   * Timestamp when the template was last updated.
   **/
  public DevportalGovernanceTemplateDTO updatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }


  @ApiModelProperty(example = "2026-04-24T12:15:00Z", value = "Timestamp when the template was last updated.")
  @JsonProperty("updatedTime")
  public String getUpdatedTime() {
    return updatedTime;
  }
  public void setUpdatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
  }

  /**
   * Rulesets bound to the template.
   **/
  public DevportalGovernanceTemplateDTO rulesetBindings(List<DevportalGovernanceRulesetBindingDTO> rulesetBindings) {
    this.rulesetBindings = rulesetBindings;
    return this;
  }


  @ApiModelProperty(value = "Rulesets bound to the template.")
      @Valid
  @JsonProperty("rulesetBindings")
  public List<DevportalGovernanceRulesetBindingDTO> getRulesetBindings() {
    return rulesetBindings;
  }
  public void setRulesetBindings(List<DevportalGovernanceRulesetBindingDTO> rulesetBindings) {
    this.rulesetBindings = rulesetBindings;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DevportalGovernanceTemplateDTO devportalGovernanceTemplate = (DevportalGovernanceTemplateDTO) o;
    return Objects.equals(id, devportalGovernanceTemplate.id) &&
        Objects.equals(name, devportalGovernanceTemplate.name) &&
        Objects.equals(description, devportalGovernanceTemplate.description) &&
        Objects.equals(tags, devportalGovernanceTemplate.tags) &&
        Objects.equals(icon, devportalGovernanceTemplate.icon) &&
        Objects.equals(formConfig, devportalGovernanceTemplate.formConfig) &&
        Objects.equals(formConfigHash, devportalGovernanceTemplate.formConfigHash) &&
        Objects.equals(status, devportalGovernanceTemplate.status) &&
        Objects.equals(isDefault, devportalGovernanceTemplate.isDefault) &&
        Objects.equals(isGlobal, devportalGovernanceTemplate.isGlobal) &&
        Objects.equals(createdBy, devportalGovernanceTemplate.createdBy) &&
        Objects.equals(createdTime, devportalGovernanceTemplate.createdTime) &&
        Objects.equals(updatedBy, devportalGovernanceTemplate.updatedBy) &&
        Objects.equals(updatedTime, devportalGovernanceTemplate.updatedTime) &&
        Objects.equals(rulesetBindings, devportalGovernanceTemplate.rulesetBindings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, tags, icon, formConfig, formConfigHash, status, isDefault, isGlobal, createdBy, createdTime, updatedBy, updatedTime, rulesetBindings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DevportalGovernanceTemplateDTO {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    formConfig: ").append(toIndentedString(formConfig)).append("\n");
    sb.append("    formConfigHash: ").append(toIndentedString(formConfigHash)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    isDefault: ").append(toIndentedString(isDefault)).append("\n");
    sb.append("    isGlobal: ").append(toIndentedString(isGlobal)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    updatedBy: ").append(toIndentedString(updatedBy)).append("\n");
    sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
    sb.append("    rulesetBindings: ").append(toIndentedString(rulesetBindings)).append("\n");
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
