/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.synapse.transport.passthru;

/**
 * State of a request or a response in transport receiver or sender
 * is represented in these values.
 */
public enum ProtocolState {
    /** Connection is at the initial stage ready to receive a request */
    REQUEST_READY,
    /** The connection is reading the request headers */
    REQUEST_HEAD,
    /** The connection is reading the request body */
    REQUEST_BODY,
    /** Request is completely received */
    REQUEST_DONE,
    /** The connection is reading the response headers */
    RESPONSE_HEAD,
    /** The connection si reading the response body */
    RESPONSE_BODY,
    /** The response is completed */
    RESPONSE_DONE,
    /** The connection is closing */
    CLOSING,
    /** The connection is closed */
    CLOSED,
    
    WSDL_RESPONSE_DONE
}
