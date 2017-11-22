package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * GoogleAnalyticsTrackingInfoDTO
 */
public class GoogleAnalyticsTrackingInfoDTO   {
  @JsonProperty("enabled")
  private Boolean enabled = null;

  @JsonProperty("trackingID")
  private String trackingID = null;

  public GoogleAnalyticsTrackingInfoDTO enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

   /**
   * Get enabled
   * @return enabled
  **/
  @ApiModelProperty(example = "true", value = "")
  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public GoogleAnalyticsTrackingInfoDTO trackingID(String trackingID) {
    this.trackingID = trackingID;
    return this;
  }

   /**
   * Get trackingID
   * @return trackingID
  **/
  @ApiModelProperty(example = "UA-XXXXXXXX-X", value = "")
  public String getTrackingID() {
    return trackingID;
  }

  public void setTrackingID(String trackingID) {
    this.trackingID = trackingID;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GoogleAnalyticsTrackingInfoDTO googleAnalyticsTrackingInfo = (GoogleAnalyticsTrackingInfoDTO) o;
    return Objects.equals(this.enabled, googleAnalyticsTrackingInfo.enabled) &&
        Objects.equals(this.trackingID, googleAnalyticsTrackingInfo.trackingID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabled, trackingID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GoogleAnalyticsTrackingInfoDTO {\n");
    
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    trackingID: ").append(toIndentedString(trackingID)).append("\n");
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

