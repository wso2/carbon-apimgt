package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ScopeList_listDTO;
import java.util.Objects;

/**
 * ScopeListDTO
 */
public class ScopeListDTO   {
  @SerializedName("count")
  private Integer count = null;

  @SerializedName("list")
  private List<ScopeList_listDTO> list = new ArrayList<ScopeList_listDTO>();

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

  public ScopeListDTO list(List<ScopeList_listDTO> list) {
    this.list = list;
    return this;
  }

  public ScopeListDTO addListItem(ScopeList_listDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<ScopeList_listDTO> getList() {
    return list;
  }

  public void setList(List<ScopeList_listDTO> list) {
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

