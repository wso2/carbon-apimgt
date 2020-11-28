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



public class DocumentSearchResultAllOfDTO   {
  

    @XmlType(name="DocTypeEnum")
    @XmlEnum(String.class)
    public enum DocTypeEnum {
        HOWTO("HOWTO"),
        SAMPLES("SAMPLES"),
        PUBLIC_FORUM("PUBLIC_FORUM"),
        SUPPORT_FORUM("SUPPORT_FORUM"),
        API_MESSAGE_FORMAT("API_MESSAGE_FORMAT"),
        SWAGGER_DOC("SWAGGER_DOC"),
        OTHER("OTHER");
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

        @JsonCreator
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
        INLINE("INLINE"),
        URL("URL"),
        FILE("FILE");
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

        @JsonCreator
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
        OWNER_ONLY("OWNER_ONLY"),
        PRIVATE("PRIVATE"),
        API_LEVEL("API_LEVEL");
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

        @JsonCreator
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
    private String apiUUID = null;
    private String associatedType = null;

  /**
   **/
  public DocumentSearchResultAllOfDTO docType(DocTypeEnum docType) {
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
  public DocumentSearchResultAllOfDTO summary(String summary) {
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
  public DocumentSearchResultAllOfDTO sourceType(SourceTypeEnum sourceType) {
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
  public DocumentSearchResultAllOfDTO sourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sourceUrl")
  public String getSourceUrl() {
    return sourceUrl;
  }
  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  /**
   **/
  public DocumentSearchResultAllOfDTO otherTypeName(String otherTypeName) {
    this.otherTypeName = otherTypeName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("otherTypeName")
  public String getOtherTypeName() {
    return otherTypeName;
  }
  public void setOtherTypeName(String otherTypeName) {
    this.otherTypeName = otherTypeName;
  }

  /**
   **/
  public DocumentSearchResultAllOfDTO visibility(VisibilityEnum visibility) {
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
  public DocumentSearchResultAllOfDTO apiName(String apiName) {
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
  public DocumentSearchResultAllOfDTO apiVersion(String apiVersion) {
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
  public DocumentSearchResultAllOfDTO apiProvider(String apiProvider) {
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

  /**
   **/
  public DocumentSearchResultAllOfDTO apiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiUUID")
  public String getApiUUID() {
    return apiUUID;
  }
  public void setApiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
  }

  /**
   **/
  public DocumentSearchResultAllOfDTO associatedType(String associatedType) {
    this.associatedType = associatedType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("associatedType")
  public String getAssociatedType() {
    return associatedType;
  }
  public void setAssociatedType(String associatedType) {
    this.associatedType = associatedType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocumentSearchResultAllOfDTO documentSearchResultAllOf = (DocumentSearchResultAllOfDTO) o;
    return Objects.equals(docType, documentSearchResultAllOf.docType) &&
        Objects.equals(summary, documentSearchResultAllOf.summary) &&
        Objects.equals(sourceType, documentSearchResultAllOf.sourceType) &&
        Objects.equals(sourceUrl, documentSearchResultAllOf.sourceUrl) &&
        Objects.equals(otherTypeName, documentSearchResultAllOf.otherTypeName) &&
        Objects.equals(visibility, documentSearchResultAllOf.visibility) &&
        Objects.equals(apiName, documentSearchResultAllOf.apiName) &&
        Objects.equals(apiVersion, documentSearchResultAllOf.apiVersion) &&
        Objects.equals(apiProvider, documentSearchResultAllOf.apiProvider) &&
        Objects.equals(apiUUID, documentSearchResultAllOf.apiUUID) &&
        Objects.equals(associatedType, documentSearchResultAllOf.associatedType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(docType, summary, sourceType, sourceUrl, otherTypeName, visibility, apiName, apiVersion, apiProvider, apiUUID, associatedType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DocumentSearchResultAllOfDTO {\n");
    
    sb.append("    docType: ").append(toIndentedString(docType)).append("\n");
    sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
    sb.append("    sourceType: ").append(toIndentedString(sourceType)).append("\n");
    sb.append("    sourceUrl: ").append(toIndentedString(sourceUrl)).append("\n");
    sb.append("    otherTypeName: ").append(toIndentedString(otherTypeName)).append("\n");
    sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    apiProvider: ").append(toIndentedString(apiProvider)).append("\n");
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    associatedType: ").append(toIndentedString(associatedType)).append("\n");
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

