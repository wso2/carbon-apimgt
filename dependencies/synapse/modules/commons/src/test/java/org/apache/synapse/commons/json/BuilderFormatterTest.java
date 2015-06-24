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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public class BuilderFormatterTest extends TestCase {
    private static final String jsonIn1 = "{\"value\":12.4,\"12X12\":\"http://localhost/images/id/img_0_24_24.png\",\"0\":{\"date\":\"19/12/12\",\"venue\":\"NONVEG\"},\"1\":[1,2,{\"newArray\":[\"one-Element\"]}]}";
    private static final String xml1 = "<jsonObject><value>12.4</value><_JsonReader_PD_12X12>http://localhost/images/id/img_0_24_24.png</_JsonReader_PD_12X12><_JsonReader_PD_0><date>19/12/12</date><venue>NONVEG</venue></_JsonReader_PD_0><?xml-multiple  1?><_JsonReader_PD_1>1</_JsonReader_PD_1><_JsonReader_PD_1>2</_JsonReader_PD_1><_JsonReader_PD_1><?xml-multiple  newArray?><newArray>one-Element</newArray></_JsonReader_PD_1></jsonObject>";

    private static final String jsonIn2 = "{\"type\":\"Polygon\",\"coordinates\":[[[116.0865381,-8.608804],[116.127196,-8.608804],[116.127196,-8.554822],[116.0865381,-8.554822]]]}";
    private static final String xml2 = "<jsonObject><type>Polygon</type><?xml-multiple  coordinates?><coordinates><jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement>116.0865381</jsonElement><jsonElement>-8.608804</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement>116.127196</jsonElement><jsonElement>-8.608804</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement>116.127196</jsonElement><jsonElement>-8.554822</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement>116.0865381</jsonElement><jsonElement>-8.554822</jsonElement></jsonArray></jsonElement></jsonArray></coordinates></jsonObject>";

    private static final String jsonIn3 = "[[[116.0865381,-8.608804],[116.127196,-8.608804],[116.127196,-8.554822],[116.0865381,-8.554822]]]";
    private static final String xml3 = "<jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement>116.0865381</jsonElement><jsonElement>-8.608804</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement>116.127196</jsonElement><jsonElement>-8.608804</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement>116.127196</jsonElement><jsonElement>-8.554822</jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement>116.0865381</jsonElement><jsonElement>-8.554822</jsonElement></jsonArray></jsonElement></jsonArray></jsonElement></jsonArray>";

    private static final String jsonIn4 = "[1]";
    private static final String xml4 = "<jsonArray><?xml-multiple  jsonElement?><jsonElement>1</jsonElement></jsonArray>";

    private static final String jsonIn5 = "{\"array\":[1,2,3]}";
    private static final String xml5 = "<jsonObject><?xml-multiple  array?><array>1</array><array>2</array><array>3</array></jsonObject>";

    private static final String jsonIn6 = "{\"array\":[[1,2,4]]}";
    private static final String xml6 = "<jsonObject><?xml-multiple  array?><array><jsonArray><?xml-multiple  jsonElement?><jsonElement>1</jsonElement><jsonElement>2</jsonElement><jsonElement>4</jsonElement></jsonArray></array></jsonObject>";

    private static final String jsonIn7 = "{\"$schema\":\"UNKNOWN\"}";
    private static final String xml7 = "<jsonObject><_JsonReader_PS_schema>UNKNOWN</_JsonReader_PS_schema></jsonObject>";

    private static final String jsonIn8 = "{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[]}";
    private static final String xml8 = "<jsonObject><?xml-multiple  hashtags?><?xml-multiple  symbols?><?xml-multiple  urls?><?xml-multiple  user_mentions?></jsonObject>";

    private static final String jsonIn10 = "{}";
    private static final String xml10 = "<jsonObject><jsonEmpty>_JsonScanner_EMPTY_OBJECT</jsonEmpty></jsonObject>";

    private static final String jsonIn11 = "[null,null,1]";
    private static final String xml11 = "<jsonArray><?xml-multiple  jsonElement?><jsonElement /><jsonElement /><jsonElement>1</jsonElement></jsonArray>";

    private static final String jsonIn12 = "{\"value\":null}";
    private static final String xml12 = "<jsonObject><value /></jsonObject>";

    private static final String jsonIn13 = "{\"value\":{}}";
    private static final String xml13 = "<jsonObject><value><jsonEmpty>_JsonScanner_EMPTY_OBJECT</jsonEmpty></value></jsonObject>";

    private static final String jsonIn14 = "[null,null,1,{}]";
    private static final String xml14 = "<jsonArray><?xml-multiple  jsonElement?><jsonElement /><jsonElement /><jsonElement>1</jsonElement><jsonElement><jsonEmpty>_JsonScanner_EMPTY_OBJECT</jsonEmpty></jsonElement></jsonArray>";

    private static final String jsonIn15 = "[[],[[[],[]],[]]]";
    private static final String xml15 = "<jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonArray><?xml-multiple  jsonElement?></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonArray><?xml-multiple  jsonElement?></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?></jsonArray></jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?></jsonArray></jsonElement></jsonArray></jsonElement></jsonArray>";

    private static final String jsonIn16 = "[[],[[[],[[],{},{\"empty\":{}}]],[{},{\"array\":[1,2,3]}]]]";
    private static final String xml16 = "<jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonArray><?xml-multiple  jsonElement?></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonArray><?xml-multiple  jsonElement?></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonArray><?xml-multiple  jsonElement?></jsonArray></jsonElement><jsonElement><jsonEmpty>_JsonScanner_EMPTY_OBJECT</jsonEmpty></jsonElement><jsonElement><empty><jsonEmpty>_JsonScanner_EMPTY_OBJECT</jsonEmpty></empty></jsonElement></jsonArray></jsonElement></jsonArray></jsonElement><jsonElement><jsonArray><?xml-multiple  jsonElement?><jsonElement><jsonEmpty>_JsonScanner_EMPTY_OBJECT</jsonEmpty></jsonElement><jsonElement><?xml-multiple  array?><array>1</array><array>2</array><array>3</array></jsonElement></jsonArray></jsonElement></jsonArray></jsonElement></jsonArray>";

    private static final String jsonIn17 = "{\"root\":[{\"level_1\":[{\"level_2\":[[1,2,3]]}]},{},{\"level_12\":[]}]}";
    private static final String xml17 = "<jsonObject><?xml-multiple  root?><root><?xml-multiple  level_1?><level_1><?xml-multiple  level_2?><level_2><jsonArray><?xml-multiple  jsonElement?><jsonElement>1</jsonElement><jsonElement>2</jsonElement><jsonElement>3</jsonElement></jsonArray></level_2></level_1></root><root><jsonEmpty>_JsonScanner_EMPTY_OBJECT</jsonEmpty></root><root><?xml-multiple  level_12?></root></jsonObject>";

    private static final String jsonIn18 = "{\"source\":\"\\u003ca href=\\\"http:\\/\\/www.tweetdeck.com\\\" rel=\\\"nofollow\\\"\\u003eTweetDeck\\u003c\\/a\\u003e\",\"truncated\":false}";

    public void testCase1() {
        runTest(jsonIn1, xml1);
    }

    public void testCase2() {
        runTest(jsonIn2, xml2);
    }

    public void testCase3() {
        runTest(jsonIn3, xml3);
    }

    public void testCase4() {
        runTest(jsonIn4, xml4);
    }

    public void testCase5() {
        runTest(jsonIn5, xml5);
    }

    public void testCase6() {
        runTest(jsonIn6, xml6);
    }

    public void testCase7() {
        runTest(jsonIn7, xml7);
    }

    public void testCase8() {
        runTest(jsonIn8, xml8);
    }

    public void testCase9() {
        try {
            MessageContext message = Util.newMessageContext();
            Builder jsonBuilder = Util.newJsonBuilder();
            InputStream inputStream = Util.getJson(1);
            OMElement element  = jsonBuilder.processDocument(inputStream, "application/json", message);
            message.getEnvelope().getBody().addChild(element);
            //System.out.println(element.toString());
            //assertTrue(xmlOut.equals(element.toString()));

            OutputStream out = Util.newOutputStream();
            MessageFormatter formatter = Util.newJsonFormatter();
            formatter.writeTo(message, null, out, false);
            //String outStr = new String(((ByteArrayOutputStream) out).toByteArray());
            //assertTrue(jsonIn.equals(outStr));
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            assertTrue(false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            assertFalse(true);
        }
    }

    public void testCase10() {
        // Disabling this test case because we disabled adding <jsonEmpty/> instruction to the xml tree from the scanner.
        // Disabled test cases 13, 14, 16, and 17 for the same reason.
        //runTest(jsonIn10, xml10);
    }

    public void testCase11() {
        runTest(jsonIn11, xml11);
    }

    public void testCase12() {
        runTest(jsonIn12, xml12);
    }

    public void testCase13() {
        //runTest(jsonIn13, xml13);
    }

    public void testCase14() {
        //runTest(jsonIn14, xml14);
    }

    public void testCase15() {
        runTest(jsonIn15, xml15);
    }

    public void testCase16() {
        //runTest(jsonIn16, xml16);
    }

    public void testCase17() {
        //runTest(jsonIn17, xml17);
    }
    private Builder jsonBuilder = Util.newJsonBuilder();
    private MessageFormatter formatter = Util.newJsonFormatter();


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
