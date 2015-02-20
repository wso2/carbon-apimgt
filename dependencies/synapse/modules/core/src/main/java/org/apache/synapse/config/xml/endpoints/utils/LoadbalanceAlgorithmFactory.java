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

package org.apache.synapse.config.xml.endpoints.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.algorithms.LoadbalanceAlgorithm;
import org.apache.synapse.endpoints.algorithms.RoundRobin;
import org.apache.axis2.clustering.Member;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Factory of all load balance algorithms. ESBSendMediatorFactroy will use this to create the
 * appropriate algorithm implementation.
 */
public class LoadbalanceAlgorithmFactory {

    private static Log log = LogFactory.getLog(LoadbalanceAlgorithmFactory.class);

    public static LoadbalanceAlgorithm createLoadbalanceAlgorithm(OMElement loadbalanceElement, List endpoints) {

        //default algorithm is round robin
        LoadbalanceAlgorithm algorithm = new RoundRobin(endpoints);

        OMAttribute policyAttribute = loadbalanceElement.getAttribute(new QName(null,
                XMLConfigConstants.LOADBALANCE_POLICY));
        OMAttribute algoAttribute = loadbalanceElement.getAttribute(new QName(null,
                XMLConfigConstants.LOADBALANCE_ALGORITHM));

        if (policyAttribute != null && algoAttribute != null) {
            String msg = "You cannot specify both the 'policy' & 'algorithm' in the configuration. " +
                         "It is sufficient to provide only the 'algorithm'.";
            log.fatal(msg); // We cannot continue execution. Hence it is logged at fatal level
            throw new SynapseException(msg);
        }

        if (algoAttribute != null) {
            String algorithmStr = algoAttribute.getAttributeValue().trim();
            try {
                algorithm = (LoadbalanceAlgorithm) Class.forName(algorithmStr).newInstance();
                algorithm.setEndpoints(endpoints);
            } catch (Exception e) {
                String msg = "Cannot instantiate LoadbalanceAlgorithm implementation class " +
                             algorithmStr;
                log.fatal(msg, e); // We cannot continue execution. Hence it is logged at fatal level
                throw new SynapseException(msg, e);
            }

        } else if (policyAttribute != null) {
            //currently only the roundRobin policy is supported
            if (!policyAttribute.getAttributeValue().trim().equals("roundRobin")) {
                String msg = "Unsupported algorithm " + policyAttribute.getAttributeValue().trim() +
                             " specified. Please use the 'algorithm' attribute to specify the " +
                             "correct loadbalance algorithm implementation.";
                log.fatal(msg); // We cannot continue execution. Hence it is logged at fatal level
                throw new SynapseException(msg);
            }
        }
        
        return algorithm;
    }

    public static LoadbalanceAlgorithm createLoadbalanceAlgorithm2(OMElement loadbalanceElement,
                                                                   List<Member> members) {
        LoadbalanceAlgorithm algorithm = createLoadbalanceAlgorithm(loadbalanceElement, null);
        algorithm.setApplicationMembers(members);
        return algorithm;
    }
}
