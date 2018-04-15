package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * BaseAPIDTO
 */
public class BaseAPIDTO   {
  @SerializedName("id")
  private String id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("context")
  private String context = null;

  @SerializedName("version")
  private String version = null;

  @SerializedName("provider")
  private String provider = null;

  @SerializedName("apiDefinition")
  private String apiDefinition = null;

  @SerializedName("transport")
  private List<String> transport = new ArrayList<String>();

  @SerializedName("labels")
  private List<String> labels = new ArrayList<String>();

  @SerializedName("hasOwnGateway")
  private Boolean hasOwnGateway = null;

  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    @SerializedName("API")
    API("API"),
    
    @SerializedName("CompositeAPI")
    COMPOSITEAPI("CompositeAPI");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @SerializedName("type")
  private TypeEnum type = null;

  public BaseAPIDTO id(String id) {
    this.id = id;
    return this;
  }

   /**
   * UUID of the api registry artifact 
   * @return id
  **/
  @ApiModelProperty(value = "UUID of the api registry artifact ")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public BaseAPIDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BaseAPIDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BaseAPIDTO context(String context) {
    this.context = context;
    return this;
  }

   /**
   * Get context
   * @return context
  **/
  @ApiModelProperty(required = true, value = "")
  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public BaseAPIDTO version(String version) {
    this.version = version;
    return this;
  }

   /**
   * Get version
   * @return version
  **/
  @ApiModelProperty(required = true, value = "")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public BaseAPIDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

   /**
   * If the provider value is not given user invoking the api will be used as the provider. 
   * @return provider
  **/
  @ApiModelProperty(required = true, value = "If the provider value is not given user invoking the api will be used as the provider. ")
  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public BaseAPIDTO apiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
    return this;
  }

   /**
   * Swagger definition of the API which contains details about URI templates and scopes 
   * @return apiDefinition
  **/
  @ApiModelProperty(value = "Swagger definition of the API which contains details about URI templates and scopes ")
  public String getApiDefinition() {
    return apiDefinition;
  }

  public void setApiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
  }

  public BaseAPIDTO transport(List<String> transport) {
    this.transport = transport;
    return this;
  }

  public BaseAPIDTO addTransportItem(String transportItem) {
    this.transport.add(transportItem);
    return this;
  }

   /**
   * Get transport
   * @return transport
  **/
  @ApiModelProperty(value = "")
  public List<String> getTransport() {
    return transport;
  }

  public void setTransport(List<String> transport) {
    this.transport = transport;
  }

  public BaseAPIDTO labels(List<String> labels) {
    this.labels = labels;
    return this;
  }

  public BaseAPIDTO addLabelsItem(String labelsItem) {
    this.labels.add(labelsItem);
    return this;
  }

   /**
   * Get labels
   * @return labels
  **/
  @ApiModelProperty(value = "")
  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  public BaseAPIDTO hasOwnGateway(Boolean hasOwnGateway) {
    this.hasOwnGateway = hasOwnGateway;
    return this;
  }

   /**
   * Get hasOwnGateway
   * @return hasOwnGateway
  **/
  @ApiModelProperty(value = "")
  public Boolean getHasOwnGateway() {
    return hasOwnGateway;
  }

  public void setHasOwnGateway(Boolean hasOwnGateway) {
    this.hasOwnGateway = hasOwnGateway;
  }

  public BaseAPIDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(required = true, value = "")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BaseAPIDTO baseAPI = (BaseAPIDTO) o;
    return Objects.equals(this.id, baseAPI.id) &&
        Objects.equals(this.name, baseAPI.name) &&
        Objects.equals(this.description, baseAPI.description) &&
        Objects.equals(this.context, baseAPI.context) &&
        Objects.equals(this.version, baseAPI.version) &&
        Objects.equals(this.provider, baseAPI.provider) &&
        Objects.equals(this.apiDefinition, baseAPI.apiDefinition) &&
        Objects.equals(this.transport, baseAPI.transport) &&
        Objects.equals(this.labels, baseAPI.labels) &&
        Objects.equals(this.hasOwnGateway, baseAPI.hasOwnGateway) &&
        Objects.equals(this.type, baseAPI.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, context, version, provider, apiDefinition, transport, labels, hasOwnGateway, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BaseAPIDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    apiDefinition: ").append(toIndentedString(apiDefinition)).append("\n");
    sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
    sb.append("    hasOwnGateway: ").append(toIndentedString(hasOwnGateway)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

