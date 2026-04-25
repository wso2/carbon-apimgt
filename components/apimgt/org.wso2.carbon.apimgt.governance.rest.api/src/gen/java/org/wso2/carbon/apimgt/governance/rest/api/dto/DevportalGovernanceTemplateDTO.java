package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Detailed information about a Devportal Governance template.
 **/
@ApiModel(description = "Detailed information about a Devportal Governance template.")
public class DevportalGovernanceTemplateDTO {

    private String id = null;
    private String name = null;
    private String description = null;
    private Map<String, Object> formConfig = new HashMap<String, Object>();
    private String formConfigHash = null;

    @XmlType(name = "StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        DRAFT("DRAFT"),
        PUBLISHED("PUBLISHED");

        private String value;

        StatusEnum(String value) {

            this.value = value;
        }

        public String value() {

            return value;
        }

        @Override
        public String toString() {

            return String.valueOf(value);
        }

        @JsonCreator
        public static StatusEnum fromValue(String value) {

            for (StatusEnum status : StatusEnum.values()) {
                if (String.valueOf(status.value).equals(value)) {
                    return status;
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
    private List<DevportalGovernanceRulesetBindingDTO> rulesetBindings =
            new ArrayList<DevportalGovernanceRulesetBindingDTO>();

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

    public DevportalGovernanceTemplateDTO name(String name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "Partner App Template", required = true, value = "Name of the template.")
    @JsonProperty("name")
    @NotNull
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public DevportalGovernanceTemplateDTO description(String description) {

        this.description = description;
        return this;
    }

    @ApiModelProperty(example = "Template used for partner-facing Devportal applications.",
            value = "A brief description of the template.")
    @JsonProperty("description")
    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public DevportalGovernanceTemplateDTO formConfig(Map<String, Object> formConfig) {

        this.formConfig = formConfig;
        return this;
    }

    @ApiModelProperty(required = true, value = "Flat UI form configuration used by the Devportal application wizard.")
    @JsonProperty("formConfig")
    @NotNull
    public Map<String, Object> getFormConfig() {

        return formConfig;
    }

    public void setFormConfig(Map<String, Object> formConfig) {

        this.formConfig = formConfig;
    }

    public DevportalGovernanceTemplateDTO formConfigHash(String formConfigHash) {

        this.formConfigHash = formConfigHash;
        return this;
    }

    @ApiModelProperty(example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            value = "SHA-256 hash of the serialized form configuration.")
    @JsonProperty("formConfigHash")
    public String getFormConfigHash() {

        return formConfigHash;
    }

    public void setFormConfigHash(String formConfigHash) {

        this.formConfigHash = formConfigHash;
    }

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

    public DevportalGovernanceTemplateDTO isDefault(Boolean isDefault) {

        this.isDefault = isDefault;
        return this;
    }

    @ApiModelProperty(example = "false", value = "Whether this template is the organization fallback template.")
    @JsonProperty("isDefault")
    public Boolean isIsDefault() {

        return isDefault;
    }

    public Boolean getIsDefault() {

        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {

        this.isDefault = isDefault;
    }

    public DevportalGovernanceTemplateDTO isGlobal(Boolean isGlobal) {

        this.isGlobal = isGlobal;
        return this;
    }

    @ApiModelProperty(example = "false",
            value = "Whether this template is a super-tenant fallback visible to all organizations.")
    @JsonProperty("isGlobal")
    public Boolean isIsGlobal() {

        return isGlobal;
    }

    public Boolean getIsGlobal() {

        return isGlobal;
    }

    public void setIsGlobal(Boolean isGlobal) {

        this.isGlobal = isGlobal;
    }

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

    public DevportalGovernanceTemplateDTO rulesetBindings(
            List<DevportalGovernanceRulesetBindingDTO> rulesetBindings) {

        this.rulesetBindings = rulesetBindings;
        return this;
    }

    @ApiModelProperty(value = "Rulesets bound to the template.")
    @JsonProperty("rulesetBindings")
    @Valid
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
        DevportalGovernanceTemplateDTO that = (DevportalGovernanceTemplateDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(formConfig, that.formConfig) &&
                Objects.equals(formConfigHash, that.formConfigHash) &&
                Objects.equals(status, that.status) &&
                Objects.equals(isDefault, that.isDefault) &&
                Objects.equals(isGlobal, that.isGlobal) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(createdTime, that.createdTime) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(updatedTime, that.updatedTime) &&
                Objects.equals(rulesetBindings, that.rulesetBindings);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, description, formConfig, formConfigHash, status, isDefault, isGlobal,
                createdBy, createdTime, updatedBy, updatedTime, rulesetBindings);
    }
}
