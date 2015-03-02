/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.rest.API;

/**
 * This will serialize the SynapseImport to the xml configuration as specified bellow
 * <p/>
 * <pre>
 * &lt;import [xmlns="http://ws.apache.org/ns/synapse"] [name="string"] [package="string"]&gt;
 * </pre>
 */

public class SynapseImportSerializer {

    private static final Log log = LogFactory.getLog(SynapseImportSerializer.class);

    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();
    protected static final OMNamespace synNS = SynapseConstants.SYNAPSE_OMNAMESPACE;
    protected static final OMNamespace nullNS = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");


    /**
     * This method will implements the serialization of SynapseImport object to its configuration
     *
     * @param s the type SynapseImport which is subjected to the serialization
     * @return OMElement serialized in to xml from the given parameters
     */
    public static OMElement serializeImport(SynapseImport s) {


        if (!(s instanceof SynapseImport)) {
            handleException("Unsupported Synapse Import passed in for serialization");
        }

        SynapseImport synapseImport = (SynapseImport) s;

        OMElement importElem = fac.createOMElement("import", synNS);

        if (synapseImport.getLibName() != null) {
            importElem.addAttribute(fac.createOMAttribute(
                    "name", nullNS, s.getLibName()));
        } else {
            handleException("Invalid Synapse Import. Target Library name is required");
        }

        if (synapseImport.getLibPackage() != null) {
            importElem.addAttribute(fac.createOMAttribute(
                    "package", nullNS, s.getLibPackage()));
        } else {
            handleException("Invalid Synapse Import. Target Library package is required");
        }
        
        if(synapseImport.isStatus()){
            importElem.addAttribute(fac.createOMAttribute(
                    "status", nullNS, "enabled"));
        }else{
            importElem.addAttribute(fac.createOMAttribute(
                    "status", nullNS, "disabled"));
        }

        return importElem;

    }

    public static OMElement serializeImport(OMElement parent, SynapseImport synapseImport) {
        OMElement importElt = serializeImport(synapseImport);
        if (parent != null) {
            parent.addChild(importElt);
        }
        return importElt;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }


}
