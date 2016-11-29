package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * API_maxTpsDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-29T15:29:39.232+05:30")
public class API_maxTpsDTO   {
  @JsonProperty("production")
  private Long production = null;

  @JsonProperty("sandbox")
  private Long sandbox = null;

  public API_maxTpsDTO production(Long production) {
    this.production = production;
    return this;
  }

   /**
   * Get production
   * @return production
  **/
  @ApiModelProperty(example = "1000", value = "")
  public Long getProduction() {
    return production;
  }

  public void setProduction(Long production) {
    this.production = production;
  }

  public API_maxTpsDTO sandbox(Long sandbox) {
    this.sandbox = sandbox;
    return this;
  }

   /**
   * Get sandbox
   * @return sandbox
  **/
  @ApiModelProperty(example = "1000", value = "")
  public Long getSandbox() {
    return sandbox;
  }

  public void setSandbox(Long sandbox) {
    this.sandbox = sandbox;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    API_maxTpsDTO apIMaxTps = (API_maxTpsDTO) o;
    return Objects.equals(this.production, apIMaxTps.production) &&
        Objects.equals(this.sandbox, apIMaxTps.sandbox);
  }

  @Override
  public int hashCode() {
    return Objects.hash(production, sandbox);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class API_maxTpsDTO {\n");
    
    sb.append("    production: ").append(toIndentedString(production)).append("\n");
    sb.append("    sandbox: ").append(toIndentedString(sandbox)).append("\n");
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

