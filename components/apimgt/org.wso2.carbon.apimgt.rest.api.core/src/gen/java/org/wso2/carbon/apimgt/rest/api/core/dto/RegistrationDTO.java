package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.core.dto.LabelInfoDTO;
import java.util.Objects;

/**
 * RegistrationDTO
 */
public class RegistrationDTO   {
  @SerializedName("labelInfo")
  private LabelInfoDTO labelInfo = null;

  public RegistrationDTO labelInfo(LabelInfoDTO labelInfo) {
    this.labelInfo = labelInfo;
    return this;
  }

   /**
   * Get labelInfo
   * @return labelInfo
  **/
  @ApiModelProperty(required = true, value = "")
  public LabelInfoDTO getLabelInfo() {
    return labelInfo;
  }

  public void setLabelInfo(LabelInfoDTO labelInfo) {
    this.labelInfo = labelInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegistrationDTO registration = (RegistrationDTO) o;
    return Objects.equals(this.labelInfo, registration.labelInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(labelInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RegistrationDTO {\n");
    
    sb.append("    labelInfo: ").append(toIndentedString(labelInfo)).append("\n");
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

