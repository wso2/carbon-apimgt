/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model.policy;

public class HeaderCondition extends Condition {
    private String headerName;
    private String value;

    public HeaderCondition() {
        setType(PolicyConstants.HEADER_TYPE);
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeader(String headerName) {
        this.headerName = headerName;
        this.queryAttributeName = "cast(map:get(properties,’"+this.headerName+"’),’string’)";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getCondition() {
        String condition = "("+getQueryAttributeName()+" == "+getValue()+")";
        if(isInvertCondition()){
            condition="!"+condition;
        }
        return condition;
    }
}
