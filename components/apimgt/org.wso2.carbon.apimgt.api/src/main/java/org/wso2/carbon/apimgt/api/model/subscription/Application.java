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

package org.wso2.carbon.apimgt.api.model.subscription;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Entity for keeping Application related information. Represents an Application in APIM.
 */
public class Application implements CacheableEntity<Integer> {
    private String uuid;
    private int id = -1;
    private String name = null;
    private String subName = null;
    private String policy = null;
    private String tokenType = null;
    private Set<String> groupIdList = new HashSet<>();
    private Map<String, String> attributes = new Hashtable<>();

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getSubName() {

        return subName;
    }

    public void setSubName(String subName) {

        this.subName = subName;
    }

    public String getPolicy() {

        return policy;
    }

    public void setPolicy(String policy) {

        this.policy = policy;
    }

    public String getTokenType() {

        return tokenType;
    }

    public void setTokenType(String tokenType) {

        this.tokenType = tokenType;
    }

    public Set<String> getGroupIds() {

        return groupIdList;
    }

    public void addGroupId(String groupId) {

        this.groupIdList.add(groupId);
    }

    public Integer getCacheKey() {

        return getId();
    }

    public Map<String, String> getAttributes() {

        return attributes;
    }

    public void addAttribute(String name, String value) {

        if (!this.attributes.containsKey(name)) {
            this.attributes.put(name, value);
        }
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }
}
