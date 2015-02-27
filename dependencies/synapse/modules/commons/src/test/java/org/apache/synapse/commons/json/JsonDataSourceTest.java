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

import junit.framework.TestCase;
import org.apache.axiom.om.OMDataSource;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

public class JsonDataSourceTest extends TestCase {
    public static final String expectedJSON = "{\n" +
                                               "    \"id\":\"0001\",\n" +
                                               "    \"ok\":true,\n" +
                                               "    \"amount\":5250,\n" +
                                               "    \"url\" : [\n" +
                                               "            \"http://org.wso2.json/32_32\"\n" +
                                               "    ]\n" +
                                               "}\n";

    public static final String expectedXML = "<?xml version='1.0' encoding='UTF-8'?><jsonObject><id>0001</id><ok>true</ok><amount>5250</amount><?xml-multiple url?><url>http://org.wso2.json/32_32</url></jsonObject>";

    public void testCase() {
        try {
            InputStream inputStream = Util.getJson(0);
            OMDataSource jsonData = Util.newJsonDataSource(inputStream);
            OutputStream outputStream = Util.newOutputStream();
            jsonData.serialize(outputStream, null);
            assertTrue(expectedJSON.equals(outputStream.toString()));

            inputStream = Util.getJson(0);
            jsonData = Util.newJsonDataSource(inputStream);
            Writer stringWriter = new StringWriter();
            jsonData.serialize(stringWriter, null);
            assertTrue(expectedJSON.equals(stringWriter.toString()));

            inputStream = Util.getJson(0);
            jsonData = Util.newJsonDataSource(inputStream);
            outputStream = Util.newOutputStream();
            XMLStreamWriter xmlWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream);
            jsonData.serialize(xmlWriter);
            assertTrue(expectedXML.equals(outputStream.toString()));
        } catch (FileNotFoundException e) {
            System.err.println("Could not create input stream. ERROR>>>\n" + e);
            assertTrue(false);
        } catch (XMLStreamException e) {
            System.err.println("Could not serialize JSON. ERROR>>>\n" + e);
            assertTrue(false);
        } catch (Exception e) {
            System.err.println("Could not run test. ERROR>>>\n" + e);
            assertTrue(false);
        }
    }
}
