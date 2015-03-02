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

import org.apache.synapse.transport.nhttp.NHttpConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NhttpConnectionDebugConfig {

    private static final String KEY_VALUE_SEPARATOR_PROPERTY = "nhttp.debug.key.value.separator";
    private static final String FIELD_SEPARATOR_PROPERTY = "nhttp.debug.field.separator";
    private static final String STATEMENT_SEPARATOR_PROPERTY = "nhttp.debug.statement.separator";
    // "all" | "none" | "foo,bar"
    private static final String HEADER_INFO_PROPERTY = "nhttp.debug.headers";

    private static final String HEADER_INFO_ALL = "all";
    private static final String HEADER_INFO_NONE = "none";

    private static final String DEFAULT_KEY_VALUE_SEPARATOR = "=";
    private static final String DEFAULT_FIELD_SEPARATOR = ", ";
    private static final String DEFAULT_STATEMENT_SEPARATOR = "\n";

    private String keyValueSeparator;
    private String fieldSeparator;
    private String statementSeparator;
    private List<String> headers;
    private boolean noHeaders = false;
    private boolean allHeaders = true;

    private static NhttpConnectionDebugConfig _instance;

    private NhttpConnectionDebugConfig() {

        this.keyValueSeparator = NHttpConfiguration.getInstance().getStringValue(
                KEY_VALUE_SEPARATOR_PROPERTY, DEFAULT_KEY_VALUE_SEPARATOR);
        this.fieldSeparator = NHttpConfiguration.getInstance().getStringValue(
                FIELD_SEPARATOR_PROPERTY, DEFAULT_FIELD_SEPARATOR);
        this.statementSeparator = NHttpConfiguration.getInstance().getStringValue(
                STATEMENT_SEPARATOR_PROPERTY, DEFAULT_STATEMENT_SEPARATOR);
        String headerSet = NHttpConfiguration.getInstance().getStringValue(
                HEADER_INFO_PROPERTY, HEADER_INFO_ALL);
        if (HEADER_INFO_NONE.equals(headerSet)) {
            this.noHeaders = true;
        } else if (HEADER_INFO_ALL.equals(headerSet)) {
            this.allHeaders = true;
        } else {
            String[] headerNames = headerSet.split(",");
            this.headers = new ArrayList<String>();
            for (String headerName : headerNames) {
                this.headers.add(headerName.trim());
            }
        }
    }

    public static NhttpConnectionDebugConfig getInstance() {
        if (_instance == null) {
            _instance = new NhttpConnectionDebugConfig();
        }
        return _instance;
    }

    public String getKeyValueSeparator() {
        return keyValueSeparator;
    }

    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public String getStatementSeparator() {
        return statementSeparator;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public boolean isNoHeaders() {
        return noHeaders;
    }

    public boolean isAllHeaders() {
        return allHeaders;
    }
}
