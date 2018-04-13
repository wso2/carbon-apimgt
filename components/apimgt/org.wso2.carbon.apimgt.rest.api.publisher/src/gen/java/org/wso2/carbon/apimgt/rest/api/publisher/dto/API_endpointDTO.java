package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import java.util.Objects;

/**
 * API_endpointDTO
 */
public class API_endpointDTO   {
  @SerializedName("key")
  private String key = null;

  @SerializedName("inline")
  private EndPointDTO inline = null;

  @SerializedName("type")
  private String type = null;

  public API_endpointDTO key(String key) {
    this.key = key;
    return this;
  }

   /**
   * Get key
   * @return key
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public API_endpointDTO inline(EndPointDTO inline) {
    this.inline = inline;
    return this;
  }

   /**
   * Get inline
   * @return inline
  **/
  @ApiModelProperty(value = "")
  public EndPointDTO getInline() {
    return inline;
  }

  public void setInline(EndPointDTO inline) {
    this.inline = inline;
  }

  public API_endpointDTO type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(example = "Production", value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    API_endpointDTO apIEndpoint = (API_endpointDTO) o;
    return Objects.equals(this.key, apIEndpoint.key) &&
        Objects.equals(this.inline, apIEndpoint.inline) &&
        Objects.equals(this.type, apIEndpoint.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, inline, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class API_endpointDTO {\n");
    
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    inline: ").append(toIndentedString(inline)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

