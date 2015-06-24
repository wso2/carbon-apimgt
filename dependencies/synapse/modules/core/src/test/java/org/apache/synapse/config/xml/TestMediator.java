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
package org.apache.synapse.config.xml;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.axiom.om.OMElement;

/**
 *
 *
 */

public class TestMediator extends AbstractMediator {

    private String testProp = "test property";

    private OMElement testElemProp;

    public boolean mediate(MessageContext synCtx) {
        return false;
    }

    public String getTestProp() {
        return this.testProp;
    }

    public void setTestProp(String value) {
        this.testProp = value;
    }

    public OMElement getTestElemProp() {
        return testElemProp;
    }

    public void setTestElemProp(OMElement testElemProp) {
        this.testElemProp = testElemProp;
    }

    public String getType() {
        return TestMediator.class.getName();
    }
}
