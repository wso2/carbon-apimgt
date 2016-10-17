package org.wso2.carbon.apimgt.model;




@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class APIBusinessInformation  {
  
  private String technicalOwnerEmail = null;
  private String businessOwnerEmail = null;
  private String businessOwner = null;
  private String technicalOwner = null;

  /**
   **/
  public String getTechnicalOwnerEmail() {
    return technicalOwnerEmail;
  }
  public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
  }

  /**
   **/
  public String getBusinessOwnerEmail() {
    return businessOwnerEmail;
  }
  public void setBusinessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
  }

  /**
   **/
  public String getBusinessOwner() {
    return businessOwner;
  }
  public void setBusinessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
  }

  /**
   **/
  public String getTechnicalOwner() {
    return technicalOwner;
  }
  public void setTechnicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIBusinessInformation {\n");
    
    sb.append("  technicalOwnerEmail: ").append(technicalOwnerEmail).append("\n");
    sb.append("  businessOwnerEmail: ").append(businessOwnerEmail).append("\n");
    sb.append("  businessOwner: ").append(businessOwner).append("\n");
    sb.append("  technicalOwner: ").append(technicalOwner).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
