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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.AbstractMediatorFactory;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Creates an instance of a Spring mediator that refers to the given Spring
 * configuration and bean. Optionally, one could specify an in-lined Spring
 * configuration as opposed to a globally defined Spring configuration
 * <p/>
 * <spring bean="exampleBean1" key="string""/>
 */
@SuppressWarnings({"UnusedDeclaration"})
public class SpringMediatorFactory extends AbstractMediatorFactory {

    private static final QName TAG_NAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "spring");

    /**
     * Create a Spring mediator instance referring to the bean and configuration given
     * by the OMElement declaration
     *
     * @param elem the OMElement that specifies the Spring mediator configuration
     * @param properties
     * @return the Spring mediator instance created
     */
    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        SpringMediator sm = new SpringMediator();
        OMAttribute bean = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "bean"));
        OMAttribute key  = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));

        if (bean == null) {
            handleException("The 'bean' attribute is required for a Spring mediator definition");
        } else if (key == null) {
            handleException("A 'key' attribute is required for a Spring mediator definition");
        } else {

             // after successfully creating the mediator
             // set its common attributes such as tracing etc
            processAuditStatus(sm,elem);
            sm.setBeanName(bean.getAttributeValue());
            sm.setConfigKey(key.getAttributeValue());
            return sm;
        }
        return null;
    }

    public QName getTagQName() {
        return TAG_NAME;
    }
}
