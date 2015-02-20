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

package org.apache.synapse.commons.executors;

public abstract class ExecutorConstants {
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String SIZE = "size";

    public static final String PRIORITY = "priority";
    public static final String IS_FIXED_SIZE = "isFixedSize";
    public static final String BEFORE_EXECUTE_HANDLER = "beforeExecuteHandler";
    public static final String NEXT_QUEUE = "nextQueue";

    public static final String PROPERTY = "property";

    public static final String QUEUES = "queues";
    public static final String QUEUE = "queue";

    public static final String PRIORITY_EXECUTOR = "priorityExecutor";
    public static final String THREADS = "threads";
    public static final String MAX = "max";
    public static final String CORE = "core";
    public static final String KEEP_ALIVE = "keep-alive";

    /** Default core threads */
    public static final int DEFAULT_CORE = 20;
    /** Default max threads */
    public static final int DEFAULT_MAX = 100;
    /** Default keep alive time in seconds */
    public static final int DEFAULT_KEEP_ALIVE = 5;
}
