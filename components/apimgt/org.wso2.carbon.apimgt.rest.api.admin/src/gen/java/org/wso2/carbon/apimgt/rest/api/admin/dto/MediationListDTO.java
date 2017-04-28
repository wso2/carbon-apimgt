package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.MediationInfoDTO;

/**
 * MediationListDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-26T12:41:56.528+05:30")
public class MediationListDTO   {
  @JsonProperty("count")
  private Integer count = null;

  @JsonProperty("next")
  private String next = null;

  @JsonProperty("previous")
  private String previous = null;

  @JsonProperty("list")
  private List<MediationInfoDTO> list = new ArrayList<MediationInfoDTO>();

  public MediationListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of mediation sequences returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of mediation sequences returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public MediationListDTO next(String next) {
    this.next = next;
    return this;
  }

   /**
   * Link to the next subset of sequences qualified. Empty if no more sequences are to be returned. 
   * @return next
  **/
  @ApiModelProperty(example = "", value = "Link to the next subset of sequences qualified. Empty if no more sequences are to be returned. ")
  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public MediationListDTO previous(String previous) {
    this.previous = previous;
    return this;
  }

   /**
   * Link to the previous subset of sequences qualified. Empty if current subset is the first subset returned. 
   * @return previous
  **/
  @ApiModelProperty(example = "", value = "Link to the previous subset of sequences qualified. Empty if current subset is the first subset returned. ")
  public String getPrevious() {
    return previous;
  }

  public void setPrevious(String previous) {
    this.previous = previous;
  }

  public MediationListDTO list(List<MediationInfoDTO> list) {
    this.list = list;
    return this;
  }

  public MediationListDTO addListItem(MediationInfoDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<MediationInfoDTO> getList() {
    return list;
  }

  public void setList(List<MediationInfoDTO> list) {
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
    MediationListDTO mediationList = (MediationListDTO) o;
    return Objects.equals(this.count, mediationList.count) &&
        Objects.equals(this.next, mediationList.next) &&
        Objects.equals(this.previous, mediationList.previous) &&
        Objects.equals(this.list, mediationList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, next, previous, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MediationListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
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

