/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;

import java.util.Objects;

/**
 * SubscriptionThrottlePolicyListDTO
 */
public class SubscriptionThrottlePolicyListDTO {
    @JsonProperty("count") private Integer count = null;

    @JsonProperty("list") private List<SubscriptionThrottlePolicyDTO> list = new ArrayList<SubscriptionThrottlePolicyDTO>();

    public SubscriptionThrottlePolicyListDTO count(Integer count) {
        this.count = count;
        return this;
    }

    /**
     * Number of Subscription throttle policies returned.
     *
     * @return count
     **/
    @ApiModelProperty(example = "1", value = "Number of Subscription throttle policies returned. ")
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public SubscriptionThrottlePolicyListDTO list(List<SubscriptionThrottlePolicyDTO> list) {
        this.list = list;
        return this;
    }

    public SubscriptionThrottlePolicyListDTO addListItem(SubscriptionThrottlePolicyDTO listItem) {
        this.list.add(listItem);
        return this;
    }

    /**
     * Get list
     *
     * @return list
     **/
    @ApiModelProperty(value = "") public List<SubscriptionThrottlePolicyDTO> getList() {
        return list;
    }

    public void setList(List<SubscriptionThrottlePolicyDTO> list) {
        this.list = list;
    }

    @Override public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubscriptionThrottlePolicyListDTO subscriptionThrottlePolicyList = (SubscriptionThrottlePolicyListDTO) o;
        return Objects.equals(this.count, subscriptionThrottlePolicyList.count) && Objects
                .equals(this.list, subscriptionThrottlePolicyList.list);
    }

    @Override public int hashCode() {
        return Objects.hash(count, list);
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SubscriptionThrottlePolicyListDTO {\n");

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

