package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class CachedDiscoveryResultResponseDTO   {
  
    private String lastDiscoveredAt = null;
    private List<Object> result = new ArrayList<Object>();

  /**
   **/
  public CachedDiscoveryResultResponseDTO lastDiscoveredAt(String lastDiscoveredAt) {
    this.lastDiscoveredAt = lastDiscoveredAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastDiscoveredAt")
  public String getLastDiscoveredAt() {
    return lastDiscoveredAt;
  }
  public void setLastDiscoveredAt(String lastDiscoveredAt) {
    this.lastDiscoveredAt = lastDiscoveredAt;
  }

  /**
   **/
  public CachedDiscoveryResultResponseDTO result(List<Object> result) {
    this.result = result;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("result")
  public List<Object> getResult() {
    return result;
  }
  public void setResult(List<Object> result) {
    this.result = result;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CachedDiscoveryResultResponseDTO cachedDiscoveryResultResponse = (CachedDiscoveryResultResponseDTO) o;
    return Objects.equals(lastDiscoveredAt, cachedDiscoveryResultResponse.lastDiscoveredAt) &&
        Objects.equals(result, cachedDiscoveryResultResponse.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lastDiscoveredAt, result);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CachedDiscoveryResultResponseDTO {\n");
    
    sb.append("    lastDiscoveredAt: ").append(toIndentedString(lastDiscoveredAt)).append("\n");
    sb.append("    result: ").append(toIndentedString(result)).append("\n");
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

