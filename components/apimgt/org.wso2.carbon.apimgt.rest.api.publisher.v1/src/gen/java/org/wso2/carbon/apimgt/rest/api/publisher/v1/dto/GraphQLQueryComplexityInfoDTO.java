package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLCustomComplexityInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class GraphQLQueryComplexityInfoDTO   {
  
    private String apiId = null;
    private Integer maxComplexity = null;
    private List<GraphQLCustomComplexityInfoDTO> list = new ArrayList<>();

  /**
   * The API id to which complexity related data is being specified 
   **/
  public GraphQLQueryComplexityInfoDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "The API id to which complexity related data is being specified ")
  @JsonProperty("-apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   * The maximum complexity allowed for all queries of this API 
   **/
  public GraphQLQueryComplexityInfoDTO maxComplexity(Integer maxComplexity) {
    this.maxComplexity = maxComplexity;
    return this;
  }

  
  @ApiModelProperty(example = "20", value = "The maximum complexity allowed for all queries of this API ")
  @JsonProperty("-maxComplexity")
  public Integer getMaxComplexity() {
    return maxComplexity;
  }
  public void setMaxComplexity(Integer maxComplexity) {
    this.maxComplexity = maxComplexity;
  }

  /**
   **/
  public GraphQLQueryComplexityInfoDTO list(List<GraphQLCustomComplexityInfoDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("-list")
  public List<GraphQLCustomComplexityInfoDTO> getList() {
    return list;
  }
  public void setList(List<GraphQLCustomComplexityInfoDTO> list) {
    this.list = list;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLQueryComplexityInfoDTO graphQLQueryComplexityInfo = (GraphQLQueryComplexityInfoDTO) o;
    return Objects.equals(apiId, graphQLQueryComplexityInfo.apiId) &&
        Objects.equals(maxComplexity, graphQLQueryComplexityInfo.maxComplexity) &&
        Objects.equals(list, graphQLQueryComplexityInfo.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiId, maxComplexity, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLQueryComplexityInfoDTO {\n");
    
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    maxComplexity: ").append(toIndentedString(maxComplexity)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
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

