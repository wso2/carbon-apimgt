package org.apache.synapse.commons.json;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.util.MiscellaneousUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Properties;

public final class JsonUtil {
    private static Log logger = LogFactory.getLog(JsonUtil.class.getName());

    private static final String ORG_APACHE_SYNAPSE_COMMONS_JSON_JSON_INPUT_STREAM = "org.apache.synapse.commons.json.JsonInputStream";

    private static final QName JSON_OBJECT = new QName("jsonObject");

    private static final QName JSON_ARRAY = new QName("jsonArray");

    /** If this property is set to <tt>true</tt> the input stream of the JSON payload will be reset
     *  after writing to the output stream within the #writeAsJson method. */
    public static final String PRESERVE_JSON_STREAM = "preserve.json.stream";

    /// JSON/XML INPUT OUTPUT Formatting Configuration
    // TODO: Build thie configuration from a "json.properties" file. Add a debug log to dump() the config to the log.
    // TODO: Add another param to empty xml element to null or empty json string mapping <a/> -> "a":null or "a":""
    // TODO: Build this configuration into a separate class.
    // TODO: Property to remove root element from XML output
    // TODO: Axis2 property/synapse static property add XML Namespace to the root element

    private static boolean preserverNamespacesForJson = false;

    private static final boolean processNCNames;

    private static final boolean jsonOutAutoPrimitive;

    private static final char jsonOutNamespaceSepChar;

    private static final boolean jsonOutEnableNsDeclarations;

    static {
        Properties properties = MiscellaneousUtil.loadProperties("synapse.properties");
        if (properties == null) {
            preserverNamespacesForJson = processNCNames = jsonOutEnableNsDeclarations = false;
            jsonOutAutoPrimitive = true;
            jsonOutNamespaceSepChar = '_';
        } else {
            // Preserve the namespace declarations() in the JSON output in the XML -> JSON transformation.
            String process = properties.getProperty("synapse.commons.json.preserve.namespace", "false").trim();
            preserverNamespacesForJson = Boolean.parseBoolean(process.toLowerCase());
            // Build valid XML NCNames when building XML element names in the JSON -> XML transformation.
            process = properties.getProperty("synapse.commons.json.buildValidNCNames", "false").trim();
            processNCNames = Boolean.parseBoolean(process.toLowerCase());
            // Enable primitive types in json out put in the XML -> JSON transformation.
            process = properties.getProperty("synapse.commons.json.json.output.autoPrimitive", "true").trim();
            jsonOutAutoPrimitive = Boolean.parseBoolean(process.toLowerCase());
            // The namespace prefix separate character in the JSON output of the XML -> JSON transformation
            process = properties.getProperty("synapse.commons.json.json.output.namespaceSepChar", "_").trim();
            jsonOutNamespaceSepChar = process.charAt(0);
            // Add XML namespace declarations in the JSON output in the XML -> JSON transformation.
            process = properties.getProperty("synapse.commons.json.json.output.enableNSDeclarations", "false").trim();
            jsonOutEnableNsDeclarations = Boolean.parseBoolean(process.toLowerCase());

            process = properties.getProperty("synapse.commons.json.json.output.emptyXmlElemToEmptyStr", "true").trim();
        }
    }

    /** Configuration used to produce XML that has processing instructions in it. */
    private static final JsonXMLConfig xmlOutputConfig = new JsonXMLConfigBuilder()
            .multiplePI(true)
            .autoArray(true)
            .autoPrimitive(true)
            .namespaceDeclarations(false)
            .namespaceSeparator( '\u0D89')
            .build();

    /** Configuration used to produce XML that has no processing instructions in it. */
    private static final JsonXMLConfig xmlOutputConfigNoPIs = new JsonXMLConfigBuilder()
            .multiplePI(false)
            .autoArray(true)
            .autoPrimitive(true)
            .namespaceDeclarations(false)
            .namespaceSeparator('\u0D89')
            .build();

    /** This configuration is used to format the JSON output produced by the JSON writer. */
    private static final JsonXMLConfig jsonOutputConfig = new JsonXMLConfigBuilder()
            .multiplePI(true)
            .autoArray(true)
            .autoPrimitive(jsonOutAutoPrimitive)
            .namespaceDeclarations(jsonOutEnableNsDeclarations)
            .namespaceSeparator(jsonOutNamespaceSepChar)
            .build();
    /// End of JSON/XML INPUT OUTPUT Formatting Configuration.

    /** Factory used to create JSON Readers */
    private static final JsonXMLInputFactory jsonInputFactory = new JsonXMLInputFactory(xmlOutputConfig);

    /** Factory used to create JSON Readers */
    private static final JsonXMLInputFactory xmlInputFactoryNoPIs = new JsonXMLInputFactory(xmlOutputConfigNoPIs);

    /** Factory used to create JSON Writers */
    private static final JsonXMLOutputFactory jsonOutputFactory = new JsonXMLOutputFactory(jsonOutputConfig);

    /** Factory used to create XML Readers */
    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    /**
     * Converts the XML payload of a message context into its JSON representation and writes it to an output stream.<br/>
     * If no XML payload is found, the existing JSON payload will be copied to the output stream.<br/>
     * Note that this method removes all existing namespace declarations and namespace prefixes of the payload that is <br/>
     * present in the provided message context.
     * @param messageContext Axis2 Message context that holds the JSON/XML payload.
     * @param out Output stream to which the payload(JSON) must be written.
     * @throws org.apache.axis2.AxisFault
     */
    public static void writeAsJson(MessageContext messageContext, OutputStream out) throws AxisFault {
        if (messageContext == null || out == null) {
            return;
        }
        OMElement element = messageContext.getEnvelope().getBody().getFirstElement();
        Object o = jsonStream(messageContext, false);
        InputStream json = null;
        if (o != null) {
            json = (InputStream) o;
        }
        o = messageContext.getProperty(org.apache.synapse.commons.json.Constants.JSON_STRING);
        String jsonStr = null;
        if (o instanceof String) {
            jsonStr = (String) o;
        }
        if (json != null) { // there is a JSON stream
            if (element instanceof OMSourcedElementImpl) {
                if (isAJsonPayloadElement(element)) {
                    writeJsonStream(json, messageContext, out);
                } else { // Ignore the JSON stream
                    writeAsJson(element, out);
                }
            } else if (element != null) { // element is not an OMSourcedElementImpl. But we ignore the JSON stream.
                writeAsJson(element, out);
            } else { // element == null.
                writeJsonStream(json, messageContext, out);
            }
        } else if (element != null) { // No JSON stream found. Convert the existing element to JSON.
            writeAsJson(element, out);
        } else if (jsonStr != null) { // No JSON stream or element found. See if there's a JSON_STRING set.
            try {
                out.write(jsonStr.getBytes());
            } catch (IOException e) {
                logger.error("#writeAsJson. Could not write JSON string. MessageID: "
                        + messageContext.getMessageID() + ". Error>> " + e.getLocalizedMessage());
                throw new AxisFault("Could not write JSON string.", e);
            }
        } else {
            logger.error("#writeAsJson. Payload could not be written as JSON. MessageID: " + messageContext.getMessageID());
            throw new AxisFault("Payload could not be written as JSON.");
        }
    }

    /**
     * Converts a JSON input stream to its XML representation.
     * @param jsonStream JSON input stream
     * @param pIs Whether or not to add XML processing instructions to the output XML.<br/>
     *            This property is useful when converting JSON payloads with array objects.
     * @return OMElement that is the XML representation of the input JSON data.
     */
    public static OMElement toXml(InputStream jsonStream, boolean pIs) throws AxisFault {
        if (jsonStream == null) {
            logger.error("#toXml. Could not convert JSON Stream to XML. JSON input stream is null.");
            return null;
        }
        try {
            XMLStreamReader streamReader = getReader(jsonStream, pIs);
            return new StAXOMBuilder(streamReader).getDocumentElement();
        } catch (XMLStreamException e) {//invalid JSON?
            logger.error("#toXml. Could not convert JSON Stream to XML. Cannot handle JSON input. Error>>> " + e.getLocalizedMessage());
            throw new AxisFault("Could not convert JSON Stream to XML. Cannot handle JSON input.", e);
        }
    }

    /**
     * Returns an XMLStreamReader for a JSON input stream
     * @param jsonStream InputStream of JSON
     * @param pIs Whether to add XML PIs to the XML output. This is used as an instruction to the returned XML Stream Reader.
     * @return An XMLStreamReader
     * @throws javax.xml.stream.XMLStreamException
     */
    public static XMLStreamReader getReader(InputStream jsonStream, boolean pIs) throws XMLStreamException {
        if (jsonStream == null) {
            logger.error("#getReader. Could not create XMLStreamReader from [null] input stream.");
            return null;
        }
        return pIs ? getReader(jsonStream)
                : new JsonReaderDelegate(xmlInputFactoryNoPIs.createXMLStreamReader(jsonStream,
                de.odysseus.staxon.json.stream.impl.Constants.SCANNER.SCANNER_1), processNCNames);
    }

    /**
     * This method is useful when you need to get an XML reader directly for the input JSON stream <br/>
     * without adding any additional object wrapper elements such as 'jsonObject' and 'jsonArray'.
     * @param jsonStream InputStream of JSON
     * @return An XMLStreamReader
     * @throws javax.xml.stream.XMLStreamException
     */
    public static XMLStreamReader getReader(InputStream jsonStream) throws XMLStreamException {
        if (jsonStream == null) {
            logger.error("#getReader. Could not create XMLStreamReader from [null] input stream.");
            return null;
        }
        return new JsonReaderDelegate(jsonInputFactory.createXMLStreamReader(jsonStream,
                de.odysseus.staxon.json.stream.impl.Constants.SCANNER.SCANNER_1), processNCNames);
    }

    /**
     * Converts an XML element to its JSON representation and writes it to an output stream.<br/>
     * Note that this method removes all existing namespace declarations and namespace prefixes of the provided XML element<br/>
     * @param element XML element of which JSON representation is expected.
     * @param outputStream Output Stream to write the JSON representation.<br/>
     *                     At the end of a successful conversion, its flush method will be called.
     * @throws AxisFault
     */
    public static void writeAsJson(OMElement element, OutputStream outputStream) throws AxisFault {
        if (element == null) {
            logger.error("#writeAsJson. OMElement is null. Cannot convert to JSON.");
            throw new AxisFault("OMElement is null. Cannot convert to JSON.");
        }
        if (outputStream == null) {
            return;
        }
        transformElement(element, true);
        try {
            org.apache.commons.io.output.ByteArrayOutputStream xmlStream =
                    new org.apache.commons.io.output.ByteArrayOutputStream();
            element.serialize(xmlStream);
            xmlStream.flush();
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(
                    new XmlReaderDelegate(xmlInputFactory.createXMLStreamReader(
                            new ByteArrayInputStream(xmlStream.toByteArray())
                    ), processNCNames)
            );
            XMLEventWriter jsonWriter = jsonOutputFactory.createXMLEventWriter(outputStream);
            jsonWriter.add(xmlEventReader);
            xmlEventReader.close();
            jsonWriter.close();
            outputStream.flush();
        } catch (XMLStreamException e) {
            logger.error("#writeAsJson. Could not convert OMElement to JSON. Invalid XML payload. Error>>> " + e.getLocalizedMessage());
            throw new AxisFault("Could not convert OMElement to JSON. Invalid XML payload.", e);
        } catch (IOException e) {
            logger.error("#writeAsJson. Could not convert OMElement to JSON. Error>>> " + e.getLocalizedMessage());
            throw new AxisFault("Could not convert OMElement to JSON.", e);
        }
    }

    /**
     * Converts an XML element to its JSON representation and returns it as a String.
     * @param element OMElement to be converted to JSON.
     * @return A String builder instance that contains the converted JSON string.
     */
    public static StringBuilder toJsonString(OMElement element) throws AxisFault {
        if (element == null) {
            return new StringBuilder("{}");
        }
        org.apache.commons.io.output.ByteArrayOutputStream byteStream =
                new org.apache.commons.io.output.ByteArrayOutputStream();
        writeAsJson(element.cloneOMElement(), byteStream);
        return new StringBuilder(new String(byteStream.toByteArray()));
    }

    /**
     * Removes XML namespace declarations, and namespace prefixes from an XML element.
     * @param element Source XML element
     * @param processAttrbs Whether to remove the namespaces from attributes as well
     */
    public static void transformElement(OMElement element, boolean processAttrbs) {
        if (element == null) {
            return;
        }
        removeIndentations(element);
        if (!preserverNamespacesForJson) {
            removeNamespaces(element, processAttrbs);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("#transformElement. Transformed OMElement. Result: " + element.toString());
        }
    }

    private static void removeIndentations(OMElement elem) {
        Iterator children = elem.getChildren();
        while (children.hasNext()) {
            OMNode child = (OMNode)children.next();
            if (child instanceof OMText) {
                if ("".equals(((OMText) child).getText().trim())) {
                    children.remove();
                }
            } else if (child instanceof OMElement) {
                removeIndentations((OMElement) child);
            }
        }
    }

    private static void removeNamespaces(OMElement element, boolean processAttrbs) {
        OMNamespace ns = element.getNamespace();
        Iterator i  = element.getAllDeclaredNamespaces();
        while (i.hasNext()) {
            i.next();
            i.remove();
        }
        String prefix;
        if (ns != null) {
            prefix = "";//element.getNamespace().getPrefix();
            element.setNamespace(element.getOMFactory().createOMNamespace("", prefix));
        }
        Iterator children = element.getChildElements();
        while (children.hasNext()) {
            removeNamespaces((OMElement)children.next(), processAttrbs);
        }
        if (!processAttrbs) {
            return;
        }
        Iterator attrbs = element.getAllAttributes();
        while (attrbs.hasNext()) {
            OMAttribute attrb = (OMAttribute)attrbs.next();
            prefix = "";//attrb.getQName().getPrefix();
            attrb.setOMNamespace(attrb.getOMFactory().createOMNamespace("", prefix));
            //element.removeAttribute(attrb);
        }
    }

    /**
     * Builds and returns a new JSON payload for a message context with a stream of JSON content. <br/>
     * This is the recommended way to build a JSON payload into an Axis2 message context.<br/>
     * A JSON payload built into a message context with this method can only be removed by calling
     * {@link #removeJsonPayload(org.apache.axis2.context.MessageContext)} method.
     * @param messageContext Axis2 Message context to which the new JSON payload must be saved (if instructed with <tt>addAsNewFirstChild</tt>).
     * @param inputStream JSON content as an input stream.
     * @param removeChildren Whether to remove existing child nodes of the existing payload of the message context
     * @param addAsNewFirstChild Whether to add the new JSON payload as the first child of this message context *after* removing the existing first child element.<br/>
     * Setting this argument to <tt>true</tt> will have no effect if the value of the argument <tt>removeChildren</tt> is already <tt>false</tt>.
     * @return Payload object that stores the input JSON content as a Sourced object (See {@link org.apache.axiom.om.OMSourcedElement}) that can build the XML tree for contained JSON payload on demand.
     */
    public static OMElement newJsonPayload(MessageContext messageContext, InputStream inputStream,
                                           boolean removeChildren, boolean addAsNewFirstChild) {
        if (messageContext == null) {
            logger.error("#newJsonPayload. Could not save JSON stream. Message context is null.");
            return null;
        }
        boolean isObject = false;
        boolean isArray = false;
        if (inputStream != null) {
            InputStream json = toReadOnlyStream(inputStream);
            messageContext.setProperty(ORG_APACHE_SYNAPSE_COMMONS_JSON_JSON_INPUT_STREAM, json);
            // read ahead few characters to see if the stream is valid...
            try {
                // check for empty/all-whitespace streams
                int c = json.read();
                boolean valid = false;
                while (c != -1 && c != '{' && c != '[') {
                    c = json.read();
                }
                if (c != -1) {
                    valid = true;
                }
                if (c == '{') {
                    isObject = true;
                    isArray = false;
                } else if (c == '[') {
                    isArray = true;
                    isObject = false;
                }
                json.reset();
                if (!valid) {
                    logger.error("#newJsonPayload. Could not save JSON payload. Invalid input stream found. MessageID: "
                            + messageContext.getMessageID());
                    return null;
                }
            } catch (IOException e) {
                logger.error("#newJsonPayload. Could not determine availability of the JSON input stream. MessageID: "
                        + messageContext.getMessageID() + ". Error>>> " + e.getLocalizedMessage());
                return null;
            }
            QName jsonElement = null;
            if (isObject) {
                jsonElement = JSON_OBJECT;
            }
            if (isArray) {
                jsonElement = JSON_ARRAY;
            }
            OMElement elem = new OMSourcedElementImpl(jsonElement,
                    OMAbstractFactory.getOMFactory(),
                    new JsonDataSource((InputStream) messageContext.getProperty(ORG_APACHE_SYNAPSE_COMMONS_JSON_JSON_INPUT_STREAM)));
            if (!removeChildren) {
                if (logger.isTraceEnabled()) {
                    logger.trace("#newJsonPayload. Not removing child elements from exiting message. Returning result. MessageID: "
                            + messageContext.getMessageID());
                }
                return elem;
            }
            SOAPEnvelope e = messageContext.getEnvelope();
            if (e != null) {
                SOAPBody b = e.getBody();
                if (b != null) {
                    removeIndentations(b);
                    Iterator children = b.getChildren();
                    while (children.hasNext()) {
                        Object o = children.next();
                        if (o instanceof OMNode) {
                            //((OMNode) o).detach();
                            children.remove();
                        }
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("#newJsonPayload. Removed child elements from exiting message. MessageID: "
                                + messageContext.getMessageID());
                    }
                    if (addAsNewFirstChild) {
                        b.addChild(elem);
                        if (logger.isTraceEnabled()) {
                            logger.trace("#newJsonPayload. Added the new JSON sourced element as the first child. MessageID: "
                                    + messageContext.getMessageID());
                        }
                    }
                }
            }
            return elem;
        }
        return null;
    }

    /**
     * Builds and returns a new JSON payload for a message context with a JSON string.<br/>
     * @see #newJsonPayload(org.apache.axis2.context.MessageContext, java.io.InputStream, boolean, boolean)
     * @param messageContext Axis2 Message context to which the new JSON payload must be saved (if instructed with <tt>addAsNewFirstChild</tt>).
     * @param jsonString JSON content as a String.
     * @param removeChildren Whether to remove existing child nodes of the existing payload of the message context
     * @param addAsNewFirstChild Whether to add the new JSON payload as the first child of this message context *after* removing the existing first child element.<br/>
     * Setting this argument to <tt>true</tt> will have no effect if the value of the argument <tt>removeChildren</tt> is already <tt>false</tt>.
     * @return Payload object that stores the input JSON content as a Sourced object (See {@link org.apache.axiom.om.OMSourcedElement}) that facilitates on demand building of the XML tree.
     */
    public static OMElement newJsonPayload(MessageContext messageContext, String jsonString,
                                           boolean removeChildren, boolean addAsNewFirstChild) {
        if (jsonString == null || jsonString.isEmpty()) {
            jsonString = "{}";
        }
        return newJsonPayload(messageContext, new ByteArrayInputStream(jsonString.getBytes()),
                removeChildren, addAsNewFirstChild);
    }

    /**
     * Builds and returns a new JSON payload for a message context with a byte array containing JSON.<br/>
     * @see #newJsonPayload(org.apache.axis2.context.MessageContext, java.io.InputStream, boolean, boolean)
     * @param messageContext Axis2 Message context to which the new JSON payload must be saved (if instructed with <tt>addAsNewFirstChild</tt>).
     * @param json JSON content as a byte array.
     * @param offset starting position of the JSON content in the provided array
     * @param length how many bytes to read starting from the offset provided.
     * @param removeChildren Whether to remove existing child nodes of the existing payload of the message context
     * @param addAsNewFirstChild Whether to add the new JSON payload as the first child of this message context *after* removing the existing first child element.<br/>
     * Setting this argument to <tt>true</tt> will have no effect if the value of the argument <tt>removeChildren</tt> is already <tt>false</tt>.
     * @return Payload object that stores the input JSON content as a Sourced object (See {@link org.apache.axiom.om.OMSourcedElement}) that facilitates on demand building of the XML tree.
     */
    public static OMElement newJsonPayload(MessageContext messageContext, byte[] json, int offset,
                                           int length, boolean removeChildren, boolean addAsNewFirstChild) {
        InputStream is;
        if (json == null || json.length < 2) {
            json = new byte[]{'{', '}'};
            is = new ByteArrayInputStream(json);
        } else {
            is = new ByteArrayInputStream(json, offset, length);
        }
        return newJsonPayload(messageContext, is, removeChildren, addAsNewFirstChild);
    }

    /**
     * Removes the existing JSON payload of a message context if any.<br/>
     * This method can only remove a JSON payload that has been set with {@link #newJsonPayload(org.apache.axis2.context.MessageContext, java.io.InputStream, boolean, boolean)}
     * and its variants.
     * @param messageContext Axis2 Message context from which the JSON stream must be removed.
     * @return <tt>true</tt> if the operation is successful.
     */
    public static boolean removeJsonPayload(MessageContext messageContext) {
        messageContext.removeProperty(ORG_APACHE_SYNAPSE_COMMONS_JSON_JSON_INPUT_STREAM);
        boolean removeChildren = true;
        if (!removeChildren) { // don't change this.
            if (logger.isTraceEnabled()) {
                logger.trace("#removeJsonPayload. Removed JSON stream. MessageID: " + messageContext.getMessageID());
            }
            return true;
        }
        SOAPEnvelope e = messageContext.getEnvelope();
        if (e != null) {
            SOAPBody b = e.getBody();
            if (b != null) {
                removeIndentations(b); // cleans payload by removing unnecessary characters
                Iterator children = b.getChildren();
                while (children.hasNext()) {
                    Object o = children.next();
                    if (o instanceof OMNode) {
                        //((OMNode) o).detach();
                        children.remove();
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("#removeJsonPayload. Removed JSON stream and child elements of payload. MessageID: "
                            + messageContext.getMessageID());
                }
            }
        }
        return true;
    }

    /**
     * Returns the JSON stream associated with the payload of this message context.
     * @param messageContext Axis2 Message context
     * @param reset Whether to reset the input stream that contains this JSON payload so that next read will start from the beginning of this stream.
     * @return JSON input stream
     */
    private static InputStream jsonStream(MessageContext messageContext, boolean reset) {
        if (messageContext == null) {
            return null;
        }
        Object o = messageContext.getProperty(ORG_APACHE_SYNAPSE_COMMONS_JSON_JSON_INPUT_STREAM);
        if (o instanceof InputStream) {
            InputStream is = (InputStream) o;
            if (reset) {
                if (is.markSupported()) {
                    try {
                        is.reset();
                    } catch (IOException e) {
                        logger.error("#jsonStream. Could not reuse JSON Stream. Error>>>\n",e);
                        return null;
                    }
                }
            }
            return is;
        }
        return null;
    }

    /**
     * Returns the READ-ONLY input stream of the JSON payload contained in the provided message context.
     * @param messageContext Axis2 Message context
     * @return {@link java.io.InputStream} of JSON payload contained in the message context. Null otherwise.<br/>
     * It is possible to read from this stream right away. This InputStream cannot be <tt>close</tt>d, <tt>mark</tt>ed, or <tt>skip</tt>ped. <br/>
     * If <tt>close()</tt> is invoked on this input stream, it will be reset to the beginning.
     */
    public static InputStream getJsonPayload(MessageContext messageContext) {
        return hasAJsonPayload(messageContext) ? jsonStream(messageContext, true) : null;
    }

    /**
     * Returns a copy of the JSON stream contained in the provided Message Context.
     * @param messageContext Axis2 Message context that contains a JSON payload.
     * @return {@link java.io.InputStream}
     */
    private static InputStream copyOfJsonPayload(MessageContext messageContext, boolean closable) {
        if (messageContext == null) {
            logger.error("#copyOfJsonPayload. Cannot copy JSON stream from message context. [null].");
            return null;
        }
        InputStream jsonStream = jsonStream(messageContext, true);
        if (jsonStream == null) {
            logger.error("#copyOfJsonPayload. Cannot copy JSON stream from message context. [null] stream.");
            return null;
        }
        org.apache.commons.io.output.ByteArrayOutputStream out = new org.apache.commons.io.output.ByteArrayOutputStream();
        try {
            IOUtils.copy(jsonStream, out);
            out.flush();
            return closable ? new ByteArrayInputStream(out.toByteArray())
                    : toReadOnlyStream(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException e) {
            logger.error("#copyOfJsonPayload. Could not copy the JSON stream from message context. Error>>> " + e.getLocalizedMessage());
        }
        return null;
    }

    private static void writeJsonStream(InputStream json, MessageContext messageContext, OutputStream out) throws AxisFault {
        try {
            if (json.markSupported()) {
                json.reset();
            }
            IOUtils.copy(json, out); // Write the JSON stream
            if (messageContext.getProperty(PRESERVE_JSON_STREAM) != null) {
                if (json.markSupported()) {
                    json.reset();
                }
                messageContext.removeProperty(PRESERVE_JSON_STREAM);
            }
        } catch (IOException e) {
            logger.error("#writeJsonStream. Could not write JSON stream. MessageID: "
                    + messageContext.getMessageID() + ". Error>> " + e.getLocalizedMessage());
            throw new AxisFault("Could not write JSON stream.", e);
        }
    }

    /**
     * Returns a reusable cached copy of the JSON stream contained in the provided Message Context.
     * @param messageContext Axis2 Message context that contains a JSON payload.
     * @return {@link java.io.InputStream}
     */
    private static InputStream cachedCopyOfJsonPayload(MessageContext messageContext) {
        if (messageContext == null) {
            logger.error("#cachedCopyOfJsonPayload. Cannot copy JSON stream from message context. [null].");
            return null;
        }
        InputStream jsonStream = jsonStream(messageContext, true);
        if (jsonStream == null) {
            logger.error("#cachedCopyOfJsonPayload. Cannot copy JSON stream from message context. [null] stream.");
            return null;
        }
        String inputStreamCache = Long.toString(jsonStream.hashCode());
        Object o = messageContext.getProperty(inputStreamCache);
        if (o instanceof InputStream) {
            InputStream inputStream = (InputStream) o;
            try {
                inputStream.reset();
                if (logger.isDebugEnabled()) {
                    logger.debug("#cachedCopyOfJsonPayload. Cache HIT");
                }
                return inputStream;
            } catch (IOException e) {
                logger.warn("#cachedCopyOfJsonPayload. Could not reuse the cached input stream. Error>>> " + e.getLocalizedMessage());
            }
        }
        org.apache.commons.io.output.ByteArrayOutputStream out = new org.apache.commons.io.output.ByteArrayOutputStream();
        try {
            IOUtils.copy(jsonStream, out);
            out.flush();
            InputStream inputStream = toReadOnlyStream(new ByteArrayInputStream(out.toByteArray()));
            messageContext.setProperty(inputStreamCache, inputStream);
            if (logger.isDebugEnabled()) {
                logger.debug("#cachedCopyOfJsonPayload. Cache MISS");
            }
            return inputStream;
        } catch (IOException e) {
            logger.error("#cachedCopyOfJsonPayload. Could not copy the JSON stream from message context. Error>>> " + e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Returns a new instance of a reader that can read from the JSON payload contained in the provided message context.
     * @param messageContext Axis2 Message context
     * @return {@link java.io.Reader} if a JSON payload is found in the message context. null otherwise.
     */
    public static Reader newJsonPayloadReader(MessageContext messageContext) {
        if (messageContext == null) {
            return null;
        }
        InputStream is = jsonStream(messageContext, true);
        if (is == null) {
            return null;
        }
        return new InputStreamReader(is);
    }

    /**
     * Returns the JSON payload contained in the provided message context as a byte array.
     * @param messageContext Axis2 Message context
     * @return <tt>byte</tt> array containing the JSON payload. Empty array if no JSON payload found or invalid message context is passed in.
     */
    public static byte[] jsonPayloadToByteArray(MessageContext messageContext) {
        if (messageContext == null) {
            return new byte[0];
        }
        InputStream is = jsonStream(messageContext, true);
        if (is == null) {
            return new byte[0];
        }
        try {
            return IOUtils.toByteArray(is); // IOUtils.toByteArray() doesn't close the input stream.
        } catch (IOException e) {
            logger.warn("#jsonPayloadToByteArray. Could not convert JSON stream to byte array.");
            return new byte[0];
        }
    }

    /**
     * Returns the JSON payload contained in the provided message context as a String.
     * @param messageContext Axis2 Message context
     * @return <tt>java.lang.String</tt> representation of the JSON payload. Returns "{}" if no JSON payload found or invalid message context is passed in.
     */
    public static String jsonPayloadToString(MessageContext messageContext) {
        if (messageContext == null) {
            return "{}";
        }
        InputStream is = jsonStream(messageContext, true);
        if (is == null) {
            return "{}";
        }
        try {
            return IOUtils.toString(is); // IOUtils.toByteArray() doesn't close the input stream.
        } catch (IOException e) {
            logger.warn("#jsonPayloadToString. Could not convert JSON stream to String.");
            return "{}";
        }
    }

    /**
     * Returns whether the provided XML element is an element that stores a sourced JSON payload.
     * @param element XML element
     * @return <tt>true</tt> if the element is a sourced JSON object (ie. an <tt>OMSourcedElement</tt> instance containing a JSON stream).
     */
    public static boolean hasAJsonPayload(OMElement element) {
        return (element instanceof OMSourcedElementImpl) && isAJsonPayloadElement(element);
    }

    /**
     * Returns true if the element passed in as the parameter is an element that contains a JSON stream.
     * @param element XML element
     * @return <tt>true</tt> if the element has the local name of a sourced (ie. an <tt>OMSourcedElement</tt>) JSON object.
     */
    public static boolean isAJsonPayloadElement(OMElement element) {
        return element != null
                && (JSON_OBJECT.getLocalPart().equals(element.getLocalName())
                || JSON_ARRAY.getLocalPart().equals(element.getLocalName()));
    }

    /**
     * Returns true if the payload stored in the provided message context is used as a JSON streaming payload.
     * @param messageContext Axis2 Message context
     * @return <tt>true</tt> if the message context contains a Streaming JSON payload.
     */
    public static boolean hasAJsonPayload(MessageContext messageContext) {
        if (messageContext == null) {
            return false;
        }
        SOAPBody b = messageContext.getEnvelope().getBody();
        return b != null && jsonStream(messageContext, false) != null && hasAJsonPayload(b.getFirstElement());
    }

    /**
     * Clones the JSON stream payload contained in the source message context, if any, to the target message context.
     * @param sourceMc Where to get the payload
     * @param targetMc Where to clone and copy the payload
     * @return <tt>true</tt> if the cloning was successful.
     */
    public static boolean cloneJsonPayload(MessageContext sourceMc, MessageContext targetMc) {
        if (!hasAJsonPayload(sourceMc)) {
            return false;
        }
        InputStream json = jsonStream(sourceMc, true);
        try {
            byte[] stream = IOUtils.toByteArray(json);
            newJsonPayload(targetMc, new ByteArrayInputStream(stream), true, true);
        } catch (IOException e) {
            logger.error("#cloneJsonPayload. Could not clone JSON stream. Error>>> " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Sets JSON media type 'application/json' as the message type to the current message context.
     * @param messageContext Axis2 Message context
     */
    public static void setContentType(MessageContext messageContext) {
        if (messageContext == null) {
            return;
        }
        messageContext.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE, "application/json");
    }

    /**
     * Returns a read only, re-readable input stream for an input stream. <br/>
     * The returned input stream cannot be closed, marked, or skipped, but it can be reset to the beginning of the stream.
     * @param inputStream Input stream to be wrapped
     * @return {@link java.io.InputStream}
     */
    public static InputStream toReadOnlyStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        return new ReadOnlyBIS(inputStream);
    }

    /**
     * Returns an input stream that contains the JSON representation of an XML element.
     * @param element XML element of which JSON representation is expected.
     * @return {@link java.io.InputStream}
     */
    public static InputStream toJsonStream(OMElement element) {
        if (element == null) {
            logger.error("#toJsonStream. Could not create input stream from XML element [null]");
            return null;
        }
        org.apache.commons.io.output.ByteArrayOutputStream bos = new org.apache.commons.io.output.ByteArrayOutputStream();
        try {
            JsonUtil.writeAsJson(element.cloneOMElement(), bos);
        } catch (AxisFault axisFault) {
            logger.error("#toJsonStream. Could not create input stream from XML element ["
                    + element.toString() + "]. Error>>> " + axisFault.getLocalizedMessage());
            return null;
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }

    /**
     * Returns a reader that can read from the JSON payload contained in the provided message context as a JavaScript source.<br/>
     * The reader returns the '(' character at the beginning of the stream and marks the end with the ')' character.<br/>
     * The reader returned by this method can be directly used with the JavaScript {@link javax.script.ScriptEngine#eval(java.io.Reader)} method.
     * @param messageContext Axis2 Message context
     * @return {@link java.io.InputStreamReader}
     */
    public static Reader newJavaScriptSourceReader(MessageContext messageContext) {
        InputStream jsonStream = jsonStream(messageContext, true);
        if (jsonStream == null) {
            logger.error("#newJavaScriptSourceReader. Could not create a JavaScript source. Error>>> No JSON stream found.");
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write('(');
            IOUtils.copy(jsonStream, out);
            out.write(')');
            out.flush();
        } catch (IOException e) {
            logger.error("#newJavaScriptSourceReader. Could not create a JavaScript source. Error>>> " + e.getLocalizedMessage());
            return null;
        }
        return new InputStreamReader(new ByteArrayInputStream(out.toByteArray()));
    }

    /**
     * An Un-closable, Read-Only, Reusable, BufferedInputStream
     */
    private static class ReadOnlyBIS extends BufferedInputStream {
        private static final String LOG_STREAM = "org.apache.synapse.commons.json.JsonReadOnlyStream";
        private static final Log logger = LogFactory.getLog(LOG_STREAM);

        public ReadOnlyBIS(InputStream inputStream) {
            super(inputStream);
            super.mark(Integer.MAX_VALUE);
            if (logger.isDebugEnabled()) {
                logger.debug("<init>");
            }
        }

        @Override
        public void close() throws IOException {
            super.reset();
            //super.mark(Integer.MAX_VALUE);
            if (logger.isDebugEnabled()) {
                logger.debug("#close");
            }
        }

        @Override
        public void mark(int readlimit) {
            if (logger.isDebugEnabled()) {
                logger.debug("#mark");
            }
        }

        @Override
        public boolean markSupported() {
            return true; //but we don't mark.
        }

        @Override
        public long skip(long n) {
            if (logger.isDebugEnabled()) {
                logger.debug("#skip");
            }
            return 0;
        }
    }
}
