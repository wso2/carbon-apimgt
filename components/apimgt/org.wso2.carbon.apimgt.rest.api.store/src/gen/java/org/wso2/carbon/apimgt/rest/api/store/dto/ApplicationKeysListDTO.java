package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeysDTO;
import java.util.Objects;

/**
 * ApplicationKeysListDTO
 */
public class ApplicationKeysListDTO   {
  @JsonProperty("count")
  private Integer count = null;

  @JsonProperty("list")
  private List<ApplicationKeysDTO> list = new ArrayList<ApplicationKeysDTO>();

  public ApplicationKeysListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of applications keys returned. 
   * @return count
  **/
  @ApiModelProperty(value = "Number of applications keys returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public ApplicationKeysListDTO list(List<ApplicationKeysDTO> list) {
    this.list = list;
    return this;
  }

  public ApplicationKeysListDTO addListItem(ApplicationKeysDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<ApplicationKeysDTO> getList() {
    return list;
  }

  public void setList(List<ApplicationKeysDTO> list) {
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
    ApplicationKeysListDTO applicationKeysList = (ApplicationKeysListDTO) o;
    return Objects.equals(this.count, applicationKeysList.count) &&
        Objects.equals(this.list, applicationKeysList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeysListDTO {\n");
    
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

