package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.KeyManagerInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class KeyManagerListDTO   {
  
    private Integer count = null;
    private List<KeyManagerInfoDTO> list = new ArrayList<>();

  /**
   * Number of Key managers returned. 
   **/
  public KeyManagerListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of Key managers returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public KeyManagerListDTO list(List<KeyManagerInfoDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<KeyManagerInfoDTO> getList() {
    return list;
  }
  public void setList(List<KeyManagerInfoDTO> list) {
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
    KeyManagerListDTO keyManagerList = (KeyManagerListDTO) o;
    return Objects.equals(count, keyManagerList.count) &&
        Objects.equals(list, keyManagerList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerListDTO {\n");
    
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

