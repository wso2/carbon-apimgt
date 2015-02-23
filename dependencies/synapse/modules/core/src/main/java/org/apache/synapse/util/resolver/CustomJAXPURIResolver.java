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

package org.apache.synapse.util.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.xml.sax.InputSource;

/**
 * Class that adapts a {@link ResourceMap} to JAXP's {@link URIResolver}.
 */
public class CustomJAXPURIResolver implements URIResolver {
    private final ResourceMap resourceMap;
    private final SynapseConfiguration synCfg;
    
    /**
     * Constructor.
     * 
     * @param resourceMap the resource map; may be null if no resource map is configured
     * @param synCfg the Synapse configuration
     */
    public CustomJAXPURIResolver(ResourceMap resourceMap, SynapseConfiguration synCfg) {
        this.resourceMap = resourceMap;
        this.synCfg = synCfg;
    }

    /**
     * Resolve an xsl:import or xsl:include.
     * This method will first attempt to resolve the location using the configured
     * {@link ResourceMap} object. If this fails (because no {@link ResourceMap} is
     * configured or because {@link ResourceMap#resolve(SynapseConfiguration, String)}
     * returns null, it will resolve the location using
     * {@link SynapseConfigUtils#resolveRelativeURI(String, String)}.
     */
    public Source resolve(String href, String base) throws TransformerException {
        Source result = null;
        if (resourceMap != null) {
            InputSource is = resourceMap.resolve(synCfg, href);
            if (is != null) {
                result = new StreamSource(is.getByteStream());
            }
        }
        if (result == null) {
            result = new StreamSource(SynapseConfigUtils.resolveRelativeURI(base, href));
        }
        return result;
    }
}
