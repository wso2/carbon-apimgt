package org.wso2.carbon.apimgt.rest.api.core.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.core.dto.APISummaryDTO;

/**
 * APISummaryListDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-31T16:40:30.481+05:30")
public class APISummaryListDTO   {
  @JsonProperty("list")
  private List<APISummaryDTO> list = new ArrayList<APISummaryDTO>();

  public APISummaryListDTO list(List<APISummaryDTO> list) {
    this.list = list;
    return this;
  }

  public APISummaryListDTO addListItem(APISummaryDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * List of apis summery. 
   * @return list
  **/
  @ApiModelProperty(value = "List of apis summery. ")
  public List<APISummaryDTO> getList() {
    return list;
  }

  public void setList(List<APISummaryDTO> list) {
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
    APISummaryListDTO apISummaryList = (APISummaryListDTO) o;
    return Objects.equals(this.list, apISummaryList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APISummaryListDTO {\n");
    
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

