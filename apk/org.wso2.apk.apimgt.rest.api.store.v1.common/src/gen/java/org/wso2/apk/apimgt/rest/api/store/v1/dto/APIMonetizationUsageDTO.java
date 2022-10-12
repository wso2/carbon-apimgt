package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;

import java.util.Objects;

public class APIMonetizationUsageDTO   {
  
    private Map<String, String> properties = new HashMap<String, String>();

  /**
   * Map of custom properties related to monetization usage
   **/
  public APIMonetizationUsageDTO properties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  
  @ApiModelProperty(value = "Map of custom properties related to monetization usage")
  @JsonProperty("properties")
  public Map<String, String> getProperties() {
    return properties;
  }
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIMonetizationUsageDTO apIMonetizationUsage = (APIMonetizationUsageDTO) o;
    return Objects.equals(properties, apIMonetizationUsage.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIMonetizationUsageDTO {\n");
    
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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

