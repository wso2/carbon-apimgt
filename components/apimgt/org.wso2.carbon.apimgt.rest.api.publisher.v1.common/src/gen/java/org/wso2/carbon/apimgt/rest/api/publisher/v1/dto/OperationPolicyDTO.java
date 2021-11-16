package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class OperationPolicyDTO   {
  

    @XmlType(name="PolicyTypeEnum")
    @XmlEnum(String.class)
    public enum PolicyTypeEnum {
        SET_HEADER("SET_HEADER"),
        REMOVE_HEADER("REMOVE_HEADER"),
        REMAP_HEADER("REMAP_HEADER"),
        REWRITE_HTTP_METHOD("REWRITE_HTTP_METHOD"),
        REWRITE_RESOURCE_PATH("REWRITE_RESOURCE_PATH"),
        ADD_QUERY_PARAM("ADD_QUERY_PARAM"),
        REMOVE_QUERY_PARAM("REMOVE_QUERY_PARAM"),
        REMAP_QUERY_PARAM("REMAP_QUERY_PARAM"),
        CALL_INTERCEPTOR_SERVICE("CALL_INTERCEPTOR_SERVICE");
        private String value;

        PolicyTypeEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static PolicyTypeEnum fromValue(String v) {
            for (PolicyTypeEnum b : PolicyTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private PolicyTypeEnum policyType = null;
    private Map<String, Object> parameters = new HashMap<String, Object>();

  /**
   **/
  public OperationPolicyDTO policyType(PolicyTypeEnum policyType) {
    this.policyType = policyType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("policyType")
  @NotNull
  public PolicyTypeEnum getPolicyType() {
    return policyType;
  }
  public void setPolicyType(PolicyTypeEnum policyType) {
    this.policyType = policyType;
  }

  /**
   **/
  public OperationPolicyDTO parameters(Map<String, Object> parameters) {
    this.parameters = parameters;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("parameters")
  public Map<String, Object> getParameters() {
    return parameters;
  }
  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationPolicyDTO operationPolicy = (OperationPolicyDTO) o;
    return Objects.equals(policyType, operationPolicy.policyType) &&
        Objects.equals(parameters, operationPolicy.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyType, parameters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationPolicyDTO {\n");
    
    sb.append("    policyType: ").append(toIndentedString(policyType)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
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

