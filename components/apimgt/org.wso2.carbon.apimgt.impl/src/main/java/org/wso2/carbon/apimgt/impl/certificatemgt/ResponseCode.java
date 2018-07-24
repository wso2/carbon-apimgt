/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.certificatemgt;

/**
 * Status codes for Certificate management operations.
 * 
 * Response Code Definitions
 * 
 * 1. SUCCESS : Operation successful
 * 2. INTERNAL_SERVER_ERROR : Operation failed due to internal error.
 * 3. ALIAS_EXISTS_IN_TRUST_STORE : Failed to add certificate to trust store. Alias exists in trust store.
 * 4. CERTIFICATE_NOT_FOUND : Failed to remove the certificate from trust store. Certificate not found.
 * 5. FAILED_TO_REMOVE_FROM_DB : Failed to remove the certificate from Database.
 * 6. CERTIFICATE_EXPIRED : Failed to add certificate to key store. Certificate expired.
 * 7. CERTIFICATE_FOR_ENDPOINT_EXISTS : Failed to add Certificate to database. Certificate for endpoint exists.
 */
public enum ResponseCode {

    SUCCESS(1),
    INTERNAL_SERVER_ERROR(2),
    ALIAS_EXISTS_IN_TRUST_STORE(3),
    CERTIFICATE_NOT_FOUND(4),
    FAILED_TO_REMOVE_FROM_DB(5),
    CERTIFICATE_EXPIRED(6);

    private final int responseCode;

    ResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

}
