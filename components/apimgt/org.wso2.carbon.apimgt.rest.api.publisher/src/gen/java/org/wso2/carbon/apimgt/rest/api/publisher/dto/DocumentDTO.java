package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

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
  
  private SourceTypeEnum sourceType = null;
  
  
  private String sourceUrl = null;
  
  
  private String filePath = null;
  
  
  private String otherTypeName = null;
  
  public enum VisibilityEnum {
     OWNER_ONLY,  PRIVATE,  API_LEVEL, 
  };
  
  private VisibilityEnum visibility = null;

  
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
  @ApiModelProperty(value = "")
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
  @JsonProperty("filePath")
  public String getFilePath() {
    return filePath;
  }
  public void setFilePath(String filePath) {
    this.filePath = filePath;
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

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("visibility")
  public VisibilityEnum getVisibility() {
    return visibility;
  }
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
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
    sb.append("  filePath: ").append(filePath).append("\n");
    sb.append("  otherTypeName: ").append(otherTypeName).append("\n");
    sb.append("  visibility: ").append(visibility).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
