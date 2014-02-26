/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Subscriber's view of the API
 */
@SuppressWarnings("unused")
public class SubscribedAPI {

    private Tier tier;
    private Subscriber subscriber;
    private APIIdentifier apiId;
    private Date lastAccessed;
    private Application application;
    private String subStatus;
    private List<APIKey> keys = new ArrayList<APIKey>();

    private boolean isBlocked;   //TODO: what is the difference & usage of revoking & blocking users

    public SubscribedAPI(Subscriber subscriber, APIIdentifier apiId) {
        this.subscriber = subscriber;
        this.apiId = apiId;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Application getApplication() {
        return application;
    }

    public void setSubStatus(String status) {
        this.subStatus = status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public APIIdentifier getApiId() {
        return apiId;
    }

    public Tier getTier() {
        return tier;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public List<APIKey> getKeys() {
        return keys;
    }

    public void addKey(APIKey key) {
        keys.add(key);
    }

    public Date getLastAccessed() {
        return new Date(lastAccessed.getTime());
    }

    public void setLastAccessed(Date lastAccessed) {
        if (lastAccessed != null) {
            this.lastAccessed = new Date(lastAccessed.getTime());
        }
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscribedAPI that = (SubscribedAPI) o;
        return apiId.equals(that.apiId) && application.equals(that.application) &&
                subscriber.equals(that.subscriber);
    }

    @Override
    public int hashCode() {
        int result = subscriber.hashCode();
        result = 31 * result + apiId.hashCode();
        result = 31 * result + application.hashCode();
        return result;
    }
}
