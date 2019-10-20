package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;



public class APIKeyRevokeRequestDTO   {
  
    private String apikey = null;

  /**
   * API Key to revoke
   **/
  public APIKeyRevokeRequestDTO apikey(String apikey) {
    this.apikey = apikey;
    return this;
  }

  
  @ApiModelProperty(value = "API Key to revoke")
  @JsonProperty("apikey")
  public String getApikey() {
    return apikey;
  }
  public void setApikey(String apikey) {
    this.apikey = apikey;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIKeyRevokeRequestDTO apIKeyRevokeRequest = (APIKeyRevokeRequestDTO) o;
    return Objects.equals(apikey, apIKeyRevokeRequest.apikey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apikey);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIKeyRevokeRequestDTO {\n");
    
    sb.append("    apikey: ").append(toIndentedString(apikey)).append("\n");
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

