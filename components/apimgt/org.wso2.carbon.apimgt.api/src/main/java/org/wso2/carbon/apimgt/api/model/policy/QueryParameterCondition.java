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

public class QueryParameterCondition extends Condition {
    private String parameter;
    private String value;

    public QueryParameterCondition() {
        setType(PolicyConstants.QUERY_PARAMETER_TYPE);
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
        this.queryAttributeName = PolicyConstants.START_QUERY + this.parameter + PolicyConstants.END_QUERY;
        // "cast(map:get(properties,’"+value+"’),’string’)";
        nullFilterQueryString =  PolicyConstants.NULL_START_QUERY + this.parameter + PolicyConstants.NULL_END_QUERY;
        // "map:get(properties,’"+value+"’) is null";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getCondition() {
        //"regex:find('+value+', cast(map:get(propertiesMap,'+name+'),'string')))"
        String condition = PolicyConstants.OPEN_BRACKET + PolicyConstants.REGEX_PREFIX_QUERY
                + PolicyConstants.QUOTE + getValue() + PolicyConstants.QUOTE + PolicyConstants.COMMA +
                getQueryAttributeName() + PolicyConstants.CLOSE_BRACKET + PolicyConstants.CLOSE_BRACKET;
        if(isInvertCondition()){
            condition = PolicyConstants.INVERT_CONDITION + condition;  // "!"+condition
        }
        return condition;
    }

    @Override
    public String getNullCondition() {
        String condition = PolicyConstants.OPEN_BRACKET + getQueryAttributeName() + PolicyConstants.EQUAL
                + PolicyConstants.QUOTE + PolicyConstants.NULL_CHECK + PolicyConstants.QUOTE + PolicyConstants.CLOSE_BRACKET; // "("+queryAttribute+"=="+value+")"
        return condition;
    }

    @Override
    public String toString() {
        return "QueryParameterCondition [parameter=" + parameter + ", value=" + value + ", toString()="
                + super.toString() + "]";
    }
    
}
