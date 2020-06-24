/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.gateway.dto;


import java.util.ArrayList;
import java.util.List;


public class BlockConditionsDTO {
  
  
  
  private List<String> api = new ArrayList<String>();
  
  
  private List<String> application = new ArrayList<String>();
  
  
  private List<IPRange> ip = new ArrayList<>();

  private List<String> subscription = new ArrayList<String>();
  
  private List<String> user = new ArrayList<String>();
  
  
  private List<String> custom = new ArrayList<String>();

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

  public List<IPRange> getIp() {
    return ip;
  }

  public void setIp(List<IPRange> ip) {
    this.ip = ip;
  }

  public List<String> getUser() {
    return user;
  }

  public void setUser(List<String> user) {
    this.user = user;
  }

  public List<String> getCustom() {
    return custom;
  }

  public void setCustom(List<String> custom) {
    this.custom = custom;
  }

  public void setSubscription(List<String> subscription) {
    this.subscription = subscription;
  }

  public List<String> getSubscription() {
    return subscription;
  }
}
