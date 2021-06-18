package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class APIDTO   {
  
    private String api = null;

  /**
   * The synapse definition of the API 
   **/
  public APIDTO api(String api) {
    this.api = api;
    return this;
  }

  
  @ApiModelProperty(value = "The synapse definition of the API ")
  @JsonProperty("api")
  public String getApi() {
    return api;
  }
  public void setApi(String api) {
    this.api = api;
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
    return Objects.equals(api, API.api);
  }

  @Override
  public int hashCode() {
    return Objects.hash(api);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDTO {\n");
    
    sb.append("    api: ").append(toIndentedString(api)).append("\n");
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

