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

import java.io.FileNotFoundException;
import java.io.InputStream;

public class JsonBuilderTest extends TestCase {
    private static final String xmlWithPi = "<jsonObject><id>0001</id><ok>true</ok><amount>5250</amount><?xml-multiple  url?><url>http://org.wso2.json/32_32</url></jsonObject>";

    private static final String xmlWithoutPi = "<jsonObject><id>0001</id><ok>true</ok><amount>5250</amount><url>http://org.wso2.json/32_32</url></jsonObject>";

    private static final String xmlOutWithPI = "<jsonObject><pizza><name>Meat Sizzler</name><price>500.0</price><toppings><?xml-multiple  topping?><topping><id>9999</id><name>Steak</name><extraPrice>4.00</extraPrice><category>NONVEG</category></topping><topping><id>9998</id><name>Sun Dried Tomato</name><extraPrice>4.00</extraPrice><category>VEGETARIAN</category></topping><topping><id>9997</id><name>Mixed Peppers</name><extraPrice>3.00</extraPrice><category>VEGETARIAN</category></topping><topping><id>9996</id><name>Cajun Chicken</name><extraPrice>3.00</extraPrice><category>NONVEG</category></topping><topping><id>9995</id><name>Chorizo Sausage</name><extraPrice>4.00</extraPrice><category>NONVEG</category></topping></toppings></pizza></jsonObject>";

    private static final String xmlOutWithoutPI = "<jsonObject><pizza><name>Meat Sizzler</name><price>500.0</price><toppings><topping><id>9999</id><name>Steak</name><extraPrice>4.00</extraPrice><category>NONVEG</category></topping><topping><id>9998</id><name>Sun Dried Tomato</name><extraPrice>4.00</extraPrice><category>VEGETARIAN</category></topping><topping><id>9997</id><name>Mixed Peppers</name><extraPrice>3.00</extraPrice><category>VEGETARIAN</category></topping><topping><id>9996</id><name>Cajun Chicken</name><extraPrice>3.00</extraPrice><category>NONVEG</category></topping><topping><id>9995</id><name>Chorizo Sausage</name><extraPrice>4.00</extraPrice><category>NONVEG</category></topping></toppings></pizza></jsonObject>";

    public void testCase() {
        try {
            InputStream inputStream = Util.getJson(0);
            OMElement element = JsonUtil.toXml(inputStream, true);
            assertTrue(xmlWithPi.equals(element.toString()));

            inputStream = Util.getJson(0);
            element = JsonUtil.toXml(inputStream, false);
            assertTrue(xmlWithoutPi.equals(element.toString()));
        } catch (FileNotFoundException e) {
            System.err.println("Could not create input stream. ERROR>>>\n" + e);
            assertTrue(false);
        } catch (AxisFault e) {
            System.out.println("Could not convert to JSON. ERROR>>>\n" + e);
            assertTrue(false);
        }
    }

    public void testCase2() {
        try {
            InputStream inputStream = Util.newInputStream(JsonFormatterTest.jsonOut.getBytes());
            OMElement element = JsonUtil.toXml(inputStream, true);
            assertTrue(xmlOutWithPI.equals(element.toString()));

            inputStream = Util.newInputStream(JsonFormatterTest.jsonOut.getBytes());
            element = JsonUtil.toXml(inputStream, false);
            assertTrue(xmlOutWithoutPI.equals(element.toString()));
        } catch (AxisFault e) {
            System.out.println("Could not convert to JSON. ERROR>>>\n" + e);
            assertTrue(false);
        }
    }
}
