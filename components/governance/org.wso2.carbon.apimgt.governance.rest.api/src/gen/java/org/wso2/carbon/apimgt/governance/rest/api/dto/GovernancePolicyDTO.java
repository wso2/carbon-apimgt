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
 * Detailed information about a governance policy.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Detailed information about a governance policy.")

public class GovernancePolicyDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null; 

    @XmlType(name="LinkedStatesEnum")
    @XmlEnum(String.class)
    public enum LinkedStatesEnum {
        API_CREATE("API_CREATE"),
        API_UPDATE("API_UPDATE"),
        API_DEPLOY("API_DEPLOY"),
        API_PUBLISH("API_PUBLISH");
        private String value;

        LinkedStatesEnum (String v) {
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
        public static LinkedStatesEnum fromValue(String v) {
            for (LinkedStatesEnum b : LinkedStatesEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private List<LinkedStatesEnum> linkedStates = new ArrayList<LinkedStatesEnum>();
    private List<ActionDTO> actions = new ArrayList<ActionDTO>();
    private List<String> rulesets = new ArrayList<String>();
    private List<String> labels = new ArrayList<String>();
    private String createdBy = null;
    private String createdTime = null;
    private String updatedBy = null;
    private String updatedTime = null;

  /**
   * UUID of the governance policy.
   **/
  public GovernancePolicyDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "987e6543-d21b-34d5-b678-912345678900", value = "UUID of the governance policy.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the governance policy.
   **/
  public GovernancePolicyDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "API Security Policy", required = true, value = "Name of the governance policy.")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * A brief description of the governance policy.
   **/
  public GovernancePolicyDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Policy for enforcing security standards across all APIs.", value = "A brief description of the governance policy.")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * List of states at which the governance policy should be enforced.
   **/
  public GovernancePolicyDTO linkedStates(List<LinkedStatesEnum> linkedStates) {
    this.linkedStates = linkedStates;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "List of states at which the governance policy should be enforced.")
  @JsonProperty("linkedStates")
  @NotNull
  public List<LinkedStatesEnum> getLinkedStates() {
    return linkedStates;
  }
  public void setLinkedStates(List<LinkedStatesEnum> linkedStates) {
    this.linkedStates = linkedStates;
  }

  /**
   * List of actions taken when the governance policy is violated.
   **/
  public GovernancePolicyDTO actions(List<ActionDTO> actions) {
    this.actions = actions;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "List of actions taken when the governance policy is violated.")
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
   * List of rulesets associated with the governance policy.
   **/
  public GovernancePolicyDTO rulesets(List<String> rulesets) {
    this.rulesets = rulesets;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "List of rulesets associated with the governance policy.")
  @JsonProperty("rulesets")
  @NotNull
  public List<String> getRulesets() {
    return rulesets;
  }
  public void setRulesets(List<String> rulesets) {
    this.rulesets = rulesets;
  }

  /**
   * Labels associated with the governance policy.
   **/
  public GovernancePolicyDTO labels(List<String> labels) {
    this.labels = labels;
    return this;
  }

  
  @ApiModelProperty(example = "[\"healthcare\"]", required = true, value = "Labels associated with the governance policy.")
  @JsonProperty("labels")
  @NotNull
  public List<String> getLabels() {
    return labels;
  }
  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  /**
   * Identifier of the user who created the governance policy.
   **/
  public GovernancePolicyDTO createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  
  @ApiModelProperty(example = "admin@wso2.com", value = "Identifier of the user who created the governance policy.")
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * Timestamp when the governance policy was created.
   **/
  public GovernancePolicyDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(example = "2024-08-01T12:00:00Z", value = "Timestamp when the governance policy was created.")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   * Identifier of the user who last updated the governance policy.
   **/
  public GovernancePolicyDTO updatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  
  @ApiModelProperty(example = "admin@wso2.com", value = "Identifier of the user who last updated the governance policy.")
  @JsonProperty("updatedBy")
  public String getUpdatedBy() {
    return updatedBy;
  }
  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  /**
   * Timestamp when the governance policy was last updated.
   **/
  public GovernancePolicyDTO updatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

  
  @ApiModelProperty(example = "2024-08-02T12:00:00Z", value = "Timestamp when the governance policy was last updated.")
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
    GovernancePolicyDTO governancePolicy = (GovernancePolicyDTO) o;
    return Objects.equals(id, governancePolicy.id) &&
        Objects.equals(name, governancePolicy.name) &&
        Objects.equals(description, governancePolicy.description) &&
        Objects.equals(linkedStates, governancePolicy.linkedStates) &&
        Objects.equals(actions, governancePolicy.actions) &&
        Objects.equals(rulesets, governancePolicy.rulesets) &&
        Objects.equals(labels, governancePolicy.labels) &&
        Objects.equals(createdBy, governancePolicy.createdBy) &&
        Objects.equals(createdTime, governancePolicy.createdTime) &&
        Objects.equals(updatedBy, governancePolicy.updatedBy) &&
        Objects.equals(updatedTime, governancePolicy.updatedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, linkedStates, actions, rulesets, labels, createdBy, createdTime, updatedBy, updatedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GovernancePolicyDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    linkedStates: ").append(toIndentedString(linkedStates)).append("\n");
    sb.append("    actions: ").append(toIndentedString(actions)).append("\n");
    sb.append("    rulesets: ").append(toIndentedString(rulesets)).append("\n");
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

