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
 * Contains request CountLimit based attributes
 */
public class RequestCountLimit extends Limit {

    public static final String REQUEST_QUOTA_UNIT = "REQ";

    private int requestCount;

    public RequestCountLimit (String timeUnit, int unitTime, int requestCount) {
        super(timeUnit, unitTime);
        this.requestCount = requestCount;
    }

    @Override
    public void populateDataInPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(6, getRequestCount());
        preparedStatement.setString(7, REQUEST_QUOTA_UNIT);
    }

    public int getRequestCount() {
        return requestCount;
    }

    @Override
    public String toString() {
        return "RequestCountLimit [requestCount=" + requestCount + ", toString()=" + super.toString() + "]";
    }

}
