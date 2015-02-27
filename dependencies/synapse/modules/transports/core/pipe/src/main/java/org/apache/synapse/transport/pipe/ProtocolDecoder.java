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

/**
 * Datagram stream decoder.
 * <p>
 * Objects implementing this interface are created by {@link Protocol} objects. They
 * are used to decompose a stream into individual datagrams.
 */
public interface ProtocolDecoder {
    /**
     * Check whether the decoder requires more input.
     * 
     * @return true if a datagram is ready to be retrieved using {@link #getNext()},
     *         false if no datagram is available and the caller should provide more
     *         input using {@link #decode(byte[], int, int)}
     */
    boolean inputRequired();
    
    /**
     * Decode data from the stream.
     * This method should be called after a call to {@link #inputRequired()} returned
     * true.
     * 
     * @param buf a byte array containing data from the stream
     * @param offset the start offset in the data
     * @param length the number of bytes
     */
    void decode(byte[] buf, int offset, int length);
    
    /**
     * Get the next datagram.
     * This method should only be called after a call to {@link #inputRequired()} returned
     * false. Otherwise the result is undefined.
     * 
     * @return a byte array containing the datagram
     */
    byte[] getNext();
}
