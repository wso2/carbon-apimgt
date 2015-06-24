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

import junit.framework.TestCase;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.util.blob.OverflowBlob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;

public class TextFileDataSourceTest extends TestCase {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    private OMSourcedElement createSourcedElement(String content, Charset charset) throws IOException {
        OverflowBlob tmp = new OverflowBlob(4, 1024, "tmp_", ".dat");
        OutputStream out = tmp.getOutputStream();
        out.write(content.getBytes(charset.name()));
        out.close();
        return TextFileDataSource.createOMSourcedElement(tmp, charset);
    }
    
    public void testWithXMLChars() throws Exception {
        String testString = "Test string with ampersand (&)";
        OMSourcedElement element = createSourcedElement(testString, UTF8);
        assertEquals(testString, element.getText());
    }
    
    public void testSerializeToBytes() throws Exception {
        OMSourcedElement element = createSourcedElement("test", UTF8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        element.serialize(baos);
        byte[] actual = baos.toByteArray();
        baos.reset();
        // We validate the result by creating an equivalent OMElement
        // and calling serialize on it. The two results must be identical.
        element.cloneOMElement().serialize(baos);
        byte[] expected = baos.toByteArray();
        assertTrue(Arrays.equals(expected, actual));
    }
    
    public void testSerializeToChars() throws Exception {
        OMSourcedElement element = createSourcedElement("test", UTF8);
        StringWriter sw = new StringWriter();
        element.serialize(sw);
        String actual = sw.toString();
        sw.getBuffer().setLength(0);
        // Compare with the behavior of an equivalent OMElement
        element.cloneOMElement().serialize(sw);
        String expected = sw.toString();
        assertEquals(expected, actual);
    }
}
