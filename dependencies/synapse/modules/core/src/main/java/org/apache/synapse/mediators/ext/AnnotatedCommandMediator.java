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
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.commons.util.PropertyHelper;
import org.apache.synapse.mediators.annotations.Namespaces;
import org.apache.synapse.mediators.annotations.ReadAndUpdate;
import org.apache.synapse.mediators.annotations.ReadFromMessage;
import org.apache.synapse.mediators.annotations.UpdateMessage;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 */
public class AnnotatedCommandMediator extends POJOCommandMediator {

    protected Map<Field, SynapseXPath> beforeFields;
    protected Map<Method, SynapseXPath> beforeMethods;
    protected Map<Field, SynapseXPath> afterFields;
    protected Map<Method, SynapseXPath> afterMethods;
    
    @Override
    public boolean mediate(MessageContext synCtx) {
        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : POJOCommand mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Creating a new instance of POJO class : " + getCommand().getClass());
        }

        Object commandObject = null;
        try {
            // instantiate a new command object each time
            commandObject = getCommand().newInstance();
        } catch (Exception e) {
            handleException("Error creating an instance of the POJO command class : " +
                            getCommand().getClass(), e, synCtx);
        }

        synLog.traceOrDebug("Instance created, setting static and dynamic properties");

        // then set the static/constant properties first
        for (Iterator iter = getStaticSetterProperties().keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            PropertyHelper.setInstanceProperty(name, getStaticSetterProperties().get(name),
                    commandObject);
        }
        
        
        for (Field f : beforeFields.keySet()) {
            SynapseXPath xpath = beforeFields.get(f);
            Object v;
            if (f.getType().equals(String.class)) {
                v = xpath.stringValueOf(synCtx);
            } else {
                throw new UnsupportedOperationException("non-String types not supportted yet");
            }
            try {
                f.set(commandObject, v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Method m : beforeMethods.keySet()) {
            SynapseXPath xpath = beforeMethods.get(m);
            Object v;
            if (m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(String.class)) {
                v = xpath.stringValueOf(synCtx);
            } else {
                throw new UnsupportedOperationException("non-String types not supportted yet");
            }
            try {
                m.invoke(commandObject, v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        synLog.traceOrDebug("POJO initialized successfully, invoking the execute() method");

        // then call the execute method if the Command interface is implemented
        if (commandObject instanceof Command) {
            try {
                ((Command) commandObject).execute();
            } catch (Exception e) {
                handleException("Error invoking POJO command class : "
                    + getCommand().getClass(), e, synCtx);
            }

        } else {

            Method exeMethod = null;
            try {
                exeMethod = getCommand().getMethod("execute", new Class[]{});
                exeMethod.invoke(commandObject, new Object[]{});
            } catch (NoSuchMethodException e) {
                handleException("Cannot locate an execute() method on POJO class : " +
                                getCommand().getClass(), e, synCtx);
            } catch (Exception e) {
                handleException("Error invoking the execute() method on POJO class : " +
                                getCommand().getClass(), e, synCtx);
            }
        }

        // TODO: now update the MessageContext from the commandObject
        
        synLog.traceOrDebug("End : POJOCommand mediator");
        return true;
    }
    
    @Override
    public void setCommand(Class commandClass) {
        super.setCommand(commandClass);
        introspectClass(commandClass);
    }

    /**
     * Introspect the command class annotations
     */
    protected void introspectClass(Class<?> commandClass) {

        beforeFields = new HashMap<Field, SynapseXPath>();
        afterFields = new HashMap<Field, SynapseXPath>();
        beforeMethods = new HashMap<Method, SynapseXPath>();
        afterMethods = new HashMap<Method, SynapseXPath>();

        for (Field f : commandClass.getDeclaredFields()) {

            ReadFromMessage readFromMessage = f.getAnnotation(ReadFromMessage.class);
            if (readFromMessage != null) {
                SynapseXPath axiomXpath = createSynapseXPATH(readFromMessage.value(), f.getAnnotation(Namespaces.class));
                beforeFields.put(f, axiomXpath);
            }

            UpdateMessage updateMessage = f.getAnnotation(UpdateMessage.class);
            if (updateMessage != null) {
                SynapseXPath axiomXpath = createSynapseXPATH(updateMessage.value(), f.getAnnotation(Namespaces.class));
                afterFields.put(f, axiomXpath);
            }

            ReadAndUpdate readAndUpdate = f.getAnnotation(ReadAndUpdate.class);
            if (readAndUpdate != null) {
                SynapseXPath axiomXpath = createSynapseXPATH(readAndUpdate.value(), f.getAnnotation(Namespaces.class));
                beforeFields.put(f, axiomXpath);
                afterFields.put(f, axiomXpath);
            }
        }

        for (Method m : commandClass.getDeclaredMethods()) {

            ReadFromMessage readFromMessage = m.getAnnotation(ReadFromMessage.class);
            if (readFromMessage != null) {
                SynapseXPath axiomXpath = createSynapseXPATH(readFromMessage.value(), m.getAnnotation(Namespaces.class));
                beforeMethods.put(m, axiomXpath);
            }

            UpdateMessage updateMessage = m.getAnnotation(UpdateMessage.class);
            if (updateMessage != null) {
                SynapseXPath axiomXpath = createSynapseXPATH(updateMessage.value(), m.getAnnotation(Namespaces.class));
                afterMethods.put(m, axiomXpath);
            }

        }
    }

    /**
     * Create an SynapseXPath from an xpath string
     */
    protected SynapseXPath createSynapseXPATH(String xpath, Namespaces nsAnnotation) {
        
        Map<String, String> namespaces = getNamespaces(nsAnnotation);     
        try {

            SynapseXPath axiomXPath = new SynapseXPath(xpath);

            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                axiomXPath.addNamespace(entry.getKey(), entry.getValue());
            }
            
            return axiomXPath;

        } catch (JaxenException e) {
            throw new RuntimeException("Error creating SynapseXPath: " + xpath, e);
        }
    }

    /**
     * Creates a Map of namespace prefixes and namespaces from a Namespace annotation
     * and the default Namespace annotation on the command class.
     */
    protected Map<String, String> getNamespaces(Namespaces namespaces) {
        Map<String, String> allNamespaces = new HashMap<String, String>();
        
        Namespaces defaultNamespaces = ((Class<?>)getCommand()).getAnnotation(Namespaces.class);

        // First add any default namespaces
        if (defaultNamespaces != null) {
            for (String namespaceValue : defaultNamespaces.value()) {
                int i = namespaceValue.indexOf(':');
                if (i > 0) {
                    String prefix = namespaceValue.substring(0, i);
                    String namespace = namespaceValue.substring(i+1);
                    allNamespaces.put(prefix, namespace);
                }
            }
        }

        // add any namespaces which will overwrite any previously added default namespaces
        if (namespaces != null) {
            for (String namespaceValue : namespaces.value()) {
                int i = namespaceValue.indexOf(':');
                if (i > 0) {
                    String prefix = namespaceValue.substring(0, i);
                    String namespace = namespaceValue.substring(i+1);
                    allNamespaces.put(prefix, namespace);
                }
            }
        }
        return allNamespaces;
    }

}
