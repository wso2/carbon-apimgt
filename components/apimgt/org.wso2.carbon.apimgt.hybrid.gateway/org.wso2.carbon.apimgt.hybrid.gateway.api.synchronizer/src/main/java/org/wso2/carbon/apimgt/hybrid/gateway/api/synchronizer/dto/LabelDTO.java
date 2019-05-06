/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

@ApiModel(description = "")
public class LabelDTO  {

    @NotNull
    private String name = null;

    private String description = null;

    private List<String> accessUrls = new ArrayList<String>();

    private String lastUpdatedTime = null;

    private String createdTime = null;

    /**
     * gets and sets the lastUpdatedTime for LabelDTO
     **/
    @JsonIgnore
    public String getLastUpdatedTime(){
        return lastUpdatedTime;
    }
    public void setLastUpdatedTime(String lastUpdatedTime){
        this.lastUpdatedTime=lastUpdatedTime;
    }

    /**
     * gets and sets the createdTime for a LabelDTO
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
    @ApiModelProperty(value = "")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("accessUrls")
    public List<String> getAccessUrls() {
        return accessUrls;
    }
    public void setAccessUrls(List<String> accessUrls) {
        this.accessUrls = accessUrls;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class LabelDTO {\n");
        sb.append("  name: ").append(name).append("\n");
        sb.append("  description:  ").append(description).append("\n");
        sb.append("  accessUrls: ").append(accessUrls).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
