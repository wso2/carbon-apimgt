/**
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

package org.apache.synapse.transport.nhttp.debug;

import org.apache.http.Header;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 */
public abstract class AbstractConnectionDebug {

    protected final DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");

    protected String keyValueSeparator;
    protected String fieldSeparator;
    protected String statementSeparator;
    protected boolean printAllHeaders;
    protected boolean printNoHeaders;
    protected List<String> printHeaderNames;

    protected Header[] headers;

    protected AbstractConnectionDebug() {
        NhttpConnectionDebugConfig connDebugConfig = NhttpConnectionDebugConfig.getInstance();
        this.keyValueSeparator = connDebugConfig.getKeyValueSeparator();
        this.fieldSeparator = connDebugConfig.getFieldSeparator();
        this.statementSeparator = connDebugConfig.getStatementSeparator();
        if (connDebugConfig.isNoHeaders()) {
            this.printNoHeaders = true;
        } else if (connDebugConfig.isAllHeaders()) {
            this.printAllHeaders = true;
        } else {
            this.printHeaderNames = connDebugConfig.getHeaders();
        }
    }

    protected String format(long ms) {
        return formatter.format(new Date(ms));
    }

    public abstract String dump();

    protected StringBuffer headersToString() {
        StringBuffer sb = new StringBuffer();
        if (headers != null) {
            if (printAllHeaders) {
                boolean first = true;
                for (Header h : headers) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(fieldSeparator);
                    }
                    sb.append(h.getName()).append(keyValueSeparator).append(h.getValue());
                }
            } else if (printHeaderNames != null) {
                boolean first = true;
                for (Header h : headers) {
                    if (printHeaderNames.contains(h.getName())) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(fieldSeparator);
                        }
                        sb.append(h.getName()).append(keyValueSeparator).append(h.getValue());
                    }
                }
            }
        }
        return sb.length() > 0 ? sb : sb.append("NOT_AVAILABLE");
    }
}
