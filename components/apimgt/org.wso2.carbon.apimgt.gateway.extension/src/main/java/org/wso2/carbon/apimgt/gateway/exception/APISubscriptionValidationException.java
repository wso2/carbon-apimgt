/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.exception;


/**
 * Exception class for key validation failures.
 */
public class APISubscriptionValidationException extends Exception {

    private static final long serialVersionUID = 5813925030638754789L;

    public APISubscriptionValidationException(String message) {
        super(message);
    }

    public APISubscriptionValidationException(String message, Throwable e) {
        super(message, e);
    }

    public APISubscriptionValidationException(Exception e) {
        super(e);
    }
}
