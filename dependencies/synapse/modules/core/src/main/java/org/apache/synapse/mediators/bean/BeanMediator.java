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

package org.apache.synapse.mediators.bean;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Map;

/**
 * Bean mediator can manipulate a JavaBean that is bound to the Synapse message context as a
 * property.
 * This mediator can be used to create a new bean (CREATE action), remove an existing bean
 * (REMOVE action), set a property of an existing JavaBean (SET_PROPERTY action) or to retrieve a
 * property of an existing JavaBean (GET_PROPERTY) action.
 */
public class BeanMediator extends AbstractMediator {

    /**
     * Action performed by this mediator.
     */
    private Action action;

    /**
     * Variable name. This corresponds to the property name using which the bean is attached to
     * the message context
     */
    private String varName;

    /**
     * Name of the bean property.
     */
    private String propertyName;

    /**
     * Value for SET_PROPERTY action
     */
    private Value value;

    /**
     * Target for GET_PROPERTY action
     */
    private Target target;

    /**
     * Whether or not the existing bean is replaced by the CREATE action.
     */
    private boolean replace = true;

    /**
     * Class object representing the class of the bean
     */
    private Class clazz;




    /**
     * Manipulates a JavaBean attached to the current message context according to the supplied
     * semantics.
     * @param synCtx The current message for mediation
     * @return true If mediation should continue
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Bean mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        boolean output = false;

        switch (action) {
            case CREATE:
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Creating a new bean of type '" + clazz.getName() +
                            "' with var name '" + varName + "'.");
                }
                output = mediateCreateBeanAction(synCtx);
                break;
            case REMOVE:
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Removing the bean with var name '" + varName + "'.");
                }
                output = mediateRemoveBeanAction(synCtx);
                break;
            case SET_PROPERTY:
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Setting the '" + propertyName + "' property of the bean " +
                            "with var name '" + varName + "'.");
                }
                output = mediateSetPropertyAction(synCtx);
                break;
            case GET_PROPERTY:
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Retrieving the '" + propertyName + "' property of the " +
                            "bean with var name '" + varName + "'.");
                }
                output = mediateGetPropertyAction(synCtx);
                break;
            default:
                assert false;
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : Bean mediator");
        }
        return output;
    }

    /**
     * Creates a new bean and attaches it to the current message context.
     * @param synCtx The current message for mediation
     * @return true If mediation should continue
     */
    private boolean mediateCreateBeanAction(MessageContext synCtx) {

        if (!replace && synCtx.getProperty(varName) != null) {
            return true;
        }

        Object instance = null;
        try {
            instance = clazz.newInstance();
        } catch (Exception ex) {
            handleException("An error occurred while instantiating '" + clazz.getName() +
                    "' class.", ex, synCtx);
        }

        synCtx.setProperty(varName, instance);

        return true;
    }

    /**
     * Removes a bean attached to the current message context.
     * @param synCtx The current message for mediation
     * @return true If mediation should continue
     */
    private boolean mediateRemoveBeanAction(MessageContext synCtx) {

        synCtx.getPropertyKeySet().remove(varName);
        return true;
    }

    /**
     * Sets a property of a bean attached to the current message context.
     * @param synCtx The current message for mediation
     * @return true If mediation should continue
     */
    private boolean mediateSetPropertyAction(MessageContext synCtx) {

        Object bean = synCtx.getProperty(varName);
        if (bean == null) {
            handleException("Bean with var name '" + varName + "' was not found.", synCtx);
            return false;
        }
        Object valueObj = value.evaluateObjectValue(synCtx);

        if (bean instanceof Map) {
            ((Map) bean).put(propertyName, valueObj);
        } else {
            try {
                BeanUtils.invokeInstanceMethod(
                        bean,
                        new PropertyDescriptor(propertyName, bean.getClass()).getWriteMethod(),
                        new Object[]{valueObj}
                );
            } catch (IntrospectionException e) {
                handleException("Could not resolve the setter method for '" + propertyName +
                        "' property in '" + bean.getClass() + "'.", e, synCtx);
            } catch (SynapseException e) {
                handleException("Error while invoking the setter method for '" + propertyName +
                        "' property on '" + bean.getClass() + "'.", e, synCtx);
            }
        }

        return true;
    }

    /**
     * Retrieves a property of a bean attached to the current message context.
     * @param synCtx The current message for mediation
     * @return true If mediation should continue
     */
    private boolean mediateGetPropertyAction(MessageContext synCtx) {

        Object bean = synCtx.getProperty(varName);
        if (bean == null) {
            handleException("Bean with var name '" + varName + "' was not found.", synCtx);
            return false;
        }
        Object value = null;

        if (bean instanceof Map) {
            value = ((Map) bean).get(propertyName);
        } else {
            try {
                value = BeanUtils.invokeInstanceMethod(
                            bean,
                            new PropertyDescriptor(propertyName, bean.getClass()).getReadMethod(),
                            new Object[0]
                        );
            } catch (IntrospectionException e) {
                handleException("Could not resolve the getter method for '" + propertyName +
                        "' property in '" + bean.getClass() + "'.", e, synCtx);
            } catch (SynapseException e) {
                handleException("Error while invoking the getter method for '" + propertyName +
                        "' property on '" + bean.getClass() + "'.", e, synCtx);
            }
        }

        try {
            target.insert(synCtx, value);
        } catch (SynapseException e) {
            handleException("Failed to set the target after retrieving the bean property.", e,
                    synCtx);
        }
        return true;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    /**
     * Enum representing the action performed by the Bean mediator.
     */
    public enum Action {
        CREATE, REMOVE, SET_PROPERTY, GET_PROPERTY
    }

}
