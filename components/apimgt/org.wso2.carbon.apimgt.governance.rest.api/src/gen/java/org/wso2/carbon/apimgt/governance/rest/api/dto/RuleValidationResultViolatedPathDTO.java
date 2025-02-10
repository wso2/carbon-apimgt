package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Path in the artifact where the rule is violated.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Path in the artifact where the rule is violated.")

public class RuleValidationResultViolatedPathDTO   {
  
    private String path = null;
    private String description = null;

  /**
   * Path in the artifact where the rule is violated.
   **/
  public RuleValidationResultViolatedPathDTO path(String path) {
    this.path = path;
    return this;
  }

  
  @ApiModelProperty(example = "info.title", value = "Path in the artifact where the rule is violated.")
  @JsonProperty("path")
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Description of the path.
   **/
  public RuleValidationResultViolatedPathDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "API name", value = "Description of the path.")
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
    RuleValidationResultViolatedPathDTO ruleValidationResultViolatedPath = (RuleValidationResultViolatedPathDTO) o;
    return Objects.equals(path, ruleValidationResultViolatedPath.path) &&
        Objects.equals(description, ruleValidationResultViolatedPath.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RuleValidationResultViolatedPathDTO {\n");
    
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
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

