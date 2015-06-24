/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.commons.json;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

final class JsonDataSourceImpl implements OMDataSource {
    public static final String END_OBJECT = "}";
    public static final String END_ARRAY = "}}";
    public static final String EMPTY_OBJECT = "{}";
    public static final String WRAPPER_OBJECT = "{" + Constants.K_OBJECT + ":";
    public static final String WRAPPER_ARRAY = "{" + Constants.K_ARRAY + ":{" + Constants.K_ARRAY_ELEM + ":";

    private byte[] stream;
    private int offset = 0;
    private int length = 0;
    private boolean isObject = true;

    private static final JsonXMLConfig xmlConfig = new JsonXMLConfigBuilder()
            .multiplePI(true)
            .autoArray(true)
            .autoPrimitive(true)
            .namespaceDeclarations(false)
            .build();

    private static final JsonXMLInputFactory xmlInputFactory = new JsonXMLInputFactory(xmlConfig);

    /** Configuration used to produce XML that has no processing instructions in it. */
    private static final JsonXMLConfig xmlConfigNoPIs = new JsonXMLConfigBuilder()
            .multiplePI(false)
            .autoArray(true)
            .autoPrimitive(true)
            .namespaceDeclarations(false)
            .build();

    private static final JsonXMLInputFactory xmlInputFactoryNoPIs = new JsonXMLInputFactory(xmlConfigNoPIs);

    private static final Log logger = LogFactory.getLog(JsonDataSourceImpl.class.getName());

    public JsonDataSourceImpl(InputStream inputStream) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int c;
        try {
            while (((c = inputStream.read()) != -1) && Character.isWhitespace((char)c)) {}
            ++length;
            if (c == '{') {
                stream.write(WRAPPER_OBJECT.getBytes());
                offset = WRAPPER_OBJECT.length();
            } else if (c == '[') {
                stream.write(WRAPPER_ARRAY.getBytes());
                isObject = false;
                offset = WRAPPER_ARRAY.length();
            } else {
                logger.error("Could not create a JSON data source from the input stream. Found '"
                             + ((char) c) + "' at the start of the input stream.") ;
                this.stream = EMPTY_OBJECT.getBytes();
                return;
            }
            stream.write(c);
            while (((c = inputStream.read()) != -1)) {
                stream.write(c);
                ++length;
            }
            if (isObject) {
                stream.write(END_OBJECT.getBytes());
            } else {
                stream.write(END_ARRAY.getBytes());
            }
            stream.flush();
            this.stream = stream.toByteArray();
            if (logger.isDebugEnabled()) {
                logger.debug("Built JSON Data Source from the incoming stream.");
            }
        } catch (IOException e) {
            logger.error("Could not create a JSON data source from the input stream. "
                         + e.getLocalizedMessage());
            this.stream = EMPTY_OBJECT.getBytes();
            isObject = true;
        }
    }

    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        try {
            output.write(stream, offset, length);
        } catch (IOException e) {
            throw new OMException("Could not serialize payload. " + e.getLocalizedMessage());
        }
    }

    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        try {
            IOUtils.copy(new ByteArrayInputStream(stream, offset, length), writer);
        } catch (IOException e) {
            throw new OMException("Could not serialize payload. " + e.getLocalizedMessage());
        }
    }

    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        XMLStreamReader reader = getReader();
        xmlWriter.writeStartDocument();
        while (reader.hasNext()) {
            int x = reader.next();
            switch (x) {
                case XMLStreamConstants.START_ELEMENT:
                    xmlWriter.writeStartElement(reader.getPrefix(), reader.getLocalName(),
                                                reader.getNamespaceURI());
                    int namespaceCount = reader.getNamespaceCount();
                    for (int i = namespaceCount - 1; i >= 0; i--) {
                        xmlWriter.writeNamespace(reader.getNamespacePrefix(i),
                                                 reader.getNamespaceURI(i));
                    }
                    int attributeCount = reader.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        xmlWriter.writeAttribute(reader.getAttributePrefix(i),
                                                 reader.getAttributeNamespace(i),
                                                 reader.getAttributeLocalName(i),
                                                 reader.getAttributeValue(i));
                    }
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.CHARACTERS:
                    xmlWriter.writeCharacters(reader.getText());
                    break;
                case XMLStreamConstants.CDATA:
                    xmlWriter.writeCData(reader.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    xmlWriter.writeEndElement();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    xmlWriter.writeEndDocument();
                    break;
                case XMLStreamConstants.SPACE:
                    break;
                case XMLStreamConstants.COMMENT:
                    xmlWriter.writeComment(reader.getText());
                    break;
                case XMLStreamConstants.DTD:
                    xmlWriter.writeDTD(reader.getText());
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    xmlWriter
                            .writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    xmlWriter.writeEntityRef(reader.getLocalName());
                    break;
                default :
                    throw new OMException();
            }
        }
        xmlWriter.writeEndDocument();
        xmlWriter.flush();
        xmlWriter.close();
    }

    public XMLStreamReader getReader() throws XMLStreamException {
        return new JsonReaderDelegate(xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(stream)), false);
    }
}
