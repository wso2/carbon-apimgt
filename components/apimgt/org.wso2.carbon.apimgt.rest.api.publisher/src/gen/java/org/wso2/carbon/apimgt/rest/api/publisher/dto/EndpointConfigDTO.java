package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_additionalPropertiesDTO;
import java.util.Objects;

/**
 * EndpointConfigDTO
 */
public class EndpointConfigDTO   {
  @SerializedName("url")
  private String url = null;

  @SerializedName("timeout")
  private String timeout = null;

  @SerializedName("isPrimary")
  private Boolean isPrimary = null;

  @SerializedName("attributes")
  private List<API_additionalPropertiesDTO> attributes = new ArrayList<API_additionalPropertiesDTO>();

  public EndpointConfigDTO url(String url) {
    this.url = url;
    return this;
  }

   /**
   * Service url of the endpoint 
   * @return url
  **/
  @ApiModelProperty(example = "http://localhost:8280", value = "Service url of the endpoint ")
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public EndpointConfigDTO timeout(String timeout) {
    this.timeout = timeout;
    return this;
  }

   /**
   * Time out of the endpoint 
   * @return timeout
  **/
  @ApiModelProperty(example = "1000", value = "Time out of the endpoint ")
  public String getTimeout() {
    return timeout;
  }

  public void setTimeout(String timeout) {
    this.timeout = timeout;
  }

  public EndpointConfigDTO isPrimary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
    return this;
  }

   /**
   * Defines whether the endpoint is primary when used in fail over. 
   * @return isPrimary
  **/
  @ApiModelProperty(example = "true", value = "Defines whether the endpoint is primary when used in fail over. ")
  public Boolean getIsPrimary() {
    return isPrimary;
  }

  public void setIsPrimary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
  }

  public EndpointConfigDTO attributes(List<API_additionalPropertiesDTO> attributes) {
    this.attributes = attributes;
    return this;
  }

  public EndpointConfigDTO addAttributesItem(API_additionalPropertiesDTO attributesItem) {
    this.attributes.add(attributesItem);
    return this;
  }

   /**
   * Get attributes
   * @return attributes
  **/
  @ApiModelProperty(value = "")
  public List<API_additionalPropertiesDTO> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<API_additionalPropertiesDTO> attributes) {
    this.attributes = attributes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndpointConfigDTO endpointConfig = (EndpointConfigDTO) o;
    return Objects.equals(this.url, endpointConfig.url) &&
        Objects.equals(this.timeout, endpointConfig.timeout) &&
        Objects.equals(this.isPrimary, endpointConfig.isPrimary) &&
        Objects.equals(this.attributes, endpointConfig.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, timeout, isPrimary, attributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndpointConfigDTO {\n");
    
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    timeout: ").append(toIndentedString(timeout)).append("\n");
    sb.append("    isPrimary: ").append(toIndentedString(isPrimary)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
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

