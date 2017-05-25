/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.ballerina.caching;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BValue;

import java.io.Serializable;

/**
 * Wrapper class for cache entry. This class was introduced because ballerina variables
 * are not support serializable implementation.
 * TODO this need to be replace.
 */
public class BCacheEntry implements Serializable {

    /**
     * Convert to string
     *
     * @return String representation of cache entry
     */
    public String toString() {
        StringBuilder builder = new StringBuilder(20);
        builder.append("BCacheEntry = { type:").append(stringType).
                append(" , value:").append(stringValue).append("}");
        return builder.toString();

    }

    private static final long serialVersionUID = 12345L;
    private transient BType type = null;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public BType getType() {
        return type;
    }

    public void setType(BType type) {
        this.type = type;
        setStringType(type.toString());
    }

    public BValue getValue() {
        return value;
    }

    public void setValue(BValue value) {
        this.value = value;
        setStringValue(value.stringValue());
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    private transient BValue value = null;
    private String stringValue;

    public String getStringType() {
        return stringType;
    }

    public void setStringType(String stringType) {
        this.stringType = stringType;
    }

    private String stringType;
}
