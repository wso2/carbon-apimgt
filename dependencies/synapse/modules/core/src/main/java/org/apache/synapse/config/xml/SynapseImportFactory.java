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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.libraries.imports.SynapseImport;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

public class SynapseImportFactory {

    private static final Log log = LogFactory.getLog(SynapseImportFactory.class);

    public static final QName NAME_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "name");
    public static final QName PACKAGE_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "package");
    public static final QName STATUS_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "status");

    public static final QName ARTIFACT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
            "artifact");

    @SuppressWarnings({"UnusedDeclaration"})
    public static SynapseImport createImport(OMElement elem, Properties properties) {

        OMAttribute pkgAtt = elem.getAttribute(PACKAGE_Q);
        SynapseImport synapseImport = new SynapseImport();


        OMAttribute nameAtt = elem.getAttribute(NAME_Q);

        if (nameAtt != null) {
            synapseImport.setLibName(nameAtt.getAttributeValue());
        } else {
            handleException("Synapse Import Target Library name is not specified");
        }

        if (pkgAtt != null) {
            synapseImport.setLibPackage(pkgAtt.getAttributeValue());
        } else {
            handleException("Synapse Import Target Library package is not specified");
        }
        
        OMAttribute status = elem.getAttribute(STATUS_Q);
        
        if (status != null && status.getAttributeValue().equals("enabled")) {
            synapseImport.setStatus(true);
        } else {
            synapseImport.setStatus(false);
        }

        log.info("Successfully created Synapse Import: " + nameAtt.getAttributeValue());
        return synapseImport;
    }



    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

}
