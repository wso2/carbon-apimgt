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

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.JavaUtils;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.eip.Target;
import org.apache.synapse.mediators.eip.splitter.CloneMediator;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

/**
 * The &lt;clone&gt; element is used to copy messages in Synapse to similar messages but with
 * different message contexts and mediated using the specified targets
 *
 * <pre>
 * &lt;clone [continueParent=(true | false)]&gt;
 *   &lt;target [to="uri"] [soapAction="qname"] [sequence="sequence_ref"]
 *          [endpoint="endpoint_ref"]&gt;
 *     &lt;sequence&gt;
 *       (mediator)+
 *     &lt;/sequence&gt;?
 *     &lt;endpoint&gt;
 *       endpoint
 *     &lt;/endpoint&gt;?
 *   &lt;/target&gt;+
 * &lt;/clone&gt;
 * </pre>
 */
public class CloneMediatorFactory extends AbstractMediatorFactory {

    /**
     * This will hold the QName of the clone mediator element in the xml configuration
     */
    private static final QName CLONE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "clone");
    private static final QName ATT_CONTINUE_PARENT = new QName("continueParent");
    private static final QName TARGET_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "target");

    private static final QName ID_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "id");
    private static final QName SEQUENTIAL_Q = new QName("sequential");

    /**
     * This method implements the createMediator method of the MediatorFactory interface
     * 
     * @param elem - OMElement describing the element which will be parsed
     *  to build the CloneMediator
     * @param properties
     * @return Mediator of the type CloneMediator built from the config element
     */
    public Mediator createSpecificMediator(OMElement elem, Properties properties) {
    	
    	boolean asynchronousExe = true;
        
    	CloneMediator mediator = new CloneMediator();
        processAuditStatus(mediator, elem);

        OMAttribute id = elem.getAttribute(ID_Q);
        if (id != null) {
            mediator.setId(id.getAttributeValue());
        }


        OMAttribute continueParent = elem.getAttribute(ATT_CONTINUE_PARENT);
        if (continueParent != null) {
            mediator.setContinueParent(JavaUtils.isTrueExplicitly(
                    continueParent.getAttributeValue()));
        }

        OMAttribute synchronousExeAttr= elem.getAttribute(SEQUENTIAL_Q);
        if (synchronousExeAttr != null && synchronousExeAttr.getAttributeValue().equals("true")) {
        	asynchronousExe = false;
        }

        mediator.setSequential(!asynchronousExe);
        
        Iterator targetElements = elem.getChildrenWithName(TARGET_Q);
        while (targetElements.hasNext()) {
        	Target target = TargetFactory.createTarget((OMElement)targetElements.next(), properties);
        	target.setAsynchronous(asynchronousExe);
            mediator.addTarget(target);
        }
    
        return mediator;
    }

    /**
     * This method will implement the getTagQName method of the MediatorFactory interface
     *
     * @return QName of the clone element in xml configuration
     */
    public QName getTagQName() {
        return CLONE_Q;
    }
}
