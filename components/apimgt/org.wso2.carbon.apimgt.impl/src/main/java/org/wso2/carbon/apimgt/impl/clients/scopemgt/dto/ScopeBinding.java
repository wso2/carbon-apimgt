/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.clients.scopemgt.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of ScopeBinding DTO
 */
public class ScopeBinding {

    private String bindingType;

    public String getBindingType() {

        return bindingType;
    }

    public void setBindingType(String bindingType) {

        this.bindingType = bindingType;
    }

    public List<String> getBinding() {

        return binding;
    }

    public void setBinding(List<String> binding) {

        this.binding = binding;
    }

    private List<String> binding = new ArrayList<String>();

}

