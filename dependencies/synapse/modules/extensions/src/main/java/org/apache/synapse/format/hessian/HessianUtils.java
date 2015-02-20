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

package org.apache.synapse.format.hessian;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Util class for writing the Hessian Fault to the output stream.
 * Most of the implementation is taken from the Hessian Java implementation in
 * com.caucho.hessian.io.HessianOutput.
 */
public final class HessianUtils {

    private HessianUtils() {
    }

    /**
     * Creates and writes a Hessian fault using the provided code, message and detail to the
     * provided output stream.
     * 
     * @param code the error code of the message
     * @param message the error message
     * @param detail an error detail
     * @param os the output stream to write the message to
     * 
     * @throws IOException if an error occurs writing to the output stream
     */
    public static void writeFault(String code, String message, String detail, OutputStream os)
            throws IOException {

        startReply(os);

        os.write('f');
        writeString("code", os);
        writeString(code, os);

        writeString("message", os);
        writeString(message, os);

        if (detail != null) {
            writeString("detail", os);
            writeString(detail, os);
        }

        os.write('z');

        completeReply(os);
    }

    /**
     * Writes the bytes to start a Hessian reply (including protocol version information) to the 
     * provided output stream.
     * 
     * @param os the output stream to write to
     * 
     * @throws IOException if an error occurs writing to the output stream
     */
    private static void startReply(OutputStream os) throws IOException {
        os.write('r');
        os.write(1);
        os.write(0);
    }

    /**
     * Writes the byte to complete a Hessian reply to the provided output stream.
     * 
     * @param os the output stream to write to
     * 
     * @throws IOException if an error occurs writing to the output stream
     */
    private static void completeReply(OutputStream os) throws IOException {
        os.write('z');
    }

    /**
     * Writes a the provided string in a Hessian string representation to the provided output 
     * stream using UTF-8 encoding.<br>
     * 
     * The string will be written with the following syntax:
     *
     * <code><pre>
     * S b16 b8 string-value
     * </pre></code>
     *
     * If the value is null, it will be written as
     *
     * <code><pre>
     * N
     * </pre></code>
     *
     * @param value the string to write to the output string
     * @param os the output stream to write the string to
     * 
     * @throws IOException
     */
    private static void writeString(String value, OutputStream os) throws IOException {

        if (value == null) {
            os.write('N');
        } else {
            int length = value.length();
            int offset = 0;

            while (length > 0x8000) {
                int sublen = 0x8000;

                // chunk can't end in high surrogate
                char tail = value.charAt(offset + sublen - 1);

                if (0xd800 <= tail && tail <= 0xdbff) {
                    sublen--;
                }

                os.write('s');
                os.write(sublen >> 8);
                os.write(sublen);

                printString(value, offset, sublen, os);

                length -= sublen;
                offset += sublen;
            }

            os.write('S');
            os.write(length >> 8);
            os.write(length);

            printString(value, offset, length, os);
        }
    }

    /**
     * Prints a string (or parts of it) to the provided output stream encoded as UTF-8.<br>
     *
     * @param v the string to print to the output stream
     * @param offset an offset indicating at which character of the string to start
     * @param length the number of characters to write
     * 
     * @throws IOException if an error occurs writing to the output stream
     */
    private static void printString(String v, int offset, int length, OutputStream os)
            throws IOException {
        
        for (int i = 0; i < length; i++) {
            char ch = v.charAt(i + offset);

            if (ch < 0x80) {
                os.write(ch);
            } else if (ch < 0x800) {
                os.write(0xc0 + ((ch >> 6) & 0x1f));
                os.write(0x80 + (ch & 0x3f));
            } else {
                os.write(0xe0 + ((ch >> 12) & 0xf));
                os.write(0x80 + ((ch >> 6) & 0x3f));
                os.write(0x80 + (ch & 0x3f));
            }
        }
    }
}
