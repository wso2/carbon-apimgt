package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class APIBackendJWTConfigurationDTO   {
  
    private List<String> audiences = new ArrayList<String>();

  /**
   * The list of audiences to which the JWT should be issued. 
   **/
  public APIBackendJWTConfigurationDTO audiences(List<String> audiences) {
    this.audiences = audiences;
    return this;
  }

  
  @ApiModelProperty(example = "[\"sampleOrg1.com\",\"sampleOrg2.com\"]", value = "The list of audiences to which the JWT should be issued. ")
  @JsonProperty("audiences")
  public List<String> getAudiences() {
    return audiences;
  }
  public void setAudiences(List<String> audiences) {
    this.audiences = audiences;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIBackendJWTConfigurationDTO apIBackendJWTConfiguration = (APIBackendJWTConfigurationDTO) o;
    return Objects.equals(audiences, apIBackendJWTConfiguration.audiences);
  }

  @Override
  public int hashCode() {
    return Objects.hash(audiences);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIBackendJWTConfigurationDTO {\n");
    
    sb.append("    audiences: ").append(toIndentedString(audiences)).append("\n");
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
