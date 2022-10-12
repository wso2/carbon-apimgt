/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.apk.apimgt.impl.workflow;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.WorkflowResponse;

/**
 *  The HttpWorkflowResponse is the  implementation of the WorkflowResponse that carries HTTP response related information.
 */
public class HttpWorkflowResponse implements WorkflowResponse {

    private String redirectUrl = "";
    private String redirectConfirmationMsg = null;
    private JSONObject jsonPayloadObj = new JSONObject();
    private JSONObject additionalParameters = new JSONObject();

    @Override
    @SuppressWarnings("unchecked")
    public String getJSONPayload() {
       jsonPayloadObj.put("redirectUrl", redirectUrl);
       jsonPayloadObj.put("redirectConfirmationMsg", redirectConfirmationMsg);
       jsonPayloadObj.put("additionalParameters", additionalParameters);
       return jsonPayloadObj.toJSONString();
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectConfirmationMsg() {
        return redirectConfirmationMsg;
    }

    public void setRedirectConfirmationMsg(String redirectConfirmationMsg) {
        this.redirectConfirmationMsg = redirectConfirmationMsg;
    }

    @SuppressWarnings("unchecked")
    public void setAdditionalParameters(String paramName, String paramValue){
        additionalParameters.put(paramName, paramValue);
    }

    public JSONObject getAdditionalParameterss() {
        return additionalParameters;
    }

}
