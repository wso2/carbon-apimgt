package org.wso2.carbon.apimgt.gateway.dto;

import java.util.ArrayList;
import java.util.List;


public class BlockConditionsDTO {
  
  
  
  private List<String> api = new ArrayList<String>();
  
  
  private List<String> application = new ArrayList<String>();
  
  
  private List<String> ip = new ArrayList<String>();

  public List<String> getApi() {
    return api;
  }

  public void setApi(List<String> api) {
    this.api = api;
  }

  public List<String> getApplication() {
    return application;
  }

  public void setApplication(List<String> application) {
    this.application = application;
  }

  public List<String> getIp() {
    return ip;
  }

  public void setIp(List<String> ip) {
    this.ip = ip;
  }
}
