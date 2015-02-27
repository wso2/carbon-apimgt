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

package org.apache.synapse.util.xpath;

/**
 * All the constants that has been used for the SynapseXPath
 */
public final class SynapseXPathConstants {

    /** Get-Property XPath extension function name */
    public static final String GET_PROPERTY_FUNCTION = "get-property";

    /** base64Encode XPath extension function name */
    public static final String BASE64_ENCODE_FUNCTION = "base64Encode";

    /** Body relative XPath variale name for the SOAPBody */
    public static final String SOAP_BODY_VARIABLE = "body";

    /** Header relative XPath variable name for the SOAPHeader */
    public static final String SOAP_HEADER_VARIABLE = "header";

    /** Variable prefix for accessing the MessageContext properties through XPath variables */
    public static final String MESSAGE_CONTEXT_VARIABLE_PREFIX = "ctx";

    /** Variable prefix for accessing the Function/Template Context properties through XPath variables */
    public static final String FUNC_CONTEXT_VARIABLE_PREFIX = "func";

    /** Variable prefix for accessing the axis2 MessageContext properties through XPath variables */
    public static final String AXIS2_CONTEXT_VARIABLE_PREFIX = "axis2";

    /** Variable prefix for accessing transport headers of the message through XPath variables */
    public static final String TRANSPORT_VARIABLE_PREFIX = "trp";

    /** Variable prefix for accessing URL parameters of the message through XPath variables */
    public static final String URL_VARIABLE_PREFIX = "url";
}
