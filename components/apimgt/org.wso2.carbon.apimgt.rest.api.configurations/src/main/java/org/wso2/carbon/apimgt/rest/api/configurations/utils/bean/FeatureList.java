/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.configurations.utils.bean;

import org.wso2.carbon.apimgt.rest.api.configurations.models.Feature;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean class for retrieving available feature list
 */
public class FeatureList {

    private List<Feature> list = new ArrayList<>();
    private int count;

    public List<Feature> getList() {
        return list;
    }

    public void setList(List<Feature> list) {
        this.list = list;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}