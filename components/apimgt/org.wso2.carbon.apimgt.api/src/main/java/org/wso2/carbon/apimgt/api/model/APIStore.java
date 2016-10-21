/*
*  Copyright WSO2 Inc.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

package org.wso2.carbon.apimgt.api.model;


public class APIStore {

    private String type = "wso2";
    private String name;
    private String displayName;
    private String endpoint;
    private String username;
    private String password;
    private boolean published;
    private APIPublisher publisher;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isPublished() {
        return published;
    }

    public void setUsername(String username) {
        this.username= username;
    }

    public String getUsername() {
        return username;
    }
    public void setPassword(String password) {
        this.password= password;
    }

    public String getPassword() {
        return password;
    }

    public APIPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(APIPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        APIStore store = (APIStore) o;
        return !(this.getName() != null ? !this.getName().equals(store.getName()) : store.getName() != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}

