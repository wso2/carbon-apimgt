/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Scope list of an Application.
 */
public class ScopeListDTO {

    @ApiModelProperty()
    private List<ApplicationScopeDTO> list = new ArrayList<>();

    /**
     * Get list
     *
     * @return list
     **/
    public List<ApplicationScopeDTO> getList() {
        return list;
    }

    public void setList(List<ApplicationScopeDTO> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "class ScopeList {\n" + "    list: " + toIndentedString(list) + "\n" + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
