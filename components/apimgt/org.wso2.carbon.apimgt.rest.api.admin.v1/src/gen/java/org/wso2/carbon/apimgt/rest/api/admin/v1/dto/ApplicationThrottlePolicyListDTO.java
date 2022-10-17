package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;


import java.util.Objects;


import javax.validation.Valid;



public class ApplicationThrottlePolicyListDTO   {
  
    private Integer count = null;
    private List<ApplicationThrottlePolicyDTO> list = new ArrayList<ApplicationThrottlePolicyDTO>();

  /**
   * Number of Application Throttling Policies returned. 
   **/
  public ApplicationThrottlePolicyListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of Application Throttling Policies returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public ApplicationThrottlePolicyListDTO list(List<ApplicationThrottlePolicyDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("list")
  public List<ApplicationThrottlePolicyDTO> getList() {
    return list;
  }
  public void setList(List<ApplicationThrottlePolicyDTO> list) {
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
    ApplicationThrottlePolicyListDTO applicationThrottlePolicyList = (ApplicationThrottlePolicyListDTO) o;
    return Objects.equals(count, applicationThrottlePolicyList.count) &&
        Objects.equals(list, applicationThrottlePolicyList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationThrottlePolicyListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
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

