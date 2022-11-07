package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SubscriberContactAttributeDTO   {
  
    private String recipient = null;
    private String delimiter = null;

  /**
   * recipient type of the email 
   **/
  public SubscriberContactAttributeDTO recipient(String recipient) {
    this.recipient = recipient;
    return this;
  }

  
  @ApiModelProperty(value = "recipient type of the email ")
  @JsonProperty("recipient")
  public String getRecipient() {
    return recipient;
  }
  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  /**
   * delimiter to seperate the email address 
   **/
  public SubscriberContactAttributeDTO delimiter(String delimiter) {
    this.delimiter = delimiter;
    return this;
  }

  
  @ApiModelProperty(value = "delimiter to seperate the email address ")
  @JsonProperty("delimiter")
  public String getDelimiter() {
    return delimiter;
  }
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriberContactAttributeDTO subscriberContactAttribute = (SubscriberContactAttributeDTO) o;
    return Objects.equals(recipient, subscriberContactAttribute.recipient) &&
        Objects.equals(delimiter, subscriberContactAttribute.delimiter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(recipient, delimiter);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriberContactAttributeDTO {\n");
    
    sb.append("    recipient: ").append(toIndentedString(recipient)).append("\n");
    sb.append("    delimiter: ").append(toIndentedString(delimiter)).append("\n");
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

