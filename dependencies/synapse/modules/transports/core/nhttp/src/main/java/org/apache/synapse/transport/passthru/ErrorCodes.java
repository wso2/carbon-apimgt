/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.synapse.transport.passthru;

/**
 * Error codes exposed by this transport. 
 */
public class ErrorCodes {
    public static final int SND_IO_ERROR       = 101500;

    public static final int SND_HTTP_ERROR     = 102510;

    public static final int SND_INVALID_STATE  = 102510;

    public static final int CONNECTION_FAILED  = 101503;

    public static final int CONNECTION_TIMEOUT = 101504;

    public static final int CONNECTION_CLOSED  = 101505;

    public static final int PROTOCOL_VIOLATION = 101506;

    public static final int CONNECT_CANCEL     = 101507;

    public static final int CONNECT_TIMEOUT    = 101508;    
}