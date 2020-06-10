/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.keymgt.model.entity;

import org.wso2.carbon.apimgt.api.model.subscription.CacheableEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity for representing a REST resource (in the API)
 */
public class Resource implements CacheableEntity<String> {

    private String urlPattern;

    private int apiId;

    private Map<String, Verb> httpVerbs;

    public Resource() {

        httpVerbs = new HashMap<>();
    }

    public Resource(int apiId, String urlPattern) {

        this();
        this.apiId = apiId;
        this.urlPattern = urlPattern;
    }

    public List<Verb> getAllVerbs() {

        return Arrays.asList(httpVerbs.values().toArray(new Verb[]{}));
    }

    public int getApiId() {

        return apiId;
    }

    public void setApiId(int apiId) {

        this.apiId = apiId;
    }

    public String getUrlPattern() {

        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {

        this.urlPattern = urlPattern;
    }

    public void addVerb(Verb resourceVerb) {

        httpVerbs.put(resourceVerb.getHttpVerb(), resourceVerb);
    }

    public Verb getVerb(String httpVerb) {

        return httpVerbs.get(httpVerb);
    }

    @Override
    public String getCacheKey() {

        return urlPattern + "." + apiId;
    }
}
