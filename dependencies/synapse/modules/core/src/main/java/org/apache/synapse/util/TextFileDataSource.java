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

package org.apache.synapse.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDataSourceExt;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.ds.OMDataSourceExtBase;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.util.blob.OverflowBlob;
import org.apache.axiom.util.stax.WrappedTextNodeStreamReader;
import org.apache.axis2.transport.base.BaseConstants;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;

public class TextFileDataSource extends OMDataSourceExtBase {
    private final OverflowBlob overflowBlob;
    private final Charset charset;

    public TextFileDataSource(OverflowBlob overflowBlob, Charset charset) {
        this.overflowBlob = overflowBlob;
        this.charset = charset;
    }
    
    public static OMSourcedElement createOMSourcedElement(OverflowBlob overflowBlob, Charset charset) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        TextFileDataSource txtFileDS = new TextFileDataSource(overflowBlob, charset);
        return new OMSourcedElementImpl(BaseConstants.DEFAULT_TEXT_WRAPPER, fac, txtFileDS);
    }

    @Override
    public void serialize(OutputStream out, OMOutputFormat format) throws XMLStreamException {
        XMLStreamWriter writer = new MTOMXMLStreamWriter(out, format);
        serialize(writer);
        writer.flush();
    }

    @Override
    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        MTOMXMLStreamWriter xmlWriter =
            new MTOMXMLStreamWriter(StAXUtils.createXMLStreamWriter(writer));
        xmlWriter.setOutputFormat(format);
        serialize(xmlWriter);
        xmlWriter.flush();
    }

    @Override
    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        StreamingOMSerializer serializer = new StreamingOMSerializer();
        serializer.serialize(getReader(), xmlWriter);
    }

    public XMLStreamReader getReader() throws XMLStreamException {
        InputStream is;
        try {
            is = overflowBlob.getInputStream();
        }
        catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
        return new WrappedTextNodeStreamReader(
                BaseConstants.DEFAULT_TEXT_WRAPPER, new InputStreamReader(is, charset));
    }

    public Object getObject() {
        return overflowBlob;
    }

    public boolean isDestructiveRead() {
        return false;
    }

    public boolean isDestructiveWrite() {
        return false;
    }
    
    public byte[] getXMLBytes(String encoding) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }

    public void close() {
    }

    public OMDataSourceExt copy() {
        return new TextFileDataSource(overflowBlob, charset);
    }
}
