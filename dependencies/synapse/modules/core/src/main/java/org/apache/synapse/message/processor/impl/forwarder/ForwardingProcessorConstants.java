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
package org.apache.synapse.message.processor.impl.forwarder;

/**
 * class <code>ForwardingProcessorConstants</code> holds the constants that are
 * used in Forwarding processors
 */
public final class ForwardingProcessorConstants {
    /**
     * Message context property that holds the name of the target endpoint to be replayed
     */
    public static final String TARGET_ENDPOINT = "target.endpoint";

    /**
     * The axis2 repository location to be used By the Message Sender
     */
    public static final String AXIS2_REPO ="axis2.repo";

    /**
     * The axis2 configuration file path to be used by the Message Sender
     */
    public static final String AXIS2_CONFIG = "axis2.config";

    public static final String BLOCKING_SENDER_ERROR="blocking.sender.error";

    /**
     * Used for in_out messages. Processor will forward the message to this sequence
     */
    public static final String REPLY_SEQUENCE = "message.processor.reply.sequence";

    /**
     * used for forward in case of Error
     */
    public static final String FAULT_SEQUENCE = "message.processor.fault.sequence";

    /**
     * Indicates if the message processor is running in throttle mode
     */
    public static final String THROTTLE = "throttle";

    /**
     * Indicates if the message processor need not retry for certain http status codes
     */
    public static final String NON_RETRY_STATUS_CODES = "non.retry.status.codes";
}
