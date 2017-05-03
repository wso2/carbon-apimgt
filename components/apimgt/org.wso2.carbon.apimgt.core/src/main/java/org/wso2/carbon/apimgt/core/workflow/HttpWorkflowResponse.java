/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.workflow;

import org.json.simple.JSONObject;

/**
 * HTTP workflow response. Can be used to pass workflow complete redirections
 */
public class HttpWorkflowResponse extends AbstractWorkflowResponse {

    private String redirectUrl = "";
    private String redirectConfirmationMsg = "";
    private JSONObject jsonPayloadObj = new JSONObject();
    private JSONObject additionalParameters = new JSONObject();
    
    private static final String REDIRECT_URL = "redirectUrl";
    private static final String CONF_MSG = "redirectConfirmationMsg";
    private static final String ADDITIONAL_PARAM = "additionalParameters";

    @Override
    @SuppressWarnings("unchecked")
    public String getJSONPayload() {

        jsonPayloadObj.put(REDIRECT_URL, redirectUrl);
        jsonPayloadObj.put(CONF_MSG, redirectConfirmationMsg);
        jsonPayloadObj.put(ADDITIONAL_PARAM, additionalParameters);

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
    public void setAdditionalParameters(String paramName, String paramValue) {
        additionalParameters.put(paramName, paramValue);
    }

    public JSONObject getAdditionalParameterss() {
        return additionalParameters;
    }    
}
