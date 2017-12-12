package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ScopeListDTO
 */
public class ScopeListDTO   {
  @JsonProperty("count")
  private Integer count = null;

  @JsonProperty("list")
  private List<String> list = new ArrayList<String>();

  public ScopeListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Scopes returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of Scopes returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public ScopeListDTO list(List<String> list) {
    this.list = list;
    return this;
  }

  public ScopeListDTO addListItem(String listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<String> getList() {
    return list;
  }

  public void setList(List<String> list) {
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
    ScopeListDTO scopeList = (ScopeListDTO) o;
    return Objects.equals(this.count, scopeList.count) &&
        Objects.equals(this.list, scopeList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeListDTO {\n");
    
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

