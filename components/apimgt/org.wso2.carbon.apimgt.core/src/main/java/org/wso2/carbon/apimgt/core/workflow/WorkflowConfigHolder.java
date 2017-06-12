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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.WorkflowConfig;
import org.wso2.carbon.apimgt.core.models.WorkflowConfigProperties;
import org.wso2.carbon.apimgt.core.models.WorkflowExecutorInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds the workflow extensions
 */
public class WorkflowConfigHolder {

    private static final Log log = LogFactory.getLog(WorkflowConfigHolder.class);

    private Map<String, WorkflowExecutor> workflowExecutorMap;

    public WorkflowExecutor getWorkflowExecutor(String workflowExecutorType) {
        return workflowExecutorMap.get(workflowExecutorType);
    }

    public void load() throws WorkflowException {

        workflowExecutorMap = new ConcurrentHashMap<>();

        try {

            WorkflowConfig config = WorkflowExtensionsConfigBuilder.getWorkflowConfig();

            // Load application creation workflow configurations
            loadWorkflowConfigurations(config.getApplicationCreation(),
                    WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);

            // Load application deletion workflow configurations
            loadWorkflowConfigurations(config.getApplicationDeletion(),
                    WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);

            // Load subscription creation workflow configurations
            loadWorkflowConfigurations(config.getSubscriptionCreation(),
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);

            // Load subscription deletion workflow configurations
            loadWorkflowConfigurations(config.getSubscriptionDeletion(),
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);

            // Load api state change workflow configurations
            loadWorkflowConfigurations(config.getApiStateChange(), WorkflowConstants.WF_TYPE_AM_API_STATE);

            // Load application update workflow configurations
            loadWorkflowConfigurations(config.getApplicationUpdate(),
                    WorkflowConstants.WF_TYPE_AM_APPLICATION_UPDATE);

        } catch (ClassNotFoundException e) {
            handleException("Unable to find class", e);
        } catch (InstantiationException e) {
            handleException("Unable to instantiate class", e);
        } catch (IllegalAccessException e) {
            handleException("Illegal attempt to invoke class methods", e);
        } catch (WorkflowException e) {
            handleException("Unable to load workflow executor class", e);
        }
    }

    private void loadWorkflowConfigurations(WorkflowExecutorInfo workflowConfig, String workflowExecutorType) throws
            ClassNotFoundException, IllegalAccessException, InstantiationException, WorkflowException {

        String executorClass = workflowConfig.getExecutor();
        Class clazz = WorkflowConfigHolder.class.getClassLoader().loadClass(executorClass);
        WorkflowExecutor workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
        List<WorkflowConfigProperties> properties = workflowConfig.getProperty();
        if (properties != null) {
            loadProperties(properties, workFlowExecutor);
        }
        workflowExecutorMap.put(workflowExecutorType, workFlowExecutor);
    }

    private void loadProperties(List<WorkflowConfigProperties> properties, WorkflowExecutor workFlowExecutor)
            throws WorkflowException {

        for (Iterator iterator = properties.iterator(); iterator.hasNext(); ) {
            WorkflowConfigProperties workflowConfigProperties = (WorkflowConfigProperties) iterator.next();
            String propertyName = workflowConfigProperties.getName();
            String propertyValue = workflowConfigProperties.getValue();
            if (propertyName == null) {
                handleException("An Executor class property must specify the name attribute");
            } else {
                setInstanceProperty(propertyName, propertyValue, workFlowExecutor);
            }
        }

    }

    /**
     * Find and invoke the setter method with the name of form setXXX passing in the value given
     * on the POJO object
     *
     * @param name name of the setter field
     * @param val  value to be set
     * @param obj  POJO instance
     * @throws WorkflowException if error occurred when setting instance property
     */
    public void setInstanceProperty(String name, Object val, Object obj) throws WorkflowException {

        String mName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        Method method;

        try {
            Method[] methods = obj.getClass().getMethods();
            boolean invoked = false;

            for (Method method1 : methods) {
                if (mName.equals(method1.getName())) {
                    Class[] params = method1.getParameterTypes();
                    if (params.length != 1) {
                        handleException("Did not find a setter method named : " + mName
                                + "() that takes a single String, int, long, float, double ,"
                                + "OMElement or boolean parameter");
                    } else if (val instanceof String) {
                        String value = (String) val;
                        if (String.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, String.class);
                            method.invoke(obj, new String[]{value});
                        } else if (int.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, int.class);
                            method.invoke(obj, new Integer[]{Integer.valueOf(value)});
                        } else if (long.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, long.class);
                            method.invoke(obj, new Long[]{Long.valueOf(value)});
                        } else if (float.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, float.class);
                            method.invoke(obj, new Float[]{Float.valueOf(value)});
                        } else if (double.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, double.class);
                            method.invoke(obj, new Double[]{Double.valueOf(value)});
                        } else if (boolean.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, boolean.class);
                            method.invoke(obj, new Boolean[]{Boolean.valueOf(value)});
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    invoked = true;
                    break;
                }
            }
            if (!invoked) {
                String message = "Did not find a setter method named : " + mName
                        + "() that takes a single String, int, long, float, double " + "or boolean parameter";
                handleException(message);
            }
            
            
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException
                | InvocationTargetException e) {
            String message = "Error invoking setter method named : " + mName
                    + "() that takes a single String, int, long, float, double " + "or boolean parameter";
            handleException(message, e);
        }
       
    }

    private static void handleException(String msg) throws WorkflowException {
        log.error(msg);
        throw new WorkflowException(msg);
    }

    private static void handleException(String msg, Exception e) throws WorkflowException {
        log.error(msg, e);
        throw new WorkflowException(msg, e);
    }
}
