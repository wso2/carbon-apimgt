/*
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
package org.apache.synapse.util.streaming_xpath.custom;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.synapse.util.streaming_xpath.custom.components.*;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

public class StreamingParser {


    ParserComponent firstComp;
    ParserComponent currentComp;

    public ParserComponent getCurrentComp() {
        return currentComp;
    }

    public void setCurrentComp(ParserComponent currentComp) {
        this.currentComp = currentComp;
    }

    public String process(InputStream xmlIn) throws XMLStreamException {
        StAXOMBuilder builder = new StAXOMBuilder(xmlIn);
        OMElement documentElement = builder.getDocumentElement();
        return firstComp.process(documentElement);
    }

    public String process(OMElement documentElement) throws XMLStreamException {
        return firstComp.process(documentElement);
    }

    public StreamingParser GetChild_GetCurrent() {
        ParserComponent newComp = new GetCurrentParserComponent();
        if (firstComp == null) {
            firstComp = newComp;
            currentComp = newComp;

        } else {
            currentComp.setNext(newComp);
            currentComp = newComp;
        }
        return this;
    }

    public StreamingParser GetChild_GetCurrentMatch(String localName, String nameSpace) {
        ParserComponent newComp = new GetCurrentMatchParserComponent(localName, nameSpace);
        if (firstComp == null) {
            firstComp = newComp;
            currentComp = newComp;

        } else {
            currentComp.setNext(newComp);
            currentComp = newComp;
        }
        return this;
    }

    public StreamingParser GetChild_FirstChild() {
        ParserComponent newComp = new GetFirstChildParserComponent();
        if (firstComp == null) {
            firstComp = newComp;
            currentComp = newComp;

        } else {
            currentComp.setNext(newComp);
            currentComp = newComp;
        }
        return this;
    }

    public StreamingParser GetChild_GetChildrenByName(String localName, String nameSpace) {
        ParserComponent newComp = new GetChildrenByNameParserComponent(localName, nameSpace);
        if (firstComp == null) {
            firstComp = newComp;
            currentComp = newComp;

        } else {
            currentComp.setNext(newComp);
            currentComp = newComp;
        }
        return this;
    }

    public StreamingParser GetChild_GetChildrenByNameRelative(String localName, String nameSpace) {
        ParserComponent newComp = new GetChildrenByNameRelativeParserComponent(localName, nameSpace);
        if (firstComp == null) {
            firstComp = newComp;
            currentComp = newComp;

        } else {
            currentComp.setNext(newComp);
            currentComp = newComp;
        }
        return this;
    }

    public StreamingParser GetChild_GetChildrenWithChild(String localName, String nameSpace) {
        ParserComponent newComp = new GetChildrenWithChildParserComponent(localName, nameSpace);
        if (firstComp == null) {
            firstComp = newComp;
            currentComp = newComp;

        } else {
            currentComp.setNext(newComp);
        }
        return this;
    }

    public StreamingParser GetChild_GetChildrenWithChildValue(String localName, String nameSpace, String value) {
        ParserComponent newComp = new GetChildrenWithChildValueParserComponent(localName, nameSpace, value);
        if (firstComp == null) {
            firstComp = newComp;
            currentComp = newComp;

        } else {
            currentComp.setNext(newComp);
        }
        return this;
    }

    public StreamingParser GetChild_GetChildrenWithAttribute(String localName, String nameSpace) {
        ParserComponent newComp = new GetChildrenWithAttributeParserComponent(localName, nameSpace);
        if (firstComp == null) {
            firstComp = newComp;
            currentComp = newComp;

        } else {
            currentComp.setNext(newComp);
        }
        return this;
    }

    public StreamingParser GetChild_GetChildrenWithAttributeValue(String localName, String nameSpace, String value) {
        ParserComponent newComp = new GetChildrenWithAttributeValueParserComponent(localName, nameSpace, value);
        if (firstComp == null) {
            firstComp = newComp;
            currentComp = newComp;

        } else {
            currentComp.setNext(newComp);
        }
        return this;
    }

    public StreamingParser GetChild_GetAttribute(String localName, String nameSpace) {
        ParserComponent newComp = new GetAttributeParserComponent(localName, nameSpace);
        if (firstComp == null) {
            firstComp = newComp;
            currentComp = newComp;

        } else {
            currentComp.setNext(newComp);
        }
        return this;
    }

    public ParserComponent getFirstComp() {
        return firstComp;
    }

    public void processOM(OMElement om) {
        firstComp.process(om);
    }
}
