/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.util;

import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

/**
 * Keeps the information on the Proxy service Policies
 */
public class PolicyInfo {

    private String policyKey;
    private int type;
    private QName operation;

    public static final int MESSAGE_TYPE_IN = 1;
    public static final int MESSAGE_TYPE_OUT = 2;

    public PolicyInfo(String policyKey) {
        this.policyKey = policyKey;
    }

    public PolicyInfo(String policyKey, int type) {
        this.policyKey = policyKey;
        this.type = type;
    }

    public PolicyInfo(String policyKey, QName operation) {
        this.policyKey = policyKey;
        this.operation = operation;
    }

    public PolicyInfo(String policyKey, int type, QName operation) {
        this.policyKey = policyKey;
        this.type = type;
        this.operation = operation;
    }

    public boolean isServicePolicy() {
        return policyKey != null && type == 0 && operation == null;
    }

    public boolean isOperationPolicy() {
        return policyKey != null && type == 0 && operation != null;
    }

    public boolean isMessagePolicy() {
        return policyKey != null && type != 0;
    }

    public String getMessageLable() {
        if (type == MESSAGE_TYPE_IN) {
            return WSDLConstants.MESSAGE_LABEL_IN_VALUE;
        } else if (type == MESSAGE_TYPE_OUT) {
            return WSDLConstants.MESSAGE_LABEL_OUT_VALUE;
        } else {
            return null;
        }
    }

    public String getPolicyKey() {
        return policyKey;
    }

    public void setPolicyKey(String policyKey) {
        this.policyKey = policyKey;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public QName getOperation() {
        return operation;
    }

    public void setOperation(QName operation) {
        this.operation = operation;
    }
}
