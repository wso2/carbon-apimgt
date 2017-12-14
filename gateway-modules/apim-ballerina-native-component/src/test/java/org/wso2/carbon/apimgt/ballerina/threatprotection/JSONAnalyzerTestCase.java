/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.ballerina.threatprotection;


import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.ballerina.threatprotection.analyzer.JSONAnalyzer;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.JSONConfig;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.XMLConfig;

public class JSONAnalyzerTestCase {
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testConfigureAnalyzerException() throws Exception {
        JSONAnalyzer analyzer = new JSONAnalyzer();
        XMLConfig config = new XMLConfig();
        analyzer.configure(config);
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testJsonDepthAnalyzeFail() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxJsonDepth()).thenReturn(3);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"a\": {\"b\": {\"c\": {\"d\": 1}}}}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test
    public void testJsonDepthAnalyzePass() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxJsonDepth()).thenReturn(4);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"a\": {\"b\": {\"c\": {\"d\": 1}}}}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testCheckMaxStringLengthFail() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxStringLength()).thenReturn(10);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"a\": \"abcdef123456\"}";
        analyzer.analyze(jsonString, "/foo2");
    }

    @Test
    public void testCheckMaxStringLengthPass() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxStringLength()).thenReturn(10);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"a\": \"abcdef1234\"}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testMaxArrayElementCountFail() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxArrayElementCount()).thenReturn(5);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"a\": [1, 2, 3, 4, 5, 6]}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test
    public void testMaxArrayElementCountPass() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxArrayElementCount()).thenReturn(5);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"a\": [1, 2, 3, 4, 5]}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testMaxFieldCountFail() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxPropertyCount()).thenReturn(3);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"a\": [1, 2, 3, 4, 5, 6], \"b\": 1, \"c\": 2, \"d\": 3, \"e\": 5}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test
    public void testMaxFieldCountPass() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxPropertyCount()).thenReturn(5);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"a\": [1, 2, 3, 4, 5, 6], \"b\": 1, \"c\": 2, \"d\": 3, \"e\": 5}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testMaxFieldLengthFail() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxKeyLength()).thenReturn(5);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"abcdef\": [1, 2, 3, 4, 5, 6]}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test
    public void testMaxFieldLengthPass() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxKeyLength()).thenReturn(5);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"abcde\": [1, 2, 3, 4, 5]}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testMaxStringLengthInsideAnArrayFail() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxStringLength()).thenReturn(5);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"abcdef\": [1, \"123456\", 3, 4, 5, 6]}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test
    public void testMaxStringLengthInsideAnArrayPass() throws Exception {
        JSONConfig config = Mockito.mock(JSONConfig.class);
        Mockito.when(config.getMaxStringLength()).thenReturn(5);

        JSONAnalyzer analyzer = new JSONAnalyzer();
        analyzer.configure(config);

        String jsonString = "{\"abcdef\": [1, \"12345\", 3, 4, 5, 6]}";
        analyzer.analyze(jsonString, "/foo");
    }

    @Test(expectedExceptions = APIMThreatAnalyzerException.class)
    public void testInvalidJsonPayload() throws Exception {
        JSONAnalyzer analyzer = new JSONAnalyzer();

        String jsonString = "{abcdef: {\"abc\"}:\", \"123456\", 3, 4, 5, 6]}";
        analyzer.analyze(jsonString, "/foo");
    }
}
