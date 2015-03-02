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
package org.apache.synapse.mediators.bsf;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.Value;

import javax.xml.stream.XMLStreamConstants;
import java.util.Map;

/**
 * Serializer for a script mediator
 *
 * @see org.apache.synapse.mediators.bsf.ScriptMediatorFactory
 */
public class ScriptMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof ScriptMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        ScriptMediator scriptMediator = (ScriptMediator) m;
        OMElement script = fac.createOMElement("script", synNS);

        String language = scriptMediator.getLanguage();
        Value key = scriptMediator.getKey();
        String function = scriptMediator.getFunction();
        ValueSerializer keySerializer = new ValueSerializer();

        if (key != null) {
            script.addAttribute(fac.createOMAttribute("language", nullNS, language));

            // Serialize Value using ValueSerializer
            keySerializer.serializeValue(key, XMLConfigConstants.KEY, script);

            if (!function.equals("mediate")) {
                script.addAttribute(fac.createOMAttribute("function", nullNS, function));
            }
        } else {
            script.addAttribute(fac.createOMAttribute("language", nullNS, language));
            OMTextImpl textData = (OMTextImpl) fac.createOMText(
                    scriptMediator.getScriptSrc().trim());
            textData.setType(XMLStreamConstants.CDATA);
            script.addChild(textData);
        }

        Map<Value, Object> includeMap = scriptMediator.getIncludeMap();
        for (Value includeKey : includeMap.keySet()) {
            if (includeKey != null) {
                OMElement includeKeyElement = fac.createOMElement("include", synNS);

                // Serialize Value using ValueSerializer
                keySerializer.serializeValue(includeKey, XMLConfigConstants.KEY, includeKeyElement);

                script.addChild(includeKeyElement);
            }
        }

        saveTracingState(script, scriptMediator);
        return script;
    }

    public String getMediatorClassName() {
        return ScriptMediator.class.getName();
    }
}
