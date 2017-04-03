/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.workflow;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;

public class WorkflowConfigHolderTestCase {

    @Test(description = "Test dynamic class loading")
    public void testInstancePropertySet()
            throws ClassNotFoundException, WorkflowException, InstantiationException, IllegalAccessException {
        String className = "org.wso2.carbon.apimgt.core.workflow.TestClass";
        Class clazz = WorkflowConfigHolderTestCase.class.getClassLoader().loadClass(className);

        TestClass testClass = (TestClass) clazz.newInstance();

        WorkflowConfigHolder holder = new WorkflowConfigHolder();

        holder.setInstanceProperty("stringParam", "testString", testClass);
        Assert.assertEquals(testClass.getStringParam(), "testString");

        int intParam = 10;
        holder.setInstanceProperty("intParam", Integer.toString(intParam), testClass);
        Assert.assertEquals(testClass.getIntParam(), intParam);

        float floatParam = 123.4f;
        holder.setInstanceProperty("floatParam", Float.toString(floatParam), testClass);
        Assert.assertEquals(testClass.getFloatParam(), floatParam);

        long longParam = 1L;
        holder.setInstanceProperty("longParam", Long.toString(longParam), testClass);
        Assert.assertEquals(testClass.getLongParam(), longParam);

        double param = 123.4;
        holder.setInstanceProperty("doubleParam", Double.toString(param), testClass);
        Assert.assertEquals(testClass.getDoubleParam(), param);

        boolean boolParam = true;
        holder.setInstanceProperty("boolParam", Boolean.toString(boolParam), testClass);
        Assert.assertEquals(testClass.isBoolParam(), boolParam);
    }

    @Test(description = "Test dynamic class loading with invalid method name",
            expectedExceptions = APIManagementException.class)

    public void testInstancePropertySetError()
            throws ClassNotFoundException, WorkflowException, InstantiationException, IllegalAccessException {
        String className = "org.wso2.carbon.apimgt.core.workflow.TestClass";
        Class clazz = WorkflowConfigHolderTestCase.class.getClassLoader().loadClass(className);

        TestClass testClass = (TestClass) clazz.newInstance();

        WorkflowConfigHolder holder = new WorkflowConfigHolder();

        holder.setInstanceProperty("stringParamInvalid", "testString", testClass);

    }

    @Test(description = "Test dynamic class loading with invalid method name", 
            expectedExceptions = APIManagementException.class)

    public void testInstancePropertySetWithoutSetters()
            throws ClassNotFoundException, WorkflowException, InstantiationException, IllegalAccessException {
        String className = "org.wso2.carbon.apimgt.core.workflow.TestClass";
        Class clazz = WorkflowConfigHolderTestCase.class.getClassLoader().loadClass(className);

        TestClass testClass = (TestClass) clazz.newInstance();
        WorkflowConfigHolderTestCase testclass = new WorkflowConfigHolderTestCase();
        WorkflowConfigHolder holder = new WorkflowConfigHolder();

        holder.setInstanceProperty("stringParamInvalid", "testString", testclass);

    }
}
