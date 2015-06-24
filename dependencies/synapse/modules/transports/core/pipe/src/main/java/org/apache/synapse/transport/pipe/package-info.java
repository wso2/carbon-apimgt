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
 * Transport that reads messages from a UNIX pipe.
 * <p>
 * The transport accesses pipes using standard Java file I/O. Note that
 * this only works with UNIX pipes (FIFOs), not with Windows
 * named pipes. Also, Java does not support non blocking I/O
 * on files.
 * <p>
 * While pipes are streams, this transport is built as a datagram
 * transport, i.e. messages are read entirely into memory before they
 * are processed. Indeed, given that a pipe is a unique communication
 * channel between two local processes it would not make sense to let
 * the processing of a message block the reception of the next message.
 * This is different from other stream based transports such as HTTP
 * where multiple concurrent channels exist and where it makes sense
 * to use streaming, i.e. read the message while it is being processed.
 * <p>
 * To listen on a given pipe, the datagram stream protocol and the
 * content type must be specified. The stream protocol describes how
 * the stream is decoded into individual datagrams (messages) while
 * the content type determines how these datagrams are decoded.
 * The protocol is specified by an implementation of the
 * {@link org.apache.synapse.transport.pipe.Protocol} interface, while
 * the content type is used to select the appropriate
 * {@link org.apache.axis2.builder.Builder} with the standard
 * lookup mechanisms in Axis2.
 * <p>
 * See the documentation of {@link org.apache.synapse.transport.pipe.PipeListener}
 * for more information about how to configure a service to listen to
 * a pipe.
 * 
 * <h4>Known issues and limitations</h4>
 * 
 * <ul>
 *   <li>The listener doesn't implement all management operations
 *       specified by
 *       {@link org.apache.synapse.transport.base.ManagementSupport}.</li>
 *   <li>Configuring the protocol at the transport level while allowing
 *       to specify the content type on a per service basis (see
 *       {@link org.apache.synapse.transport.pipe.PipeListener}) is somewhat
 *       arbitrary. This may not be suitable for some use cases.</li>
 * </ul>
 */
package org.apache.synapse.transport.pipe;
