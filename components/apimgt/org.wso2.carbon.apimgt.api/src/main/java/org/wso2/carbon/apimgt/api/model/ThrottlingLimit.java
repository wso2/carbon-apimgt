/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents throttling limit relevant information
 */
public class ThrottlingLimit implements Serializable {
    private static final long serialVersionUID = 1L;
    private int requestCount;
    private String unit;

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ThrottlingLimit throttlingLimit = (ThrottlingLimit) o;
        return requestCount == throttlingLimit.getRequestCount() && unit.equals(throttlingLimit.getUnit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestCount, unit);
    }
}
