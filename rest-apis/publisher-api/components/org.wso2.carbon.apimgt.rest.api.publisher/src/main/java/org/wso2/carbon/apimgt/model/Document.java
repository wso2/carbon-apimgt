package org.wso2.carbon.apimgt.model;




@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class Document  {
  
  private String documentId = null;
  private String name = null;
  public enum TypeEnum {
     HOWTO,  SAMPLES,  PUBLIC_FORUM,  SUPPORT_FORUM,  API_MESSAGE_FORMAT,  SWAGGER_DOC,  OTHER, 
  };
  private TypeEnum type = null;
  private String summary = null;
  public enum SourceTypeEnum {
     INLINE,  URL,  FILE, 
  };
  private SourceTypeEnum sourceType = null;
  private String sourceUrl = null;
  private String otherTypeName = null;
  public enum VisibilityEnum {
     OWNER_ONLY,  PRIVATE,  API_LEVEL, 
  };
  private VisibilityEnum visibility = null;

  /**
   **/
  public String getDocumentId() {
    return documentId;
  }
  public void setDocumentId(String documentId) {
    this.documentId = documentId;
  }

  /**
   **/
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   **/
  public String getSummary() {
    return summary;
  }
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   **/
  public SourceTypeEnum getSourceType() {
    return sourceType;
  }
  public void setSourceType(SourceTypeEnum sourceType) {
    this.sourceType = sourceType;
  }

  /**
   **/
  public String getSourceUrl() {
    return sourceUrl;
  }
  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  /**
   **/
  public String getOtherTypeName() {
    return otherTypeName;
  }
  public void setOtherTypeName(String otherTypeName) {
    this.otherTypeName = otherTypeName;
  }

  /**
   **/
  public VisibilityEnum getVisibility() {
    return visibility;
  }
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Document {\n");
    
    sb.append("  documentId: ").append(documentId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  summary: ").append(summary).append("\n");
    sb.append("  sourceType: ").append(sourceType).append("\n");
    sb.append("  sourceUrl: ").append(sourceUrl).append("\n");
    sb.append("  otherTypeName: ").append(otherTypeName).append("\n");
    sb.append("  visibility: ").append(visibility).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
