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
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.builtin.ValidateMediator;
import org.jaxen.JaxenException;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Factory for {@link ValidateMediator} instances.
 * <p>
 * Configuration syntax:
 * <pre>
 * &lt;validate [source="xpath"]>
 *   &lt;schema key="string">+
 *   &lt;resource location="&lt;external-schema>" key="string">+
 *   &lt;feature name="&lt;validation-feature-name>" value="true|false"/>
 *   &lt;on-fail>
 *     mediator+
 *   &lt;/on-fail>
 * &lt;/validate>
 * </pre>
 */
public class ValidateMediatorFactory extends AbstractListMediatorFactory {

    private static final QName VALIDATE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "validate");
    private static final QName ON_FAIL_Q  = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "on-fail");
    private static final QName SCHEMA_Q   = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "schema");

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        ValidateMediator validateMediator = new ValidateMediator();

        // process schema element definitions and create DynamicProperties
        List<Value> schemaKeys = new ArrayList<Value>();
        Iterator schemas = elem.getChildrenWithName(SCHEMA_Q);

        while (schemas.hasNext()) {
            Object o = schemas.next();
            if (o instanceof OMElement) {
                OMElement omElem = (OMElement) o;
                OMAttribute keyAtt = omElem.getAttribute(ATT_KEY);
                if (keyAtt != null) {
                    // ValueFactory for creating dynamic or static Value
                    ValueFactory keyFac = new ValueFactory();
                    // create dynamic or static key based on OMElement
                    Value generatedKey = keyFac.createValue(XMLConfigConstants.KEY, omElem);
                    schemaKeys.add(generatedKey);
                } else {
                    handleException("A 'schema' definition must contain a local property 'key'");
                }
            } else {
                handleException("Invalid 'schema' declaration for validate mediator");
            }
        }

        if (schemaKeys.size() == 0) {
            handleException("No schema specified for the validate mediator");
        } else {
            validateMediator.setSchemaKeys(schemaKeys);
        }

        // process source XPath attribute if present
        OMAttribute attSource = elem.getAttribute(ATT_SOURCE);

        if (attSource != null) {
            try {
                validateMediator.setSource(SynapseXPathFactory.getSynapseXPath(elem, ATT_SOURCE));
            } catch (JaxenException e) {
                handleException("Invalid XPath expression specified for attribute 'source'", e);
            }
        }

        //process external schema resources
        validateMediator.setResourceMap(ResourceMapFactory.createResourceMap(elem));

        // process on-fail
        OMElement onFail = null;
        Iterator iterator = elem.getChildrenWithName(ON_FAIL_Q);
        if (iterator.hasNext()) {
            onFail = (OMElement)iterator.next();
        }

        if (onFail != null && onFail.getChildElements().hasNext()) {
            addChildren(onFail, validateMediator, properties);
        } else {
            handleException("A non-empty <on-fail> child element is required for " +
                "the <validate> mediator");
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(validateMediator,elem);
        // set the features
        for (Map.Entry<String,String> entry : collectNameValuePairs(elem, FEATURE_Q).entrySet()) {
            String value = entry.getValue();
            boolean isFeatureEnabled;
            if ("true".equals(value)) {
                isFeatureEnabled = true;
            } else if ("false".equals(value)) {
                isFeatureEnabled = false;
            } else {
                handleException("The feature must have value true or false");
                break;
            }
            try {
                validateMediator.addFeature(entry.getKey(), isFeatureEnabled);
            } catch (SAXException e) {
                handleException("Error setting validation feature : " + entry.getKey()
                        + " to : " + value, e);
            }
        }
        return validateMediator;
    }

    public QName getTagQName() {
        return VALIDATE_Q;
    }
}
