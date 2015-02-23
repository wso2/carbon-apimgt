/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.transport.fix;

public class FIXConstants {
    //----------------------------Defaults parameters-----------------------------------

    public static final String FIX_PREFIX = "fix://";
    public static final String TRANSPORT_NAME = "fix";

    public static final String FIX_ACCEPTOR = "acceptor";
    public static final String FIX_INITIATOR = "initiator";
    public static final String FIX_MESSAGE = "message";
    public static final String FIX_FIELD = "field";
    public static final String FIX_HEADER = "header";
    public static final String FIX_BODY = "body";
    public static final String FIX_TRAILER = "trailer";
    public static final String FIX_FIELD_ID = "id";
    public static final String FIX_GROUPS = "groups";
    public static final String FIX_GROUP = "group";
    public static final String FIX_MESSAGE_SERVICE = "service";
    public static final String FIX_MESSAGE_APPLICATION = "fixApplication";
    public static final String FIX_BINARY_FIELD = "rawdata";
    public static final String FIX_MESSAGE_REFERENCE = "href";
    public static final String FIX_MESSAGE_COUNTER = "counter";
    public static final String FIX_MESSAGE_INCOMING_SESSION = "inSession";

    public static final String FILE_BASED_MESSAGE_LOGGING = "file";
    public static final String JDBC_BASED_MESSAGE_LOGGING = "jdbc";
    public static final String CONSOLE_BASED_MESSAGE_LOGGING = "console";

    public static final String FILE_BASED_MESSAGE_STORE = "file";
    public static final String JDBC_BASED_MESSAGE_STORE = "jdbc";
    public static final String MEMORY_BASED_MESSAGE_STORE = "memory";
    public static final String SLEEPYCAT_BASED_MESSAGE_STORE = "sleepycat";

    public static final long DEFAULT_HEART_BT_INT_VALUE = 30;
    public static final String DEFAULT_START_TIME_VALUE = "00:00:00";
    public static final String DEFAULT_END_TIME_VALUE = "00:00:00";
    public static final int DEFAULT_COUNTER_UPPER_LIMIT = 1000000000;

    public static final String HEART_BY_INT = "HeartBtInt";
    public static final String BEGIN_STRING = "BeginString";
    public static final String SENDER_COMP_ID = "SenderCompID";
    public static final String TARGET_COMP_ID = "TargetCompID";
    public static final String SENDER_SUB_ID = "SenderSubID";
    public static final String TARGET_SUB_ID = "TargetSubID";
    public static final String SENDER_LOCATION_ID = "SenderLocationID";
    public static final String TARGET_LOCATION_ID = "TargetLocationID";
    public static final String SESSION_QUALIFIER = "SessionQualifier";
    public static final String SOCKET_CONNECT_HOST = "SocketConnectHost";
    public static final String SOCKET_CONNECT_PORT = "SocketConnectPort";
    public static final String START_TIME = "StartTime";
    public static final String END_TIME = "EndTime";
    public static final String CONNECTION_TYPE = "ConnectionType";
    public static final String DELIVER_TO_COMP_ID = "DeliverToCompID";
    public static final String DELIVER_TO_SUB_ID = "DeliverToSubID";
    public static final String DELIVER_TO_LOCATION_ID = "DeliverToLocationID";
    public static final String ON_BEHALF_OF_COMP_ID = "OnBehalfOfCompID";
    public static final String ON_BEHALF_OF_SUB_ID = "OnBehalfOfSubID";
    public static final String ON_BEHALF_OF_LOCATION_ID = "OnBehalfOfLocationID";

    public static final String FIX_DEFAULT_CONTENT_TYPE = "text/xml";


    //-------------------------- services.xml parameters --------------------------------

    public static final String FIX_ACCEPTOR_CONFIG_URL_PARAM = "transport.fix.AcceptorConfigURL";
    public static final String FIX_INITIATOR_CONFIG_URL_PARAM = "transport.fix.InitiatorConfigURL";

    public static final String FIX_ACCEPTOR_LOGGER_PARAM = "transport.fix.AcceptorLogFactory";
    public static final String FIX_INITIATOR_LOGGER_PARAM = "transport.fix.InitiatorLogFactory";

    public static final String FIX_ACCEPTOR_MESSAGE_STORE_PARAM = "transport.fix.AcceptorMessageStore";
    public static final String FIX_INITIATOR_MESSAGE_STORE_PARAM = "transport.fix.InitiatorMessageStore";

    public static final String FIX_RESPONSE_DELIVER_TO_COMP_ID_PARAM = "transport.fix.ResponseDeliverToCompID";
    public static final String FIX_RESPONSE_DELIVER_TO_SUB_ID_PARAM = "transport.fix.ResponseDeliverToSubID";
    public static final String FIX_RESPONSE_DELIVER_TO_LOCATION_ID_PARAM = "transport.fix.ResponseDeliverToLocationID";

    public static final String FIX_SERVICE_NAME = "transport.fix.ServiceName";

    public static final String FIX_RESPONSE_HANDLER_APPROACH = "transport.fix.SendAllToInSequence";

    public static final String FIX_BEGIN_STRING_VALIDATION = "transport.fix.BeginStringValidation";

    public static final String FIX_DROP_EXTRA_RESPONSES = "transport.fix.DropExtraResponses";

    public static final String FIX_ACCEPTOR_EVENT_HANDLER = "transport.fix.AcceptorSessionEventHandler";
    public static final String FIX_INITIATOR_EVENT_HANDLER = "transport.fix.InitiatorSessionEventHandler";

    //--------------------------- Message level properties -----------------------------------

    public static final String FIX_IGNORE_ORDER = "transport.fix.IgnoreOrder";

}