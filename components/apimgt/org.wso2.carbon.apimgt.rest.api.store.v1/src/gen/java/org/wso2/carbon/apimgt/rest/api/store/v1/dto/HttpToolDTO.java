package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

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



public class HttpToolDTO   {
  
    private String name = null;
    private String description = null;

    @XmlType(name="MethodEnum")
    @XmlEnum(String.class)
    public enum MethodEnum {
        OPTIONS("OPTIONS"),
        HEAD("HEAD"),
        PATCH("PATCH"),
        PUT("PUT"),
        DELETE("DELETE"),
        POST("POST"),
        GET("GET");
        private String value;

        MethodEnum (String v) {
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
        public static MethodEnum fromValue(String v) {
            for (MethodEnum b : MethodEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private MethodEnum method = null;
    private String path = null;
    private Object queryParameters = null;
    private Object pathParameters = null;
    private Object requestBody = null;

  /**
   * Name of the Http resource tool
   **/
  public HttpToolDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Name of the Http resource tool")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Description of the Http resource tool used by the LLM
   **/
  public HttpToolDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Description of the Http resource tool used by the LLM")
  @JsonProperty("description")
  @NotNull
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Http method type (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)
   **/
  public HttpToolDTO method(MethodEnum method) {
    this.method = method;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Http method type (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)")
  @JsonProperty("method")
  @NotNull
  public MethodEnum getMethod() {
    return method;
  }
  public void setMethod(MethodEnum method) {
    this.method = method;
  }

  /**
   * Path of the Http resource
   **/
  public HttpToolDTO path(String path) {
    this.path = path;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Path of the Http resource")
  @JsonProperty("path")
  @NotNull
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }

  /**
   **/
  public HttpToolDTO queryParameters(Object queryParameters) {
    this.queryParameters = queryParameters;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("queryParameters")
  public Object getQueryParameters() {
    return queryParameters;
  }
  public void setQueryParameters(Object queryParameters) {
    this.queryParameters = queryParameters;
  }

  /**
   **/
  public HttpToolDTO pathParameters(Object pathParameters) {
    this.pathParameters = pathParameters;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("pathParameters")
  public Object getPathParameters() {
    return pathParameters;
  }
  public void setPathParameters(Object pathParameters) {
    this.pathParameters = pathParameters;
  }

  /**
   **/
  public HttpToolDTO requestBody(Object requestBody) {
    this.requestBody = requestBody;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("requestBody")
  public Object getRequestBody() {
    return requestBody;
  }
  public void setRequestBody(Object requestBody) {
    this.requestBody = requestBody;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HttpToolDTO httpTool = (HttpToolDTO) o;
    return Objects.equals(name, httpTool.name) &&
        Objects.equals(description, httpTool.description) &&
        Objects.equals(method, httpTool.method) &&
        Objects.equals(path, httpTool.path) &&
        Objects.equals(queryParameters, httpTool.queryParameters) &&
        Objects.equals(pathParameters, httpTool.pathParameters) &&
        Objects.equals(requestBody, httpTool.requestBody);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, method, path, queryParameters, pathParameters, requestBody);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HttpToolDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    method: ").append(toIndentedString(method)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    queryParameters: ").append(toIndentedString(queryParameters)).append("\n");
    sb.append("    pathParameters: ").append(toIndentedString(pathParameters)).append("\n");
    sb.append("    requestBody: ").append(toIndentedString(requestBody)).append("\n");
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

