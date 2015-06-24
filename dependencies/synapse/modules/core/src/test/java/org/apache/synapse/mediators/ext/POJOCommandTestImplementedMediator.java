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

import org.apache.synapse.Command;

/**
 * This class is just to test the behaviour of the POJO Command when implemented by implementing
 * the Command interface. This will use static members to accomplish the test tasks.
 */
public class POJOCommandTestImplementedMediator implements Command {
    
    public static String testProp = null;
    private String ctxTest = "";

    public void execute() {
        POJOCommandTestHelper.getInstance().setExecuted(true);
        ctxTest += "command";
    }

    public void setTestProp(String s) {
        testProp = s;
        POJOCommandTestHelper.getInstance().setChangedProperty(testProp);
    }

    public String getTestProp() {
        return testProp;
    }

    public String getCtxTest() {
        return ctxTest;
    }

    public void setCtxTest(String ctxTest) {
        this.ctxTest = ctxTest;
    }
}
