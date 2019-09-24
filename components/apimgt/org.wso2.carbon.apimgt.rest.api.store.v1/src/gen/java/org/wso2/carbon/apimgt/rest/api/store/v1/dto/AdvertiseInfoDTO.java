package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class AdvertiseInfoDTO   {
  
    private Boolean advertised = null;
    private String originalStoreUrl = null;
    private String apiOwner = null;

  /**
   **/
  public AdvertiseInfoDTO advertised(Boolean advertised) {
    this.advertised = advertised;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("advertised")
  public Boolean isAdvertised() {
    return advertised;
  }
  public void setAdvertised(Boolean advertised) {
    this.advertised = advertised;
  }

  /**
   **/
  public AdvertiseInfoDTO originalStoreUrl(String originalStoreUrl) {
    this.originalStoreUrl = originalStoreUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9443/store", value = "")
  @JsonProperty("originalStoreUrl")
  public String getOriginalStoreUrl() {
    return originalStoreUrl;
  }
  public void setOriginalStoreUrl(String originalStoreUrl) {
    this.originalStoreUrl = originalStoreUrl;
  }

  /**
   **/
  public AdvertiseInfoDTO apiOwner(String apiOwner) {
    this.apiOwner = apiOwner;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "")
  @JsonProperty("apiOwner")
  public String getApiOwner() {
    return apiOwner;
  }
  public void setApiOwner(String apiOwner) {
    this.apiOwner = apiOwner;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdvertiseInfoDTO advertiseInfo = (AdvertiseInfoDTO) o;
    return Objects.equals(advertised, advertiseInfo.advertised) &&
        Objects.equals(originalStoreUrl, advertiseInfo.originalStoreUrl) &&
        Objects.equals(apiOwner, advertiseInfo.apiOwner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(advertised, originalStoreUrl, apiOwner);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdvertiseInfoDTO {\n");
    
    sb.append("    advertised: ").append(toIndentedString(advertised)).append("\n");
    sb.append("    originalStoreUrl: ").append(toIndentedString(originalStoreUrl)).append("\n");
    sb.append("    apiOwner: ").append(toIndentedString(apiOwner)).append("\n");
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

