package org.wso2.carbon.apimgt.rest.api.core.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.core.dto.LabelDTO;

/**
 * LabelInfoDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-31T16:40:30.481+05:30")
public class LabelInfoDTO   {
  @JsonProperty("overwriteLabels")
  private String overwriteLabels = null;

  @JsonProperty("labelList")
  private List<LabelDTO> labelList = new ArrayList<LabelDTO>();

  public LabelInfoDTO overwriteLabels(String overwriteLabels) {
    this.overwriteLabels = overwriteLabels;
    return this;
  }

   /**
   * Yes or No to overwrite label values 
   * @return overwriteLabels
  **/
  @ApiModelProperty(value = "Yes or No to overwrite label values ")
  public String getOverwriteLabels() {
    return overwriteLabels;
  }

  public void setOverwriteLabels(String overwriteLabels) {
    this.overwriteLabels = overwriteLabels;
  }

  public LabelInfoDTO labelList(List<LabelDTO> labelList) {
    this.labelList = labelList;
    return this;
  }

  public LabelInfoDTO addLabelListItem(LabelDTO labelListItem) {
    this.labelList.add(labelListItem);
    return this;
  }

   /**
   * Get labelList
   * @return labelList
  **/
  @ApiModelProperty(required = true, value = "")
  public List<LabelDTO> getLabelList() {
    return labelList;
  }

  public void setLabelList(List<LabelDTO> labelList) {
    this.labelList = labelList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LabelInfoDTO labelInfo = (LabelInfoDTO) o;
    return Objects.equals(this.overwriteLabels, labelInfo.overwriteLabels) &&
        Objects.equals(this.labelList, labelInfo.labelList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(overwriteLabels, labelList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LabelInfoDTO {\n");
    
    sb.append("    overwriteLabels: ").append(toIndentedString(overwriteLabels)).append("\n");
    sb.append("    labelList: ").append(toIndentedString(labelList)).append("\n");
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

