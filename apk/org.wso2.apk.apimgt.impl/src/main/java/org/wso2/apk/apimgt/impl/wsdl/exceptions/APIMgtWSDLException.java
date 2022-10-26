/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.impl.wsdl.exceptions;


import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ErrorHandler;

/**
 * This is the custom exception class to be thrown when an issue encountered in WSDL related operations.
 */
public class APIMgtWSDLException extends APIManagementException {
    public APIMgtWSDLException(String msg, Throwable e) {
        super(msg, e);
    }

    public APIMgtWSDLException(String msg) {
        super(msg);
    }

    public APIMgtWSDLException(String msg, ErrorHandler errorHandler) {
        super(msg, errorHandler);
    }

    public APIMgtWSDLException(String msg, Throwable e, ErrorHandler errorHandler) {
        super(msg, e, errorHandler);
    }

    public APIMgtWSDLException(Throwable throwable) {
        super(throwable);
    }
}
