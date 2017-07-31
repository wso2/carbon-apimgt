/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.ballerina.publisher.util;

/**
 * this class is hold list of event stream definitions that used to published to DAS for APIM analytics
 */
public class StreamDefinitions {
    public static final String APIMGT_STATISTICS_FAULT_STREAM_ID = "org.wso2.apimgt.statistics.fault:1.0.0";

    public static String getStreamDefinitionFault() {
        return "{" + "  'name': 'org.wso2.apimgt.statistics.fault',"
                + "  'version': '1.0.0',"
                + "  'nickName': 'API Manager Fault Data',"
                + "  'description': 'Fault Data',"
                + "  'metaData': ["
                + "    {'name': 'clientType','type': 'STRING' }"
                + "  ],"
                + "  'payloadData': ["
                + "    {'name': 'consumerKey','type': 'STRING' },"
                + "    {'name': 'context','type': 'STRING' },"
                + "    {'name': 'api_version','type': 'STRING'},"
                + "    {  'name': 'api','type': 'STRING'},"
                + "    {  'name': 'resourcePath','type': 'STRING'},"
                + "    {  'name': 'method','type': 'STRING'},"
                + "    {  'name': 'version','type': 'STRING'},"
                + "    {  'name': 'errorCode','type': 'STRING'},"
                + "    {  'name': 'errorMessage','type': 'STRING'},"
                + "    {  'name': 'requestTime','type': 'LONG'},"
                + "    {  'name': 'userId','type': 'STRING'},"
                + "    {  'name': 'tenantDomain','type': 'STRING'},"
                + "    {  'name': 'hostName','type': 'STRING'},"
                + "    {  'name': 'apiPublisher','type': 'STRING'},"
                + "    {  'name': 'applicationName','type': 'STRING'},"
                + "    {  'name': 'applicationId','type': 'STRING'},"
                + "    {  'name': 'protocol','type': 'STRING'}"
                + "  ]"
                + "}";
    }

    public static String getStreamDefinitionThrottle() {
        return "{" + "  'name': 'org.wso2.apimgt.statistics.throttle',"
                + "  'version': '1.0.0',"
                + "  'nickName': 'API Manager Throttle Data',"
                + "  'description': 'Throttle Data',"
                + "  'metaData': [{"
                + "      'name': 'clientType','type': 'STRING'" + "    }"
                + "  ],"
                + "  'payloadData': ["
                + "    {'name': 'accessToken','type': 'STRING'},"
                + "    {'name': 'userId','type': 'STRING'},"
                + "    {'name': 'tenantDomain','type': 'STRING'},"
                + "    {'name': 'api','type': 'STRING'},"
                + "    {'name': 'api_version','type': 'STRING'},"
                + "    {'name': 'context','type': 'STRING'},"
                + "    {'name': 'apiPublisher','type': 'STRING'},"
                + "    {'name': 'throttledTime','type': 'LONG'},"
                + "    {'name': 'applicationName','type': 'STRING'},"
                + "    {'name': 'applicationId','type': 'STRING'},"
                + "    {'name': 'subscriber','type': 'STRING'},"
                + "    {'name': 'throttledOutReason','type': 'STRING'}"
                + "  ]"
                + "}";
    }
}
