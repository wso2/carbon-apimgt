package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.SearchResultDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class DocumentSearchResultDTO extends SearchResultDTO {
  
  
  public enum DocTypeEnum {
     HOWTO,  SAMPLES,  PUBLIC_FORUM,  SUPPORT_FORUM,  API_MESSAGE_FORMAT,  SWAGGER_DOC,  OTHER, 
  };
  
  private DocTypeEnum docType = null;
  
  
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
  
  
  private String apiName = null;
  
  
  private String apiVersion = null;
  
  
  private String apiProvider = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("docType")
  public DocTypeEnum getDocType() {
    return docType;
  }
  public void setDocType(DocTypeEnum docType) {
    this.docType = docType;
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

  
  /**
   * The name of the associated API
   **/
  @ApiModelProperty(value = "The name of the associated API")
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
  @ApiModelProperty(value = "The version of the associated API")
  @JsonProperty("apiVersion")
  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("apiProvider")
  public String getApiProvider() {
    return apiProvider;
  }
  public void setApiProvider(String apiProvider) {
    this.apiProvider = apiProvider;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class DocumentSearchResultDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  docType: ").append(docType).append("\n");
    sb.append("  summary: ").append(summary).append("\n");
    sb.append("  sourceType: ").append(sourceType).append("\n");
    sb.append("  sourceUrl: ").append(sourceUrl).append("\n");
    sb.append("  otherTypeName: ").append(otherTypeName).append("\n");
    sb.append("  visibility: ").append(visibility).append("\n");
    sb.append("  apiName: ").append(apiName).append("\n");
    sb.append("  apiVersion: ").append(apiVersion).append("\n");
    sb.append("  apiProvider: ").append(apiProvider).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
