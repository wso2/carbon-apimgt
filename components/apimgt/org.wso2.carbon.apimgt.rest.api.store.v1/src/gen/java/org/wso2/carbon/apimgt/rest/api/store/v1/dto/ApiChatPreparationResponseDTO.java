package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.EnrichedAPISpecDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SampleQueryDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApiChatPreparationResponseDTO   {
  
    private EnrichedAPISpecDTO apiSpec = null;
    private List<SampleQueryDTO> queries = new ArrayList<SampleQueryDTO>();

  /**
   **/
  public ApiChatPreparationResponseDTO apiSpec(EnrichedAPISpecDTO apiSpec) {
    this.apiSpec = apiSpec;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("apiSpec")
  @NotNull
  public EnrichedAPISpecDTO getApiSpec() {
    return apiSpec;
  }
  public void setApiSpec(EnrichedAPISpecDTO apiSpec) {
    this.apiSpec = apiSpec;
  }

  /**
   * list of sample queries
   **/
  public ApiChatPreparationResponseDTO queries(List<SampleQueryDTO> queries) {
    this.queries = queries;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "list of sample queries")
      @Valid
  @JsonProperty("queries")
  @NotNull
  public List<SampleQueryDTO> getQueries() {
    return queries;
  }
  public void setQueries(List<SampleQueryDTO> queries) {
    this.queries = queries;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiChatPreparationResponseDTO apiChatPreparationResponse = (ApiChatPreparationResponseDTO) o;
    return Objects.equals(apiSpec, apiChatPreparationResponse.apiSpec) &&
        Objects.equals(queries, apiChatPreparationResponse.queries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiSpec, queries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiChatPreparationResponseDTO {\n");
    
    sb.append("    apiSpec: ").append(toIndentedString(apiSpec)).append("\n");
    sb.append("    queries: ").append(toIndentedString(queries)).append("\n");
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

