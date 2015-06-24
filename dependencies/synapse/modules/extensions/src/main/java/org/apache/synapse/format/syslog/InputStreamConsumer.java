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
package org.apache.synapse.format.syslog;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream consumer.
 * This is a helper class used by {@link SyslogMessageBuilder} to parse data
 * from an input stream. In particular it supports look ahead and allows to
 * buffer and reread data from the stream.
 */
public class InputStreamConsumer {
    private final InputStream in;
    private byte[] buffer = new byte[64];
    private int position;
    private int bufferPosition;
    private boolean endOfStream;
    
    /**
     * Constructor.
     * 
     * @param in the input stream to consume data from
     */
    public InputStreamConsumer(InputStream in) {
        this.in = in;
    }
    
    /**
     * Get the next byte from the stream without consuming it.
     * If the byte is not consumed between invocations, two successive
     * calls to this method will return the same result.
     * 
     * @return the next byte as an integer value in the range 0..255 or
     *         -1 if the end of the stream has been reached
     * @throws IOException if an I/O error occurred while reading from
     *         the stream
     */
    public int next() throws IOException {
        if (position < bufferPosition) {
            return buffer[position];
        } else if (endOfStream) {
            return -1;
        } else {
            if (bufferPosition == buffer.length) {
                byte[] newBuffer = new byte[buffer.length*2];
                System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                buffer = newBuffer;
            }
            int c = in.read(buffer, bufferPosition, buffer.length-bufferPosition);
            if (c == -1) {
                endOfStream = true;
                return -1;
            } else {
                bufferPosition += c;
            }
            return buffer[position];
        }
    }
    
    /**
     * Consume the last byte read from the stream and advance to the next byte.
     */
    public void consume() {
        position++;
    }
    
    /**
     * Get the current position in the stream.
     * 
     * @return the position in the stream
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * Reset the stream position to a previous value.
     * 
     * @param position the new position
     */
    public void setPosition(int position) {
        this.position = position;
    }
    
    /**
     * Check the value of the next byte in the stream.
     * 
     * @param expected the expected value
     * @throws IOException if an I/O error occurred while reading from
     *         the stream
     * @throws ProtocolException if the next byte doesn't have the expected value
     */
    public void expect(int expected) throws IOException, ProtocolException {
        int next = next();
        if (next != expected) {
            throw new ProtocolException("Unexpected byte: expected " + expected +
                    " ('" + (char) expected + "') , got " + next + " ('" + (char) next + "')");
        }
    }
    
    /**
     * Check the value of the next byte in the stream and consume it. This is
     * a convenience method that combines a call to {@link #expect} with a
     * call to {@link #consume}.
     * 
     * @param expected the expected value
     * @throws IOException if an I/O error occurred while reading from
     *         the stream
     * @throws ProtocolException if the next byte doesn't have the expected value
     */
    public void consume(int expected) throws IOException, ProtocolException {
        expect(expected);
        consume();
    }
    
    /**
     * Read a decimal representation of an integer from the stream.
     * 
     * @param maxDigits the maximum number of expected digits
     * @return the integer value
     * @throws IOException if an I/O error occurred while reading from
     *         the stream
     * @throws ProtocolException if no integer value was found or if it
     *         was too long
     */
    public int getInteger(int maxDigits) throws IOException, ProtocolException {
        int digits = 0;
        int result = 0;
        while (true) {
            int next = next();
            if ('0' <= next && next <= '9') {
                if (++digits > maxDigits) {
                    throw new ProtocolException("Numeric value too long");
                }
                consume();
                result = result*10 + (next - '0');
            } else {
                break;
            }
        }
        if (digits == 0) {
            throw new ProtocolException("Expected numeric value");
        }
        return result;
    }
}
