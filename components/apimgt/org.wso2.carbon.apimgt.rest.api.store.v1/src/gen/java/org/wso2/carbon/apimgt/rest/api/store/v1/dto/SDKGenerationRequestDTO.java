package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIIdsListDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SDKGenerationRequestDTO   {
  
    private APIIdsListDTO apiIdsList = null;
    private String useCaseDescription = null;

  /**
   **/
  public SDKGenerationRequestDTO apiIdsList(APIIdsListDTO apiIdsList) {
    this.apiIdsList = apiIdsList;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("apiIdsList")
  public APIIdsListDTO getApiIdsList() {
    return apiIdsList;
  }
  public void setApiIdsList(APIIdsListDTO apiIdsList) {
    this.apiIdsList = apiIdsList;
  }

  /**
   * An optional description of the use case for generating the SDK. 
   **/
  public SDKGenerationRequestDTO useCaseDescription(String useCaseDescription) {
    this.useCaseDescription = useCaseDescription;
    return this;
  }

  
  @ApiModelProperty(example = "This SDK will be used for integrating multiple APIs into a mobile application.", value = "An optional description of the use case for generating the SDK. ")
  @JsonProperty("useCaseDescription")
  public String getUseCaseDescription() {
    return useCaseDescription;
  }
  public void setUseCaseDescription(String useCaseDescription) {
    this.useCaseDescription = useCaseDescription;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SDKGenerationRequestDTO sdKGenerationRequest = (SDKGenerationRequestDTO) o;
    return Objects.equals(apiIdsList, sdKGenerationRequest.apiIdsList) &&
        Objects.equals(useCaseDescription, sdKGenerationRequest.useCaseDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiIdsList, useCaseDescription);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SDKGenerationRequestDTO {\n");
    
    sb.append("    apiIdsList: ").append(toIndentedString(apiIdsList)).append("\n");
    sb.append("    useCaseDescription: ").append(toIndentedString(useCaseDescription)).append("\n");
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

