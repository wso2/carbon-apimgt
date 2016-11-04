package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * API_endpointSecurityDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-04T10:24:27.156+05:30")
public class API_endpointSecurityDTO   {
  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    BASIC("basic"),
    
    DIGEST("digest");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("type")
  private TypeEnum type = null;

  @JsonProperty("username")
  private String username = null;

  @JsonProperty("password")
  private String password = null;

  public API_endpointSecurityDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(example = "basic", value = "")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public API_endpointSecurityDTO username(String username) {
    this.username = username;
    return this;
  }

   /**
   * Get username
   * @return username
  **/
  @ApiModelProperty(example = "admin", value = "")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public API_endpointSecurityDTO password(String password) {
    this.password = password;
    return this;
  }

   /**
   * Get password
   * @return password
  **/
  @ApiModelProperty(example = "password", value = "")
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    API_endpointSecurityDTO aPIEndpointSecurity = (API_endpointSecurityDTO) o;
    return Objects.equals(this.type, aPIEndpointSecurity.type) &&
        Objects.equals(this.username, aPIEndpointSecurity.username) &&
        Objects.equals(this.password, aPIEndpointSecurity.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, username, password);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class API_endpointSecurityDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
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

