package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * LabelInfoListDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-22T14:28:21.878+05:30")
public class LabelInfoListDTO   {
  @JsonProperty("labels")
  private List<String> labels = new ArrayList<String>();

  public LabelInfoListDTO labels(List<String> labels) {
    this.labels = labels;
    return this;
  }

  public LabelInfoListDTO addLabelsItem(String labelsItem) {
    this.labels.add(labelsItem);
    return this;
  }

   /**
   * Get labels
   * @return labels
  **/
  @ApiModelProperty(required = true, value = "")
  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LabelInfoListDTO labelInfoList = (LabelInfoListDTO) o;
    return Objects.equals(this.labels, labelInfoList.labels);
  }

  @Override
  public int hashCode() {
    return Objects.hash(labels);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LabelInfoListDTO {\n");
    
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
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

