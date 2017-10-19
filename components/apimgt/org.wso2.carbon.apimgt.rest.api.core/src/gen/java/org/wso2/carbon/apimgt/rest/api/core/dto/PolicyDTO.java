package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * PolicyDTO
 */
public class PolicyDTO   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("stopOnQuotaReach")
  private Boolean stopOnQuotaReach = null;

  public PolicyDTO id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PolicyDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "CalculatorAPI", value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PolicyDTO stopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
    return this;
  }

   /**
   * Get stopOnQuotaReach
   * @return stopOnQuotaReach
  **/
  @ApiModelProperty(example = "false", value = "")
  public Boolean getStopOnQuotaReach() {
    return stopOnQuotaReach;
  }

  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PolicyDTO policy = (PolicyDTO) o;
    return Objects.equals(this.id, policy.id) &&
        Objects.equals(this.name, policy.name) &&
        Objects.equals(this.stopOnQuotaReach, policy.stopOnQuotaReach);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, stopOnQuotaReach);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    stopOnQuotaReach: ").append(toIndentedString(stopOnQuotaReach)).append("\n");
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

