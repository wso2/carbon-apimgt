package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottlePolicyDTO;
import java.util.Objects;

/**
 * ApplicationThrottlePolicyDTO
 */
public class ApplicationThrottlePolicyDTO extends ThrottlePolicyDTO  {
  @JsonProperty("quotaPolicy")
  private ThrottleLimitDTO quotaPolicy = null;

  public ApplicationThrottlePolicyDTO quotaPolicy(ThrottleLimitDTO quotaPolicy) {
    this.quotaPolicy = quotaPolicy;
    return this;
  }

   /**
   * Get quotaPolicy
   * @return quotaPolicy
  **/
  @ApiModelProperty(value = "")
  public ThrottleLimitDTO getQuotaPolicy() {
    return quotaPolicy;
  }

  public void setQuotaPolicy(ThrottleLimitDTO quotaPolicy) {
    this.quotaPolicy = quotaPolicy;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationThrottlePolicyDTO applicationThrottlePolicy = (ApplicationThrottlePolicyDTO) o;
    return Objects.equals(this.quotaPolicy, applicationThrottlePolicy.quotaPolicy) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(quotaPolicy, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationThrottlePolicyDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    quotaPolicy: ").append(toIndentedString(quotaPolicy)).append("\n");
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

