package org.wso2.carbon.apimgt.rest.api.core.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * RegistrationSummaryDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-31T16:40:30.481+05:30")
public class RegistrationSummaryDTO   {
  @JsonProperty("KeyManagerInfo")
  private String keyManagerInfo = null;

  public RegistrationSummaryDTO keyManagerInfo(String keyManagerInfo) {
    this.keyManagerInfo = keyManagerInfo;
    return this;
  }

   /**
   * Key Manager related information 
   * @return keyManagerInfo
  **/
  @ApiModelProperty(value = "Key Manager related information ")
  public String getKeyManagerInfo() {
    return keyManagerInfo;
  }

  public void setKeyManagerInfo(String keyManagerInfo) {
    this.keyManagerInfo = keyManagerInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegistrationSummaryDTO registrationSummary = (RegistrationSummaryDTO) o;
    return Objects.equals(this.keyManagerInfo, registrationSummary.keyManagerInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyManagerInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RegistrationSummaryDTO {\n");
    
    sb.append("    keyManagerInfo: ").append(toIndentedString(keyManagerInfo)).append("\n");
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

