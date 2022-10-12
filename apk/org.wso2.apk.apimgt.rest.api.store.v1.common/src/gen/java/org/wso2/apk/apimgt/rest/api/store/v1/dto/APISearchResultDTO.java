package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

import javax.validation.Valid;



public class APISearchResultDTO extends SearchResultDTO  {
  
    private String description = null;
    private String context = null;
    private String version = null;
    private String provider = null;
    private String status = null;
    private String thumbnailUri = null;
    private APIBusinessInformationDTO businessInformation = null;
    private String avgRating = null;

  /**
   * A brief description about the API
   **/
  public APISearchResultDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A calculator API that supports basic operations", value = "A brief description about the API")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * A string that represents the context of the user&#39;s request
   **/
  public APISearchResultDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorAPI", value = "A string that represents the context of the user's request")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * The version of the API
   **/
  public APISearchResultDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "The version of the API")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * If the provider value is notgiven, the user invoking the API will be used as the provider. 
   **/
  public APISearchResultDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "If the provider value is notgiven, the user invoking the API will be used as the provider. ")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * This describes in which status of the lifecycle the API is
   **/
  public APISearchResultDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "CREATED", value = "This describes in which status of the lifecycle the API is")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public APISearchResultDTO thumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
    return this;
  }

  
  @ApiModelProperty(example = "/apis/01234567-0123-0123-0123-012345678901/thumbnail", value = "")
  @JsonProperty("thumbnailUri")
  public String getThumbnailUri() {
    return thumbnailUri;
  }
  public void setThumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
  }

  /**
   **/
  public APISearchResultDTO businessInformation(APIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("businessInformation")
  public APIBusinessInformationDTO getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(APIBusinessInformationDTO businessInformation) {
    this.businessInformation = businessInformation;
  }

  /**
   * Average rating of the API
   **/
  public APISearchResultDTO avgRating(String avgRating) {
    this.avgRating = avgRating;
    return this;
  }

  
  @ApiModelProperty(example = "4.5", value = "Average rating of the API")
  @JsonProperty("avgRating")
  public String getAvgRating() {
    return avgRating;
  }
  public void setAvgRating(String avgRating) {
    this.avgRating = avgRating;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APISearchResultDTO apISearchResult = (APISearchResultDTO) o;
    return Objects.equals(description, apISearchResult.description) &&
        Objects.equals(context, apISearchResult.context) &&
        Objects.equals(version, apISearchResult.version) &&
        Objects.equals(provider, apISearchResult.provider) &&
        Objects.equals(status, apISearchResult.status) &&
        Objects.equals(thumbnailUri, apISearchResult.thumbnailUri) &&
        Objects.equals(businessInformation, apISearchResult.businessInformation) &&
        Objects.equals(avgRating, apISearchResult.avgRating);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, context, version, provider, status, thumbnailUri, businessInformation, avgRating);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APISearchResultDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    thumbnailUri: ").append(toIndentedString(thumbnailUri)).append("\n");
    sb.append("    businessInformation: ").append(toIndentedString(businessInformation)).append("\n");
    sb.append("    avgRating: ").append(toIndentedString(avgRating)).append("\n");
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

