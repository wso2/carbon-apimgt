package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

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



public class SampleQueryDTO   {
  
    private String scenario = null;
    private String query = null;

  /**
   * scenario
   **/
  public SampleQueryDTO scenario(String scenario) {
    this.scenario = scenario;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "scenario")
  @JsonProperty("scenario")
  @NotNull
  public String getScenario() {
    return scenario;
  }
  public void setScenario(String scenario) {
    this.scenario = scenario;
  }

  /**
   * generated query
   **/
  public SampleQueryDTO query(String query) {
    this.query = query;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "generated query")
  @JsonProperty("query")
  @NotNull
  public String getQuery() {
    return query;
  }
  public void setQuery(String query) {
    this.query = query;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SampleQueryDTO sampleQuery = (SampleQueryDTO) o;
    return Objects.equals(scenario, sampleQuery.scenario) &&
        Objects.equals(query, sampleQuery.query);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scenario, query);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SampleQueryDTO {\n");
    
    sb.append("    scenario: ").append(toIndentedString(scenario)).append("\n");
    sb.append("    query: ").append(toIndentedString(query)).append("\n");
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

