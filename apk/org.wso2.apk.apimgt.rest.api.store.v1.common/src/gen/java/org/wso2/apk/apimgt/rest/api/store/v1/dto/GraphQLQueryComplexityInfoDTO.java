package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

import javax.validation.Valid;



public class GraphQLQueryComplexityInfoDTO   {
  
    private List<GraphQLCustomComplexityInfoDTO> list = new ArrayList<GraphQLCustomComplexityInfoDTO>();

  /**
   **/
  public GraphQLQueryComplexityInfoDTO list(List<GraphQLCustomComplexityInfoDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("list")
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
    return Objects.equals(list, graphQLQueryComplexityInfo.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLQueryComplexityInfoDTO {\n");
    
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

