/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.extension.listener.model.dto;

import java.security.cert.X509Certificate;
import java.util.Map;

public class RequestContextDTO {

    MsgInfoDTO msgInfo;
    APIRequestInfoDTO apiRequestInfo;
    X509Certificate clientCert;
    Map<String,String> customProperties;

    public MsgInfoDTO getMsgInfo() {

        return msgInfo;
    }

    public void setMsgInfo(MsgInfoDTO msgInfo) {

        this.msgInfo = msgInfo;
    }

    public APIRequestInfoDTO getApiRequestInfo() {

        return apiRequestInfo;
    }

    public void setApiRequestInfo(APIRequestInfoDTO apiRequestInfo) {

        this.apiRequestInfo = apiRequestInfo;
    }

    public X509Certificate getClientCert() {

        return clientCert;
    }

    public void setClientCert(X509Certificate clientCert) {

        this.clientCert = clientCert;
    }

    public Map<String, String> getCustomProperties() {

        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {

        this.customProperties = customProperties;
    }
}

