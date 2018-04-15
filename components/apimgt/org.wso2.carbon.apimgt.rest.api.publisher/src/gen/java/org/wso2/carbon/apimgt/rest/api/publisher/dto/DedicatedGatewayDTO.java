package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * DedicatedGatewayDTO
 */
public class DedicatedGatewayDTO   {
  @SerializedName("isEnabled")
  private Boolean isEnabled = null;

  public DedicatedGatewayDTO isEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled;
    return this;
  }

   /**
   * This attribute declares whether an API should have a dedicated Gateway or not. 
   * @return isEnabled
  **/
  @ApiModelProperty(example = "true", required = true, value = "This attribute declares whether an API should have a dedicated Gateway or not. ")
  public Boolean getIsEnabled() {
    return isEnabled;
  }

  public void setIsEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DedicatedGatewayDTO dedicatedGateway = (DedicatedGatewayDTO) o;
    return Objects.equals(this.isEnabled, dedicatedGateway.isEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isEnabled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DedicatedGatewayDTO {\n");
    
    sb.append("    isEnabled: ").append(toIndentedString(isEnabled)).append("\n");
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

