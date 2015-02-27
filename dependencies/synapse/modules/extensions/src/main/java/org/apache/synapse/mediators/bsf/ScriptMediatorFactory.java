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
 *  KIND, either express or implied.  See // TreeMap used to keep given scripts order if needed the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.mediators.bsf;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.config.xml.ValueFactory;
import org.mozilla.javascript.Context;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.*;

/**
 * Creates an instance of a Script mediator for inline or external script mediation for BSF
 * scripting languages.
 * <p/>
 * * <pre>
 *    &lt;script [key=&quot;entry-key&quot;]
 *      [function=&quot;script-function-name&quot;] language="javascript|groovy|ruby"&gt
 *      (text | xml)?
 *      &lt;include key=&quot;entry-key&quot; /&gt;
 *    &lt;/script&gt;
 * </pre>
 * <p/>
 * The boolean response from the in-lined mediator is either the response from the evaluation of the
 * script statements or if that result is not a boolean then a response of true is assumed.
 * <p/>
 * The MessageContext passed in to the script mediator has additional methods over the Synapse
 * MessageContext to enable working with the XML in a way natural to the scripting language. For
 * example when using JavaScript get/setPayloadXML use E4X XML objects, when using Ruby they
 * use REXML documents.
 * <p/>
 * For external script mediation, that is when using key, function, language attributes,
 * &lt;include key&quot;entry-key&quot; /&gt; is used to include one or more additional script files.
 */
public class ScriptMediatorFactory extends AbstractMediatorFactory {

    private static final QName TAG_NAME
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "script");

    private static final QName INCLUDE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "include");

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        ScriptMediator mediator;
        ClassLoader  classLoader = (ClassLoader) properties.get(SynapseConstants.SYNAPSE_LIB_LOADER);
        OMAttribute keyAtt = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                "key"));
        OMAttribute langAtt = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                "language"));
        OMAttribute functionAtt = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                "function"));

        if (langAtt == null) {
            throw new SynapseException("The 'language' attribute is required for" +
                    " a script mediator");
            // TODO: couldn't this be determined from the key in some scenarios?
        }
        if (keyAtt == null && functionAtt != null) {
            throw new SynapseException("Cannot use 'function' attribute without 'key' " +
                    "attribute for a script mediator");
        }

        Map<Value, Object> includeKeysMap = getIncludeKeysMap(elem);

        if (keyAtt != null) {

            // ValueFactory for creating dynamic or static Key
            ValueFactory keyFac = new ValueFactory();
            // create dynamic or static key based on OMElement
            Value generatedKey = keyFac.createValue(XMLConfigConstants.KEY, elem);

            String functionName = (functionAtt == null ? null : functionAtt.getAttributeValue());
            mediator = new ScriptMediator(langAtt.getAttributeValue(),
                    includeKeysMap, generatedKey, functionName,classLoader);
        } else {
            mediator = new ScriptMediator(langAtt.getAttributeValue(), elem.getText(),classLoader);
        }

        processAuditStatus(mediator, elem);
        return mediator;
    }

    private Map<Value, Object> getIncludeKeysMap(OMElement elem) {
        // get <include /> scripts
        // map key = registry entry key, value = script source
        // at this time map values are null, later loaded
        // from void ScriptMediator.prepareExternalScript(MessageContext synCtx)

        // TreeMap used to keep given scripts order if needed
        Map<Value, Object> includeKeysMap = new LinkedHashMap<Value, Object>();
        Iterator itr = elem.getChildrenWithName(INCLUDE_Q);
        while (itr.hasNext()) {
            OMElement includeElem = (OMElement) itr.next();
            OMAttribute key = includeElem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                    "key"));
            // ValueFactory for creating dynamic or static Value
            ValueFactory keyFac = new ValueFactory();
            // create dynamic or static key based on OMElement
            Value generatedKey = keyFac.createValue(XMLConfigConstants.KEY, includeElem);

            if (key == null) {
                throw new SynapseException("Cannot use 'include' element without 'key'" +
                        " attribute for a script mediator");
            }

            includeKeysMap.put(generatedKey, null);
        }

        return includeKeysMap;
    }

    public QName getTagQName() {
        return TAG_NAME;
    }
}
