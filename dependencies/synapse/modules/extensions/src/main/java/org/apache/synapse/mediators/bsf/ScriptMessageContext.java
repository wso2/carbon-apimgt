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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.Endpoint;
import org.mozilla.javascript.*;
import org.mozilla.javascript.xml.XMLObject;
import org.mozilla.javascript.xmlimpl.XMLLibImpl;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * ScriptMessageContext decorates the Synapse MessageContext adding methods to use the
 * message payload XML in a way natural to the scripting languageS
 */
@SuppressWarnings({"UnusedDeclaration"})
public class ScriptMessageContext implements MessageContext {
    private static final Log logger = LogFactory.getLog(ScriptMessageContext.class.getName());

    private static final String JSON_OBJECT = "JSON_OBJECT";

    /** The actual Synapse message context reference */
    private final MessageContext mc;


    private ScriptEngine scriptEngine;

    public ScriptMessageContext(MessageContext mc) {
        this.mc = mc;
    }

    /**
     * Get the XML representation of SOAP Body payload.
     * The payload is the first element inside the SOAP <Body> tags
     *
     * @return the XML SOAP Body
     * @throws ScriptException in-case of an error in getting
     * the XML representation of SOAP Body payload
     */
    public Object getPayloadXML() throws ScriptException {
        return toScriptXML(mc.getEnvelope().getBody().getFirstElement());
    }

    private Object toScriptXML(OMElement om) throws ScriptException {
        if (om == null) {
            return null;
        }
        //Mozilla Rhino Engine based functions instead of BSF.
        Context cx = Context.enter();
        try {

            ScriptableObject scope = cx.initStandardObjects();
//            // TODO: why is this needed? A bug in axiom-e4x?
//            ContextHelper.setTopCallScope(cx, scope);

            return cx.newObject(scope, "XML", new Object[]{om.toString()});

        } finally {
            Context.exit();
        }
    }


    /**
     * Set the SOAP body payload from XML
     *
     * @param payload Message payload
     * @throws ScriptException For errors in converting xml To OM
     * @throws OMException     For errors in OM manipulation
     */

    public void setPayloadXML(Object payload) throws OMException, ScriptException {
        SOAPBody body = mc.getEnvelope().getBody();
        OMElement firstChild = body.getFirstElement();
        OMElement omElement = null;
        try {
            Method method = payload.getClass().getDeclaredMethod("toXMLString");
            method.setAccessible(true);
            omElement = AXIOMUtil.stringToOM((String)method.invoke(payload));
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (firstChild == null) {
            body.addChild(omElement);
        } else {
            firstChild.insertSiblingAfter(omElement);
            firstChild.detach();
        }
    }

    /**
     * Get the JSON object representation of the JSON message body of the request.
     *
     * @return JSON object of the message body
     */
    public Object getPayloadJSON() {
        return jsonObject(mc);
    }

    /**
     * Saves the payload of this message context as a JSON payload.
     *
     * @param jsonPayload Javascript native object to be set as the message body
     * @throws ScriptException in case of creating a JSON object out of
     *                         the javascript native object.
     */
    public void setPayloadJSON0(Object jsonPayload) throws ScriptException {
        org.apache.axis2.context.MessageContext messageContext;
        messageContext = ((Axis2MessageContext) mc).getAxis2MessageContext();

        String jsonString;
        if (jsonPayload instanceof String) {
            jsonString = (String) jsonPayload;
        } else {
            jsonString = serializeJSON(jsonPayload);
        }
        JsonUtil.newJsonPayload(messageContext, jsonString, true, true);
        //JsonUtil.setContentType(messageContext);
        Object jsonObject = scriptEngine.eval('(' + jsonString + ')');
        setJsonObject(mc, jsonObject);
    }

    /**
     * Saves the JavaScript Object to the message context.
     * @param messageContext
     * @param jsonObject
     * @return
     */
    public boolean setJsonObject(MessageContext messageContext, Object jsonObject) {
        if (jsonObject == null) {
            logger.error("Setting null JSON object.");
        }
        messageContext.setProperty(JSON_OBJECT, jsonObject);
        return true;
    }

    /**
     * Returns the JavaScript Object saved in this message context.
     * @param messageContext
     * @return
     */
    public Object jsonObject(MessageContext messageContext) {
        if (messageContext == null) {
            return null;
        }
        Object o = messageContext.getProperty(JSON_OBJECT);
        if (o == null) {
            logger.error("JSON object is null.");
            if (this.scriptEngine == null) {
                logger.error("Cannot create empty JSON object. ScriptEngine instance not available.");
                return null;
            }
            try {
                return this.scriptEngine.eval("({})");
            } catch (ScriptException e) {
                logger.error("Could not return an empty JSON object. Error>>> " + e.getLocalizedMessage());
            }
        }
        return o;
    }

    /**
     * Set a script engine
     *
     * @param scriptEngine a ScriptEngine instance
     */
    public void setScriptEngine(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
        if (this.scriptEngine == null) {
            logger.error("Script engine is invalid.");
        }
    }

    /**
     * Add a new SOAP header to the message.
     * 
     * @param mustUnderstand the value for the <code>soapenv:mustUnderstand</code> attribute
     * @param content the XML for the new header
     * @throws ScriptException if an error occurs when converting the XML to OM
     */
    public void addHeader(boolean mustUnderstand, Object content) throws ScriptException {
        SOAPEnvelope envelope = mc.getEnvelope();
        SOAPFactory factory = (SOAPFactory)envelope.getOMFactory();
        SOAPHeader header = envelope.getHeader();
        if (header == null) {
            header = factory.createSOAPHeader(envelope);
        }
        OMElement element = null;
        try {
            element = AXIOMUtil.stringToOM(content.toString());
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        // We can't add the element directly to the SOAPHeader. Instead, we need to copy the
        // information over to a SOAPHeaderBlock.
        SOAPHeaderBlock headerBlock = header.addHeaderBlock(element.getLocalName(),
                element.getNamespace());
        for (Iterator it = element.getAllAttributes(); it.hasNext(); ) {
            headerBlock.addAttribute((OMAttribute)it.next());
        }
        headerBlock.setMustUnderstand(mustUnderstand);
        OMNode child = element.getFirstOMChild();
        while (child != null) {
            // Get the next child before addChild will detach the node from its original place. 
            OMNode next = child.getNextOMSibling();
            headerBlock.addChild(child);
            child = next;
        }
    }
    
    /**
     * Get the XML representation of the complete SOAP envelope
     * @return return an object that represents the payload in the current scripting language
     * @throws ScriptException in-case of an error in getting
     * the XML representation of SOAP envelope
     */
    public Object getEnvelopeXML() throws ScriptException {
        return toScriptXML(mc.getEnvelope());
    }

    // helpers to set EPRs from a script string
    public void setTo(String reference) {
        mc.setTo(new EndpointReference(reference));
    }

    public void setFaultTo(String reference) {
        mc.setFaultTo(new EndpointReference(reference));
    }

    public void setFrom(String reference) {
        mc.setFrom(new EndpointReference(reference));
    }

    public void setReplyTo(String reference) {
        mc.setReplyTo(new EndpointReference(reference));
    }

    // -- all the remainder just use the underlying MessageContext
    public SynapseConfiguration getConfiguration() {
        return mc.getConfiguration();
    }

    public void setConfiguration(SynapseConfiguration cfg) {
        mc.setConfiguration(cfg);
    }

    public SynapseEnvironment getEnvironment() {
        return mc.getEnvironment();
    }

    public void setEnvironment(SynapseEnvironment se) {
        mc.setEnvironment(se);
    }

    public Map<String, Object> getContextEntries() {
        return mc.getContextEntries();
    }

    public void setContextEntries(Map<String, Object> entries) {
        mc.setContextEntries(entries);
    }

    public Object getProperty(String key) {
        return mc.getProperty(key);
    }

    public Object getEntry(String key) {
        return mc.getEntry(key);
    }

    public void setProperty(String key, Object value) {
        if (value instanceof XMLObject) {
            OMElement omElement = null;
                OMElement element = null;
                try {
                    omElement = AXIOMUtil.stringToOM(value.toString());
                } catch (XMLStreamException e) {
                    mc.setProperty(key, value);
                }
            if (omElement != null) {
                mc.setProperty(key, omElement);
            }
        } else {
            mc.setProperty(key, value);
        }
    }

    public Set getPropertyKeySet() {
        return mc.getPropertyKeySet();
    }

    public Mediator getMainSequence() {
        return mc.getMainSequence();
    }

    public Mediator getFaultSequence() {
        return mc.getFaultSequence();
    }

    public Mediator getSequence(String key) {
        return mc.getSequence(key);
    }

    public OMElement getFormat(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Endpoint getEndpoint(String key) {
        return mc.getEndpoint(key);
    }

    public SOAPEnvelope getEnvelope() {
        return mc.getEnvelope();
    }

    public void setEnvelope(SOAPEnvelope envelope) throws AxisFault {
        mc.setEnvelope(envelope);
    }

    public EndpointReference getFaultTo() {
        return mc.getFaultTo();
    }

    public void setFaultTo(EndpointReference reference) {
        mc.setFaultTo(reference);
    }

    public EndpointReference getFrom() {
        return mc.getFrom();
    }

    public void setFrom(EndpointReference reference) {
        mc.setFrom(reference);
    }

    public String getMessageID() {
        return mc.getMessageID();
    }

    public void setMessageID(String string) {
        mc.setMessageID(string);
    }

    public RelatesTo getRelatesTo() {
        return mc.getRelatesTo();
    }

    public void setRelatesTo(RelatesTo[] reference) {
        mc.setRelatesTo(reference);
    }

    public EndpointReference getReplyTo() {
        return mc.getReplyTo();
    }

    public void setReplyTo(EndpointReference reference) {
        mc.setReplyTo(reference);
    }

    public EndpointReference getTo() {
        return mc.getTo();
    }

    public void setTo(EndpointReference reference) {
        mc.setTo(reference);
    }

    public void setWSAAction(String actionURI) {
        mc.setWSAAction(actionURI);
    }

    public String getWSAAction() {
        return mc.getWSAAction();
    }

    public String getSoapAction() {
        return mc.getSoapAction();
    }

    public void setSoapAction(String string) {
        mc.setSoapAction(string);
    }

    public void setWSAMessageID(String messageID) {
        mc.setWSAMessageID(messageID);
    }

    public String getWSAMessageID() {
        return mc.getWSAMessageID();
    }

    public boolean isDoingMTOM() {
        return mc.isDoingMTOM();
    }

    public boolean isDoingSWA() {
        return mc.isDoingSWA();
    }

    public void setDoingMTOM(boolean b) {
        mc.setDoingMTOM(b);
    }

    public void setDoingSWA(boolean b) {
        mc.setDoingSWA(b);
    }

    public boolean isDoingPOX() {
        return mc.isDoingPOX();
    }

    public void setDoingPOX(boolean b) {
        mc.setDoingPOX(b);
    }

    public boolean isDoingGET() {
        return mc.isDoingGET();
    }

    public void setDoingGET(boolean b) {
        mc.setDoingGET(b);
    }

    public boolean isSOAP11() {
        return mc.isSOAP11();
    }

    public void setResponse(boolean b) {
        mc.setResponse(b);
    }

    public boolean isResponse() {
        return mc.isResponse();
    }

    public void setFaultResponse(boolean b) {
        mc.setFaultResponse(b);
    }

    public boolean isFaultResponse() {
        return mc.isFaultResponse();
    }

    public int getTracingState() {
        return mc.getTracingState();
    }

    public void setTracingState(int tracingState) {
        mc.setTracingState(tracingState);
    }

    public Stack<FaultHandler> getFaultStack() {
        return mc.getFaultStack();
    }

    public void pushFaultHandler(FaultHandler fault) {
        mc.pushFaultHandler(fault);
    }

    public void pushContinuationState(ContinuationState continuationState) {
    }

    public Stack<ContinuationState> getContinuationStateStack() {
        return null;
    }

    public boolean isContinuationEnabled() {
        return false;
    }

    public void setContinuationEnabled(boolean contStateStackEnabled) {
    }

    public Log getServiceLog() {
        return LogFactory.getLog(ScriptMessageContext.class);
    }

    public Mediator getSequenceTemplate(String key) {
        return mc.getSequenceTemplate(key);
    }

    private String serializeJSON(Object obj) {
        StringWriter json = new StringWriter();
        if(obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }

        if (obj instanceof NativeObject) {
            json.append("{");
            NativeObject o = (NativeObject) obj;
            Object[] ids = o.getIds();
            boolean first = true;
            for (Object id : ids) {
                String key = (String) id;
                Object value = o.get((String) id, o);
                if (!first) {
                    json.append(", ");
                } else {
                    first = false;
                }
                json.append("\"").append(key).append("\" : ").append(serializeJSON(value));
            }
            json.append("}");
        } else if (obj instanceof NativeArray) {
            json.append("[");
            NativeArray o = (NativeArray) obj;
            Object[] ids = o.getIds();
            boolean first = true;
            for (Object id : ids) {
                Object value = o.get((Integer) id, o);
                if (!first) {
                    json.append(", ");
                } else {
                    first = false;
                }
                json.append(serializeJSON(value));
            }
            json.append("]");
        } else if (obj instanceof Object[]) {
            json.append("[");
            boolean first = true;
            for (Object value : (Object[]) obj) {
                if (!first) {
                    json.append(", ");
                } else {
                    first = false;
                }
                json.append(serializeJSON(value));
            }
            json.append("]");

        } else if (obj instanceof String) {
            json.append("\"").append(obj.toString()).append("\"");
        } else if (obj instanceof Integer ||
                obj instanceof Long ||
                obj instanceof Float ||
                obj instanceof Double ||
                obj instanceof Short ||
                obj instanceof BigInteger ||
                obj instanceof BigDecimal ||
                obj instanceof Boolean) {
            json.append(obj.toString());
        } else {
            json.append("{}");
        }
        return json.toString();
    }

    public void setPayloadJSON(Object jsonPayload) throws ScriptException {
        org.apache.axis2.context.MessageContext messageContext;
        messageContext = ((Axis2MessageContext) mc).getAxis2MessageContext();

        byte[] json = {'{', '}'};
        if (jsonPayload instanceof String) {
            json = jsonPayload.toString().getBytes();
        } else if (jsonPayload != null) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                serializeJSON_(jsonPayload, out);
                json = out.toByteArray();
            } catch (IOException e) {
                logger.error("#setPayloadJSON. Could not retrieve bytes from JSON object. Error>>> "
                        + e.getLocalizedMessage());
            }
        }
        // save this JSON object as the new payload.
        JsonUtil.newJsonPayload(messageContext, json, 0, json.length, true, true);
        //JsonUtil.setContentType(messageContext);
        Object jsonObject = scriptEngine.eval(JsonUtil.newJavaScriptSourceReader(messageContext));
        setJsonObject(mc, jsonObject);
    }

    private void serializeJSON_(Object obj, OutputStream out) throws IOException {
        if (obj == null || out == null) {
            logger.warn("#serializeJSON_. Did not serialize JSON object. Object: " + obj + "  Stream: " + out);
            return;
        }
        if(obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }

        if (obj instanceof NativeObject) {
            out.write('{');
            NativeObject o = (NativeObject) obj;
            Object[] ids = o.getIds();
            boolean first = true;
            for (Object id : ids) {
                String key = (String) id;
                Object value = o.get((String) id, o);
                if (!first) {
                    out.write(',');
                    out.write(' ');
                } else {
                    first = false;
                }
                out.write('"');
                out.write(key.getBytes());
                out.write('"');
                out.write(':');
                serializeJSON_(value, out);
            }
            out.write('}');
        } else if (obj instanceof NativeArray) {
            out.write('[');
            NativeArray o = (NativeArray) obj;
            Object[] ids = o.getIds();
            boolean first = true;
            for (Object id : ids) {
                Object value = o.get((Integer) id, o);
                if (!first) {
                    out.write(',');
                    out.write(' ');
                } else {
                    first = false;
                }
                serializeJSON_(value, out);
            }
            out.write(']');
        } else if (obj instanceof Object[]) {
            out.write('[');
            boolean first = true;
            for (Object value : (Object[]) obj) {
                if (!first) {
                    out.write(',');
                    out.write(' ');
                } else {
                    first = false;
                }
                serializeJSON_(value, out);
            }
            out.write(']');
        } else if (obj instanceof String) {
            out.write('"');
            out.write(((String) obj).getBytes());
            out.write('"');
        } else if (obj instanceof Integer ||
                obj instanceof Long ||
                obj instanceof Float ||
                obj instanceof Double ||
                obj instanceof Short ||
                obj instanceof BigInteger ||
                obj instanceof BigDecimal ||
                obj instanceof Boolean) {
            out.write(obj.toString().getBytes());
        } else {
            out.write('{');
            out.write('}');
        }
    }

	public Mediator getDefaultConfiguration(String arg0) {
	    // TODO Auto-generated method stub
	    return null;
    }
}
