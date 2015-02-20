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

package org.apache.synapse.commons.evaluators;

/**
 * Constants used through out the evaluators
 */
public class EvaluatorConstants {

    public static final String AND = "and";
    public static final String OR = "or";
    public static final String NOT = "not";
    public static final String MATCH = "match";
    public static final String EQUAL = "equal";

    // Types of source text retrievers
    public static final String HEADER = "header";
    public static final String PARAM = "param";
    public static final String URL = "url";
    public static final String PROPERTY = "property";
    public static final String SOAP = "soap";

    public static final String TYPE = "type";
    public static final String SOURCE = "source";
    public static final String REGEX = "regex";
    public static final String VALUE = "value";

    public static final String CONDITIONS = "conditions";
    public static final String CONDITION = "condition";
    public static final String PRIORITY = "priority";
    public static final String DEFAULT_PRIORITY = "defaultPriority";

    public static enum URI_FRAGMENTS {
        protocol,
        user,
        host,
        port,
        path,
        query,
        ref
    }
    public static final String SYNAPSE_NAMESPACE = "http://ws.apache.org/ns/synapse";
    public static final String EMPTY_PREFIX = "";
}
