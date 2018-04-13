package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * UriTemplateDTO
 */
public class UriTemplateDTO   {
  @SerializedName("uriTemplate")
  private String uriTemplate = null;

  @SerializedName("httpVerb")
  private String httpVerb = null;

  @SerializedName("authType")
  private String authType = null;

  @SerializedName("policy")
  private String policy = null;

  @SerializedName("scope")
  private String scope = null;

  public UriTemplateDTO uriTemplate(String uriTemplate) {
    this.uriTemplate = uriTemplate;
    return this;
  }

   /**
   * uri template of api. 
   * @return uriTemplate
  **/
  @ApiModelProperty(value = "uri template of api. ")
  public String getUriTemplate() {
    return uriTemplate;
  }

  public void setUriTemplate(String uriTemplate) {
    this.uriTemplate = uriTemplate;
  }

  public UriTemplateDTO httpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
    return this;
  }

   /**
   * http verb of the uri template. 
   * @return httpVerb
  **/
  @ApiModelProperty(value = "http verb of the uri template. ")
  public String getHttpVerb() {
    return httpVerb;
  }

  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }

  public UriTemplateDTO authType(String authType) {
    this.authType = authType;
    return this;
  }

   /**
   * auth type of uri tamplate. 
   * @return authType
  **/
  @ApiModelProperty(value = "auth type of uri tamplate. ")
  public String getAuthType() {
    return authType;
  }

  public void setAuthType(String authType) {
    this.authType = authType;
  }

  public UriTemplateDTO policy(String policy) {
    this.policy = policy;
    return this;
  }

   /**
   * policy of uri template. 
   * @return policy
  **/
  @ApiModelProperty(value = "policy of uri template. ")
  public String getPolicy() {
    return policy;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }

  public UriTemplateDTO scope(String scope) {
    this.scope = scope;
    return this;
  }

   /**
   * scope for uri template. 
   * @return scope
  **/
  @ApiModelProperty(value = "scope for uri template. ")
  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UriTemplateDTO uriTemplate = (UriTemplateDTO) o;
    return Objects.equals(this.uriTemplate, uriTemplate.uriTemplate) &&
        Objects.equals(this.httpVerb, uriTemplate.httpVerb) &&
        Objects.equals(this.authType, uriTemplate.authType) &&
        Objects.equals(this.policy, uriTemplate.policy) &&
        Objects.equals(this.scope, uriTemplate.scope);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uriTemplate, httpVerb, authType, policy, scope);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UriTemplateDTO {\n");
    
    sb.append("    uriTemplate: ").append(toIndentedString(uriTemplate)).append("\n");
    sb.append("    httpVerb: ").append(toIndentedString(httpVerb)).append("\n");
    sb.append("    authType: ").append(toIndentedString(authType)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
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

