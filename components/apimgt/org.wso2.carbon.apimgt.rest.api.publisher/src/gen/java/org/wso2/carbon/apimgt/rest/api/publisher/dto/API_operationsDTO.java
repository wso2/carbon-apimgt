package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_endpointDTO;
import java.util.Objects;

/**
 * API_operationsDTO
 */
public class API_operationsDTO   {
  @SerializedName("id")
  private String id = null;

  @SerializedName("uritemplate")
  private String uritemplate = "/_*";

  @SerializedName("httpVerb")
  private String httpVerb = "GET";

  @SerializedName("authType")
  private String authType = "Any";

  @SerializedName("policy")
  private String policy = null;

  @SerializedName("endpoint")
  private List<API_endpointDTO> endpoint = new ArrayList<API_endpointDTO>();

  @SerializedName("scopes")
  private List<String> scopes = new ArrayList<String>();

  public API_operationsDTO id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(example = "postapiresource", value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public API_operationsDTO uritemplate(String uritemplate) {
    this.uritemplate = uritemplate;
    return this;
  }

   /**
   * Get uritemplate
   * @return uritemplate
  **/
  @ApiModelProperty(value = "")
  public String getUritemplate() {
    return uritemplate;
  }

  public void setUritemplate(String uritemplate) {
    this.uritemplate = uritemplate;
  }

  public API_operationsDTO httpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
    return this;
  }

   /**
   * Get httpVerb
   * @return httpVerb
  **/
  @ApiModelProperty(value = "")
  public String getHttpVerb() {
    return httpVerb;
  }

  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }

  public API_operationsDTO authType(String authType) {
    this.authType = authType;
    return this;
  }

   /**
   * Get authType
   * @return authType
  **/
  @ApiModelProperty(value = "")
  public String getAuthType() {
    return authType;
  }

  public void setAuthType(String authType) {
    this.authType = authType;
  }

  public API_operationsDTO policy(String policy) {
    this.policy = policy;
    return this;
  }

   /**
   * Get policy
   * @return policy
  **/
  @ApiModelProperty(example = "Unlimited", value = "")
  public String getPolicy() {
    return policy;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }

  public API_operationsDTO endpoint(List<API_endpointDTO> endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  public API_operationsDTO addEndpointItem(API_endpointDTO endpointItem) {
    this.endpoint.add(endpointItem);
    return this;
  }

   /**
   * Get endpoint
   * @return endpoint
  **/
  @ApiModelProperty(value = "")
  public List<API_endpointDTO> getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(List<API_endpointDTO> endpoint) {
    this.endpoint = endpoint;
  }

  public API_operationsDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  public API_operationsDTO addScopesItem(String scopesItem) {
    this.scopes.add(scopesItem);
    return this;
  }

   /**
   * Get scopes
   * @return scopes
  **/
  @ApiModelProperty(value = "")
  public List<String> getScopes() {
    return scopes;
  }

  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    API_operationsDTO apIOperations = (API_operationsDTO) o;
    return Objects.equals(this.id, apIOperations.id) &&
        Objects.equals(this.uritemplate, apIOperations.uritemplate) &&
        Objects.equals(this.httpVerb, apIOperations.httpVerb) &&
        Objects.equals(this.authType, apIOperations.authType) &&
        Objects.equals(this.policy, apIOperations.policy) &&
        Objects.equals(this.endpoint, apIOperations.endpoint) &&
        Objects.equals(this.scopes, apIOperations.scopes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uritemplate, httpVerb, authType, policy, endpoint, scopes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class API_operationsDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    uritemplate: ").append(toIndentedString(uritemplate)).append("\n");
    sb.append("    httpVerb: ").append(toIndentedString(httpVerb)).append("\n");
    sb.append("    authType: ").append(toIndentedString(authType)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
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

