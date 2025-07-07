package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class DesignAssistantRegenerateSpecDTO   {
  
    private String regeneratedSpec = null;

  /**
   * Regenerated API specification.
   **/
  public DesignAssistantRegenerateSpecDTO regeneratedSpec(String regeneratedSpec) {
    this.regeneratedSpec = regeneratedSpec;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Regenerated API specification.")
  @JsonProperty("regenerated_spec")
  @NotNull
  public String getRegeneratedSpec() {
    return regeneratedSpec;
  }
  public void setRegeneratedSpec(String regeneratedSpec) {
    this.regeneratedSpec = regeneratedSpec;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesignAssistantRegenerateSpecDTO designAssistantRegenerateSpec = (DesignAssistantRegenerateSpecDTO) o;
    return Objects.equals(regeneratedSpec, designAssistantRegenerateSpec.regeneratedSpec);
  }

  @Override
  public int hashCode() {
    return Objects.hash(regeneratedSpec);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesignAssistantRegenerateSpecDTO {\n");
    
    sb.append("    regeneratedSpec: ").append(toIndentedString(regeneratedSpec)).append("\n");
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

