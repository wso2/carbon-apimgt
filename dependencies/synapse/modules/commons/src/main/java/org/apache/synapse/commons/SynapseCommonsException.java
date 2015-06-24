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

package org.apache.synapse.commons;

import org.apache.commons.logging.Log;

/**
 *
 */
public class SynapseCommonsException extends RuntimeException {

    private static final long serialVersionUID = -7361599095528938810L;

    public SynapseCommonsException(String string) {
        super(string);
    }

    public SynapseCommonsException(String msg, Throwable e) {
        super(msg, e);
    }

    public SynapseCommonsException(Throwable t) {
        super(t);
    }

    public SynapseCommonsException(String msg, Log synLog) {
        super(msg);
        synLog.error(msg);
    }

    public SynapseCommonsException(String msg, Throwable cause, Log synLog) {
        super(msg, cause);
        synLog.error(msg, cause);
    }
}