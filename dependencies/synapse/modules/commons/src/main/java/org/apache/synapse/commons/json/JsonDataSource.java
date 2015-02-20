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

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;

final class JsonDataSource implements OMDataSource {
    private static final Log logger = LogFactory.getLog(JsonDataSourceImpl.class.getName());

    private final InputStream inputStream;

    public JsonDataSource(InputStream inputStream) {
        if (inputStream instanceof BufferedInputStream) {
            this.inputStream = inputStream;
            // and we assume that this passed in input stream is fresh and it has been marked first.
        } else if (inputStream != null) {
            this.inputStream = new BufferedInputStream(inputStream);
            this.inputStream.mark(Integer.MAX_VALUE);
        } else {
            this.inputStream = null;
            logger.error("#JsonDataSource. Created invalid JSON DataSource. No JSON input stream found.");
        }
    }

    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        try {
            if (format != null && format.getContentType() != null) {
                if (format.getContentType().contains("xml")) {
                    inputStream.reset(); // reuse the stream
                    JsonUtil.toXml(inputStream, false).serialize(output, format);
                    return;
                }
            }
            inputStream.reset();
            IOUtils.copy(inputStream, output);
        } catch (IOException e) {
            logger.error("#serialize:OutputStream. Could not serialize JSON payload. Error>>> " + e.getLocalizedMessage());
            throw new OMException("Could not serialize JSON payload.", e);
        }
    }

    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        try {
            if (format != null && format.getContentType() != null) {
                if (format.getContentType().contains("xml")) {
                    inputStream.reset(); // reuse the stream
                    JsonUtil.toXml(inputStream, false).serialize(writer, format);
                    return;
                }
            }
            inputStream.reset();
            IOUtils.copy(inputStream, writer);
        } catch (IOException e) {
            logger.error("#serialize:Writer. Could not serialize JSON payload. Error>>> " + e.getLocalizedMessage());
            throw new OMException("Could not serialize JSON payload.", e);
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
        try {
            inputStream.reset();
        } catch (IOException e) {
            logger.error("#getReader. Could not reuse JSON stream from JSON Data Source. Error>> " + e.getLocalizedMessage());
            throw new XMLStreamException("Could not reuse JSON stream from JSON Data Source.", e);
        }
        return JsonUtil.getReader(inputStream, false); // Do not add PIs to the XML output of this reader
    }
}
