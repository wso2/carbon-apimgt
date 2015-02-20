/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.synapse.util.resolver;

import javax.wsdl.xml.WSDLLocator;

import org.apache.synapse.config.SynapseConfiguration;
import org.xml.sax.InputSource;

/**
 * This interface lets user to write his/her own WSDLResolver rather using {@link CustomWSDLLocator} .
 * Here using WSDLKey, user can perform his/her own mapping between Relativelocation 
 * and Registrypath. User needs to provide a synapse.property call,"synapse.wsdl.resolver"
 * pointing to the implementation.
 */
public interface UserDefinedWSDLLocator extends WSDLLocator {

	/**
	 * Initiate the UserDefinedWSDLLocator with the required parameters
	 * 
	 * @param baseInputSource InputSource "pointed at" the base document
	 * @param baseURI URI representing the location of the base document.
	 * @param resourceMap {@link ResourceMap} object
	 * @param synCfg Synapseconfiguration
	 * @param wsdlKey The registry key of the wsdl file
	 */
	void init(InputSource baseInputSource, String baseURI, ResourceMap resourceMap,
	          SynapseConfiguration synCfg, String wsdlKey);
}
