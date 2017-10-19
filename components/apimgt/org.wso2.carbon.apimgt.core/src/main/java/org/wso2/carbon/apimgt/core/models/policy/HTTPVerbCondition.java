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

package org.wso2.carbon.apimgt.core.models.policy;

/**
 * Contains {@link HTTPVerbCondition} attributes
 */
public class HTTPVerbCondition extends Condition {
    private String httpVerb;

    public HTTPVerbCondition() {
        setType(PolicyConstants.HTTP_VERB_CONDITION_TYPE);
        this.queryAttributeName = PolicyConstants.START_QUERY + PolicyConstants.HTTP_VERB_QUERY
                + PolicyConstants.END_QUERY;
        // "cast(map:get(properties,’"+value+"’),’string’)";
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    @Override
    public String getCondition() {
        String condition = PolicyConstants.OPEN_BRACKET + getQueryAttributeName() + PolicyConstants.EQUAL
                + PolicyConstants.QUOTE + getHttpVerb() + PolicyConstants.QUOTE + PolicyConstants.CLOSE_BRACKET; // "
        // ("+queryAttribute+"=="+value+")"
        if (isInvertCondition()) {
            condition = PolicyConstants.INVERT_CONDITION + condition; // "!"+condition
        }
        return condition;
    }

    @Override
    public String getNullCondition() {
        return null;
    }

    @Override
    public String toString() {
        return "HTTPVerbCondition [httpVerb=" + httpVerb + ", toString()=" + super.toString() + "]";
    }

}
