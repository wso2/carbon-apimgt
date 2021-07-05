package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class UUIDListDTO   {
  
    private List<String> uuids = new ArrayList<>();

  /**
   **/
  public UUIDListDTO uuids(List<String> uuids) {
    this.uuids = uuids;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("uuids")
  public List<String> getUuids() {
    return uuids;
  }
  public void setUuids(List<String> uuids) {
    this.uuids = uuids;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UUIDListDTO uuIDList = (UUIDListDTO) o;
    return Objects.equals(uuids, uuIDList.uuids);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuids);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UUIDListDTO {\n");
    
    sb.append("    uuids: ").append(toIndentedString(uuids)).append("\n");
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

