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
package org.apache.synapse.transport.pipe;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.ParamUtils;

/**
 * End delimited protocol implementation.
 * <p>
 * In the end delimited protocol datagrams are encapsulated in the stream
 * without modification. After each datagram an end byte is appended.
 * Since datagrams may contain arbitrary byte sequences and no escape
 * algorithm is defined, this protocol is only suitable if it can be assumed
 * that the end byte never appears inside a datagram.
 * <p>
 * This protocol implementation is mainly useful with legacy protocols.
 * For example, in conjunction with {@link PipeListener} and
 * {@link org.apache.synapse.format.syslog.SyslogMessageBuilder} it can be
 * used to receive syslog events via a UNIX pipe. In this case the end byte
 * is 10.
 * <p>
 * This protocol recognizes a single mandatory parameter <tt>delimiter</tt>
 * that must be configured in the transport and that specifies the end byte.
 * The value must be given as an integer. An example transport receiver
 * configuration looks like:
 * <pre>
 * &lt;transportReceiver name="pipe" class="org.apache.synapse.transport.pipe.PipeListener">
 *   &lt;parameter name="protocol">org.apache.synapse.transport.pipe.EndDelimitedProtocol&lt;/parameter>
 *   &lt;parameter name="delimiter">10&lt;/parameter>
 * &lt;/transportReceiver></pre>
 */
public class EndDelimitedProtocol implements Protocol {
    private class ProtocolDecoderImpl implements ProtocolDecoder {
        private final ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();
        private final Queue<byte[]> messages = new LinkedList<byte[]>();
        
        public ProtocolDecoderImpl() {}
        
        public boolean inputRequired() {
            return messages.isEmpty();
        }

        public void decode(byte[] buf, int offset, int length) {
            byte delimiter = getDelimiter();
            int start = offset;
            for (int i=offset; i<length; i++) {
                if (buf[i] == delimiter) {
                    messageBuffer.write(buf, start, i-start);
                    start = i+1;
                    messages.add(messageBuffer.toByteArray());
                    messageBuffer.reset();
                }
            }
            messageBuffer.write(buf, start, length-start);
        }

        public byte[] getNext() {
            return messages.poll();
        }
    }
    
    private byte delimiter;
    
    public void init(ParameterInclude paramInclude) throws AxisFault {
        delimiter = (byte)ParamUtils.getRequiredParamInt(paramInclude, "delimiter");
    }

    public byte getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(byte delimiter) {
        this.delimiter = delimiter;
    }

    public ProtocolDecoder createProtocolDecoder() {
        return new ProtocolDecoderImpl();
    }
}
