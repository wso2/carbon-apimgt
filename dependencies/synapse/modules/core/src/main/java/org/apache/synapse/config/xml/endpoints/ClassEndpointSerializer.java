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
package org.apache.synapse.config.xml.endpoints;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.endpoints.ClassEndpoint;
import org.apache.synapse.endpoints.Endpoint;

/**
 * Serializer for classEndpoint.
 * eg:
 * <endpoint name="CustomEndpoint">
 * 	<class name="org.apache.synapse.endpoint.CustomEndpoint">
 * 		<parameter name="foo">XYZ</parameter>*
 * 	</class>
 * </endpoint>
 * 
 */
public class ClassEndpointSerializer extends EndpointSerializer {

	private static final QName PARAMETER_QNAME = new QName("parameter");	
	
	@Override
	protected OMElement serializeEndpoint(Endpoint endpoint) {
		if (!(endpoint instanceof ClassEndpoint)) {
			handleException("Invalid endpoint :" + endpoint.getName());
		}

		fac = OMAbstractFactory.getOMFactory();
		ClassEndpoint classEndpoint = (ClassEndpoint) endpoint;

		OMElement endpointElement = fac.createOMElement("endpoint",
		                                                SynapseConstants.SYNAPSE_OMNAMESPACE);

		serializeCommonAttributes(classEndpoint, endpointElement);

		OMElement clazzElement = fac.createOMElement("class", SynapseConstants.SYNAPSE_OMNAMESPACE);

		
		if (classEndpoint.getClassEndpoint() != null &&
		    classEndpoint.getClassEndpoint().getClass().getName() != null) {
			clazzElement.addAttribute(fac.createOMAttribute("name",SynapseConstants.NULL_NAMESPACE,
			                                                classEndpoint.getClassEndpoint().
			                                                getClass().getName()));
		} else {
			handleException("Invalid class endpoint. Class name is required");
		}		
		
		serializeParameters(classEndpoint, clazzElement);
		endpointElement.addChild(clazzElement);
		return endpointElement;
	}

	/**
	 * Serialize the classEndpoint parameters
	 * eg:<parameter name="foo">xyz</parameter>
	 * 
	 * @param clazzEndpoint - ClassEndpoint instance
	 * @param clazzElement - OMElement starts with <class/> tag
	 * 
	 */
	private void serializeParameters(ClassEndpoint clazzEndpoint, OMElement clazzElement) {
		
		for (Iterator<String> parameterItr = clazzEndpoint.getParameters().keySet().iterator(); 
		parameterItr.hasNext();) {
			String parameterName = (String) parameterItr.next();
			String value = clazzEndpoint.getParameters().get(parameterName);
			OMElement paramEle = fac.createOMElement(PARAMETER_QNAME, clazzElement);
			paramEle.addAttribute(fac.createOMAttribute("name", SynapseConstants.NULL_NAMESPACE, parameterName));
			paramEle.setText(value);

			clazzElement.addChild(paramEle);
		}	
	}
	
}
