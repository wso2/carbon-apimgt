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
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.commons.io.IOUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;

public class StreamBuilderFormatterTest extends TestCase {
    private Builder jsonBuilder = Util.newJsonStreamBuilder();
    private MessageFormatter formatter = Util.newJsonStreamFormatter();

    private static final String jsonIn2 = "{\"type\":\"Polygon\",\"coordinates\":[[[116.0865381,-8.608804],[116.127196,-8.608804],[116.127196,-8.554822],[116.0865381,-8.554822]]]}";
    private static final String xml2 = "<jsonObject><type>Polygon</type><coordinates><jsonArray><jsonElement><jsonArray><jsonElement>116.0865381</jsonElement><jsonElement>-8.608804</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><jsonElement>116.127196</jsonElement><jsonElement>-8.608804</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><jsonElement>116.127196</jsonElement><jsonElement>-8.554822</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><jsonElement>116.0865381</jsonElement><jsonElement>-8.554822</jsonElement></jsonArray></jsonElement></jsonArray></coordinates></jsonObject>";

    private static final String jsonIn3 = "[[[116.0865381,-8.608804],[116.127196,-8.608804],[116.127196,-8.554822],[116.0865381,-8.554822]]]";
    private static final String xml3 = "<jsonArray><jsonElement><jsonArray><jsonElement><jsonArray><jsonElement>116.0865381</jsonElement><jsonElement>-8.608804</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><jsonElement>116.127196</jsonElement><jsonElement>-8.608804</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><jsonElement>116.127196</jsonElement><jsonElement>-8.554822</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><jsonElement>116.0865381</jsonElement><jsonElement>-8.554822</jsonElement></jsonArray></jsonElement></jsonArray></jsonElement></jsonArray>";

    private static final String jsonIn4 = "[\n" +
            " {\"name\":\"Ishan\", \"phone\":\"+94 (11) 222 3333\"},\n" +
            " {\"name\":null, \"phone\":{}},\n" +
            " {\"name\":\"WSO2\", \"phone\":\"+94 (11) 222 3333\"}\n" +
            "]";
    private static final String xml4 = "<jsonArray><jsonElement><name>Ishan</name><phone>+94 (11) 222 3333</phone></jsonElement><jsonElement><name /><phone /></jsonElement><jsonElement><name>WSO2</name><phone>+94 (11) 222 3333</phone></jsonElement></jsonArray>";

    private static final String jsonIn5 = "{ \"id\":12345, \"id_str\":\"12345\", \"array\":[1, 2, [[],[{\"inner_id\":6789}]]], \"name\":null, \"object\":{}, \"$schema_location\":\"unknown\", \"12X12\":\"image12x12.png\"}";
    private static final String xml5 = "<jsonObject><id>12345</id><id_str>12345</id_str><array>1</array><array>2</array><array><jsonArray><jsonElement><jsonArray /></jsonElement><jsonElement><jsonArray><jsonElement><inner_id>6789</inner_id></jsonElement></jsonArray></jsonElement></jsonArray></array><name /><object /><_JsonReader_PS_schema_location>unknown</_JsonReader_PS_schema_location><_JsonReader_PD_12X12>image12x12.png</_JsonReader_PD_12X12></jsonObject>";

    private static final String xmlString =
            "<object xmlns:ns1=\"namespace_1\" xmlns:ns2=\"namespace_2\" xmlns:n3=\"namespace_3\"" +
                    " attrb_1=\"Value1\" ns2:attrb_11=\"Value2\">\n" +
            "  <child c_attrb=\"Child\">Child Text</child>\n" +
            "  <inner xmlns:p=\"item:Namespace\" p:c_attrb=\"Inner\"><e>Text</e></inner>\n" +
            "  <none>\n" +
            "   <noneChild>\n" +
            "    <key>IDString</key>\n" +
            "    <value>00200je832oiuh91hnu8833</value>\n" +
            "   </noneChild>\n" +
            "  </none>\n" +
            "</object>";
    private static final String jsonString = "{\"object\":{\"@attrb_1\":\"Value1\",\"@attrb_11\":\"Value2\",\"child\":{\"@c_attrb\":\"Child\",\"$\":\"Child Text\"},\"inner\":{\"@c_attrb\":\"Inner\",\"e\":\"Text\"},\"none\":{\"noneChild\":{\"key\":\"IDString\",\"value\":\"00200je832oiuh91hnu8833\"}}}}";
    private static final String convertedXml = "<jsonObject><object attrb_1=\"Value1\" attrb_11=\"Value2\"><child c_attrb=\"Child\">Child Text</child><inner c_attrb=\"Inner\"><e>Text</e></inner><none><noneChild><key>IDString</key><value>00200je832oiuh91hnu8833</value></noneChild></none></object></jsonObject>";

    public void test1() {
        runTest(jsonIn2, xml2);
    }

    public void test3() {
        runTest(jsonIn3, xml3);
    }

    public void test4() {
        runTest(jsonIn4, xml4);
    }

    public void test5() {
        runTest(jsonIn5, xml5);
    }

    public void test2()  {
        try {
            InputStream is = Util.getJson(1);
            MessageContext message = Util.newMessageContext();

            OMElement element  = jsonBuilder.processDocument(is, "application/json", message);

            OutputStream out = Util.newOutputStream();
            formatter.writeTo(message, null, out, false);
            InputStream outContent = new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());
            InputStream compare = Util.getJson(1);
            assertTrue(IOUtils.contentEquals(outContent, compare));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test6() {
        runTest(jsonString, convertedXml);
    }

    public  void test7() {
        // The Input XML must be such that 1) it does not have mixed content 2) no attributes have same name even they
        // belong to different namespaces.
        // mixed content is not supported. ie both Text nodes and Elements as child nodes of an element.
        // cannot have same attribute names even with different ns s. That's because, we remove all namespace declarations
        // before converting to JSON.
        try {
            MessageContext message = null;
            try {
                message = Util.newMessageContext(xmlString);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
            //System.out.println(message.getEnvelope().getBody().toString());
            OutputStream out = Util.newOutputStream();
            formatter.writeTo(message, null, out, false);
            String outStr = new String(((ByteArrayOutputStream) out).toByteArray());
            //System.out.println(outStr);
            assertTrue(jsonString.equals(outStr));
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            assertTrue(false);
        }
    }

    public  void runTest(String jsonIn, String xmlOut) {
        try {
            MessageContext message = Util.newMessageContext();
            InputStream inputStream = Util.newInputStream(jsonIn.getBytes());
            OMElement element  = jsonBuilder.processDocument(inputStream, "application/json", message);
            message.getEnvelope().getBody().addChild(element);
            //System.out.println(element.toString());
            assertTrue(xmlOut.equals(element.toString()));

            OutputStream out = Util.newOutputStream();
            formatter.writeTo(message, null, out, false);
            String outStr = new String(((ByteArrayOutputStream) out).toByteArray());

            //System.out.println(outStr);
            assertTrue(jsonIn.equals(outStr));
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            assertTrue(false);
        }
    }
}
