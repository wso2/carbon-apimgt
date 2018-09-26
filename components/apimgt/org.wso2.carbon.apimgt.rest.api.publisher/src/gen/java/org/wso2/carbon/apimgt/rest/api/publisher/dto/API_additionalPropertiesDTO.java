package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * API_additionalPropertiesDTO
 */
public class API_additionalPropertiesDTO   {
  @SerializedName("name")
  private String name = null;

  @SerializedName("value")
  private String value = null;

  public API_additionalPropertiesDTO(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public API_additionalPropertiesDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "Suspension time", value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public API_additionalPropertiesDTO value(String value) {
    this.value = value;
    return this;
  }

   /**
   * Get value
   * @return value
  **/
  @ApiModelProperty(example = "2s", value = "")
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    API_additionalPropertiesDTO apIAdditionalProperties = (API_additionalPropertiesDTO) o;
    return Objects.equals(this.name, apIAdditionalProperties.name) &&
        Objects.equals(this.value, apIAdditionalProperties.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class API_additionalPropertiesDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

