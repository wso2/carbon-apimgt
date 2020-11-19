package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class WorkflowDTO   {
  

    @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        APPROVED("APPROVED"),
        REJECTED("REJECTED");
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
    private StatusEnum status = null;
    private Map<String, String> attributes = new HashMap<String, String>();
    private String description = null;

  /**
   * This attribute declares whether this workflow task is approved or rejected. 
   **/
  public WorkflowDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "APPROVED", required = true, value = "This attribute declares whether this workflow task is approved or rejected. ")
  @JsonProperty("status")
  @NotNull
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * Custom attributes to complete the workflow task 
   **/
  public WorkflowDTO attributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  
  @ApiModelProperty(example = "{}", value = "Custom attributes to complete the workflow task ")
  @JsonProperty("attributes")
  public Map<String, String> getAttributes() {
    return attributes;
  }
  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  /**
   **/
  public WorkflowDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Approve workflow request.", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowDTO workflow = (WorkflowDTO) o;
    return Objects.equals(status, workflow.status) &&
        Objects.equals(attributes, workflow.attributes) &&
        Objects.equals(description, workflow.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, attributes, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowDTO {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

