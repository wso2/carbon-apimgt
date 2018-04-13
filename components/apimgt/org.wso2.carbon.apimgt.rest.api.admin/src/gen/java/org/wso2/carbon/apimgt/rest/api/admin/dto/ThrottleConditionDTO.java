package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.dto.HeaderConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.IPConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.JWTClaimsConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.QueryParameterConditionDTO;
import java.util.Objects;

/**
 * Throttling Conditions
 */
@ApiModel(description = "Throttling Conditions")
public class ThrottleConditionDTO   {
  @SerializedName("headerCondition")
  private HeaderConditionDTO headerCondition = null;

  @SerializedName("ipCondition")
  private IPConditionDTO ipCondition = null;

  @SerializedName("jwtClaimsCondition")
  private JWTClaimsConditionDTO jwtClaimsCondition = null;

  @SerializedName("queryParameterCondition")
  private QueryParameterConditionDTO queryParameterCondition = null;

  @SerializedName("type")
  private String type = null;

  @SerializedName("invertCondition")
  private Boolean invertCondition = false;

  public ThrottleConditionDTO headerCondition(HeaderConditionDTO headerCondition) {
    this.headerCondition = headerCondition;
    return this;
  }

   /**
   * Get headerCondition
   * @return headerCondition
  **/
  @ApiModelProperty(value = "")
  public HeaderConditionDTO getHeaderCondition() {
    return headerCondition;
  }

  public void setHeaderCondition(HeaderConditionDTO headerCondition) {
    this.headerCondition = headerCondition;
  }

  public ThrottleConditionDTO ipCondition(IPConditionDTO ipCondition) {
    this.ipCondition = ipCondition;
    return this;
  }

   /**
   * Get ipCondition
   * @return ipCondition
  **/
  @ApiModelProperty(value = "")
  public IPConditionDTO getIpCondition() {
    return ipCondition;
  }

  public void setIpCondition(IPConditionDTO ipCondition) {
    this.ipCondition = ipCondition;
  }

  public ThrottleConditionDTO jwtClaimsCondition(JWTClaimsConditionDTO jwtClaimsCondition) {
    this.jwtClaimsCondition = jwtClaimsCondition;
    return this;
  }

   /**
   * Get jwtClaimsCondition
   * @return jwtClaimsCondition
  **/
  @ApiModelProperty(value = "")
  public JWTClaimsConditionDTO getJwtClaimsCondition() {
    return jwtClaimsCondition;
  }

  public void setJwtClaimsCondition(JWTClaimsConditionDTO jwtClaimsCondition) {
    this.jwtClaimsCondition = jwtClaimsCondition;
  }

  public ThrottleConditionDTO queryParameterCondition(QueryParameterConditionDTO queryParameterCondition) {
    this.queryParameterCondition = queryParameterCondition;
    return this;
  }

   /**
   * Get queryParameterCondition
   * @return queryParameterCondition
  **/
  @ApiModelProperty(value = "")
  public QueryParameterConditionDTO getQueryParameterCondition() {
    return queryParameterCondition;
  }

  public void setQueryParameterCondition(QueryParameterConditionDTO queryParameterCondition) {
    this.queryParameterCondition = queryParameterCondition;
  }

  public ThrottleConditionDTO type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(required = true, value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ThrottleConditionDTO invertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
    return this;
  }

   /**
   * Get invertCondition
   * @return invertCondition
  **/
  @ApiModelProperty(value = "")
  public Boolean getInvertCondition() {
    return invertCondition;
  }

  public void setInvertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottleConditionDTO throttleCondition = (ThrottleConditionDTO) o;
    return Objects.equals(this.headerCondition, throttleCondition.headerCondition) &&
        Objects.equals(this.ipCondition, throttleCondition.ipCondition) &&
        Objects.equals(this.jwtClaimsCondition, throttleCondition.jwtClaimsCondition) &&
        Objects.equals(this.queryParameterCondition, throttleCondition.queryParameterCondition) &&
        Objects.equals(this.type, throttleCondition.type) &&
        Objects.equals(this.invertCondition, throttleCondition.invertCondition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(headerCondition, ipCondition, jwtClaimsCondition, queryParameterCondition, type, invertCondition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleConditionDTO {\n");
    
    sb.append("    headerCondition: ").append(toIndentedString(headerCondition)).append("\n");
    sb.append("    ipCondition: ").append(toIndentedString(ipCondition)).append("\n");
    sb.append("    jwtClaimsCondition: ").append(toIndentedString(jwtClaimsCondition)).append("\n");
    sb.append("    queryParameterCondition: ").append(toIndentedString(queryParameterCondition)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    invertCondition: ").append(toIndentedString(invertCondition)).append("\n");
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

