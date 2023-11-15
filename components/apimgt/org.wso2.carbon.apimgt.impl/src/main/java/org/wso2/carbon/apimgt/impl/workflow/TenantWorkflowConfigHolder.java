/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl.workflow;

import com.google.gson.JsonElement;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.WorkflowConfigDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.securevault.SecretResolver;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

public class TenantWorkflowConfigHolder implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(TenantWorkflowConfigHolder.class);

    private static final QName PROP_Q = new QName("Property");

    private static final QName ATT_NAME = new QName("name");

    private static final String DEFAULT_SUBSCRIPTION_UPDATE_EXECUTOR_CLASS =
            "org.wso2.carbon.apimgt.impl.workflow.SubscriptionUpdateSimpleWorkflowExecutor";

    private transient SecretResolver secretResolver;

    private String tenantDomain;

    private int tenantId;

    private Map<String, WorkflowExecutor> workflowExecutorMap;

    public TenantWorkflowConfigHolder(String tenantDomain, int tenantId) {
        this.tenantDomain = tenantDomain;
        this.tenantId = tenantId;
    }

    public WorkflowExecutor getWorkflowExecutor(String workflowExecutorType) {

        return workflowExecutorMap.get(workflowExecutorType);
    }

    public void load() throws WorkflowException {
        workflowExecutorMap = new ConcurrentHashMap<>();

        try {
            Object configObject =
                    ServiceReferenceHolder.getInstance().getApimConfigService().getWorkFlowConfig(tenantDomain);
            if (configObject instanceof WorkflowConfigDTO) {
                WorkflowConfigDTO workflowConfigDTO = (WorkflowConfigDTO) configObject;

                WorkflowConfigDTO.WorkflowConfig workflowElem;
                String executorClassName;
                Class clazz;
                WorkflowExecutor workFlowExecutor;

                // Application Creation
                workflowElem = workflowConfigDTO.
                        getWorkflowConfigMap().get(WorkflowConstants.APPLICATION_CREATION);
                executorClassName = getWorkflowExecutorClassName(workflowElem,
                        WorkflowConstants.WorkflowClasses.APPLICATION_CREATION_SIMPLE_FLOW_CLASS,
                        WorkflowConstants.WorkflowClasses.APPLICATION_CREATION_APPROVAL_FLOW_CLASS);

                try {
                    clazz = TenantWorkflowConfigHolder.class.getClassLoader().loadClass(executorClassName);
                    workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
                    loadProperties(workflowElem, workFlowExecutor);
                } catch (ClassNotFoundException e1) {
                    workFlowExecutor = new ApplicationCreationSimpleWorkflowExecutor();
                }
                workflowExecutorMap.put(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION, workFlowExecutor);

                // Production Application Registration
                workflowElem = workflowConfigDTO.
                        getWorkflowConfigMap().get(WorkflowConstants.PRODUCTION_APPLICATION_REGISTRATION);
                executorClassName = getWorkflowExecutorClassName(workflowElem,
                        WorkflowConstants.WorkflowClasses.APPLICATION_REGISTRATION_SIMPLE_FLOW_CLASS,
                        WorkflowConstants.WorkflowClasses.APPLICATION_REGISTRATION_APPROVAL_FLOW_CLASS);

                try {
                    clazz = TenantWorkflowConfigHolder.class.getClassLoader().loadClass(executorClassName);
                    workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
                    loadProperties(workflowElem, workFlowExecutor);
                } catch (ClassNotFoundException e1) {
                    workFlowExecutor = new ApplicationRegistrationSimpleWorkflowExecutor();
                }
                workflowExecutorMap.put(
                        WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION, workFlowExecutor);

                // Sandbox Application Registration
                workflowElem = workflowConfigDTO.
                        getWorkflowConfigMap().get(WorkflowConstants.SANDBOX_APPLICATION_REGISTRATION);
                executorClassName = getWorkflowExecutorClassName(workflowElem,
                        WorkflowConstants.WorkflowClasses.APPLICATION_REGISTRATION_SIMPLE_FLOW_CLASS,
                        WorkflowConstants.WorkflowClasses.APPLICATION_REGISTRATION_APPROVAL_FLOW_CLASS);

                try {
                    clazz = TenantWorkflowConfigHolder.class.getClassLoader().loadClass(executorClassName);
                    workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
                    loadProperties(workflowElem, workFlowExecutor);
                } catch (ClassNotFoundException e1) {
                    workFlowExecutor = new ApplicationRegistrationSimpleWorkflowExecutor();
                }
                workflowExecutorMap.put(
                        WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX, workFlowExecutor);

                // User Signup
                workflowElem = workflowConfigDTO.
                        getWorkflowConfigMap().get(WorkflowConstants.USER_SIGN_UP);
                executorClassName = getWorkflowExecutorClassName(workflowElem,
                        WorkflowConstants.WorkflowClasses.USER_SIGNUP_SIMPLE_FLOW_CLASS,
                        WorkflowConstants.WorkflowClasses.USER_SIGNUP_APPROVAL_FLOW_CLASS);

                try {
                    clazz = TenantWorkflowConfigHolder.class.getClassLoader().loadClass(executorClassName);
                    workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
                    loadProperties(workflowElem, workFlowExecutor);
                } catch (ClassNotFoundException e1) {
                    workFlowExecutor = new UserSignUpSimpleWorkflowExecutor();
                }
                workflowExecutorMap.put(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP, workFlowExecutor);

                // Subscription Creation
                workflowElem = workflowConfigDTO.
                        getWorkflowConfigMap().get(WorkflowConstants.SUBSCRIPTION_CREATION);
                executorClassName = getWorkflowExecutorClassName(workflowElem,
                        WorkflowConstants.WorkflowClasses.SUBSCRIPTION_CREATION_SIMPLE_FLOW_CLASS,
                        WorkflowConstants.WorkflowClasses.SUBSCRIPTION_CREATION_APPROVAL_FLOW_CLASS);

                try {
                    clazz = TenantWorkflowConfigHolder.class.getClassLoader().loadClass(executorClassName);
                    workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
                    loadProperties(workflowElem, workFlowExecutor);
                } catch (ClassNotFoundException e1) {
                    workFlowExecutor = new SubscriptionCreationSimpleWorkflowExecutor();
                }
                workflowExecutorMap.put(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION, workFlowExecutor);

                // Subscription Update
                workflowElem = workflowConfigDTO.
                        getWorkflowConfigMap().get(WorkflowConstants.SUBSCRIPTION_UPDATE);
                executorClassName = getWorkflowExecutorClassName(workflowElem,
                        WorkflowConstants.WorkflowClasses.SUBSCRIPTION_UPDATE_SIMPLE_FLOW_CLASS,
                        WorkflowConstants.WorkflowClasses.SUBSCRIPTION_UPDATE_APPROVAL_FLOW_CLASS);

                try {
                    clazz = TenantWorkflowConfigHolder.class.getClassLoader().loadClass(executorClassName);
                    workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
                    loadProperties(workflowElem, workFlowExecutor);
                } catch (ClassNotFoundException e1) {
                    workFlowExecutor = new SubscriptionUpdateSimpleWorkflowExecutor();
                }
                workflowExecutorMap.put(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE, workFlowExecutor);

                // Subscription Deletion
                workflowElem = workflowConfigDTO.
                        getWorkflowConfigMap().get(WorkflowConstants.SUBSCRIPTION_DELETION);
                executorClassName = getWorkflowExecutorClassName(workflowElem,
                        WorkflowConstants.WorkflowClasses.SUBSCRIPTION_DELETION_SIMPLE_FLOW_CLASS,
                        WorkflowConstants.WorkflowClasses.SUBSCRIPTION_DELETION_APPROVAL_FLOW_CLASS);

                try {
                    clazz = TenantWorkflowConfigHolder.class.getClassLoader().loadClass(executorClassName);
                    workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
                    loadProperties(workflowElem, workFlowExecutor);
                } catch (ClassNotFoundException e1) {
                    workFlowExecutor = new SubscriptionDeletionSimpleWorkflowExecutor();
                }
                workflowExecutorMap.put(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION, workFlowExecutor);

                // Application Deletion
                workflowElem = workflowConfigDTO.
                        getWorkflowConfigMap().get(WorkflowConstants.APPLICATION_DELETION);
                executorClassName = getWorkflowExecutorClassName(workflowElem,
                        WorkflowConstants.WorkflowClasses.APPLICATION_DELETION_SIMPLE_FLOW_CLASS,
                        WorkflowConstants.WorkflowClasses.APPLICATION_DELETION_APPROVAL_FLOW_CLASS);

                try {
                    clazz = TenantWorkflowConfigHolder.class.getClassLoader().loadClass(executorClassName);
                    workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
                    loadProperties(workflowElem, workFlowExecutor);
                } catch (ClassNotFoundException e1) {
                    workFlowExecutor = new ApplicationDeletionSimpleWorkflowExecutor();
                }
                workflowExecutorMap.put(WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION, workFlowExecutor);

                // API State Change
                workflowElem = workflowConfigDTO.
                        getWorkflowConfigMap().get(WorkflowConstants.API_STATE_CHANGE);
                executorClassName = getWorkflowExecutorClassName(workflowElem,
                        WorkflowConstants.WorkflowClasses.API_STATE_CHANGE_SIMPLE_FLOW_CLASS,
                        WorkflowConstants.WorkflowClasses.API_STATE_CHANGE_APPROVAL_FLOW_CLASS);

                try {
                    clazz = TenantWorkflowConfigHolder.class.getClassLoader().loadClass(executorClassName);
                    workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
                    loadProperties(workflowElem, workFlowExecutor);
                } catch (ClassNotFoundException e1) {
                    workFlowExecutor = new APIStateChangeSimpleWorkflowExecutor();
                }
                workflowExecutorMap.put(WorkflowConstants.WF_TYPE_AM_API_STATE, workFlowExecutor);

                // API Product State Change
                workflowElem = workflowConfigDTO.
                        getWorkflowConfigMap().get(WorkflowConstants.API_PRODUCT_STATE_CHANGE);
                executorClassName = getWorkflowExecutorClassName(workflowElem,
                        WorkflowConstants.WorkflowClasses.API_PRODUCT_STATE_CHANGE_SIMPLE_FLOW_CLASS,
                        WorkflowConstants.WorkflowClasses.API_PRODUCT_STATE_CHANGE_APPROVAL_FLOW_CLASS);

                try {
                    clazz = TenantWorkflowConfigHolder.class.getClassLoader().loadClass(executorClassName);
                    workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
                    loadProperties(workflowElem, workFlowExecutor);
                } catch (ClassNotFoundException e1) {
                    workFlowExecutor = new APIProductStateChangeSimpleWorkflowExecutor();
                }
                workflowExecutorMap.put(WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE, workFlowExecutor);
            }
        } catch (InstantiationException e) {
            log.error("Unable to instantiate class", e);
            handleException("Unable to instantiate class", e);
        } catch (IllegalAccessException e) {
            log.error("Illegal attempt to invoke class methods", e);
            handleException("Illegal attempt to invoke class methods", e);
        } catch (APIManagementException e) {
            log.error("Unable to retrieve workflow configurations", e);
            handleException("Unable to retrieve workflow configurations", e);
        }
    }

    private void loadProperties(WorkflowConfigDTO.WorkflowConfig workflowElem,
                                Object workflowExecutorClass) throws WorkflowException {

        // Iterate through the JSON object
        if (workflowElem != null && workflowElem.getProperties() != null) {
            for (java.util.Map.Entry<String, JsonElement> entry :
                    workflowElem.getProperties().entrySet()) {
                String propName = entry.getKey();
                JsonElement value = entry.getValue();
                if (!propName.isEmpty()) {
                    setInstanceProperty(propName, value, workflowExecutorClass);
                } else {
                    handleException("An Executor class property must specify a name.");
                }
            }
        }
    }

    /**
     * Find and invoke the setter method with the name of form setXXX passing in the value given
     * on the POJO object.
     *
     * @param name    name of the setter field
     * @param valElem value to be set
     * @param obj     POJO instance
     */
    public void setInstanceProperty(String name, JsonElement valElem, Object obj) throws WorkflowException {

        String mName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        Method method;

        try {
            Method[] methods = obj.getClass().getMethods();
            boolean invoked = false;

            for (Method method1 : methods) {
                if (mName.equals(method1.getName())) {
                    Class[] params = method1.getParameterTypes();
                    if (params.length != 1) {
                        handleException("Did not find a setter method named : " + mName +
                                "() that takes a single String, int, long, float, double ," +
                                "OMElement or boolean parameter");
                    } else if (valElem.isJsonPrimitive()) {
                        JsonElement primitive = valElem.getAsJsonPrimitive();
                        if (String.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, String.class);
                            method.invoke(obj, new String[]{primitive.getAsString()});
                        } else if (int.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, int.class);
                            method.invoke(obj, new Integer[]{primitive.getAsInt()});
                        } else if (long.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, long.class);
                            method.invoke(obj, new Long[]{primitive.getAsLong()});
                        } else if (float.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, float.class);
                            method.invoke(obj, new Float[]{primitive.getAsFloat()});
                        } else if (double.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, double.class);
                            method.invoke(obj, new Double[]{primitive.getAsDouble()});
                        } else if (boolean.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, boolean.class);
                            method.invoke(obj, new Boolean[]{primitive.getAsBoolean()});
                        } else if (Class.forName("[C").equals(params[0])) {
                            method = obj.getClass().getMethod(mName, Class.forName("[C"));
                            method.invoke(obj, primitive.getAsString().toCharArray());
                        } else if (OMElement.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, OMElement.class);
                            OMElement el = AXIOMUtil.stringToOM(primitive.getAsString());
                            method.invoke(obj, new OMElement[]{el});
                        } else {
                            continue;
                        }
                    } else if (!valElem.isJsonPrimitive() && JsonElement.class.equals(params[0])) {
                        method = obj.getClass().getMethod(mName, JsonElement.class);
                        method.invoke(obj, new JsonElement[]{valElem});
                    } else {
                        continue;
                    }
                    invoked = true;
                    break;
                }
            }
            if (!invoked) {
                handleException("Did not find a setter method named : " + mName +
                        "() that takes a single String, int, long, float, double " +
                        "or boolean parameter");
            }
        } catch (Exception e) {
            handleException("Error invoking setter method named : " + mName +
                    "() that takes a single String, int, long, float, double " +
                    "or boolean parameter", e);
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

    /**
     * Return either the simple workflow, approval flow or custom flow class name based on the config.
     *
     * @param workflowElem          the workflow config element
     * @param simpleFlowClassName   simple flow class name
     * @param approvalFlowClassName approval flow class name
     * @return class name
     */
    private String getWorkflowExecutorClassName(WorkflowConfigDTO.WorkflowConfig workflowElem,
                                                String simpleFlowClassName, String approvalFlowClassName) {

        if (workflowElem != null && workflowElem.isEnabled() && !workflowElem.getClassName().isEmpty()) {
            return workflowElem.getClassName();
        } else if (workflowElem != null && workflowElem.isEnabled() && workflowElem.getClassName().isEmpty()) {
            return approvalFlowClassName;
        } else {
            return simpleFlowClassName;
        }
    }
}
