package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ActionDTO;
import javax.validation.constraints.*;

/**
 * Detailed information about a governance policy attachment.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Detailed information about a governance policy attachment.")

public class APIMGovernancePolicyAttachmentDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null; 

    @XmlType(name="GovernableStatesEnum")
    @XmlEnum(String.class)
    public enum GovernableStatesEnum {
        API_CREATE("API_CREATE"),
        API_UPDATE("API_UPDATE"),
        API_DEPLOY("API_DEPLOY"),
        API_PUBLISH("API_PUBLISH");
        private String value;

        GovernableStatesEnum (String v) {
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
        public static GovernableStatesEnum fromValue(String v) {
            for (GovernableStatesEnum b : GovernableStatesEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private List<GovernableStatesEnum> governableStates = new ArrayList<GovernableStatesEnum>();
    private List<ActionDTO> actions = new ArrayList<ActionDTO>();
    private List<String> policies = new ArrayList<String>();
    private List<String> labels = new ArrayList<String>();
    private String createdBy = null;
    private String createdTime = null;
    private String updatedBy = null;
    private String updatedTime = null;

  /**
   * UUID of the governance policy attachment.
   **/
  public APIMGovernancePolicyAttachmentDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "987e6543-d21b-34d5-b678-912345678900", value = "UUID of the governance policy attachment.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the governance policy attachment.
   **/
  public APIMGovernancePolicyAttachmentDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "API Security Policy Attachment", required = true, value = "Name of the governance policy attachment.")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * A brief description of the governance policy attachment.
   **/
  public APIMGovernancePolicyAttachmentDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Policy Attachment for enforcing security standards across all APIs.", value = "A brief description of the governance policy attachment.")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * List of states at which the governance policy attachment should be enforced.
   **/
  public APIMGovernancePolicyAttachmentDTO governableStates(List<GovernableStatesEnum> governableStates) {
    this.governableStates = governableStates;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "List of states at which the governance policy attachment should be enforced.")
  @JsonProperty("governableStates")
  @NotNull
  public List<GovernableStatesEnum> getGovernableStates() {
    return governableStates;
  }
  public void setGovernableStates(List<GovernableStatesEnum> governableStates) {
    this.governableStates = governableStates;
  }

  /**
   * List of actions taken when the governance policy attachment is violated. An action is defined by the state and rule severity. If an action is not specified to each state and rule severity, the default action is &#x60;NOTIFY&#x60;.
   **/
  public APIMGovernancePolicyAttachmentDTO actions(List<ActionDTO> actions) {
    this.actions = actions;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "List of actions taken when the governance policy attachment is violated. An action is defined by the state and rule severity. If an action is not specified to each state and rule severity, the default action is `NOTIFY`.")
      @Valid
  @JsonProperty("actions")
  @NotNull
  public List<ActionDTO> getActions() {
    return actions;
  }
  public void setActions(List<ActionDTO> actions) {
    this.actions = actions;
  }

  /**
   * List of policies associated with the governance policy attachment.
   **/
  public APIMGovernancePolicyAttachmentDTO policies(List<String> policies) {
    this.policies = policies;
    return this;
  }

  
  @ApiModelProperty(value = "List of policies associated with the governance policy attachment.")
  @JsonProperty("policies")
  public List<String> getPolicies() {
    return policies;
  }
  public void setPolicies(List<String> policies) {
    this.policies = policies;
  }

  /**
   * List of labels IDs associated with the governance policy attachment.
   **/
  public APIMGovernancePolicyAttachmentDTO labels(List<String> labels) {
    this.labels = labels;
    return this;
  }

  
  @ApiModelProperty(example = "[\"54d5833a-ca86-44bb-bcda-5b9fcdacd79d\"]", required = true, value = "List of labels IDs associated with the governance policy attachment.")
  @JsonProperty("labels")
  @NotNull
  public List<String> getLabels() {
    return labels;
  }
  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  /**
   * Identifier of the user who created the governance policy attachment.
   **/
  public APIMGovernancePolicyAttachmentDTO createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  
  @ApiModelProperty(example = "admin@wso2.com", value = "Identifier of the user who created the governance policy attachment.")
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * Timestamp when the governance policy attachment was created.
   **/
  public APIMGovernancePolicyAttachmentDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(example = "2024-08-01T12:00:00Z", value = "Timestamp when the governance policy attachment was created.")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   * Identifier of the user who last updated the governance policy attachment.
   **/
  public APIMGovernancePolicyAttachmentDTO updatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  
  @ApiModelProperty(example = "admin@wso2.com", value = "Identifier of the user who last updated the governance policy attachment.")
  @JsonProperty("updatedBy")
  public String getUpdatedBy() {
    return updatedBy;
  }
  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  /**
   * Timestamp when the governance policy attachment was last updated.
   **/
  public APIMGovernancePolicyAttachmentDTO updatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

  
  @ApiModelProperty(example = "2024-08-02T12:00:00Z", value = "Timestamp when the governance policy attachment was last updated.")
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
    APIMGovernancePolicyAttachmentDTO apIMGovernancePolicyAttachment = (APIMGovernancePolicyAttachmentDTO) o;
    return Objects.equals(id, apIMGovernancePolicyAttachment.id) &&
        Objects.equals(name, apIMGovernancePolicyAttachment.name) &&
        Objects.equals(description, apIMGovernancePolicyAttachment.description) &&
        Objects.equals(governableStates, apIMGovernancePolicyAttachment.governableStates) &&
        Objects.equals(actions, apIMGovernancePolicyAttachment.actions) &&
        Objects.equals(policies, apIMGovernancePolicyAttachment.policies) &&
        Objects.equals(labels, apIMGovernancePolicyAttachment.labels) &&
        Objects.equals(createdBy, apIMGovernancePolicyAttachment.createdBy) &&
        Objects.equals(createdTime, apIMGovernancePolicyAttachment.createdTime) &&
        Objects.equals(updatedBy, apIMGovernancePolicyAttachment.updatedBy) &&
        Objects.equals(updatedTime, apIMGovernancePolicyAttachment.updatedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, governableStates, actions, policies, labels, createdBy, createdTime, updatedBy, updatedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIMGovernancePolicyAttachmentDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    governableStates: ").append(toIndentedString(governableStates)).append("\n");
    sb.append("    actions: ").append(toIndentedString(actions)).append("\n");
    sb.append("    policies: ").append(toIndentedString(policies)).append("\n");
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
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

