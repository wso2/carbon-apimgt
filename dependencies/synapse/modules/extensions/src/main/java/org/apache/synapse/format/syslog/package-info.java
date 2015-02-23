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

/**
 * Implementation of the BSD syslog protocol described in RFC 3164.
 * <p>
 * The protocol is implemented as an Axis2 message builder that takes
 * a single syslog message as input and that produces an XML
 * representation of the event in the form of an AXIOM tree. The
 * XML format is non standard and is defined by the
 * <tt>org/apache/synapse/format/syslog/schema.xsd</tt> included in
 * the JAR. 
 * <p>
 * A typical message looks as follows:
 * <pre>
 * &lt;message xmlns="http://synapse.apache.org/ns/syslog"
 *          facility="authpriv" severity="info" tag="CRON" pid="6813">
 *   pam_unix(cron:session): session closed for user root
 * &lt;/message></pre>
 * The message builder can be registered in the Axis2 configuration file
 * (<tt>axis2.xml</tt>) in the following way:
 * <pre>
 * &lt;messageBuilders>
 *   &lt;messageBuilder contentType="application/x-syslog"
 *                   class="org.apache.synapse.format.syslog.SyslogMessageBuilder"/>
 * &lt;/messageBuilders>
 * </pre>
 * Again the content type <tt>application/x-syslog</tt> is non standard and is only
 * used to refer to the message builder later.
 * <h4>Known issues</h4>
 * <ul>
 *   <li>The message builder currently doesn't support timestamp and host fields.
 *       Messages containing these fields are not parsed correctly.</li>
 * </ul>
 */
package org.apache.synapse.format.syslog;