package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class DocumentSearchResultDTO   {
  
    private String id = null;
    private String name = null;

@XmlType(name="TypeEnum")
@XmlEnum(String.class)
public enum TypeEnum {

    @XmlEnumValue("DOC") DOC(String.valueOf("DOC")), @XmlEnumValue("API") API(String.valueOf("API"));


    private String value;

    TypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TypeEnum fromValue(String v) {
        for (TypeEnum b : TypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private TypeEnum type = null;

@XmlType(name="DocTypeEnum")
@XmlEnum(String.class)
public enum DocTypeEnum {

    @XmlEnumValue("HOWTO") HOWTO(String.valueOf("HOWTO")), @XmlEnumValue("SAMPLES") SAMPLES(String.valueOf("SAMPLES")), @XmlEnumValue("PUBLIC_FORUM") PUBLIC_FORUM(String.valueOf("PUBLIC_FORUM")), @XmlEnumValue("SUPPORT_FORUM") SUPPORT_FORUM(String.valueOf("SUPPORT_FORUM")), @XmlEnumValue("API_MESSAGE_FORMAT") API_MESSAGE_FORMAT(String.valueOf("API_MESSAGE_FORMAT")), @XmlEnumValue("SWAGGER_DOC") SWAGGER_DOC(String.valueOf("SWAGGER_DOC")), @XmlEnumValue("OTHER") OTHER(String.valueOf("OTHER"));


    private String value;

    DocTypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static DocTypeEnum fromValue(String v) {
        for (DocTypeEnum b : DocTypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private DocTypeEnum docType = null;
    private String summary = null;

@XmlType(name="SourceTypeEnum")
@XmlEnum(String.class)
public enum SourceTypeEnum {

    @XmlEnumValue("INLINE") INLINE(String.valueOf("INLINE")), @XmlEnumValue("URL") URL(String.valueOf("URL")), @XmlEnumValue("FILE") FILE(String.valueOf("FILE"));


    private String value;

    SourceTypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SourceTypeEnum fromValue(String v) {
        for (SourceTypeEnum b : SourceTypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private SourceTypeEnum sourceType = null;
    private String sourceUrl = null;
    private String otherTypeName = null;

@XmlType(name="VisibilityEnum")
@XmlEnum(String.class)
public enum VisibilityEnum {

    @XmlEnumValue("OWNER_ONLY") OWNER_ONLY(String.valueOf("OWNER_ONLY")), @XmlEnumValue("PRIVATE") PRIVATE(String.valueOf("PRIVATE")), @XmlEnumValue("API_LEVEL") API_LEVEL(String.valueOf("API_LEVEL"));


    private String value;

    VisibilityEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static VisibilityEnum fromValue(String v) {
        for (VisibilityEnum b : VisibilityEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private VisibilityEnum visibility = null;
    private String apiName = null;
    private String apiVersion = null;
    private String apiProvider = null;

  /**
   **/
  public DocumentSearchResultDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public DocumentSearchResultDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "TestAPI", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public DocumentSearchResultDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "API", value = "")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   **/
  public DocumentSearchResultDTO docType(DocTypeEnum docType) {
    this.docType = docType;
    return this;
  }

  
  @ApiModelProperty(example = "HOWTO", value = "")
  @JsonProperty("docType")
  public DocTypeEnum getDocType() {
    return docType;
  }
  public void setDocType(DocTypeEnum docType) {
    this.docType = docType;
  }

  /**
   **/
  public DocumentSearchResultDTO summary(String summary) {
    this.summary = summary;
    return this;
  }

  
  @ApiModelProperty(example = "Summary of Calculator Documentation", value = "")
  @JsonProperty("summary")
  public String getSummary() {
    return summary;
  }
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   **/
  public DocumentSearchResultDTO sourceType(SourceTypeEnum sourceType) {
    this.sourceType = sourceType;
    return this;
  }

  
  @ApiModelProperty(example = "INLINE", value = "")
  @JsonProperty("sourceType")
  public SourceTypeEnum getSourceType() {
    return sourceType;
  }
  public void setSourceType(SourceTypeEnum sourceType) {
    this.sourceType = sourceType;
  }

  /**
   **/
  public DocumentSearchResultDTO sourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("sourceUrl")
  public String getSourceUrl() {
    return sourceUrl;
  }
  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  /**
   **/
  public DocumentSearchResultDTO otherTypeName(String otherTypeName) {
    this.otherTypeName = otherTypeName;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "")
  @JsonProperty("otherTypeName")
  public String getOtherTypeName() {
    return otherTypeName;
  }
  public void setOtherTypeName(String otherTypeName) {
    this.otherTypeName = otherTypeName;
  }

  /**
   **/
  public DocumentSearchResultDTO visibility(VisibilityEnum visibility) {
    this.visibility = visibility;
    return this;
  }

  
  @ApiModelProperty(example = "API_LEVEL", value = "")
  @JsonProperty("visibility")
  public VisibilityEnum getVisibility() {
    return visibility;
  }
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }

  /**
   * The name of the associated API
   **/
  public DocumentSearchResultDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(example = "TestAPI", value = "The name of the associated API")
  @JsonProperty("apiName")
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  /**
   * The version of the associated API
   **/
  public DocumentSearchResultDTO apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "The version of the associated API")
  @JsonProperty("apiVersion")
  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  /**
   **/
  public DocumentSearchResultDTO apiProvider(String apiProvider) {
    this.apiProvider = apiProvider;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "")
  @JsonProperty("apiProvider")
  public String getApiProvider() {
    return apiProvider;
  }
  public void setApiProvider(String apiProvider) {
    this.apiProvider = apiProvider;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocumentSearchResultDTO documentSearchResult = (DocumentSearchResultDTO) o;
    return Objects.equals(id, documentSearchResult.id) &&
        Objects.equals(name, documentSearchResult.name) &&
        Objects.equals(type, documentSearchResult.type) &&
        Objects.equals(docType, documentSearchResult.docType) &&
        Objects.equals(summary, documentSearchResult.summary) &&
        Objects.equals(sourceType, documentSearchResult.sourceType) &&
        Objects.equals(sourceUrl, documentSearchResult.sourceUrl) &&
        Objects.equals(otherTypeName, documentSearchResult.otherTypeName) &&
        Objects.equals(visibility, documentSearchResult.visibility) &&
        Objects.equals(apiName, documentSearchResult.apiName) &&
        Objects.equals(apiVersion, documentSearchResult.apiVersion) &&
        Objects.equals(apiProvider, documentSearchResult.apiProvider);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, docType, summary, sourceType, sourceUrl, otherTypeName, visibility, apiName, apiVersion, apiProvider);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DocumentSearchResultDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    docType: ").append(toIndentedString(docType)).append("\n");
    sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
    sb.append("    sourceType: ").append(toIndentedString(sourceType)).append("\n");
    sb.append("    sourceUrl: ").append(toIndentedString(sourceUrl)).append("\n");
    sb.append("    otherTypeName: ").append(toIndentedString(otherTypeName)).append("\n");
    sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    apiProvider: ").append(toIndentedString(apiProvider)).append("\n");
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

