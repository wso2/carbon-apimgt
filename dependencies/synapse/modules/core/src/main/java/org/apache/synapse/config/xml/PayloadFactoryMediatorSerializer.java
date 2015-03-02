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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.transform.Argument;
import org.apache.synapse.mediators.transform.PayloadFactoryMediator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.List;

public class PayloadFactoryMediatorSerializer extends AbstractMediatorSerializer {

    private static final String PAYLOAD_FACTORY = "payloadFactory";
    private static final String FORMAT = "format";
    private static final String ARGS = "args";
    private static final String ARG = "arg";
    private static final String VALUE = "value";
    private static final String EXPRESSION = "expression";
    private static final String EVALUATOR = "evaluator";
    private final String JSON_TYPE="json";
    private final String MEDIA_TYPE="media-type";

    private final String XML = "xml";
    private final String JSON = "json";

    private String getEvaluator(String pathType) {
        if(pathType == SynapsePath.JSON_PATH) {
            return JSON;
        } else {
            return XML;
        }
    }

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof PayloadFactoryMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
            return null;
        }

        PayloadFactoryMediator mediator = (PayloadFactoryMediator) m;

        OMElement payloadFactoryElem = fac.createOMElement(PAYLOAD_FACTORY, synNS);

        if(mediator.getType()!=null){
            payloadFactoryElem.addAttribute(fac.createOMAttribute(MEDIA_TYPE,null,mediator.getType()));
        }

        saveTracingState(payloadFactoryElem, mediator);

        if(!mediator.isFormatDynamic()){
            if (mediator.getFormat() != null) {

                try {
                    OMElement formatElem = fac.createOMElement(FORMAT, synNS);
                String type = mediator.getType();
                if(type!=null && type.contains(JSON_TYPE)) {
                     formatElem.setText(mediator.getFormat());
                } else{
                    formatElem.addChild(AXIOMUtil.stringToOM(mediator.getFormat()));
                }
                    payloadFactoryElem.addChild(formatElem);
                } catch (XMLStreamException e) {
                    handleException("Error while serializing payloadFactory mediator", e);
                }
            } else {
                handleException("Invalid payloadFactory mediator, format is required");
            }

        } else {
            // Serialize Value using ValueSerializer
            OMElement formatElem = fac.createOMElement(FORMAT, synNS);
            formatElem.addAttribute(fac.createOMAttribute(
                    "key", nullNS, mediator.getFormatKey().getKeyValue()));
                ValueSerializer keySerializer = new ValueSerializer();
                keySerializer.serializeValue(mediator.getFormatKey(), XMLConfigConstants.KEY, formatElem);
             payloadFactoryElem.addChild(formatElem);
        }

        OMElement argumentsElem = fac.createOMElement(ARGS, synNS);
        List<Argument> pathArgList = mediator.getPathArgumentList();

        if (null != pathArgList && pathArgList.size() > 0) {

            for (Argument arg : pathArgList) {
                if(arg.getExpression() == null && arg.getValue() == null) {
                    continue;
                }
                OMElement argElem = fac.createOMElement(ARG, synNS);
                if(null != arg.getExpression() && null != arg.getExpression().getPathType()) {
                    argElem.addAttribute(fac.createOMAttribute(EVALUATOR, nullNS, getEvaluator(arg.getExpression().getPathType())));
                } else if(null == arg.getExpression() && arg.getValue() != null) {
                    argElem.addAttribute(fac.createOMAttribute(VALUE, nullNS, arg.getValue()));
                } else {
                    argElem.addAttribute(fac.createOMAttribute(EVALUATOR, nullNS, getEvaluator(SynapsePath.X_PATH)));
                }
                if (null != arg.getExpression()) {
                    SynapsePathSerializer.serializePath(arg.getExpression(), argElem, EXPRESSION);

                    // We don't want the "json-eval(" prefix in PayloadFactory, for backward compatibility and since
                    // PF has evaluator attribute.
                    QName EXPR_Q = new QName(EXPRESSION);
                    String strExpr = argElem.getAttribute(EXPR_Q).getAttributeValue();
                    if (strExpr.startsWith("json-eval(")) {
                        argElem.getAttribute(EXPR_Q).setAttributeValue(strExpr.substring(10, strExpr.length()-1));
                    }
                }
                argumentsElem.addChild(argElem);
            }
        }

        payloadFactoryElem.addChild(argumentsElem);
        return payloadFactoryElem;
    }

    public String getMediatorClassName() {
        return PayloadFactoryMediator.class.getName();
    }

}
