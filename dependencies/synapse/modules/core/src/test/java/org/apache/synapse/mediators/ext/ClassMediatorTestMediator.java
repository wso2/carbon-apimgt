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

package org.apache.synapse.mediators.ext;

import org.apache.synapse.MessageContext;
import org.apache.synapse.Mediator;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;

/**
 * Since the class mediator always "instantiates" a new instance of a class
 * use a static member variable just to test this.. This class is not nice.. :-)
 * but does what is expected... :-(
 */
public class ClassMediatorTestMediator extends AbstractMediator implements ManagedLifecycle {

    public static boolean invoked = false;
    public static boolean initialized = false;
    public static boolean destroyed = false;

    public static String testProp = null;

    public boolean mediate(MessageContext synCtx) {
        invoked = true;
        return false;
    }

    public String getType() {
        return null;
    }

    public void setTestProp(String s) {
        testProp = s;
    }

    public String getTestProp() {
        return testProp;
    }

    public void init(SynapseEnvironment se) {
        initialized = true;
    }

    public void destroy() {
        destroyed = true;
    }
}
