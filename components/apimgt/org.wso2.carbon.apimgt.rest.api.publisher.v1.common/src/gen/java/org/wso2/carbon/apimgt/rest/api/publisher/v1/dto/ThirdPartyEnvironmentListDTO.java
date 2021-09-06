/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

import javax.validation.Valid;



public class ThirdPartyEnvironmentListDTO   {

    private Integer count = null;
    private List<ThirdPartyEnvironmentDTO> list = new ArrayList<ThirdPartyEnvironmentDTO>();

    /**
     * Number of Third party environments returned.
     **/
    public ThirdPartyEnvironmentListDTO count(Integer count) {
        this.count = count;
        return this;
    }


    @ApiModelProperty(example = "1", value = "Number of Third party environments returned. ")
    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     **/
    public ThirdPartyEnvironmentListDTO list(List<ThirdPartyEnvironmentDTO> list) {
        this.list = list;
        return this;
    }


    @ApiModelProperty(value = "")
    @Valid
    @JsonProperty("list")
    public List<ThirdPartyEnvironmentDTO> getList() {
        return list;
    }
    public void setList(List<ThirdPartyEnvironmentDTO> list) {
        this.list = list;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThirdPartyEnvironmentListDTO thirdPartyEnvironmentList = (ThirdPartyEnvironmentListDTO) o;
        return Objects.equals(count, thirdPartyEnvironmentList.count) &&
                Objects.equals(list, thirdPartyEnvironmentList.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, list);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ThirdPartyEnvironmentListDTO {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    list: ").append(toIndentedString(list)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
