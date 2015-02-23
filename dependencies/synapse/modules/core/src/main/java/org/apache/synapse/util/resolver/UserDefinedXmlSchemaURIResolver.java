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

import java.util.List;

import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.mediators.Value;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * This interface lets user to write his/her own XmlSchemaURIResolver rather
 * using {@link CustomXmlSchemaURIResolver} .
 * Here using WSDLKey/schemaKey, user can perform his/her own mapping between Relativelocation 
 * and Registrypath . User needs to provide a synapse.property call,"synapse.schema.resolver="
 * pointing to the implementation.
 */

public interface UserDefinedXmlSchemaURIResolver extends URIResolver, LSResourceResolver {

	/**
	 * Initiate the UserDefinedXmlSchemaURIResolver with the required parameters
	 * 
	 * @param resourceMap {@link ResourceMap} object
	 * @param synCfg Synapseconfiguration
	 * @param wsdlKey The registry key of the wsdl file
	 */
	void init(ResourceMap resourceMap, SynapseConfiguration synCfg, String wsdlKey);

	/**
	 * This will used by Validate mediator to resolve external schema references
	 * defined in Validate mediator configuration
	 * using
	 *
	 * <pre>
	 * &lt;resource location="location" key="key"/&gt;
	 * </pre>
	 *
	 * inside Validate mediator configuration.
	 *
	 * @param resourceMap
	 *            {@link ResourceMap} object
	 * @param synCfg
	 *            Synapseconfiguration
	 * @param schemaRegKey
	 *            , List of base schemas' registryKeys
	 */
	void init(ResourceMap resourceMap, SynapseConfiguration synCfg, List<Value> schemaRegKey);
}
