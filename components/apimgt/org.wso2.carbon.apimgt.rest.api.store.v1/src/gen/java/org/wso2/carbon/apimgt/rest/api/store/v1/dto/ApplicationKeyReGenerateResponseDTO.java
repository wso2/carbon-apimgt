package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ApplicationKeyReGenerateResponseDTO   {
  
    private String consumerKey = null;
    private String consumerSecret = null;

  /**
   * The consumer key associated with the application, used to indetify the client
   **/
  public ApplicationKeyReGenerateResponseDTO consumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
    return this;
  }

  
  @ApiModelProperty(example = "vYDoc9s7IgAFdkSyNDaswBX7ejoa", value = "The consumer key associated with the application, used to indetify the client")
  @JsonProperty("consumerKey")
  public String getConsumerKey() {
    return consumerKey;
  }
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  /**
   * The client secret that is used to authenticate the client with the authentication server
   **/
  public ApplicationKeyReGenerateResponseDTO consumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
    return this;
  }

  
  @ApiModelProperty(example = "TIDlOFkpzB7WjufO3OJUhy1fsvAa", value = "The client secret that is used to authenticate the client with the authentication server")
  @JsonProperty("consumerSecret")
  public String getConsumerSecret() {
    return consumerSecret;
  }
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationKeyReGenerateResponseDTO applicationKeyReGenerateResponse = (ApplicationKeyReGenerateResponseDTO) o;
    return Objects.equals(consumerKey, applicationKeyReGenerateResponse.consumerKey) &&
        Objects.equals(consumerSecret, applicationKeyReGenerateResponse.consumerSecret);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerKey, consumerSecret);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyReGenerateResponseDTO {\n");
    
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
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

