/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.exception;

/**
 * This exception has to be thrown when key is not found in the keystore or in case of other exceptions when trying to
 * get key.
 */
public class NoSuchKeyException extends Exception {

    // Constructor without parameters
    public NoSuchKeyException() {

    }

    //Constructor that accepts a message
    public NoSuchKeyException(String message) {
        super(message);
    }

    // Constructor accepts a message and a cause
    public NoSuchKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor accepts a cause
    public NoSuchKeyException(Throwable cause) {
        super(cause);
    }

}
