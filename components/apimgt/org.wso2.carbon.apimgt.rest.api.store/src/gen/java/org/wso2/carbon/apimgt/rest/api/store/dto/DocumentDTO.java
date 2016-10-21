package org.wso2.carbon.apimgt.rest.api.store.dto;


import io.swagger.annotations.*;
import javax.ws.rs.*;
import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class DocumentDTO  {
  
  
  
  private String documentId = null;
  
  @NotNull
  private String name = null;
  
  public enum TypeEnum {
     HOWTO,  SAMPLES,  PUBLIC_FORUM,  SUPPORT_FORUM,  API_MESSAGE_FORMAT,  SWAGGER_DOC,  OTHER, 
  };
  @NotNull
  private TypeEnum type = null;
  
  
  private String summary = null;
  
  public enum SourceTypeEnum {
     INLINE,  URL,  FILE, 
  };
  @NotNull
  private SourceTypeEnum sourceType = null;
  
  
  private String sourceUrl = null;
  
  
  private String otherTypeName = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("documentId")
  public String getDocumentId() {
    return documentId;
  }
  public void setDocumentId(String documentId) {
    this.documentId = documentId;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("summary")
  public String getSummary() {
    return summary;
  }
  public void setSummary(String summary) {
    this.summary = summary;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("sourceType")
  public SourceTypeEnum getSourceType() {
    return sourceType;
  }
  public void setSourceType(SourceTypeEnum sourceType) {
    this.sourceType = sourceType;
  }

  
  /**
   **/
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
  @ApiModelProperty(value = "")
  @JsonProperty("otherTypeName")
  public String getOtherTypeName() {
    return otherTypeName;
  }
  public void setOtherTypeName(String otherTypeName) {
    this.otherTypeName = otherTypeName;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class DocumentDTO {\n");
    
    sb.append("  documentId: ").append(documentId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  summary: ").append(summary).append("\n");
    sb.append("  sourceType: ").append(sourceType).append("\n");
    sb.append("  sourceUrl: ").append(sourceUrl).append("\n");
    sb.append("  otherTypeName: ").append(otherTypeName).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
