package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DocumentDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-22T13:03:03.175+05:30")
public class DocumentDTO   {
  @JsonProperty("documentId")
  private String documentId = null;

  @JsonProperty("name")
  private String name = null;

  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    HOWTO("HOWTO"),
    
    SAMPLES("SAMPLES"),
    
    PUBLIC_FORUM("PUBLIC_FORUM"),
    
    SUPPORT_FORUM("SUPPORT_FORUM"),
    
    API_MESSAGE_FORMAT("API_MESSAGE_FORMAT"),
    
    SWAGGER_DOC("SWAGGER_DOC"),
    
    OTHER("OTHER");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("type")
  private TypeEnum type = null;

  @JsonProperty("summary")
  private String summary = null;

  /**
   * Gets or Sets sourceType
   */
  public enum SourceTypeEnum {
    INLINE("INLINE"),
    
    URL("URL"),
    
    FILE("FILE");

    private String value;

    SourceTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SourceTypeEnum fromValue(String text) {
      for (SourceTypeEnum b : SourceTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("sourceType")
  private SourceTypeEnum sourceType = null;

  @JsonProperty("sourceUrl")
  private String sourceUrl = null;

  @JsonProperty("inlineContent")
  private String inlineContent = null;

  @JsonProperty("otherTypeName")
  private String otherTypeName = null;

  @JsonProperty("permission")
  private String permission = null;

  /**
   * Gets or Sets visibility
   */
  public enum VisibilityEnum {
    OWNER_ONLY("OWNER_ONLY"),
    
    PRIVATE("PRIVATE"),
    
    API_LEVEL("API_LEVEL");

    private String value;

    VisibilityEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static VisibilityEnum fromValue(String text) {
      for (VisibilityEnum b : VisibilityEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("visibility")
  private VisibilityEnum visibility = null;

  public DocumentDTO documentId(String documentId) {
    this.documentId = documentId;
    return this;
  }

   /**
   * Get documentId
   * @return documentId
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  public String getDocumentId() {
    return documentId;
  }

  public void setDocumentId(String documentId) {
    this.documentId = documentId;
  }

  public DocumentDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "CalculatorDoc", required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DocumentDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(example = "HOWTO", required = true, value = "")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public DocumentDTO summary(String summary) {
    this.summary = summary;
    return this;
  }

   /**
   * Get summary
   * @return summary
  **/
  @ApiModelProperty(example = "Summary of Calculator Documentation", value = "")
  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public DocumentDTO sourceType(SourceTypeEnum sourceType) {
    this.sourceType = sourceType;
    return this;
  }

   /**
   * Get sourceType
   * @return sourceType
  **/
  @ApiModelProperty(example = "INLINE", required = true, value = "")
  public SourceTypeEnum getSourceType() {
    return sourceType;
  }

  public void setSourceType(SourceTypeEnum sourceType) {
    this.sourceType = sourceType;
  }

  public DocumentDTO sourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
    return this;
  }

   /**
   * Get sourceUrl
   * @return sourceUrl
  **/
  @ApiModelProperty(example = "", value = "")
  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public DocumentDTO inlineContent(String inlineContent) {
    this.inlineContent = inlineContent;
    return this;
  }

   /**
   * Get inlineContent
   * @return inlineContent
  **/
  @ApiModelProperty(example = "This is doc content. This can have many lines.", value = "")
  public String getInlineContent() {
    return inlineContent;
  }

  public void setInlineContent(String inlineContent) {
    this.inlineContent = inlineContent;
  }

  public DocumentDTO otherTypeName(String otherTypeName) {
    this.otherTypeName = otherTypeName;
    return this;
  }

   /**
   * Get otherTypeName
   * @return otherTypeName
  **/
  @ApiModelProperty(example = "", value = "")
  public String getOtherTypeName() {
    return otherTypeName;
  }

  public void setOtherTypeName(String otherTypeName) {
    this.otherTypeName = otherTypeName;
  }

  public DocumentDTO permission(String permission) {
    this.permission = permission;
    return this;
  }

   /**
   * Get permission
   * @return permission
  **/
  @ApiModelProperty(example = "[{&quot;groupId&quot; : 1000, &quot;permission&quot; : [&quot;READ&quot;,&quot;UPDATE&quot;]},{&quot;groupId&quot; : 1001, &quot;permission&quot; : [&quot;READ&quot;,&quot;UPDATE&quot;]}]", value = "")
  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public DocumentDTO visibility(VisibilityEnum visibility) {
    this.visibility = visibility;
    return this;
  }

   /**
   * Get visibility
   * @return visibility
  **/
  @ApiModelProperty(example = "API_LEVEL", required = true, value = "")
  public VisibilityEnum getVisibility() {
    return visibility;
  }

  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocumentDTO document = (DocumentDTO) o;
    return Objects.equals(this.documentId, document.documentId) &&
        Objects.equals(this.name, document.name) &&
        Objects.equals(this.type, document.type) &&
        Objects.equals(this.summary, document.summary) &&
        Objects.equals(this.sourceType, document.sourceType) &&
        Objects.equals(this.sourceUrl, document.sourceUrl) &&
        Objects.equals(this.inlineContent, document.inlineContent) &&
        Objects.equals(this.otherTypeName, document.otherTypeName) &&
        Objects.equals(this.permission, document.permission) &&
        Objects.equals(this.visibility, document.visibility);
  }

  @Override
  public int hashCode() {
    return Objects.hash(documentId, name, type, summary, sourceType, sourceUrl, inlineContent, otherTypeName, permission, visibility);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DocumentDTO {\n");
    
    sb.append("    documentId: ").append(toIndentedString(documentId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
    sb.append("    sourceType: ").append(toIndentedString(sourceType)).append("\n");
    sb.append("    sourceUrl: ").append(toIndentedString(sourceUrl)).append("\n");
    sb.append("    inlineContent: ").append(toIndentedString(inlineContent)).append("\n");
    sb.append("    otherTypeName: ").append(toIndentedString(otherTypeName)).append("\n");
    sb.append("    permission: ").append(toIndentedString(permission)).append("\n");
    sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
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

