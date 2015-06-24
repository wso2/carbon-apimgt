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
package org.apache.synapse.transport.fix.message;

import javax.xml.namespace.QName;

public class FIXConstants {

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
	public static final String FIX_SOH_EOD = "<SOH>";

	public static final String HTML_START_TAG = "<HTML>";
	public static final String HTML_END_TAG = "</HTML>";

	public static final QName BINARY_CONTENT_QNAME = new QName("http://ws.apache.org/commons/ns/content", "binary");
	
	public static final String SOH = "\u0001";
}
