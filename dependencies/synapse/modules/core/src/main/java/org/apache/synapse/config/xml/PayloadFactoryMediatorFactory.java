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
import org.apache.axiom.om.OMText;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.transform.Argument;
import org.apache.synapse.mediators.transform.PayloadFactoryMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class PayloadFactoryMediatorFactory extends AbstractMediatorFactory {

    private static final QName PAYLOAD_FACTORY_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "payloadFactory");

    private static final QName FORMAT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "format");
    private static final QName ARGS_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "args");

    private static final QName TYPE_Q = new QName("media-type");// media-type attribute in payloadFactory

    private final String JSON_TYPE="json";
    private final String XML_TYPE="xml";


    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        PayloadFactoryMediator payloadFactoryMediator = new PayloadFactoryMediator();
        String mediaTypeValue = elem.getAttributeValue(TYPE_Q);
        //for the backward compatibility.
        if(mediaTypeValue != null) {
            payloadFactoryMediator.setType(mediaTypeValue); //set the mediaType for the PF
        } else {
            payloadFactoryMediator.setType(XML_TYPE);
        }

        OMElement formatElem = elem.getFirstChildWithName(FORMAT_Q);
        if (formatElem != null) {
            OMAttribute n = formatElem.getAttribute(ATT_KEY);
            if (n == null) {
                //OMElement copy = formatElem.getFirstElement().cloneOMElement();
                OMElement copy = formatElem.cloneOMElement();
                removeIndentations(copy);

                if(mediaTypeValue != null && mediaTypeValue.contains(JSON_TYPE))  {
                    payloadFactoryMediator.setFormat(copy.getText());
                } else {
                    payloadFactoryMediator.setFormat(copy.getFirstElement().toString());
                }
            } else {
                ValueFactory keyFac = new ValueFactory();
                Value generatedKey = keyFac.createValue(XMLConfigConstants.KEY, formatElem);
                payloadFactoryMediator.setFormatKey(generatedKey);
                payloadFactoryMediator.setFormatDynamic(true);

            }
        } else {
            handleException("format element of payloadFactoryMediator is required");
        }

        OMElement argumentsElem = elem.getFirstChildWithName(ARGS_Q);

        if (argumentsElem != null) {

            Iterator itr = argumentsElem.getChildElements();

            while (itr.hasNext()) {
                OMElement argElem = (OMElement) itr.next();
                Argument arg = new Argument();
                String value;

                if ((value = argElem.getAttributeValue(ATT_VALUE)) != null) {
                    arg.setValue(value);
                    arg.setExpression(null);
                    payloadFactoryMediator.addPathArgument(arg);
                } else if ((value = argElem.getAttributeValue(ATT_EXPRN)) != null) {
                    if (value.trim().length() == 0) {
                        handleException("Attribute value for xpath cannot be empty");
                    } else {
                        try {
                            //set the evaluator
                            String evaluator = argElem.getAttributeValue(ATT_EVAL);
                            if(evaluator != null && evaluator.equals(JSON_TYPE)){
                                if(value.startsWith("json-eval(")) {
                                    value = value.substring(10, value.length()-1);
                                }
                                arg.setExpression(SynapseJsonPathFactory.getSynapseJsonPath(value));
                                // we have to explicitly define the path type since we are not going to mark
                                // JSON Path's with prefix "json-eval(".
                                arg.getExpression().setPathType(SynapsePath.JSON_PATH);
                                payloadFactoryMediator.addPathArgument(arg);
                            } else {
                                SynapseXPath sxp = SynapseXPathFactory.getSynapseXPath(argElem, ATT_EXPRN);
                                //we need to disable stream Xpath forcefully
                                sxp.setForceDisableStreamXpath(Boolean.TRUE);
                                arg.setExpression(sxp);
                                arg.getExpression().setPathType(SynapsePath.X_PATH);
                                payloadFactoryMediator.addPathArgument(arg);
                            }
                        } catch (JaxenException e) {
                            handleException("Invalid XPath expression for attribute expression : " +
                                    value, e);
                        }
                    }


                } else {
                    handleException("Unsupported arg type. value or expression attribute required");
                }
            }
        }

        return payloadFactoryMediator;
    }

    public QName getTagQName() {
        return PAYLOAD_FACTORY_Q;
    }

    private void removeIndentations(OMElement element) {
        List<OMText> removables = new ArrayList<OMText>();
        removeIndentations(element, removables);
        for (OMText node : removables) {
            node.detach();
        }
    }

    private void removeIndentations(OMElement element, List<OMText> removables) {
        Iterator children = element.getChildren();
        while (children.hasNext()) {
            Object next = children.next();
            if (next instanceof OMText) {
                OMText text = (OMText) next;
                if (text.getText().trim().equals("")) {
                    removables.add(text);
                }
            } else if (next instanceof OMElement) {
                removeIndentations((OMElement) next, removables);
            }
        }
    }

}
