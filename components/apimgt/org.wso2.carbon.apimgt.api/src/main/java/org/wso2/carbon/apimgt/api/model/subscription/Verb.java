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

/**
 * Entity to represent a Verb in a REST resource.
 */
public class Verb {

    private int verbId;

    private String httpVerb;

    private String authType;

    private String throttlingTier;

    private String script;

    public String getScript() {

        return script;
    }

    public void setScript(String script) {

        this.script = script;
    }

    public int getVerbId() {

        return verbId;
    }

    public void setVerbId(int verbId) {

        this.verbId = verbId;
    }

    public String getHttpVerb() {

        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {

        this.httpVerb = httpVerb;
    }

    public String getAuthType() {

        return authType;
    }

    public void setAuthType(String authType) {

        this.authType = authType;
    }

    public String getThrottlingTier() {

        return throttlingTier;
    }

    public void setThrottlingTier(String throttlingTier) {

        this.throttlingTier = throttlingTier;
    }
}
