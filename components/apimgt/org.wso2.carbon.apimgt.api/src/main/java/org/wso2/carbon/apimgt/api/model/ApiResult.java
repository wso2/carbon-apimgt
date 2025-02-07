/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.api.model;


import java.util.Objects;

/**
 * This class represents the API result object.
 */
public class ApiResult {

  private String provider = null;
  private String name = null;
  private String version = null;
  private String id = null;
  private String type = null;

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getProvider() {
      return provider;
  }

  public void setProvider(String provider) {
      this.provider = provider;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (this == obj) return true;
    ApiResult other = (ApiResult) obj;
    if (!Objects.equals(this.id, other.id)) return false;
    if (!Objects.equals(this.name, other.name)) return false;
    if (!Objects.equals(this.version, other.version)) return false;
    if (!Objects.equals(this.provider, other.provider)) return false;
    if (!Objects.equals(this.type, other.type)) return false;
    return true;
  }

  public int hashCode() {
    return Objects.hash(super.hashCode(), id, name, version, provider, type);
  }
}

