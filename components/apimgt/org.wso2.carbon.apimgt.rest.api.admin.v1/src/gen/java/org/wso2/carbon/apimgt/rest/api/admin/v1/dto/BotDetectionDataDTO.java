package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class BotDetectionDataDTO   {
  
    private Long recordedTime = null;
    private String messageID = null;
    private String apiMethod = null;
    private String headerSet = null;
    private String messageBody = null;
    private String clientIp = null;

  /**
   * The time of detection
   **/
  public BotDetectionDataDTO recordedTime(Long recordedTime) {
    this.recordedTime = recordedTime;
    return this;
  }

  
  @ApiModelProperty(example = "1591734138413", value = "The time of detection")
  @JsonProperty("recordedTime")
  public Long getRecordedTime() {
    return recordedTime;
  }
  public void setRecordedTime(Long recordedTime) {
    this.recordedTime = recordedTime;
  }

  /**
   * The message ID
   **/
  public BotDetectionDataDTO messageID(String messageID) {
    this.messageID = messageID;
    return this;
  }

  
  @ApiModelProperty(example = "urn:uuid:1ed6d2de-29df-4fed-a96a-46d2329dce65", value = "The message ID")
  @JsonProperty("messageID")
  public String getMessageID() {
    return messageID;
  }
  public void setMessageID(String messageID) {
    this.messageID = messageID;
  }

  /**
   * The api method
   **/
  public BotDetectionDataDTO apiMethod(String apiMethod) {
    this.apiMethod = apiMethod;
    return this;
  }

  
  @ApiModelProperty(example = "GET", value = "The api method")
  @JsonProperty("apiMethod")
  public String getApiMethod() {
    return apiMethod;
  }
  public void setApiMethod(String apiMethod) {
    this.apiMethod = apiMethod;
  }

  /**
   * The header set
   **/
  public BotDetectionDataDTO headerSet(String headerSet) {
    this.headerSet = headerSet;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "The header set")
  @JsonProperty("headerSet")
  public String getHeaderSet() {
    return headerSet;
  }
  public void setHeaderSet(String headerSet) {
    this.headerSet = headerSet;
  }

  /**
   * The content of the message body
   **/
  public BotDetectionDataDTO messageBody(String messageBody) {
    this.messageBody = messageBody;
    return this;
  }

  
  @ApiModelProperty(example = "<soapenv:Body xmlns:soapenv=\\\"http://www.w3.org/2003/05/soap-envelope\\\"/>", value = "The content of the message body")
  @JsonProperty("messageBody")
  public String getMessageBody() {
    return messageBody;
  }
  public void setMessageBody(String messageBody) {
    this.messageBody = messageBody;
  }

  /**
   * The IP of the client
   **/
  public BotDetectionDataDTO clientIp(String clientIp) {
    this.clientIp = clientIp;
    return this;
  }

  
  @ApiModelProperty(example = "127.0.0.1", value = "The IP of the client")
  @JsonProperty("clientIp")
  public String getClientIp() {
    return clientIp;
  }
  public void setClientIp(String clientIp) {
    this.clientIp = clientIp;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BotDetectionDataDTO botDetectionData = (BotDetectionDataDTO) o;
    return Objects.equals(recordedTime, botDetectionData.recordedTime) &&
        Objects.equals(messageID, botDetectionData.messageID) &&
        Objects.equals(apiMethod, botDetectionData.apiMethod) &&
        Objects.equals(headerSet, botDetectionData.headerSet) &&
        Objects.equals(messageBody, botDetectionData.messageBody) &&
        Objects.equals(clientIp, botDetectionData.clientIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(recordedTime, messageID, apiMethod, headerSet, messageBody, clientIp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BotDetectionDataDTO {\n");
    
    sb.append("    recordedTime: ").append(toIndentedString(recordedTime)).append("\n");
    sb.append("    messageID: ").append(toIndentedString(messageID)).append("\n");
    sb.append("    apiMethod: ").append(toIndentedString(apiMethod)).append("\n");
    sb.append("    headerSet: ").append(toIndentedString(headerSet)).append("\n");
    sb.append("    messageBody: ").append(toIndentedString(messageBody)).append("\n");
    sb.append("    clientIp: ").append(toIndentedString(clientIp)).append("\n");
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

