package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPoint_endpointConfig_circuitBreakerDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndpointConfigDTO;
import java.util.Objects;

/**
 * EndPoint_endpointConfigDTO
 */
public class EndPoint_endpointConfigDTO   {
  /**
   * Gets or Sets endpointType
   */
  public enum EndpointTypeEnum {
    @SerializedName("SINGLE")
    SINGLE("SINGLE"),
    
    @SerializedName("LOAD_BALANCED")
    LOAD_BALANCED("LOAD_BALANCED"),
    
    @SerializedName("FAIL_OVER")
    FAIL_OVER("FAIL_OVER");

    private String value;

    EndpointTypeEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    public static EndpointTypeEnum fromValue(String text) {
      for (EndpointTypeEnum b : EndpointTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @SerializedName("endpointType")
  private EndpointTypeEnum endpointType = null;

  @SerializedName("list")
  private List<EndpointConfigDTO> list = new ArrayList<EndpointConfigDTO>();

  @SerializedName("circuitBreaker")
  private EndPoint_endpointConfig_circuitBreakerDTO circuitBreaker = null;

  public EndPoint_endpointConfigDTO endpointType(EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
    return this;
  }

   /**
   * Get endpointType
   * @return endpointType
  **/
  @ApiModelProperty(example = "FAIL_OVER", value = "")
  public EndpointTypeEnum getEndpointType() {
    return endpointType;
  }

  public void setEndpointType(EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
  }

  public EndPoint_endpointConfigDTO list(List<EndpointConfigDTO> list) {
    this.list = list;
    return this;
  }

  public EndPoint_endpointConfigDTO addListItem(EndpointConfigDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<EndpointConfigDTO> getList() {
    return list;
  }

  public void setList(List<EndpointConfigDTO> list) {
    this.list = list;
  }

  public EndPoint_endpointConfigDTO circuitBreaker(EndPoint_endpointConfig_circuitBreakerDTO circuitBreaker) {
    this.circuitBreaker = circuitBreaker;
    return this;
  }

   /**
   * Get circuitBreaker
   * @return circuitBreaker
  **/
  @ApiModelProperty(value = "")
  public EndPoint_endpointConfig_circuitBreakerDTO getCircuitBreaker() {
    return circuitBreaker;
  }

  public void setCircuitBreaker(EndPoint_endpointConfig_circuitBreakerDTO circuitBreaker) {
    this.circuitBreaker = circuitBreaker;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndPoint_endpointConfigDTO endPointEndpointConfig = (EndPoint_endpointConfigDTO) o;
    return Objects.equals(this.endpointType, endPointEndpointConfig.endpointType) &&
        Objects.equals(this.list, endPointEndpointConfig.list) &&
        Objects.equals(this.circuitBreaker, endPointEndpointConfig.circuitBreaker);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endpointType, list, circuitBreaker);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndPoint_endpointConfigDTO {\n");
    
    sb.append("    endpointType: ").append(toIndentedString(endpointType)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
    sb.append("    circuitBreaker: ").append(toIndentedString(circuitBreaker)).append("\n");
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

