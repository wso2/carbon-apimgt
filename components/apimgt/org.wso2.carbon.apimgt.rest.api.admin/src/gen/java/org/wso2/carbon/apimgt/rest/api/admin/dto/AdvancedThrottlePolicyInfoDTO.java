package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottlePolicyDTO;
import java.util.Objects;

/**
 * AdvancedThrottlePolicyInfoDTO
 */
public class AdvancedThrottlePolicyInfoDTO extends ThrottlePolicyDTO  {
  @SerializedName("defaultLimit")
  private ThrottleLimitDTO defaultLimit = null;

  public AdvancedThrottlePolicyInfoDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

   /**
   * Get defaultLimit
   * @return defaultLimit
  **/
  @ApiModelProperty(value = "")
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }

  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdvancedThrottlePolicyInfoDTO advancedThrottlePolicyInfo = (AdvancedThrottlePolicyInfoDTO) o;
    return Objects.equals(this.defaultLimit, advancedThrottlePolicyInfo.defaultLimit) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultLimit, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdvancedThrottlePolicyInfoDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
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

