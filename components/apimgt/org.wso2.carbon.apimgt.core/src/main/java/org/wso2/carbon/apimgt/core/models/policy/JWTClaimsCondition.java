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

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Contains {@link JWTClaimsCondition} attributes
 */
public class JWTClaimsCondition extends Condition {
    private String claimUrl;
    private String attribute;

    public JWTClaimsCondition() {
        setType(PolicyConstants.JWT_CLAIMS_CONDITION_TYPE);
    }

    public String getClaimUrl() {
        return claimUrl;
    }

    public void setClaimUrl(String claimUrl) {
        this.claimUrl = claimUrl;
        this.queryAttributeName = PolicyConstants.START_QUERY + this.claimUrl + PolicyConstants.END_QUERY;
    }

    @Override
    public void populateDataInPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, getClaimUrl());
        preparedStatement.setString(2, getAttribute());
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getCondition() {
        //"regex:find('+value+', cast(map:get(propertiesMap,'+name+'),'string')))"
        String condition = PolicyConstants.OPEN_BRACKET + PolicyConstants.REGEX_PREFIX_QUERY +
                PolicyConstants.QUOTE + getAttribute() + PolicyConstants.QUOTE + PolicyConstants.COMMA +
                getQueryAttributeName() + PolicyConstants.CLOSE_BRACKET + PolicyConstants.CLOSE_BRACKET;
        if (isInvertCondition()) {
            condition = PolicyConstants.INVERT_CONDITION + condition; // "!"+condition
        }
        return condition;
    }

    @Override
    public String getNullCondition() {
        String condition = PolicyConstants.OPEN_BRACKET + getQueryAttributeName() + PolicyConstants.EQUAL
                + PolicyConstants.QUOTE + PolicyConstants.NULL_CHECK + PolicyConstants.QUOTE + PolicyConstants
                .CLOSE_BRACKET; // "("+queryAttribute+"=="+value+")"
        return condition;
    }

    @Override
    public String toString() {
        return "JWTClaimsCondition [claimUrl=" + claimUrl + ", attribute=" + attribute + ", toString()="
                + super.toString() + "]";
    }

}
