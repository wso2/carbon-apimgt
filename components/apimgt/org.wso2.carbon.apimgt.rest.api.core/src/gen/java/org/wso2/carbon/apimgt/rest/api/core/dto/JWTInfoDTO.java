package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * JWTInfoDTO
 */
public class JWTInfoDTO   {
  @SerializedName("enableJWTGeneration")
  private Boolean enableJWTGeneration = null;

  @SerializedName("jwtHeader")
  private String jwtHeader = null;

  public JWTInfoDTO enableJWTGeneration(Boolean enableJWTGeneration) {
    this.enableJWTGeneration = enableJWTGeneration;
    return this;
  }

   /**
   * Get enableJWTGeneration
   * @return enableJWTGeneration
  **/
  @ApiModelProperty(example = "false", value = "")
  public Boolean getEnableJWTGeneration() {
    return enableJWTGeneration;
  }

  public void setEnableJWTGeneration(Boolean enableJWTGeneration) {
    this.enableJWTGeneration = enableJWTGeneration;
  }

  public JWTInfoDTO jwtHeader(String jwtHeader) {
    this.jwtHeader = jwtHeader;
    return this;
  }

   /**
   * Get jwtHeader
   * @return jwtHeader
  **/
  @ApiModelProperty(example = "X-JWT-Assertion", value = "")
  public String getJwtHeader() {
    return jwtHeader;
  }

  public void setJwtHeader(String jwtHeader) {
    this.jwtHeader = jwtHeader;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JWTInfoDTO jwTInfo = (JWTInfoDTO) o;
    return Objects.equals(this.enableJWTGeneration, jwTInfo.enableJWTGeneration) &&
        Objects.equals(this.jwtHeader, jwTInfo.jwtHeader);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enableJWTGeneration, jwtHeader);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class JWTInfoDTO {\n");
    
    sb.append("    enableJWTGeneration: ").append(toIndentedString(enableJWTGeneration)).append("\n");
    sb.append("    jwtHeader: ").append(toIndentedString(jwtHeader)).append("\n");
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

