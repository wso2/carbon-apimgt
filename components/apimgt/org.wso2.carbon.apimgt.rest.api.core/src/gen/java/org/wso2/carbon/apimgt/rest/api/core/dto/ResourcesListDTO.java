package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.core.dto.UriTemplateDTO;
import java.util.Objects;

/**
 * ResourcesListDTO
 */
public class ResourcesListDTO   {
  @SerializedName("list")
  private List<UriTemplateDTO> list = new ArrayList<UriTemplateDTO>();

  public ResourcesListDTO list(List<UriTemplateDTO> list) {
    this.list = list;
    return this;
  }

  public ResourcesListDTO addListItem(UriTemplateDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<UriTemplateDTO> getList() {
    return list;
  }

  public void setList(List<UriTemplateDTO> list) {
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
    ResourcesListDTO resourcesList = (ResourcesListDTO) o;
    return Objects.equals(this.list, resourcesList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourcesListDTO {\n");
    
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

