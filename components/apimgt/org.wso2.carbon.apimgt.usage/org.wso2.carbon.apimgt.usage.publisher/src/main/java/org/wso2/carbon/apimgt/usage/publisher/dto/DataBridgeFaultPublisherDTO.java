/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.usage.publisher.dto;

import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;

import java.util.ArrayList;
import java.util.List;

public class DataBridgeFaultPublisherDTO extends FaultPublisherDTO{

    public DataBridgeFaultPublisherDTO(FaultPublisherDTO faultPublisherDTO){
        setApplicationConsumerKey(faultPublisherDTO.getApplicationConsumerKey());
        setApiContext(faultPublisherDTO.getApiContext());
        setApiVersion(faultPublisherDTO.getApiVersion());
        setApiName(faultPublisherDTO.getApiName());
        setApiResourcePath(faultPublisherDTO.getApiResourcePath());
        setApiMethod(faultPublisherDTO.getApiMethod());
        setApiVersion(faultPublisherDTO.getApiVersion());
        setErrorCode(faultPublisherDTO.getErrorCode());
        setErrorMessage(faultPublisherDTO.getErrorMessage());
        setRequestTimestamp((faultPublisherDTO.getRequestTimestamp()));
        setUsername(faultPublisherDTO.getUsername());
        setApiCreatorTenantDomain(faultPublisherDTO.getApiCreatorTenantDomain());
        setHostname(DataPublisherUtil.getHostAddress());
        setApiCreator(faultPublisherDTO.getApiCreator());
        setApplicationName(faultPublisherDTO.getApplicationName());
        setApplicationId(faultPublisherDTO.getApplicationId());
        setProtocol(faultPublisherDTO.getProtocol());
        setMetaClientType(faultPublisherDTO.getMetaClientType());
        setGatewaType(faultPublisherDTO.getGatewaType());
        setUserTenantDomain(faultPublisherDTO.getUserTenantDomain());
        setProperties(faultPublisherDTO.getProperties());
    }

    public Object createPayload() {
        return new Object[] { getApplicationConsumerKey(), getApiName(), getApiVersion(),
                getApiContext(), getApiResourcePath(), getApiMethod(), getApiCreator(), 
                getUsername(), getApiCreatorTenantDomain(), getUserTenantDomain(), getHostname(), getApplicationId(), getApplicationName(),
                getProtocol(), getErrorCode(), getErrorMessage(), getRequestTimestamp(),
                DataPublisherUtil.toJsonString(getProperties()) };
    }

    public Object createMetaData() {
        return new Object[] { getMetaClientType() };
    }

    /*
     *  This method validates null for any mandatory field
     *
     *  @return Alist of mandatory values which are null
     *
     * */
    public List<String> getMissingMandatoryValues() {

        List<String> missingMandatoryValues = new ArrayList<String>();
        if (getApplicationConsumerKey() == null) {
            missingMandatoryValues.add("Application consumer key");
        }
        if (getApiName() == null) {
            missingMandatoryValues.add("API name");
        }
        if (getApiVersion() == null) {
            missingMandatoryValues.add("API version");
        }
        if (getApiContext() == null) {
            missingMandatoryValues.add("API context");
        }
        if (getApiResourcePath() == null) {
            missingMandatoryValues.add("API resource path");
        }
        if (getApiMethod() == null) {
            missingMandatoryValues.add("API method");
        }
        if (getApiCreator() == null) {
            missingMandatoryValues.add("API creator");
        }
        if (getApiCreatorTenantDomain() == null) {
            missingMandatoryValues.add("API creator tenant domain");
        }
        if (getApplicationName() == null) {
            missingMandatoryValues.add("Application anme");
        }
        if (getApplicationId() == null) {
            missingMandatoryValues.add("Application ID");
        }
        if (getHostname() == null) {
            missingMandatoryValues.add("API hostname");
        }
        return missingMandatoryValues;
    }

    @Override
    public String toString() {

        return "Application consumer key: " + DataPublisherUtil.maskValue(getApplicationConsumerKey()) +
                ", Application name: " + getApplicationName() + ", Application ID: " + getApplicationId() +
                ", API name: " + getApiName() + ", API version: " + getApiVersion() +
                ", API context: " + getApiContext() + ", API resource path: " + getApiResourcePath() +
                ", API method: " + getApiMethod() + ", API creator: " + getApiCreator() +
                ", API creator tenant domain: " + getApiCreatorTenantDomain() + ", Username: " + getUsername() +
                ", User tenant domain: " + getUserTenantDomain() + ", Host name: " + getHostname() +
                ", Protocol: " + getProtocol() + ", Error code: " + getErrorCode() +
                ", Error message: " + getErrorMessage() + ", Request timestamp: " + getRequestTimestamp();
    }
}
