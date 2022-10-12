package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class AdditionalSubscriptionInfoSolaceURLsDTO   {
  
    private String protocol = null;
    private String endpointURL = null;

  /**
   **/
  public AdditionalSubscriptionInfoSolaceURLsDTO protocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  
  @ApiModelProperty(example = "Defalt", value = "")
  @JsonProperty("protocol")
  public String getProtocol() {
    return protocol;
  }
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   **/
  public AdditionalSubscriptionInfoSolaceURLsDTO endpointURL(String endpointURL) {
    this.endpointURL = endpointURL;
    return this;
  }

  
  @ApiModelProperty(example = "Default", value = "")
  @JsonProperty("endpointURL")
  public String getEndpointURL() {
    return endpointURL;
  }
  public void setEndpointURL(String endpointURL) {
    this.endpointURL = endpointURL;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdditionalSubscriptionInfoSolaceURLsDTO additionalSubscriptionInfoSolaceURLs = (AdditionalSubscriptionInfoSolaceURLsDTO) o;
    return Objects.equals(protocol, additionalSubscriptionInfoSolaceURLs.protocol) &&
        Objects.equals(endpointURL, additionalSubscriptionInfoSolaceURLs.endpointURL);
  }

  @Override
  public int hashCode() {
    return Objects.hash(protocol, endpointURL);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalSubscriptionInfoSolaceURLsDTO {\n");
    
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    endpointURL: ").append(toIndentedString(endpointURL)).append("\n");
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

