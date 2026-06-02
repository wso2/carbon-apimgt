package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.TemplateDefaultViolationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class TemplateDefaultViolationListDTO   {
  
    private Boolean hasViolations = null;
    private List<TemplateDefaultViolationDTO> violations = new ArrayList<TemplateDefaultViolationDTO>();

  /**
   * True when at least one hidden default violates a bound ruleset rule.
   **/
  public TemplateDefaultViolationListDTO hasViolations(Boolean hasViolations) {
    this.hasViolations = hasViolations;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "True when at least one hidden default violates a bound ruleset rule.")
  @JsonProperty("hasViolations")
  public Boolean isHasViolations() {
    return hasViolations;
  }
  public void setHasViolations(Boolean hasViolations) {
    this.hasViolations = hasViolations;
  }

  /**
   **/
  public TemplateDefaultViolationListDTO violations(List<TemplateDefaultViolationDTO> violations) {
    this.violations = violations;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("violations")
  public List<TemplateDefaultViolationDTO> getViolations() {
    return violations;
  }
  public void setViolations(List<TemplateDefaultViolationDTO> violations) {
    this.violations = violations;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateDefaultViolationListDTO templateDefaultViolationList = (TemplateDefaultViolationListDTO) o;
    return Objects.equals(hasViolations, templateDefaultViolationList.hasViolations) &&
        Objects.equals(violations, templateDefaultViolationList.violations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hasViolations, violations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TemplateDefaultViolationListDTO {\n");
    
    sb.append("    hasViolations: ").append(toIndentedString(hasViolations)).append("\n");
    sb.append("    violations: ").append(toIndentedString(violations)).append("\n");
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

