package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetInfoDTO;
import javax.validation.constraints.*;

/**
 * A list of rulesets.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "A list of rulesets.")

public class RulesetListDTO   {
  
    private Integer count = null;
    private List<RulesetInfoDTO> list = new ArrayList<RulesetInfoDTO>();
    private PaginationDTO pagination = null;

  /**
   * Number of rulesets returned.
   **/
  public RulesetListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "2", value = "Number of rulesets returned.")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * List of rulesets.
   **/
  public RulesetListDTO list(List<RulesetInfoDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "List of rulesets.")
      @Valid
  @JsonProperty("list")
  public List<RulesetInfoDTO> getList() {
    return list;
  }
  public void setList(List<RulesetInfoDTO> list) {
    this.list = list;
  }

  /**
   **/
  public RulesetListDTO pagination(PaginationDTO pagination) {
    this.pagination = pagination;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("pagination")
  public PaginationDTO getPagination() {
    return pagination;
  }
  public void setPagination(PaginationDTO pagination) {
    this.pagination = pagination;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RulesetListDTO rulesetList = (RulesetListDTO) o;
    return Objects.equals(count, rulesetList.count) &&
        Objects.equals(list, rulesetList.list) &&
        Objects.equals(pagination, rulesetList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RulesetListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
    sb.append("    pagination: ").append(toIndentedString(pagination)).append("\n");
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

