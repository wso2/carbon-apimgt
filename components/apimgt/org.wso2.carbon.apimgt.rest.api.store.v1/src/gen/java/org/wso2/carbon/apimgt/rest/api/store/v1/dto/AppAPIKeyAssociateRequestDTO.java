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
  
    private String apiUUID = null;
    private String keyUUID = null;

  /**
   * The unique identifier of the API.
   **/
  public AppAPIKeyAssociateRequestDTO apiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
    return this;
  }

  
  @ApiModelProperty(example = "2962f3bb-8330-438e-baee-0ee1d6434ba4", required = true, value = "The unique identifier of the API.")
  @JsonProperty("apiUUID")
  @NotNull
  public String getApiUUID() {
    return apiUUID;
  }
  public void setApiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
  }

  /**
   * The UUID of the API key
   **/
  public AppAPIKeyAssociateRequestDTO keyUUID(String keyUUID) {
    this.keyUUID = keyUUID;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The UUID of the API key")
  @JsonProperty("keyUUID")
  @NotNull
  public String getKeyUUID() {
    return keyUUID;
  }
  public void setKeyUUID(String keyUUID) {
    this.keyUUID = keyUUID;
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
    return Objects.equals(apiUUID, appAPIKeyAssociateRequest.apiUUID) &&
        Objects.equals(keyUUID, appAPIKeyAssociateRequest.keyUUID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiUUID, keyUUID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppAPIKeyAssociateRequestDTO {\n");
    
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    keyUUID: ").append(toIndentedString(keyUUID)).append("\n");
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

