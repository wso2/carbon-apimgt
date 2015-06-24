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

package org.apache.synapse.util.jaxp;

import java.nio.charset.Charset;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.dom.jaxp.DOOMDocumentBuilderFactory;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.w3c.dom.Document;

/**
 * {@link ResultBuilder} implementation that produces a {@link DOMResult} with
 * an empty DOOM document. It reimports the document as a normal AXIOM tree
 * using {@link ElementHelper#importOMElement(OMElement, org.apache.axiom.om.OMFactory)}.
 */
public class DOOMResultBuilder implements ResultBuilder {
    private static final Log log = LogFactory.getLog(DOOMResultBuilder.class);
    
    private Document document;

    public Result getResult() {
        try {
            document = new DOOMDocumentBuilderFactory().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            handleException("Unable to create empty DOOM document", e);
        }
        return new DOMResult(document);
    }

    public OMElement getNode(Charset charset) {
        // TODO: we need to support SOAPEnvelope
        //       (not supported by the original code in XSLTMediator)
        if (document.getDocumentElement() != null) {
            return ElementHelper.importOMElement(
                    (OMElement) document.getDocumentElement(), OMAbstractFactory.getOMFactory());
        } else {
            handleException("Cannot find the Document Element");
        }

        return null;
    }

    public void release() {
    }

    private static void handleException(String message, Throwable ex) {
        log.error(message, ex);
        throw new SynapseException(message, ex);
    }

    private static void handleException(String message) {
        log.error(message);
        throw new SynapseException(message);
    }
}
