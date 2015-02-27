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

import org.jaxen.Function;
import org.jaxen.Context;
import org.jaxen.FunctionCallException;
import org.jaxen.function.StringFunction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.codec.binary.Base64;

import java.util.List;
import java.io.UnsupportedEncodingException;

/**
 * Implements the XPath extension function synapse:base64Encode(string)
 */
public class Base64EncodeFunction implements Function {
    private static final Log log = LogFactory.getLog(Base64EncodeFunction.class);

    public static final String NULL_STRING = "";

    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * Returns the base64 encoded string value of the first argument.
     *
     * @param context the context at the point in the expression when the function is called
     * @param args  arguments of the functions
     * @return The string value of a property
     * @throws FunctionCallException
     */
    public Object call(Context context, List args) throws FunctionCallException {
        boolean debugOn = log.isDebugEnabled();

        if (args == null || args.size() == 0) {
            if (debugOn) {
                log.debug("Property key value for lookup is not specified");
            }
            return NULL_STRING;
        }

        int size = args.size();
        if (size == 1) {
            // get the first argument, it can be a function returning a string as well
            String value = StringFunction.evaluate(args.get(0), context.getNavigator());

            // use the default UTF-8 encoding
            return encode(debugOn, DEFAULT_CHARSET, value);
        } else if (size == 2) {
            // get the first argument, it can be a function returning a string as well
            String value = StringFunction.evaluate(args.get(0), context.getNavigator());

            // encoding is in the second argument
            String encoding = StringFunction.evaluate(args.get(1), context.getNavigator());
            
            return encode(debugOn, encoding, value);
        } else {
            if (debugOn) {
                log.debug("base64Encode function expects only one argument, returning empty string");
            }
        }
        // return empty string if the arguments are wrong
        return NULL_STRING;
    }

    private Object encode(boolean debugOn, String encoding, String value)
            throws FunctionCallException {
        if (value == null || "".equals(value)) {
            if (debugOn) {
                log.debug("Non emprty string value should be provided for encoding");
            }

            return NULL_STRING;
        }

        byte[] encodedValue;
        try {
            encodedValue = new Base64().encode(value.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            String msg = "Unsupported Encoding";
            log.error(msg, e);
            throw new FunctionCallException(msg, e);
        }

        String encodedString;
        try {
            encodedString = new String(encodedValue, encoding).trim();
            encodedString = encodedString.replace("\r\n", "");
        } catch (UnsupportedEncodingException e) {
            String msg = "Unsupported Encoding";
            log.error(msg, e);
            throw new FunctionCallException(msg, e);
        }

        if (debugOn) {
            log.debug("Converted string: " + value + " with encoding: " + encoding +
                    " to base64 encoded value: " + encodedString);
        }

        return encodedString;
    }
}
