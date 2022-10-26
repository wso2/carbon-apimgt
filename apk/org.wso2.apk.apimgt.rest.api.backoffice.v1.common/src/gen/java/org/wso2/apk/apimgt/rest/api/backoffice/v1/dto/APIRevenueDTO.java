package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class APIRevenueDTO   {
  
    private Map<String, String> properties = new HashMap<String, String>();

  /**
   * Map of custom properties related to API revenue
   **/
  public APIRevenueDTO properties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  
  @ApiModelProperty(value = "Map of custom properties related to API revenue")
  @JsonProperty("properties")
  public Map<String, String> getProperties() {
    return properties;
  }
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIRevenueDTO apIRevenue = (APIRevenueDTO) o;
    return Objects.equals(properties, apIRevenue.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIRevenueDTO {\n");
    
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

