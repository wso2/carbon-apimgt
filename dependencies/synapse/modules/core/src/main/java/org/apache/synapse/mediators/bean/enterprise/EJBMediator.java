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

package org.apache.synapse.mediators.bean.enterprise;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.beanstalk.enterprise.EnterpriseBeanstalk;
import org.apache.synapse.commons.beanstalk.enterprise.EnterpriseBeanstalkConstants;
import org.apache.synapse.commons.beanstalk.enterprise.EnterpriseBeanstalkManager;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.bean.BeanUtils;
import org.apache.synapse.mediators.bean.Target;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * EJB mediator calls an external Enterprise JavaBean(EJB) and stores the result in the
 * message content or in a message context property.
 * Currently, this mediator supports Stateless Session Beans and Stateful Session Beans.
 */
public class EJBMediator extends AbstractMediator implements ManagedLifecycle {

    /**
     * Name of the beanstalk to be used for this invocation.
     */
    private String beanstalkName;

    /**
     * Fully qualified name of the remote interface of the EJB.
     */
    private String className;

    /**
     * Session id of the stateful session bean, null for stateless session beans.
     */
    private Value beanId;

    /**
     * Target to store the result of the EJB method call.
     */
    private Target target;

    /**
     * JNDI name of the bean. Could be null if this bean is already cached in the beanstalk.
     */
    private String jndiName;

    /**
     * Whether or not this bean is removed from the beanstalk after the method invocation.
     */
    private boolean remove;

    /**
     * Argument list for the remote method invocation.
     */
    private List<Value> argumentList = new ArrayList<Value>();

    /**
     * Beanstalk retrieved from the Synapse environment.
     */
    private volatile EnterpriseBeanstalk beanstalk;

    /**
     * Resolved method. This is resolved by looking at the method name and the argument count.
     */
    private volatile Method method;

    /**
     *
     * @param se SynapseEnvironment to be used for initialization
     */
    public void init(SynapseEnvironment se) {

        EnterpriseBeanstalkManager beanstalkManager =
                (EnterpriseBeanstalkManager) se.getServerContextInformation().getProperty(
                        EnterpriseBeanstalkConstants.BEANSTALK_MANAGER_PROP_NAME);

        if (beanstalkManager == null) {
            throw new SynapseException("Initialization failed. BeanstalkManager not found.");
        }

        beanstalk = beanstalkManager.getBeanstalk(beanstalkName);

        if (beanstalk == null) {
            throw new SynapseException("Initialization failed. '" + beanstalkName +
                    "' beanstalk not found.");
        }
    }

    /**
     * Calls an external EJB according to the supplied semantics and attaches the result into the
     * message/message context.
     * @param synCtx The current message for mediation
     * @return true If mediation should continue
     */
    public boolean mediate(MessageContext synCtx) {

        Object ejb = beanstalk.getEnterpriseBean(
                                    className,
                                    beanId == null ? null : beanId.evaluateValue(synCtx),
                                    jndiName);

        if (ejb == null) {
            handleException("EJB was not found. class: " + className + ", bean id: " + beanId +
                    ", jndi name: " + jndiName + ".", synCtx);
        }

        Object result = null;
        try {
            result = BeanUtils.invokeInstanceMethod(ejb, method, buildArguments(synCtx));
        } catch (SynapseException e) {
            handleException("Failed to invoke method: " + method + " on EJB object of " +
                    "type: " + className, e, synCtx);
        }

        if (target != null) {
            target.insert(synCtx, result);
        }

        if (remove) {
            beanstalk.removeEnterpriseBean(className,
                                beanId == null ? null : beanId.evaluateValue(synCtx));
        }

        return true;
    }

    public void destroy() {
    }

    private Object[] buildArguments(MessageContext synCtx) {
        Object[] args = new Object[argumentList.size()];
        for (int i = 0; i < args.length; ++i) {
            args[i] = argumentList.get(i).evaluateObjectValue(synCtx);
        }
        return args;
    }

    public String getBeanstalkName() {
        return beanstalkName;
    }

    public void setBeanstalkName(String beanstalkName) {
        this.beanstalkName = beanstalkName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Value getBeanId() {
        return beanId;
    }

    public void setBeanId(Value beanId) {
        this.beanId = beanId;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public List<Value> getArgumentList() {
        return argumentList;
    }

    public void addArgument(Value argument) {
        argumentList.add(argument);
    }

}
