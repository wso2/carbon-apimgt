package org.wso2.carbon.apimgt.model;




@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class Application  {
  
  private String applicationId = null;
  private String name = null;
  private String subscriber = null;
  private String throttlingTier = null;
  private String description = null;
  private String groupId = null;

  /**
   **/
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
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
  public String getSubscriber() {
    return subscriber;
  }
  public void setSubscriber(String subscriber) {
    this.subscriber = subscriber;
  }

  /**
   **/
  public String getThrottlingTier() {
    return throttlingTier;
  }
  public void setThrottlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
  }

  /**
   **/
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Application {\n");
    
    sb.append("  applicationId: ").append(applicationId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  subscriber: ").append(subscriber).append("\n");
    sb.append("  throttlingTier: ").append(throttlingTier).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  groupId: ").append(groupId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
