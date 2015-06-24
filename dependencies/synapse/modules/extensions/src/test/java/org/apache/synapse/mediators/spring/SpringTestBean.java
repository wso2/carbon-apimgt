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

package org.apache.synapse.mediators.spring;

import org.apache.synapse.MessageContext;
import org.apache.synapse.TestMediateHandler;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.AbstractMediator;

/**
 * This is a very simple Spring bean, that has one int property, and a
 * reference to another bean. This second bean is invoked on each mediate()
 * call, and it keeps a count of the invocations to be tested by JUnit
 */
public class SpringTestBean extends AbstractMediator {

    private int testProperty;

    private TestMediateHandler handler = null;

    public SpringTestBean() {
    }

    public boolean mediate(MessageContext synCtx) {
        if (handler != null) {
            handler.handle(synCtx);
        }
        return true;
    }

    public TestMediateHandler getHandler() {
        return handler;
    }

    public void setHandler(TestMediateHandler handlerTest) {
        this.handler = handlerTest;
    }

    public String getType() {
        return "SpringTestBean";
    }

    public void setTestProperty(int i) {
        this.testProperty = i;
        TestMediateHandlerImpl.invokeCount += i;
    }

    public int getTestProperty() {
        return testProperty;
    }
}
