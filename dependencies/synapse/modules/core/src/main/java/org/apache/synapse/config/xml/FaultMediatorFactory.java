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

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.transform.FaultMediator;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Properties;

/**
 * Factory for {@link FaultMediator} instances.
 * <p>
 * Configuration syntax:
 * <pre>
 * &lt;makefault [version="soap11|soap12|pox"] [response="true|false"]&gt;
 *   &lt;code (value="literal" | expression="xpath")/&gt;
 *   &lt;reason (value="literal" | expression="xpath")/&gt;
 *   &lt;node&gt;...&lt;/node&gt;?
 *   &lt;role&gt;...&lt;/role&gt;?
 *   (&lt;detail expression="xpath"/&gt; | &lt;detail&gt;...&lt;/detail&gt;)?
 * &lt;/makefault&gt;
 * </pre>
 */
public class FaultMediatorFactory extends AbstractMediatorFactory  {

    private static final QName FAULT_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "makefault");

    private static final QName ATT_VERSION_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "version");
    private static final QName ATT_RESPONSE_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "response");
    private static final QName CODE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "code");
    private static final QName REASON_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "reason");
    private static final QName NODE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "node");
    private static final QName ROLE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "role");
    private static final QName DETAIL_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "detail");

    private static final String SOAP11 = "soap11";
    private static final String SOAP12 = "soap12";
    private static final String POX = "pox";

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        FaultMediator faultMediator = new FaultMediator();

        OMAttribute version = elem.getAttribute(ATT_VERSION_Q);
        if (version != null) {
            if (SOAP11.equals(version.getAttributeValue())) {
                faultMediator.setSoapVersion(FaultMediator.SOAP11);
            } else if (SOAP12.equals(version.getAttributeValue())) {
                faultMediator.setSoapVersion(FaultMediator.SOAP12);
            } else if (POX.equals(version.getAttributeValue())) {
                faultMediator.setSoapVersion(FaultMediator.POX);
            } else {
                handleException("Invalid SOAP version");
            }
        }else{
            //Setting the default SOAP version
            faultMediator.setSoapVersion(FaultMediator.SOAP11);
        }

        OMAttribute response = elem.getAttribute(ATT_RESPONSE_Q);
        if (response != null) {
            if ("true".equals(response.getAttributeValue())) {
                faultMediator.setMarkAsResponse(true);
            } else if ("false".equals(response.getAttributeValue())) {
                faultMediator.setMarkAsResponse(false);
            } else {
                handleException("Invalid value '" + response.getAttributeValue()
                        + "' passed as response. Expected 'true' or 'false'");
            }
            faultMediator.setSerializeResponse(true);
        }

        OMElement code = elem.getFirstChildWithName(CODE_Q);
        if (code != null) {
            OMAttribute value = code.getAttribute(ATT_VALUE);
            OMAttribute expression = code.getAttribute(ATT_EXPRN);

            if (value != null) {
                String strValue = value.getAttributeValue();
                String prefix = null;
                String name = null;
                if (strValue.indexOf(":") != -1) {
                    prefix = strValue.substring(0, strValue.indexOf(":"));
                    name = strValue.substring(strValue.indexOf(":")+1);
                } else {
                    handleException("A QName is expected for fault code as prefix:name");
                }
                String namespaceURI = OMElementUtils.getNameSpaceWithPrefix(prefix, code);
                if (namespaceURI == null) {
                    handleException("Invalid namespace prefix '" + prefix + "' in code attribute");
                }
                faultMediator.setFaultCodeValue(new QName(namespaceURI, name, prefix));
            } else if (expression != null) {
                try {
                    faultMediator.setFaultCodeExpr(
                        SynapseXPathFactory.getSynapseXPath(code, ATT_EXPRN));
                } catch (JaxenException je) {
                    handleException("Invalid fault code expression : " + je.getMessage(), je);
                }
            } else {
                handleException("A 'value' or 'expression' attribute must specify the fault code");
            }

        } else if (faultMediator.getSoapVersion() != FaultMediator.POX) {
            handleException("The fault code is a required attribute for the " +
                    "makefault mediator unless it is a pox fault");
        }

        OMElement reason = elem.getFirstChildWithName(REASON_Q);
        if (reason != null) {
            OMAttribute value = reason.getAttribute(ATT_VALUE);
            OMAttribute expression = reason.getAttribute(ATT_EXPRN);

            if (value != null) {
                faultMediator.setFaultReasonValue(value.getAttributeValue());
            } else if (expression != null) {
                try {
                    faultMediator.setFaultReasonExpr(
                        SynapseXPathFactory.getSynapseXPath(reason, ATT_EXPRN));
                } catch (JaxenException je) {
                    handleException("Invalid fault reason expression : " + je.getMessage(), je);
                }
            } else {
                handleException("A 'value' or 'expression' attribute must specify the fault code");
            }

        } else if (faultMediator.getSoapVersion() != FaultMediator.POX) {
            handleException("The fault reason is a required attribute for the " +
                    "makefault mediator unless it is a pox fault");
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(faultMediator,elem);

        OMElement node = elem.getFirstChildWithName(NODE_Q);
        if (node != null && node.getText() != null && !SOAP11.equals(version.getAttributeValue())) {
            try {
                faultMediator.setFaultNode(new URI(node.getText()));
            } catch (URISyntaxException e) {
                handleException("Invalid URI specified for fault node : " + node.getText(), e);
            }
        }

        OMElement role = elem.getFirstChildWithName(ROLE_Q);
        if (role != null && role.getText() != null) {
            try {
                faultMediator.setFaultRole(new URI(role.getText()));
            } catch (URISyntaxException e) {
                handleException("Invalid URI specified for fault role : " + role.getText(), e);
            }
        }

        OMElement detail = elem.getFirstChildWithName(DETAIL_Q);
        if (detail != null) {
            OMAttribute detailExpr = detail.getAttribute(ATT_EXPRN);
            if (detailExpr != null && detailExpr.getAttributeValue() != null) {
                try {
                    faultMediator.setFaultDetailExpr(
                            SynapseXPathFactory.getSynapseXPath(detail, ATT_EXPRN));
                } catch (JaxenException e) {
                    handleException("Unable to build the XPath for fault detail " +
                            "from the expression : " + detailExpr.getAttributeValue(), e);
                }
            } else if (detail.getChildElements().hasNext()) {
                Iterator it = detail.getChildElements();
                while (it.hasNext()) {
                    OMElement child = (OMElement) it.next();
                    if (child != null) {
                        faultMediator.addFaultDetailElement(child);
                    }
                }
            } else if (detail.getText() != null) {
                faultMediator.setFaultDetail(detail.getText());
            } else {
                // we have an empty detail element
                faultMediator.setFaultDetail("");
            }
        }

        return faultMediator;
    }

    public QName getTagQName() {
        return FAULT_Q;
    }
}
