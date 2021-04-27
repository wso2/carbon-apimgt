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



public class MockResponsePayloadInfoDTO   {
  
    private String path = null;
    private String content = null;
    private String verb = null;

  /**
   * path of the resource
   **/
  public MockResponsePayloadInfoDTO path(String path) {
    this.path = path;
    return this;
  }

  
  @ApiModelProperty(example = "/menu", value = "path of the resource")
  @JsonProperty("path")
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * new modified code
   **/
  public MockResponsePayloadInfoDTO content(String content) {
    this.content = content;
    return this;
  }

  
  @ApiModelProperty(example = "var accept = \"\\\"\"+mc.getProperty('AcceptHeader')+\"\\\"\"; var responseCode = mc.getProperty('query.param.responseCode'); var responseCodeStr = \"\\\"\"+responseCode+\"\\\"\"; var responses = [];  if (!responses[200]) {  responses [200] = []; } responses[200][\"application/json\"] =  [ {   \"price\" : \"string\",   \"description\" : \"string\",   \"name\" : \"string\",   \"image\" : \"string\" } ]  /_*if (!responses[304]) {   responses[304] = []; } responses[304][\"application/(json or xml)\"] = {}/<>*_/  if (!responses[406]) {  responses [406] = []; } responses[406][\"application/json\"] =  {   \"message\" : \"string\",   \"error\" : [ {     \"message\" : \"string\",     \"code\" : 0   } ],   \"description\" : \"string\",   \"code\" : 0,   \"moreInfo\" : \"string\" }  responses[501] = []; responses[501][\"application/json\"] = { \"code\" : 501, \"description\" : \"Not Implemented\"} responses[501][\"application/xml\"] = <response><code>501</code><description>Not Implemented</description></response>;  if (!responses[responseCode]) {  responseCode = 501; }  if (responseCode == null) {  responseCode = 200;  responseCodeStr = \"200\"; }  if (accept == null || !responses[responseCode][accept]) {  accept = \"application/json\"; }  if (accept === \"application/json\") {  mc.setProperty('CONTENT_TYPE', 'application/json');  mc.setProperty('HTTP_SC', responseCodeStr);  mc.setPayloadJSON(responses[responseCode][\"application/json\"]); } else if (accept === \"application/xml\") {  mc.setProperty('CONTENT_TYPE', 'application/xml');  mc.setProperty('HTTP_SC', responseCodeStr);  mc.setPayloadXML(responses[responseCode][\"application/xml\"]); }", value = "new modified code")
  @JsonProperty("content")
  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }

  /**
   **/
  public MockResponsePayloadInfoDTO verb(String verb) {
    this.verb = verb;
    return this;
  }

  
  @ApiModelProperty(example = "POST", value = "")
  @JsonProperty("verb")
  public String getVerb() {
    return verb;
  }
  public void setVerb(String verb) {
    this.verb = verb;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MockResponsePayloadInfoDTO mockResponsePayloadInfo = (MockResponsePayloadInfoDTO) o;
    return Objects.equals(path, mockResponsePayloadInfo.path) &&
        Objects.equals(content, mockResponsePayloadInfo.content) &&
        Objects.equals(verb, mockResponsePayloadInfo.verb);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, content, verb);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MockResponsePayloadInfoDTO {\n");
    
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    verb: ").append(toIndentedString(verb)).append("\n");
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

