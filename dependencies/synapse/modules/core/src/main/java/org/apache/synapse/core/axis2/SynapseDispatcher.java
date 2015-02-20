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

package org.apache.synapse.core.axis2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.SynapseConstants;

import javax.xml.namespace.QName;

/**
 * This is the Axis2 Dispatcher which is registered with the Axis2 engine. It dispatches
 * each and every message received to the SynapseMessageReceiver for processing.
 */
public class SynapseDispatcher extends AbstractDispatcher {

    public static final String NAME = "SynapseDispatcher";

    public void initDispatcher() {
        QName qn = new QName("http://synapse.apache.org", NAME);
        HandlerDescription hd = new HandlerDescription(qn.getLocalPart());
        super.init(hd);
    }

    public AxisService findService(MessageContext mc) throws AxisFault {
        AxisConfiguration ac = mc.getConfigurationContext().getAxisConfiguration();
        return ac.getService(SynapseConstants.SYNAPSE_SERVICE_NAME);
    }

    public AxisOperation findOperation(AxisService svc, MessageContext mc) throws AxisFault {
    	AxisOperation operation =  svc.getOperation(SynapseConstants.SYNAPSE_OPERATION_NAME);
    	if(operation == null && mc.getAxisService() != null){
    		operation = processOperationValidation(svc);
    	}
    	return operation;
    }
    
	private AxisOperation processOperationValidation(AxisService svc) {
		Object operationObj = svc
				.getParameterValue("_default_mediate_operation_");
		if (operationObj != null) {
			return (AxisOperation) operationObj;
		}
		return null;
	}
    
    
   
}
