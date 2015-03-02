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

import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.xml.sax.InputSource;

import javax.wsdl.xml.WSDLLocator;

/**
 * Class that adapts a {@link ResourceMap} object to {@link WSDLLocator}.
 */
public class CustomWSDLLocator implements WSDLLocator {
    private final InputSource baseInputSource;
    private final String baseURI;
    private ResourceMap resourceMap;
    private SynapseConfiguration synCfg;

    private String latestImportURI;

    public CustomWSDLLocator(InputSource baseInputSource,
                                  String baseURI) {
        this.baseInputSource = baseInputSource;
        this.baseURI = baseURI;
    }

    public CustomWSDLLocator(InputSource baseInputSource,
                                  String baseURI,
                                  ResourceMap resourceMap,
                                  SynapseConfiguration synCfg) {
        this(baseInputSource, baseURI);
        this.resourceMap = resourceMap;
        this.synCfg = synCfg;
    }

    public InputSource getBaseInputSource() {
        return baseInputSource;
    }

    public String getBaseURI() {
        return baseURI;
    }

    /**
     * Resolve a schema or WSDL import.
     * This method will first attempt to resolve the location using the configured
     * {@link ResourceMap} object. If this fails (because no {@link ResourceMap} is
     * configured or because {@link ResourceMap#resolve(SynapseConfiguration, String)}
     * returns null, it will resolve the location using
     * {@link SynapseConfigUtils#resolveRelativeURI(String, String)}.
     */
    public InputSource getImportInputSource(String parentLocation, String relativeLocation) {
        InputSource result = null;
        if (resourceMap != null) {
            result = resourceMap.resolve(synCfg, relativeLocation);
        }
        if (result == null) {
            String location = SynapseConfigUtils.resolveRelativeURI(parentLocation, relativeLocation);
            result = new InputSource(location);
            latestImportURI = location;
        } else {
            latestImportURI = relativeLocation;
        }
        return result;
    }

    public String getLatestImportURI() {
        return latestImportURI;
    }

    public void close() {
    }
}
