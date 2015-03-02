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

package org.apache.synapse.mediators.transform;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.http.protocol.HTTP;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.AXIOMUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayloadFactoryMediator extends AbstractMediator {
    private Value formatKey = null;
    private boolean isFormatDynamic = false;
    private String formatRaw;
    private String mediaType = XML_TYPE;
    private final static String JSON_CONTENT_TYPE = "application/json";
    private final static String XML_CONTENT_TYPE  = "application/xml";
    private final static String JSON_TYPE = "json";
    private final static String XML_TYPE = "xml";
    private final static String STRING_TYPE = "str";

    private List<Argument> pathArgumentList = new ArrayList<Argument>();
    private Pattern pattern = Pattern.compile("\\$(\\d)+");

    private static final Log log = LogFactory.getLog(PayloadFactoryMediator.class);

    /**
     * Contains 2 paths - one when JSON Streaming is in use (mediateJsonStreamPayload) and the other for regular
     * builders (mediatePayload).
     * @param synCtx the current message for mediation
     * @return
     */
    public boolean mediate(MessageContext synCtx) {
        String format = formatRaw;
        return mediate(synCtx, format);
    }

    /**
     * Sets the content type based on the request content type and payload factory media type. This should be called
     * at the end before returning from the mediate() function.
     * @param synCtx
     */
    private void setContentType(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext a2mc = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        if(mediaType.equals(XML_TYPE)) {
                a2mc.setProperty(Constants.Configuration.MESSAGE_TYPE, XML_CONTENT_TYPE);
                a2mc.setProperty(Constants.Configuration.CONTENT_TYPE, XML_CONTENT_TYPE);
                handleSpecialProperties(XML_CONTENT_TYPE, a2mc);
        } else if(mediaType.equals(JSON_TYPE)) {
            a2mc.setProperty(Constants.Configuration.MESSAGE_TYPE, JSON_CONTENT_TYPE);
            a2mc.setProperty(Constants.Configuration.CONTENT_TYPE, JSON_CONTENT_TYPE);
            handleSpecialProperties(JSON_CONTENT_TYPE, a2mc);
        }
    }

    // This is copied from PropertyMediator, required to change Content-Type
    private void handleSpecialProperties(Object resultValue,
                                         org.apache.axis2.context.MessageContext axis2MessageCtx) {
        axis2MessageCtx.setProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE, resultValue);
        Object o = axis2MessageCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Map headers = (Map) o;
        if (headers != null) {
            headers.remove(HTTP.CONTENT_TYPE);
            headers.put(HTTP.CONTENT_TYPE, resultValue);
        }
    }

    private boolean mediate(MessageContext synCtx, String format) {
        if (!isDoingXml(synCtx) && !isDoingJson(synCtx)) {
            log.error("#mediate. Could not identify the payload format of the existing payload prior to mediate.");
            return false;
        }
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        StringBuffer result = new StringBuffer();
        regexTransform(result, synCtx, format);
        String out = result.toString().trim();
        if (log.isDebugEnabled()) {
            log.debug("#mediate. Transformed payload format>>> " + out);
        }
        if (mediaType.equals(XML_TYPE)) {
            try {
                JsonUtil.removeJsonPayload(axis2MessageContext);
                OMElement omXML = AXIOMUtil.stringToOM(out);
                if (!checkAndReplaceEnvelop(omXML, synCtx)) { // check if the target of the PF 'format' is the entire SOAP envelop, not just the body.
                    axis2MessageContext.getEnvelope().getBody().addChild(omXML.getFirstElement());
                }
            } catch (XMLStreamException e) {
                handleException("Error creating SOAP Envelope from source " + out, synCtx);
            }
        } else {
            JsonUtil.newJsonPayload(axis2MessageContext, out, true, true);
        }
        return true;
    }

    /**
     * Calls the replace function. isFormatDynamic check is used to remove indentations which come from registry based
     * configurations.
     * @param result
     * @param synCtx
     * @param format
     */
    private void regexTransform(StringBuffer result, MessageContext synCtx, String format) {
        if (isFormatDynamic()) {
            String key = formatKey.evaluateValue(synCtx);
            Object entry = synCtx.getEntry(key);
            String text = "";
            if (entry instanceof OMElement) {
                OMElement e = (OMElement) entry;
                removeIndentations(e);
                text = e.toString();
            } else if (entry instanceof OMText) {
                text =  ((OMText) entry).getText();
            } else if (entry instanceof String) {
                text = (String) entry;
            }
            replace(text, result, synCtx);
        } else {
            replace(format, result, synCtx);
        }
    }

    /**
     * Replaces the payload format with SynapsePath arguments which are evaluated using getArgValues().
     *
     * @param format
     * @param result
     * @param synCtx
     */
    private void replace(String format, StringBuffer result, MessageContext synCtx) {
        HashMap<String, String>[] argValues = getArgValues(synCtx);
        HashMap<String, String> replacement;
        Map.Entry<String, String> replacementEntry;
        String replacementValue = null;
        Matcher matcher;

        if (mediaType != null && mediaType.equals(JSON_TYPE)) {
            matcher = pattern.matcher(format);
        } else {
            matcher = pattern.matcher("<pfPadding>" + format + "</pfPadding>");
        }
        try {
            while (matcher.find()) {
                String matchSeq = matcher.group();
                int argIndex;
                try {
                    argIndex = Integer.parseInt(matchSeq.substring(1, matchSeq.length()));
                } catch (NumberFormatException e) {
                    argIndex = Integer.parseInt(matchSeq.substring(2, matchSeq.length()-1));
                }
                replacement = argValues[argIndex-1];
                replacementEntry =  replacement.entrySet().iterator().next();
                if(mediaType.equals(JSON_TYPE) && inferReplacementType(replacementEntry).equals(XML_TYPE)) {
                    // XML to JSON conversion here
                    try {
                        replacementValue = "<jsonObject>" + replacementEntry.getKey() + "</jsonObject>";
                        OMElement omXML = AXIOMUtil.stringToOM(replacementValue);
                        replacementValue = JsonUtil.toJsonString(omXML).toString();
                    } catch (XMLStreamException e) {
                        handleException("Error parsing XML for JSON conversion, please check your xPath expressions return valid XML: ", synCtx);
                    } catch (AxisFault e) {
                        handleException("Error converting XML to JSON", synCtx);
                    }
                } else if(mediaType.equals(XML_TYPE) && inferReplacementType(replacementEntry).equals(JSON_TYPE)) {
                    // JSON to XML conversion here
                    try {
                        OMElement omXML = JsonUtil.toXml(IOUtils.toInputStream(replacementEntry.getKey()), false);
                        if (JsonUtil.isAJsonPayloadElement(omXML)) { // remove <jsonObject/> from result.
                            Iterator children = omXML.getChildElements();
                            String childrenStr = "";
                            while (children.hasNext()) {
                                childrenStr += (children.next()).toString().trim();
                            }
                            replacementValue = childrenStr;
                        } else { ///~
                            replacementValue = omXML.toString();
                        }
                        //replacementValue = omXML.toString();
                    } catch (AxisFault e) {
                        handleException("Error converting JSON to XML, please check your JSON Path expressions return valid JSON: ", synCtx);
                    }
                } else {
                    // No conversion required, as path evaluates to regular String.
                    replacementValue = replacementEntry.getKey();
                }
                matcher.appendReplacement(result, replacementValue);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("#replace. Mis-match detected between number of formatters and arguments", e);
        }
        matcher.appendTail(result);
    }

    /**
     * Helper function that takes a Map of String, String where key contains the value of an evaluated SynapsePath
     * expression and value contains the type of SynapsePath in use.
     *
     * It returns the type of conversion required (XML | JSON | String) based on the actual returned value and the path
     * type.
     *
     * @param entry
     * @return
     */
    private String inferReplacementType(Map.Entry<String, String> entry) {
        if(entry.getValue().equals(SynapsePath.X_PATH) && isXML(entry.getKey())) {
            return XML_TYPE;
        } else if(entry.getValue().equals(SynapsePath.X_PATH) && !isXML(entry.getKey())) {
            return STRING_TYPE;
        } else if(entry.getValue().equals(SynapsePath.JSON_PATH) && isJson(entry.getKey())) {
            return JSON_TYPE;
        } else if(entry.getValue().equals(SynapsePath.JSON_PATH) && !isJson((entry.getKey()))) {
            return STRING_TYPE;
        } else {
            return STRING_TYPE;
        }
    }

    private boolean checkAndReplaceEnvelop(OMElement resultElement, MessageContext synCtx) {
        OMElement firstChild = resultElement.getFirstElement();
        QName resultQName = firstChild.getQName();
        if (resultQName.getLocalPart().equals("Envelope") && (
                resultQName.getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI) ||
                        resultQName.getNamespaceURI().
                                equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))) {
            SOAPEnvelope soapEnvelope = AXIOMUtils.getSOAPEnvFromOM(resultElement.getFirstElement());
            if (soapEnvelope != null) {
                try {
                    synCtx.setEnvelope(soapEnvelope);
                } catch (AxisFault axisFault) {
                    handleException("Unable to attach SOAPEnvelope", axisFault, synCtx);
                }
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Helper function that returns true if value passed is of JSON type.
     * @param value
     * @return
     */
    private boolean isJson(String value) {
        return !(value == null || value.isEmpty()) && (value.trim().charAt(0) == '{' || value.trim().charAt(0) == '[');
    }

    /**
     * Helper function to remove indentations.
     * @param element
     */
    private void removeIndentations(OMElement element) {
        List<OMText> removables = new ArrayList<OMText>();
        removeIndentations(element, removables);
        for (OMText node : removables) {
            node.detach();
        }
    }

    /**
     * Helper function to remove indentations.
     * @param element
     * @param removables
     */
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

    /**
     * Goes through SynapsePath argument list, evaluating each by calling stringValueOf and returns a HashMap String, String
     * array where each item will contain a hash map with key "evaluated expression" and value "SynapsePath type".
     * @param synCtx
     * @return
     */
    private HashMap<String, String>[] getArgValues(MessageContext synCtx) {
        HashMap<String, String>[] argValues = new HashMap[pathArgumentList.size()];
        HashMap<String, String> valueMap;
        String value = "";
        for (int i = 0; i < pathArgumentList.size(); ++i) {       /*ToDo use foreach*/
            Argument arg = pathArgumentList.get(i);
            if (arg.getValue() != null) {
                value = arg.getValue();
                if (!isXML(value)) {
                    value = StringEscapeUtils.escapeXml(value);
                }
                value = Matcher.quoteReplacement(value);
            } else if (arg.getExpression() != null) {
                value = arg.getExpression().stringValueOf(synCtx);
                if (value != null) {
                    // XML escape the result of an expression that produces a literal, if the target format
                    // of the payload is XML.
                    if (!isXML(value) && !arg.getExpression().getPathType().equals(SynapsePath.JSON_PATH)
                            && XML_TYPE.equals(getType())) {
                        value = StringEscapeUtils.escapeXml(value);
                    }
                    value = Matcher.quoteReplacement(value);
                } else {
                    value = "";
                }
            } else {
                handleException("Unexpected arg type detected", synCtx);
            }
            //value = value.replace(String.valueOf((char) 160), " ").trim();
            valueMap = new HashMap<String, String>();
            if(null != arg.getExpression()) {
                valueMap.put(value, arg.getExpression().getPathType());
            } else {
                valueMap.put(value, SynapsePath.X_PATH);
            }
            argValues[i] = valueMap;
        }
        return argValues;
    }

    public String getFormat() {
        return formatRaw;
    }

    public void setFormat(String format) {
        this.formatRaw = format;
    }

    public void addPathArgument(Argument arg) {
        pathArgumentList.add(arg);
    }

    public List<Argument> getPathArgumentList() {
        return pathArgumentList;
    }

    /**
     * Helper function that returns true if value passed is of XML Type.
     * @param value
     * @return
     */
    private boolean isXML(String value) {
        try {
            AXIOMUtil.stringToOM(value);
        } catch (XMLStreamException ignore) {
            // means not a xml
            return false;
        } catch (OMException ignore) {
            // means not a xml
            return false;
        }
        return true;
    }

    public String getType() {
        return mediaType;
    }

    public void setType(String type) {
        this.mediaType = type;
    }

    /**
     * To get the key which is used to pick the format definition from the local registry
     *
     * @return return the key which is used to pick the format definition from the local registry
     */
    public Value getFormatKey() {
        return formatKey;
    }

    /**
     * To set the local registry key in order to pick the format definition
     *
     * @param key the local registry key
     */
    public void setFormatKey(Value key) {
        this.formatKey = key;
    }

    public void setFormatDynamic(boolean formatDynamic) {
        this.isFormatDynamic = formatDynamic;
    }

    public boolean isFormatDynamic() {
        return isFormatDynamic;
    }

    private boolean isDoingJson(MessageContext messageContext) {
        return JsonUtil.hasAJsonPayload(((Axis2MessageContext) messageContext).getAxis2MessageContext());
    }

    private boolean isDoingXml(MessageContext messageContext) {
        return !isDoingJson(messageContext);
    }
}
