/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.exception;

/**
 * This is the custom exception class to be thrown when the user tries to access/update a resource but the user does not
 *  have enough permission
 */
public class APIMgtAuthorizationFailedException extends APIManagementException {

    public APIMgtAuthorizationFailedException(String msg) {
        super(msg);
    }

    public APIMgtAuthorizationFailedException(String msg, Throwable e) {
        super(msg, e);
    }

    public APIMgtAuthorizationFailedException(Throwable throwable) {
        super(throwable);
    }
}
