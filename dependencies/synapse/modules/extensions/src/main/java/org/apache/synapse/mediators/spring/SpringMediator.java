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

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

/**
 * This mediator allows Spring beans implementing the org.apache.synapse.Mediator
 * interface to mediate messages passing through Synapse.
 *
 * A Spring mediator is instantiated by Spring (see www.springframework.org). The mediator
 * refers to a Spring bean name, and also either a Spring configuration defined to Synapse
 * or an in-lined Spring configuration.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class SpringMediator extends AbstractMediator implements ManagedLifecycle {

    /**
     * The Spring bean ref to be used
     */
    private String beanName = null;
    /**
     * The named Spring config to be used
     */
    private String configKey = null;
    /**
     * The Spring ApplicationContext to be used
     */
    private ApplicationContext appContext = null;

    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Spring mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        Entry entry = synCtx.getConfiguration().getEntryDefinition(configKey);

        // if the configKey refers to a dynamic property
        if (entry != null && entry.isDynamic()) {
            if (!entry.isCached() || entry.isExpired()) {
                buildAppContext(synCtx, synLog);
            }
        // if the property is not a DynamicProperty, we will create an ApplicationContext only once
        } else {
            if (appContext == null) {
                buildAppContext(synCtx, synLog);
            }
        }

        if (appContext != null) {

        	try{
            Object o = appContext.getBean(beanName);    
            if (o != null && Mediator.class.isAssignableFrom(o.getClass())) {
                Mediator m = (Mediator) o;
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Loaded mediator from bean : " + beanName + " executing...");
                }
                return m.mediate(synCtx);

            } else {
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Unable to load mediator from bean : " + beanName);
                }
                handleException("Could not load bean named : " + beanName +
                    " from the Spring configuration with key : " + configKey, synCtx);
            }
        	}catch (Exception e) {
        		 handleException("No bean named '"+beanName+"' is defined", synCtx);
			}
        } else {
            handleException("Cannot reference application context with key : " + configKey, synCtx);
        }

        synLog.traceOrDebug("End : Spring mediator");
        return true;
    }

    private synchronized void buildAppContext(MessageContext synCtx,
        SynapseLog synLog) {

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Creating Spring ApplicationContext from key : " + configKey);
        }
        GenericApplicationContext appContext = new GenericApplicationContext();
        XmlBeanDefinitionReader xbdr = new XmlBeanDefinitionReader(appContext);
        xbdr.setValidating(false);

        Object springConfig = synCtx.getEntry(configKey);
        if(springConfig == null) {
          String errorMessage = "Cannot look up Spring configuration " + configKey;
          log.error(errorMessage);
          //throw new SynapseException(errorMessage);
          return;
        }

        xbdr.loadBeanDefinitions(
            new InputStreamResource(
                SynapseConfigUtils.getStreamSource(springConfig).getInputStream()));
        appContext.refresh();
        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Spring ApplicationContext from key : " + configKey + " created");
        }
        this.appContext = appContext;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public ApplicationContext getAppContext() {
        return appContext;
    }

    public void setAppContext(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    public void init(SynapseEnvironment se) {
        MessageContext synCtx = se.createMessageContext();
        buildAppContext(synCtx, getLog(synCtx));
    }

    public void destroy() {
    }
}
