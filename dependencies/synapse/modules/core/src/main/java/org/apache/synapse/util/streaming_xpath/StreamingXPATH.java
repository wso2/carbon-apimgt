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
package org.apache.synapse.util.streaming_xpath;

import org.antlr.runtime.RecognitionException;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.util.streaming_xpath.compiler.StreamingXPATHCompiler;
import org.apache.synapse.util.streaming_xpath.custom.StreamingParser;
import org.apache.synapse.util.streaming_xpath.exception.StreamingXPATHException;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

public class StreamingXPATH {
    private String xPath;
    private StreamingParser streamingParser;

    /**
     * This constructor is responsible For Create a Custom XPATH Parser Object
     *
     * @param xPath is the XPATH String
     * @throws org.apache.synapse.util.streaming_xpath.exception.StreamingXPATHException
     */
    public StreamingXPATH(String xPath) throws StreamingXPATHException {
        setxPath(xPath);
        try {
            setStreamingParser(StreamingXPATHCompiler.parse(getxPath()));
            if (streamingParser.getFirstComp() == null) {
                throw new StreamingXPATHException();
            }
        } catch (RecognitionException e) {
            throw new StreamingXPATHException(e);
        }
    }

    /**
     * This will return the XPATH expression's result when you provide a Input Stream To a XML
     *
     * @param inputStream for a XML
     * @return Result of the XPATH expression
     * @throws javax.xml.stream.XMLStreamException
     *
     * @throws org.apache.synapse.util.streaming_xpath.exception.StreamingXPATHException
     */
    public String getStringValue(InputStream inputStream) throws XMLStreamException, StreamingXPATHException {
        if (streamingParser != null) {

            return getStreamingParser().process(inputStream);
        }
        return null;
    }

    /**
     * This will return the XPATH expression's result when you provide a Input Stream To a XML
     *
     * @param documentElement for a XML
     * @return Result of the XPATH expression
     * @throws javax.xml.stream.XMLStreamException
     *
     * @throws org.apache.synapse.util.streaming_xpath.exception.StreamingXPATHException
     */
    public String getStringValue(OMElement documentElement) throws XMLStreamException, StreamingXPATHException {
        if (streamingParser != null) {
            return getStreamingParser().process(documentElement);
        }
        return null;
    }

    public String getxPath() {
        return xPath;
    }

    public void setxPath(String xPath) {
        this.xPath = xPath;
    }

    public StreamingParser getStreamingParser() {
        return streamingParser;
    }

    public void setStreamingParser(StreamingParser streamingParser) {
        this.streamingParser = streamingParser;
    }
}
