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

public class JWTClaimsCondition extends Condition{
    private String claimUrl;
    private String attribute;

    public JWTClaimsCondition() {
        setType(PolicyConstants.JWT_CLAIMS_TYPE);
    }

    public String getClaimUrl() {
        return claimUrl;
    }

    public void setClaimUrl(String claimUrl) {
        this.claimUrl = claimUrl;
        this.queryAttributeName = "cast(map:get(properties,’"+this.claimUrl+"’),’string’)";
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getCondition() {
        String condition = "("+getQueryAttributeName()+" == "+getAttribute()+")";
        if(isInvertCondition()){
            condition="!"+condition;
        }
        return condition;
    }
}
