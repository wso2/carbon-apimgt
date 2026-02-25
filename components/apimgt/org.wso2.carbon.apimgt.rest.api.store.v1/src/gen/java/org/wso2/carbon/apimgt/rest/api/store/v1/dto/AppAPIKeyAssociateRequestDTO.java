package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

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



public class AppAPIKeyAssociateRequestDTO   {
  
    private String apiName = null;
    private String apiId = null;
    private String keyName = null;

  /**
   * API name
   **/
  public AppAPIKeyAssociateRequestDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(example = "SampleAPI", value = "API name")
  @JsonProperty("apiName")
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  /**
   * The unique identifier of the API.
   **/
  public AppAPIKeyAssociateRequestDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(example = "2962f3bb-8330-438e-baee-0ee1d6434ba4", value = "The unique identifier of the API.")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   * API key name
   **/
  public AppAPIKeyAssociateRequestDTO keyName(String keyName) {
    this.keyName = keyName;
    return this;
  }

  
  @ApiModelProperty(example = "Test_Key", value = "API key name")
  @JsonProperty("keyName")
  public String getKeyName() {
    return keyName;
  }
  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AppAPIKeyAssociateRequestDTO appAPIKeyAssociateRequest = (AppAPIKeyAssociateRequestDTO) o;
    return Objects.equals(apiName, appAPIKeyAssociateRequest.apiName) &&
        Objects.equals(apiId, appAPIKeyAssociateRequest.apiId) &&
        Objects.equals(keyName, appAPIKeyAssociateRequest.keyName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiName, apiId, keyName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppAPIKeyAssociateRequestDTO {\n");
    
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    keyName: ").append(toIndentedString(keyName)).append("\n");
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

