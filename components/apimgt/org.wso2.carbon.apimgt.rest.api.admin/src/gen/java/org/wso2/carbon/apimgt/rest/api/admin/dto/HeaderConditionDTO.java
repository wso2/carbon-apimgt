package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleConditionDTO;

/**
 * HeaderConditionDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-05-04T16:42:24.822+05:30")
public class HeaderConditionDTO extends ThrottleConditionDTO  {
  @JsonProperty("headerName")
  private String headerName = null;

  @JsonProperty("headerValue")
  private String headerValue = null;

  public HeaderConditionDTO headerName(String headerName) {
    this.headerName = headerName;
    return this;
  }

   /**
   * Get headerName
   * @return headerName
  **/
  @ApiModelProperty(value = "")
  public String getHeaderName() {
    return headerName;
  }

  public void setHeaderName(String headerName) {
    this.headerName = headerName;
  }

  public HeaderConditionDTO headerValue(String headerValue) {
    this.headerValue = headerValue;
    return this;
  }

   /**
   * Get headerValue
   * @return headerValue
  **/
  @ApiModelProperty(value = "")
  public String getHeaderValue() {
    return headerValue;
  }

  public void setHeaderValue(String headerValue) {
    this.headerValue = headerValue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HeaderConditionDTO headerCondition = (HeaderConditionDTO) o;
    return Objects.equals(this.headerName, headerCondition.headerName) &&
        Objects.equals(this.headerValue, headerCondition.headerValue) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(headerName, headerValue, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HeaderConditionDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    headerName: ").append(toIndentedString(headerName)).append("\n");
    sb.append("    headerValue: ").append(toIndentedString(headerValue)).append("\n");
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

