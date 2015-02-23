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

/**
 * This class will be used to test the execution of the POJOCommand mediator because POJOCommands
 * are instantiated and destroyed in the mediation time. This will provide a common point that can
 * be accessed by the test case as well as by the POJOCommand
 */
public class POJOCommandTestHelper {

    private static POJOCommandTestHelper HELPER_OBJ = null;

    private boolean executed = false;

    private String changedProperty = null;

    public static POJOCommandTestHelper getInstance() {
        if(HELPER_OBJ == null) {
            HELPER_OBJ = new POJOCommandTestHelper();
        }
        return HELPER_OBJ;
    }

    public static void reset() {
        getInstance().setExecuted(false);
        getInstance().setChangedProperty(null);
    }

    public boolean isExecuted() {
        return this.executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public void setChangedProperty(String changedProperty) {
        this.changedProperty = changedProperty;
    }

    public String getChangedProperty() {
        return this.changedProperty;
    }
}
