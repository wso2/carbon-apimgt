/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.external.model;

/**
 * Response configuration for an external rule call.
 */
public class ExternalResponseDefinition {

    private String resultPath;
    private Object expectedValue;
    private String messagePath;

    public String getResultPath() {

        return resultPath;
    }

    public void setResultPath(String resultPath) {

        this.resultPath = resultPath;
    }

    public Object getExpectedValue() {

        return expectedValue;
    }

    public void setExpectedValue(Object expectedValue) {

        this.expectedValue = expectedValue;
    }

    public String getMessagePath() {

        return messagePath;
    }

    public void setMessagePath(String messagePath) {

        this.messagePath = messagePath;
    }
}
