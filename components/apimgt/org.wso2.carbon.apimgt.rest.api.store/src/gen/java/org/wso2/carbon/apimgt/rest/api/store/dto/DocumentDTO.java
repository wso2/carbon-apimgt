package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * DocumentDTO
 */
public class DocumentDTO   {
  @SerializedName("documentId")
  private String documentId = null;

  @SerializedName("name")
  private String name = null;

  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    @SerializedName("HOWTO")
    HOWTO("HOWTO"),
    
    @SerializedName("SAMPLES")
    SAMPLES("SAMPLES"),
    
    @SerializedName("PUBLIC_FORUM")
    PUBLIC_FORUM("PUBLIC_FORUM"),
    
    @SerializedName("SUPPORT_FORUM")
    SUPPORT_FORUM("SUPPORT_FORUM"),
    
    @SerializedName("API_MESSAGE_FORMAT")
    API_MESSAGE_FORMAT("API_MESSAGE_FORMAT"),
    
    @SerializedName("SWAGGER_DOC")
    SWAGGER_DOC("SWAGGER_DOC"),
    
    @SerializedName("OTHER")
    OTHER("OTHER");

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

  @SerializedName("summary")
  private String summary = null;

  /**
   * Gets or Sets sourceType
   */
  public enum SourceTypeEnum {
    @SerializedName("INLINE")
    INLINE("INLINE"),
    
    @SerializedName("URL")
    URL("URL"),
    
    @SerializedName("FILE")
    FILE("FILE");

    private String value;

    SourceTypeEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    public static SourceTypeEnum fromValue(String text) {
      for (SourceTypeEnum b : SourceTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @SerializedName("sourceType")
  private SourceTypeEnum sourceType = null;

  @SerializedName("sourceUrl")
  private String sourceUrl = null;

  @SerializedName("inlineContent")
  private String inlineContent = null;

  @SerializedName("otherTypeName")
  private String otherTypeName = null;

  public DocumentDTO documentId(String documentId) {
    this.documentId = documentId;
    return this;
  }

   /**
   * Get documentId
   * @return documentId
  **/
  @ApiModelProperty(value = "")
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
  @ApiModelProperty(required = true, value = "")
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
  @ApiModelProperty(required = true, value = "")
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
  @ApiModelProperty(value = "")
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
  @ApiModelProperty(required = true, value = "")
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
  @ApiModelProperty(value = "")
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
  @ApiModelProperty(value = "")
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
  @ApiModelProperty(value = "")
  public String getOtherTypeName() {
    return otherTypeName;
  }

  public void setOtherTypeName(String otherTypeName) {
    this.otherTypeName = otherTypeName;
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
        Objects.equals(this.otherTypeName, document.otherTypeName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(documentId, name, type, summary, sourceType, sourceUrl, inlineContent, otherTypeName);
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

