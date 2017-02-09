package org.wso2.carbon.apimgt.rest.api.core.dto;

import java.util.Objects;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * APISummaryDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-09T14:55:54.962+05:30")
public class APISummaryDTO   {
  private String id = null;

  private String name = null;

  private String context = null;

  private String version = null;

  private List<UriTemplateDTO> uriTemplates = new ArrayList<UriTemplateDTO>();

  public APISummaryDTO id(String id) {
    this.id = id;
    return this;
  }

   /**
   * uuid of the api. 
   * @return id
  **/
  @ApiModelProperty(value = "uuid of the api. ")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public APISummaryDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * api name. 
   * @return name
  **/
  @ApiModelProperty(value = "api name. ")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public APISummaryDTO context(String context) {
    this.context = context;
    return this;
  }

   /**
   * api context. 
   * @return context
  **/
  @ApiModelProperty(value = "api context. ")
  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public APISummaryDTO version(String version) {
    this.version = version;
    return this;
  }

   /**
   * api version. 
   * @return version
  **/
  @ApiModelProperty(value = "api version. ")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public APISummaryDTO uriTemplates(List<UriTemplateDTO> uriTemplates) {
    this.uriTemplates = uriTemplates;
    return this;
  }

  public APISummaryDTO addUriTemplatesItem(UriTemplateDTO uriTemplatesItem) {
    this.uriTemplates.add(uriTemplatesItem);
    return this;
  }

   /**
   * List of uriTemplates. 
   * @return uriTemplates
  **/
  @ApiModelProperty(value = "List of uriTemplates. ")
  public List<UriTemplateDTO> getUriTemplates() {
    return uriTemplates;
  }

  public void setUriTemplates(List<UriTemplateDTO> uriTemplates) {
    this.uriTemplates = uriTemplates;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APISummaryDTO aPISummary = (APISummaryDTO) o;
    return Objects.equals(this.id, aPISummary.id) &&
        Objects.equals(this.name, aPISummary.name) &&
        Objects.equals(this.context, aPISummary.context) &&
        Objects.equals(this.version, aPISummary.version) &&
        Objects.equals(this.uriTemplates, aPISummary.uriTemplates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, context, version, uriTemplates);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APISummaryDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    uriTemplates: ").append(toIndentedString(uriTemplates)).append("\n");
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

