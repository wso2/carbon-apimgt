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



public class DesignAssistantChatQueryDTO   {
  
    private String text = null;
    private String sessionId = null;

  /**
   **/
  public DesignAssistantChatQueryDTO text(String text) {
    this.text = text;
    return this;
  }

  
  @ApiModelProperty(example = "create an API for a banking transaction", value = "")
  @JsonProperty("text")
  public String getText() {
    return text;
  }
  public void setText(String text) {
    this.text = text;
  }

  /**
   **/
  public DesignAssistantChatQueryDTO sessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  
  @ApiModelProperty(example = "1234567890", value = "")
  @JsonProperty("session_id")
  public String getSessionId() {
    return sessionId;
  }
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesignAssistantChatQueryDTO designAssistantChatQuery = (DesignAssistantChatQueryDTO) o;
    return Objects.equals(text, designAssistantChatQuery.text) &&
        Objects.equals(sessionId, designAssistantChatQuery.sessionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, sessionId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesignAssistantChatQueryDTO {\n");
    
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
    sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
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

