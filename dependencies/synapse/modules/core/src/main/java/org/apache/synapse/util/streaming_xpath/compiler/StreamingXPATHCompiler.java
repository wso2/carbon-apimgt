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
package org.apache.synapse.util.streaming_xpath.compiler;


import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.synapse.util.streaming_xpath.custom.StreamingParser;

public class StreamingXPATHCompiler {
    /**
     * This will create the Custom XPATH Parser Components for a given XPATH. This will Use the Antlr Grammar XPATH1.0 for this process.
     *
     * @param source is the String for XPATH Expression
     * @return A Custom XPATH Parser Component Chain
     * @throws RecognitionException
     */
    public static StreamingParser parse(String source) throws RecognitionException {

        XPath1Lexer lexer = new XPath1Lexer();
        lexer.setCharStream(new ANTLRStringStream(source));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XPath1Parser parser = new XPath1Parser(tokens);

        org.apache.synapse.util.streaming_xpath.compiler.XPath1Parser.xpath_return r = parser.xpath();
        CommonTree t = (CommonTree) r.getTree();

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        nodes.setTokenStream(tokens);
        XPath1Walker walker = new XPath1Walker(nodes);
        walker.xpath=source;
        return walker.xpath();
    }
}
