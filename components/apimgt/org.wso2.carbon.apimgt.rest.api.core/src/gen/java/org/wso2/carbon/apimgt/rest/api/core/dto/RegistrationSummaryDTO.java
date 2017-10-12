package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.core.dto.AnalyticsInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.GoogleAnalyticsTrackingInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.JWTInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.KeyManagerInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ThrottlingInfoDTO;
import java.util.Objects;

/**
 * RegistrationSummaryDTO
 */
public class RegistrationSummaryDTO   {
  @JsonProperty("KeyManagerInfo")
  private KeyManagerInfoDTO keyManagerInfo = null;

  @JsonProperty("JWTInfo")
  private JWTInfoDTO jwTInfo = null;

  @JsonProperty("AnalyticsInfo")
  private AnalyticsInfoDTO analyticsInfo = null;

  @JsonProperty("ThrottlingInfo")
  private ThrottlingInfoDTO throttlingInfo = null;

  @JsonProperty("GoogleAnalyticsTrackingInfo")
  private GoogleAnalyticsTrackingInfoDTO googleAnalyticsTrackingInfo = null;

  public RegistrationSummaryDTO keyManagerInfo(KeyManagerInfoDTO keyManagerInfo) {
    this.keyManagerInfo = keyManagerInfo;
    return this;
  }

   /**
   * Get keyManagerInfo
   * @return keyManagerInfo
  **/
  @ApiModelProperty(value = "")
  public KeyManagerInfoDTO getKeyManagerInfo() {
    return keyManagerInfo;
  }

  public void setKeyManagerInfo(KeyManagerInfoDTO keyManagerInfo) {
    this.keyManagerInfo = keyManagerInfo;
  }

  public RegistrationSummaryDTO jwTInfo(JWTInfoDTO jwTInfo) {
    this.jwTInfo = jwTInfo;
    return this;
  }

   /**
   * Get jwTInfo
   * @return jwTInfo
  **/
  @ApiModelProperty(value = "")
  public JWTInfoDTO getJwTInfo() {
    return jwTInfo;
  }

  public void setJwTInfo(JWTInfoDTO jwTInfo) {
    this.jwTInfo = jwTInfo;
  }

  public RegistrationSummaryDTO analyticsInfo(AnalyticsInfoDTO analyticsInfo) {
    this.analyticsInfo = analyticsInfo;
    return this;
  }

   /**
   * Get analyticsInfo
   * @return analyticsInfo
  **/
  @ApiModelProperty(value = "")
  public AnalyticsInfoDTO getAnalyticsInfo() {
    return analyticsInfo;
  }

  public void setAnalyticsInfo(AnalyticsInfoDTO analyticsInfo) {
    this.analyticsInfo = analyticsInfo;
  }

  public RegistrationSummaryDTO throttlingInfo(ThrottlingInfoDTO throttlingInfo) {
    this.throttlingInfo = throttlingInfo;
    return this;
  }

   /**
   * Get throttlingInfo
   * @return throttlingInfo
  **/
  @ApiModelProperty(value = "")
  public ThrottlingInfoDTO getThrottlingInfo() {
    return throttlingInfo;
  }

  public void setThrottlingInfo(ThrottlingInfoDTO throttlingInfo) {
    this.throttlingInfo = throttlingInfo;
  }

  public RegistrationSummaryDTO googleAnalyticsTrackingInfo(GoogleAnalyticsTrackingInfoDTO googleAnalyticsTrackingInfo) {
    this.googleAnalyticsTrackingInfo = googleAnalyticsTrackingInfo;
    return this;
  }

   /**
   * Get googleAnalyticsTrackingInfo
   * @return googleAnalyticsTrackingInfo
  **/
  @ApiModelProperty(value = "")
  public GoogleAnalyticsTrackingInfoDTO getGoogleAnalyticsTrackingInfo() {
    return googleAnalyticsTrackingInfo;
  }

  public void setGoogleAnalyticsTrackingInfo(GoogleAnalyticsTrackingInfoDTO googleAnalyticsTrackingInfo) {
    this.googleAnalyticsTrackingInfo = googleAnalyticsTrackingInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegistrationSummaryDTO registrationSummary = (RegistrationSummaryDTO) o;
    return Objects.equals(this.keyManagerInfo, registrationSummary.keyManagerInfo) &&
        Objects.equals(this.jwTInfo, registrationSummary.jwTInfo) &&
        Objects.equals(this.analyticsInfo, registrationSummary.analyticsInfo) &&
        Objects.equals(this.throttlingInfo, registrationSummary.throttlingInfo) &&
        Objects.equals(this.googleAnalyticsTrackingInfo, registrationSummary.googleAnalyticsTrackingInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyManagerInfo, jwTInfo, analyticsInfo, throttlingInfo, googleAnalyticsTrackingInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RegistrationSummaryDTO {\n");
    
    sb.append("    keyManagerInfo: ").append(toIndentedString(keyManagerInfo)).append("\n");
    sb.append("    jwTInfo: ").append(toIndentedString(jwTInfo)).append("\n");
    sb.append("    analyticsInfo: ").append(toIndentedString(analyticsInfo)).append("\n");
    sb.append("    throttlingInfo: ").append(toIndentedString(throttlingInfo)).append("\n");
    sb.append("    googleAnalyticsTrackingInfo: ").append(toIndentedString(googleAnalyticsTrackingInfo)).append("\n");
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

