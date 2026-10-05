package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class DesignAssistantChatResponseDTO   {
  
    private String backendResponse = null;
    private Boolean isSuggestions = null;
    private String typeOfApi = null;
    private String code = null;
    private List<String> paths = new ArrayList<String>();
    private String apiTypeSuggestion = null;
    private String missingValues = null;
    private String state = null;

  /**
   **/
  public DesignAssistantChatResponseDTO backendResponse(String backendResponse) {
    this.backendResponse = backendResponse;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("backendResponse")
  public String getBackendResponse() {
    return backendResponse;
  }
  public void setBackendResponse(String backendResponse) {
    this.backendResponse = backendResponse;
  }

  /**
   **/
  public DesignAssistantChatResponseDTO isSuggestions(Boolean isSuggestions) {
    this.isSuggestions = isSuggestions;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("isSuggestions")
  public Boolean isIsSuggestions() {
    return isSuggestions;
  }
  public void setIsSuggestions(Boolean isSuggestions) {
    this.isSuggestions = isSuggestions;
  }

  /**
   **/
  public DesignAssistantChatResponseDTO typeOfApi(String typeOfApi) {
    this.typeOfApi = typeOfApi;
    return this;
  }

  
  @ApiModelProperty(example = "REST", value = "")
  @JsonProperty("typeOfApi")
  public String getTypeOfApi() {
    return typeOfApi;
  }
  public void setTypeOfApi(String typeOfApi) {
    this.typeOfApi = typeOfApi;
  }

  /**
   **/
  public DesignAssistantChatResponseDTO code(String code) {
    this.code = code;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("code")
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }

  /**
   **/
  public DesignAssistantChatResponseDTO paths(List<String> paths) {
    this.paths = paths;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "")
  @JsonProperty("paths")
  public List<String> getPaths() {
    return paths;
  }
  public void setPaths(List<String> paths) {
    this.paths = paths;
  }

  /**
   **/
  public DesignAssistantChatResponseDTO apiTypeSuggestion(String apiTypeSuggestion) {
    this.apiTypeSuggestion = apiTypeSuggestion;
    return this;
  }

  
  @ApiModelProperty(example = "This is suitable for CRUD operations in banking. Do you want to proceed?", value = "")
  @JsonProperty("apiTypeSuggestion")
  public String getApiTypeSuggestion() {
    return apiTypeSuggestion;
  }
  public void setApiTypeSuggestion(String apiTypeSuggestion) {
    this.apiTypeSuggestion = apiTypeSuggestion;
  }

  /**
   **/
  public DesignAssistantChatResponseDTO missingValues(String missingValues) {
    this.missingValues = missingValues;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("missingValues")
  public String getMissingValues() {
    return missingValues;
  }
  public void setMissingValues(String missingValues) {
    this.missingValues = missingValues;
  }

  /**
   **/
  public DesignAssistantChatResponseDTO state(String state) {
    this.state = state;
    return this;
  }

  
  @ApiModelProperty(example = "COMPLETE", value = "")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DesignAssistantChatResponseDTO designAssistantChatResponse = (DesignAssistantChatResponseDTO) o;
    return Objects.equals(backendResponse, designAssistantChatResponse.backendResponse) &&
        Objects.equals(isSuggestions, designAssistantChatResponse.isSuggestions) &&
        Objects.equals(typeOfApi, designAssistantChatResponse.typeOfApi) &&
        Objects.equals(code, designAssistantChatResponse.code) &&
        Objects.equals(paths, designAssistantChatResponse.paths) &&
        Objects.equals(apiTypeSuggestion, designAssistantChatResponse.apiTypeSuggestion) &&
        Objects.equals(missingValues, designAssistantChatResponse.missingValues) &&
        Objects.equals(state, designAssistantChatResponse.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(backendResponse, isSuggestions, typeOfApi, code, paths, apiTypeSuggestion, missingValues, state);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DesignAssistantChatResponseDTO {\n");
    
    sb.append("    backendResponse: ").append(toIndentedString(backendResponse)).append("\n");
    sb.append("    isSuggestions: ").append(toIndentedString(isSuggestions)).append("\n");
    sb.append("    typeOfApi: ").append(toIndentedString(typeOfApi)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    paths: ").append(toIndentedString(paths)).append("\n");
    sb.append("    apiTypeSuggestion: ").append(toIndentedString(apiTypeSuggestion)).append("\n");
    sb.append("    missingValues: ").append(toIndentedString(missingValues)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
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

