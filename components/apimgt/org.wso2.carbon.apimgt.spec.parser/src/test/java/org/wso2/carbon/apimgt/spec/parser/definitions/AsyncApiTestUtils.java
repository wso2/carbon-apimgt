/*
 *   Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.spec.parser.definitions;

public final class AsyncApiTestUtils {

    public static final String ASYNCAPI_V20= "2.0" ;

    // Minimal AsyncAPI v3 JSON (derived from Streetlights sample, simplified)
    public static final String ASYNCAPI_V3_SAMPLE = "{\n" +
            "  \"asyncapi\":\"3.0.0\",\n" +
            "  \"info\":{ \"title\":\"Streetlights Kafka API\",\"version\":\"1.0.0\" },\n" +
            "  \"channels\":{\n" +
            "    \"lightingMeasured\":{\n" +
            "      \"address\":\"smartylighting.streetlights.1.0.event.{streetlightId}.lighting.measured\",\n" +
            "      \"description\":\"The topic on which measured values may be produced and consumed.\"\n" +
            "    },\n" +
            "    \"lightTurnOn\":{ \"address\":\"smartylighting.streetlights.1.0.action.{streetlightId}.turn.on\" },\n" +
            "    \"lightTurnOff\":{ \"address\":\"smartylighting.streetlights.1.0.action.{streetlightId}.turn.off\" },\n" +
            "    \"lightsDim\":{ \"address\":\"smartylighting.streetlights.1.0.action.{streetlightId}.dim\" }\n" +
            "  },\n" +
            "  \"operations\":{\n" +
            "    \"receiveLightMeasurement\":{\"action\":\"receive\",\"channel\":{\"$ref\":\"#/channels/lightingMeasured\"}},\n" +
            "    \"turnOn\":{\"action\":\"send\",\"channel\":{\"$ref\":\"#/channels/lightTurnOn\"}},\n" +
            "    \"turnOff\":{\"action\":\"send\",\"channel\":{\"$ref\":\"#/channels/lightTurnOff\"}},\n" +
            "    \"dimLight\":{\"action\":\"send\",\"channel\":{\"$ref\":\"#/channels/lightsDim\"}}\n" +
            "  }\n" +
            "}";

    // Minimal AsyncAPI v2 JSON (very small example)
    public static final String ASYNCAPI_V2_SAMPLE = "{\n" +
            "  \"asyncapi\": \"2.0.0\",\n" +
            "  \"info\": { \"title\": \"Simple API\", \"version\": \"1.0.0\" },\n" +
            "  \"channels\": {\n" +
            "    \"orders\": {\n" +
            "      \"subscribe\": { \"summary\": \"receive orders\" },\n" +
            "      \"publish\": { \"summary\": \"publish order status\" }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}