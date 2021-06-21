package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class APIDTO   {
  
    private String apiDefition = null;

  /**
   * The velocity definition of the API 
   **/
  public APIDTO apiDefition(String apiDefition) {
    this.apiDefition = apiDefition;
    return this;
  }

  
  @ApiModelProperty(value = "The velocity definition of the API ")
  @JsonProperty("api-defition")
  public String getApiDefition() {
    return apiDefition;
  }
  public void setApiDefition(String apiDefition) {
    this.apiDefition = apiDefition;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIDTO API = (APIDTO) o;
    return Objects.equals(apiDefition, API.apiDefition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiDefition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDTO {\n");
    
    sb.append("    apiDefition: ").append(toIndentedString(apiDefition)).append("\n");
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

