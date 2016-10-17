package org.wso2.carbon.apimgt.model;

import org.wso2.carbon.apimgt.model.EnvironmentEndpoints;



@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class Environment  {
  
  private String name = null;
  private String type = null;
  private String serverUrl = null;
  private Boolean showInApiConsole = null;
  private EnvironmentEndpoints endpoints = null;

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
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public String getServerUrl() {
    return serverUrl;
  }
  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  /**
   **/
  public Boolean getShowInApiConsole() {
    return showInApiConsole;
  }
  public void setShowInApiConsole(Boolean showInApiConsole) {
    this.showInApiConsole = showInApiConsole;
  }

  /**
   **/
  public EnvironmentEndpoints getEndpoints() {
    return endpoints;
  }
  public void setEndpoints(EnvironmentEndpoints endpoints) {
    this.endpoints = endpoints;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Environment {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  serverUrl: ").append(serverUrl).append("\n");
    sb.append("  showInApiConsole: ").append(showInApiConsole).append("\n");
    sb.append("  endpoints: ").append(endpoints).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
