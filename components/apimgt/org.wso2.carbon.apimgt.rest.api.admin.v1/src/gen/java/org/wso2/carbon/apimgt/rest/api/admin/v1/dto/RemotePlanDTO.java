package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;

/**
 * A plan available on an external gateway (e.g., an AWS Usage Plan).
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;
@ApiModel(description = "A plan available on an external gateway (e.g., an AWS Usage Plan).")


public class RemotePlanDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private Map<String, String> limits = new HashMap<String, String>();

  /**
   * Unique identifier of the remote plan.
   **/
  public RemotePlanDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "abc123", value = "Unique identifier of the remote plan.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Display name of the remote plan.
   **/
  public RemotePlanDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "AWS Unlimited Plan", value = "Display name of the remote plan.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Description of the remote plan.
   **/
  public RemotePlanDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited requests per month", value = "Description of the remote plan.")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gateway-specific limit details (rateLimit, burstLimit, quotaLimit, quotaPeriod).
   **/
  public RemotePlanDTO limits(Map<String, String> limits) {
    this.limits = limits;
    return this;
  }

  
  @ApiModelProperty(value = "Gateway-specific limit details (rateLimit, burstLimit, quotaLimit, quotaPeriod).")
  @JsonProperty("limits")
  public Map<String, String> getLimits() {
    return limits;
  }
  public void setLimits(Map<String, String> limits) {
    this.limits = limits;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RemotePlanDTO remotePlan = (RemotePlanDTO) o;
    return Objects.equals(id, remotePlan.id) &&
        Objects.equals(name, remotePlan.name) &&
        Objects.equals(description, remotePlan.description) &&
        Objects.equals(limits, remotePlan.limits);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, limits);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RemotePlanDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    limits: ").append(toIndentedString(limits)).append("\n");
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

