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

package org.wso2.carbon.apimgt.ballerina.publisher;

import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.ballerina.publisher.util.DASThriftTestServer;
import org.wso2.carbon.apimgt.ballerina.publisher.util.DataPublisherTestUtil;
import org.wso2.carbon.apimgt.ballerina.publisher.util.StreamDefinitions;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache Test class which handles caching test for ballerina native cache implementation for API Manager
 *
 *  @since 0.10-SNAPSHOT
 */
public class EventPublisherTestCase {
    private ProgramFile bLangProgram;
    private static final DASThriftTestServer thriftTestServer = new DASThriftTestServer();
    private final int thriftServerListenPort = 7614;
    private static final long WAIT_TIME = 300 * 1000;
    private static final Logger log = LoggerFactory.getLogger(EventPublisherTestCase.class);
    private static final String BALLERINA_TEST_SCRIPT_LOCATION = "samples" + File.separator + "publisher"
                                                                 + File.separator + "publisherTest.bal";
    @BeforeClass
    public void setup() throws DataBridgeException, StreamDefinitionStoreException,
                                                                MalformedStreamDefinitionException, URISyntaxException {
        bLangProgram = BTestUtils.parseBalFile(BALLERINA_TEST_SCRIPT_LOCATION);
        Path programPath = Paths.get(EventPublisherTestCase.class.getProtectionDomain()
                                                                                .getCodeSource().getLocation().toURI());
        DataPublisherTestUtil.configPath = programPath.toString();
        //set ballerina home to test running directory as event publisher looking to it
        System.setProperty("ballerina.home", programPath.toString());
        thriftTestServer.addStreamDefinition(StreamDefinitions.getStreamDefinitionFault());
        thriftTestServer.start(thriftServerListenPort);
    }

    @Test
    public void testCacheOperations() throws InterruptedException {
        //Test ballerina event publishing by sending a event
        BLangFunctions.invokeNew(bLangProgram, "publishData");
        //adding waiting time to prevent intermittent test failure
        Thread.sleep(10000);

        List<Event> requestTable = null;
        long currentTime = System.currentTimeMillis();
        long waitTime = currentTime + WAIT_TIME;
        while (waitTime > System.currentTimeMillis()) {
            requestTable = thriftTestServer.getDataTables().get(StreamDefinitions.APIMGT_STATISTICS_FAULT_STREAM_ID);
            if (requestTable == null || requestTable.isEmpty()) {
                log.info("Request data table is empty or null. waiting 1s and retry..");
                Thread.sleep(1000);
                continue;
            } else {
                break;
            }
        }
        if (requestTable == null) {
            log.error("Response data table is null!!");
        }

        Assert.assertEquals(1, requestTable.size(), "Stat publisher published events not match");
        Map<String, Object> map = convertToMap(requestTable.get(0).getPayloadData(),
                StreamDefinitions.getStreamDefinitionFault());
        Assert.assertEquals("testconsumerid", map.get("consumerKey").toString(), "Wrong consumer key is received");
        Assert.assertEquals("/test", map.get("context").toString(), "Wrong consumer context received");
        Assert.assertEquals("1.0.0", map.get("version").toString(), "Wrong version received");
    }

    @AfterClass
    public void cleanup() {
        thriftTestServer.stop();
    }

    /**
     * used to convert received event stream json payload into key-value pair
     *
     * @param result json payload of the event
     * @param stream Stream definition of the result
     * @return list of map having payload attribute and it's value
     * @throws JSONException throws if json payload is malformed
     */
    private Map<String, Object> convertToMap(Object[] result, String stream) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONArray payloadData = new JSONObject(stream).getJSONArray("payloadData");
        Assert.assertEquals(result.length, payloadData.length(), "attributes counts are not equal");
        for (int i = 0; i < result.length; i++) {
            String key = payloadData.getJSONObject(i).getString("name");
            map.put(key, result[i]);
        }
        return map;
    }
}
