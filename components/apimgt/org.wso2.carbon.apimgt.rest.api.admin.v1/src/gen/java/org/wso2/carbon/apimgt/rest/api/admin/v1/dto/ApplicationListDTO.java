package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ApplicationListDTO   {
  
    private Integer count = null;
    private String next = null;
    private String previous = null;
    private List<ApplicationInfoDTO> list = new ArrayList<>();
    private Integer total = null;

  /**
   * Number of applications returned. 
   **/
  public ApplicationListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of applications returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * Link to the next subset of resources qualified. Empty if no more resources are to be returned. 
   **/
  public ApplicationListDTO next(String next) {
    this.next = next;
    return this;
  }

  
  @ApiModelProperty(example = "/applications?limit=1&offset=2&user=", value = "Link to the next subset of resources qualified. Empty if no more resources are to be returned. ")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }

  /**
   * Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. 
   **/
  public ApplicationListDTO previous(String previous) {
    this.previous = previous;
    return this;
  }

  
  @ApiModelProperty(example = "/applications?limit=1&offset=0&user=", value = "Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. ")
  @JsonProperty("previous")
  public String getPrevious() {
    return previous;
  }
  public void setPrevious(String previous) {
    this.previous = previous;
  }

  /**
   **/
  public ApplicationListDTO list(List<ApplicationInfoDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<ApplicationInfoDTO> getList() {
    return list;
  }
  public void setList(List<ApplicationInfoDTO> list) {
    this.list = list;
  }

  /**
   * Applications total. 
   **/
  public ApplicationListDTO total(Integer total) {
    this.total = total;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Applications total. ")
  @JsonProperty("total")
  public Integer getTotal() {
    return total;
  }
  public void setTotal(Integer total) {
    this.total = total;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationListDTO applicationList = (ApplicationListDTO) o;
    return Objects.equals(count, applicationList.count) &&
        Objects.equals(next, applicationList.next) &&
        Objects.equals(previous, applicationList.previous) &&
        Objects.equals(list, applicationList.list) &&
        Objects.equals(total, applicationList.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, next, previous, list, total);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
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

