package org.wso2.carbon.apimgt.rest.api.store.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIBusinessInformationDTO  {
  
  
  
  private String businessOwnerEmail = null;
  
  
  private String technicalOwnerEmail = null;
  
  
  private String technicalOwner = null;
  
  
  private String businessOwner = null;

  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for APIBusinessInformationDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a APIBusinessInformationDTO
  **/

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("businessOwnerEmail")
  public String getBusinessOwnerEmail() {
    return businessOwnerEmail;
  }
  public void setBusinessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("technicalOwnerEmail")
  public String getTechnicalOwnerEmail() {
    return technicalOwnerEmail;
  }
  public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("technicalOwner")
  public String getTechnicalOwner() {
    return technicalOwner;
  }
  public void setTechnicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("businessOwner")
  public String getBusinessOwner() {
    return businessOwner;
  }
  public void setBusinessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIBusinessInformationDTO {\n");
    
    sb.append("  businessOwnerEmail: ").append(businessOwnerEmail).append("\n");
    sb.append("  technicalOwnerEmail: ").append(technicalOwnerEmail).append("\n");
    sb.append("  technicalOwner: ").append(technicalOwner).append("\n");
    sb.append("  businessOwner: ").append(businessOwner).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
