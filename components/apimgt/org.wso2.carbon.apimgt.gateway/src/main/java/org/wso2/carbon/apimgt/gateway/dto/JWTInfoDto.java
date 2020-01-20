/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.dto;

import org.apache.synapse.MessageContext;
import org.json.JSONObject;

public class JWTInfoDto {

    private String applicationtier;
    private String keytype;
    private String version;
    private String applicationname;
    private String enduser;
    private int endusertenantid;
    private String applicationuuid;
    private String subscriber;
    private String subscriptionTier;
    private String applicationid;
    private String apicontext;
    private JSONObject jwtToken;
    private MessageContext messageContext;

    public String getApplicationtier() {

        return applicationtier;
    }

    public void setApplicationtier(String applicationtier) {

        this.applicationtier = applicationtier;
    }

    public String getKeytype() {

        return keytype;
    }

    public void setKeytype(String keytype) {

        this.keytype = keytype;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public String getApplicationname() {

        return applicationname;
    }

    public void setApplicationname(String applicationname) {

        this.applicationname = applicationname;
    }

    public String getEnduser() {

        return enduser;
    }

    public void setEnduser(String enduser) {

        this.enduser = enduser;
    }

    public int getEndusertenantid() {

        return endusertenantid;
    }

    public void setEndusertenantid(int endusertenantid) {

        this.endusertenantid = endusertenantid;
    }

    public String getApplicationuuid() {

        return applicationuuid;
    }

    public void setApplicationuuid(String applicationuuid) {

        this.applicationuuid = applicationuuid;
    }

    public String getSubscriber() {

        return subscriber;
    }

    public void setSubscriber(String subscriber) {

        this.subscriber = subscriber;
    }

    public String getSubscriptionTier() {

        return subscriptionTier;
    }

    public void setSubscriptionTier(String subscriptionTier) {

        this.subscriptionTier = subscriptionTier;
    }

    public String getApplicationid() {

        return applicationid;
    }

    public void setApplicationid(String applicationid) {

        this.applicationid = applicationid;
    }

    public String getApicontext() {

        return apicontext;
    }

    public void setApicontext(String apicontext) {

        this.apicontext = apicontext;
    }

    public JSONObject getJwtToken() {

        return jwtToken;
    }

    public void setJwtToken(JSONObject jwtToken) {

        this.jwtToken = jwtToken;
    }

    public MessageContext getMessageContext() {

        return messageContext;
    }

    public void setMessageContext(MessageContext messageContext) {

        this.messageContext = messageContext;
    }
}
