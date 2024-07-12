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



public class MarketplaceAssistantApiCountResponseDTO   {
  
    private Integer count = null;
    private Integer limit = null;

  /**
   * no of apis stored in the vectordb.
   **/
  public MarketplaceAssistantApiCountResponseDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "100", required = true, value = "no of apis stored in the vectordb.")
  @JsonProperty("count")
  @NotNull
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * maximum no of apis allowed for an org.
   **/
  public MarketplaceAssistantApiCountResponseDTO limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  
  @ApiModelProperty(example = "1000", required = true, value = "maximum no of apis allowed for an org.")
  @JsonProperty("limit")
  @NotNull
  public Integer getLimit() {
    return limit;
  }
  public void setLimit(Integer limit) {
    this.limit = limit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarketplaceAssistantApiCountResponseDTO marketplaceAssistantApiCountResponse = (MarketplaceAssistantApiCountResponseDTO) o;
    return Objects.equals(count, marketplaceAssistantApiCountResponse.count) &&
        Objects.equals(limit, marketplaceAssistantApiCountResponse.limit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, limit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MarketplaceAssistantApiCountResponseDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
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

