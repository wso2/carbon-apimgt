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
package org.apache.synapse.task;

import org.apache.commons.logging.Log;

/**
 *
 */
public class SynapseTaskException extends RuntimeException {

    private static final long serialVersionUID = -7967803433723984038L;

    public SynapseTaskException(String string) {
        super(string);
    }

    public SynapseTaskException(String msg, Throwable e) {
        super(msg, e);
    }

    public SynapseTaskException(Throwable t) {
        super(t);
    }

    public SynapseTaskException(String msg, Log log) {
        super(msg);
        log.error(msg);
    }


    public SynapseTaskException(String msg, Throwable cause, Log log) {
        super(msg, cause);
        log.error(msg, cause);
    }
}
