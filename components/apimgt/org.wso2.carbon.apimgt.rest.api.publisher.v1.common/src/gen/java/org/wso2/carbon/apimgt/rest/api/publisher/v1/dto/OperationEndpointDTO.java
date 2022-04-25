package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class OperationEndpointDTO   {
  
    private String id = null;
    private String name = null;
    private Object endpointConfig = null;

  /**
   **/
  public OperationEndpointDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public OperationEndpointDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Endpoint configuration of the API. This can be used to provide different types of endpoints including Simple REST Endpoints, Loadbalanced and Failover.  &#x60;Simple REST Endpoint&#x60;   {     \&quot;endpoint_type\&quot;: \&quot;http\&quot;,     \&quot;sandbox_endpoints\&quot;:       {        \&quot;url\&quot;: \&quot;https://localhost:9443/am/sample/pizzashack/v1/api/\&quot;     },     \&quot;production_endpoints\&quot;:       {        \&quot;url\&quot;: \&quot;https://localhost:9443/am/sample/pizzashack/v1/api/\&quot;     }   }  &#x60;Loadbalanced Endpoint&#x60;    {     \&quot;endpoint_type\&quot;: \&quot;load_balance\&quot;,     \&quot;algoCombo\&quot;: \&quot;org.apache.synapse.endpoints.algorithms.RoundRobin\&quot;,     \&quot;sessionManagement\&quot;: \&quot;\&quot;,     \&quot;sandbox_endpoints\&quot;:       [                 {           \&quot;url\&quot;: \&quot;https://localhost:9443/am/sample/pizzashack/v1/api/1\&quot;        },                 {           \&quot;endpoint_type\&quot;: \&quot;http\&quot;,           \&quot;template_not_supported\&quot;: false,           \&quot;url\&quot;: \&quot;https://localhost:9443/am/sample/pizzashack/v1/api/2\&quot;        }     ],     \&quot;production_endpoints\&quot;:       [                 {           \&quot;url\&quot;: \&quot;https://localhost:9443/am/sample/pizzashack/v1/api/3\&quot;        },                 {           \&quot;endpoint_type\&quot;: \&quot;http\&quot;,           \&quot;template_not_supported\&quot;: false,           \&quot;url\&quot;: \&quot;https://localhost:9443/am/sample/pizzashack/v1/api/4\&quot;        }     ],     \&quot;sessionTimeOut\&quot;: \&quot;\&quot;,     \&quot;algoClassName\&quot;: \&quot;org.apache.synapse.endpoints.algorithms.RoundRobin\&quot;   }  &#x60;Failover Endpoint&#x60;    {     \&quot;production_failovers\&quot;:[        {           \&quot;endpoint_type\&quot;:\&quot;http\&quot;,           \&quot;template_not_supported\&quot;:false,           \&quot;url\&quot;:\&quot;https://localhost:9443/am/sample/pizzashack/v1/api/1\&quot;        }     ],     \&quot;endpoint_type\&quot;:\&quot;failover\&quot;,     \&quot;sandbox_endpoints\&quot;:{        \&quot;url\&quot;:\&quot;https://localhost:9443/am/sample/pizzashack/v1/api/2\&quot;     },     \&quot;production_endpoints\&quot;:{        \&quot;url\&quot;:\&quot;https://localhost:9443/am/sample/pizzashack/v1/api/3\&quot;     },     \&quot;sandbox_failovers\&quot;:[        {           \&quot;endpoint_type\&quot;:\&quot;http\&quot;,           \&quot;template_not_supported\&quot;:false,           \&quot;url\&quot;:\&quot;https://localhost:9443/am/sample/pizzashack/v1/api/4\&quot;        }     ]   }  &#x60;Default Endpoint&#x60;    {     \&quot;endpoint_type\&quot;:\&quot;default\&quot;,     \&quot;sandbox_endpoints\&quot;:{        \&quot;url\&quot;:\&quot;default\&quot;     },     \&quot;production_endpoints\&quot;:{        \&quot;url\&quot;:\&quot;default\&quot;     }   }  &#x60;Endpoint from Endpoint Registry&#x60;   {     \&quot;endpoint_type\&quot;: \&quot;Registry\&quot;,     \&quot;endpoint_id\&quot;: \&quot;{registry-name:entry-name:version}\&quot;,   } 
   **/
  public OperationEndpointDTO endpointConfig(Object endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

  
  @ApiModelProperty(example = "{\"endpoint_type\":\"http\",\"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/\"}", value = "Endpoint configuration of the API. This can be used to provide different types of endpoints including Simple REST Endpoints, Loadbalanced and Failover.  `Simple REST Endpoint`   {     \"endpoint_type\": \"http\",     \"sandbox_endpoints\":       {        \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/\"     },     \"production_endpoints\":       {        \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/\"     }   }  `Loadbalanced Endpoint`    {     \"endpoint_type\": \"load_balance\",     \"algoCombo\": \"org.apache.synapse.endpoints.algorithms.RoundRobin\",     \"sessionManagement\": \"\",     \"sandbox_endpoints\":       [                 {           \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/1\"        },                 {           \"endpoint_type\": \"http\",           \"template_not_supported\": false,           \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/2\"        }     ],     \"production_endpoints\":       [                 {           \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/3\"        },                 {           \"endpoint_type\": \"http\",           \"template_not_supported\": false,           \"url\": \"https://localhost:9443/am/sample/pizzashack/v1/api/4\"        }     ],     \"sessionTimeOut\": \"\",     \"algoClassName\": \"org.apache.synapse.endpoints.algorithms.RoundRobin\"   }  `Failover Endpoint`    {     \"production_failovers\":[        {           \"endpoint_type\":\"http\",           \"template_not_supported\":false,           \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/1\"        }     ],     \"endpoint_type\":\"failover\",     \"sandbox_endpoints\":{        \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/2\"     },     \"production_endpoints\":{        \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/3\"     },     \"sandbox_failovers\":[        {           \"endpoint_type\":\"http\",           \"template_not_supported\":false,           \"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/4\"        }     ]   }  `Default Endpoint`    {     \"endpoint_type\":\"default\",     \"sandbox_endpoints\":{        \"url\":\"default\"     },     \"production_endpoints\":{        \"url\":\"default\"     }   }  `Endpoint from Endpoint Registry`   {     \"endpoint_type\": \"Registry\",     \"endpoint_id\": \"{registry-name:entry-name:version}\",   } ")
      @Valid
  @JsonProperty("endpointConfig")
  public Object getEndpointConfig() {
    return endpointConfig;
  }
  public void setEndpointConfig(Object endpointConfig) {
    this.endpointConfig = endpointConfig;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationEndpointDTO operationEndpoint = (OperationEndpointDTO) o;
    return Objects.equals(id, operationEndpoint.id) &&
        Objects.equals(name, operationEndpoint.name) &&
        Objects.equals(endpointConfig, operationEndpoint.endpointConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, endpointConfig);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationEndpointDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    endpointConfig: ").append(toIndentedString(endpointConfig)).append("\n");
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

