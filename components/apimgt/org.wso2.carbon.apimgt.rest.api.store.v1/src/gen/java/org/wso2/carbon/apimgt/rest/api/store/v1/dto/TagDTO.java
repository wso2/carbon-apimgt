package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class TagDTO   {
  
    private String value = null;
    private Integer count = null;

  /**
   **/
  public TagDTO value(String value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(example = "tag1", value = "")
  @JsonProperty("value")
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }

  /**
   **/
  public TagDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "5", value = "")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TagDTO tag = (TagDTO) o;
    return Objects.equals(value, tag.value) &&
        Objects.equals(count, tag.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, count);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TagDTO {\n");
    
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
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

