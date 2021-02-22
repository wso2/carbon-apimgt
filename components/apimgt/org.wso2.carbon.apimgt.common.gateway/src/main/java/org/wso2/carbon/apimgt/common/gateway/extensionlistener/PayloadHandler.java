/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.common.gateway.extensionlistener;

import java.io.InputStream;

/**
 * This Interface is provides specific extension points to consume the payload from the related message context.
 */
public interface PayloadHandler {

    /**
     * Consume and return the payload as String.
     *
     * @return String payload
     * @throws Exception if an error occurs
     */
    String consumeAsString() throws Exception;

    /**
     * Consume and return payload as InputStream.
     *
     * @return InputStream payload
     * @throws Exception if an error occurs
     */
    InputStream consumeAsStream() throws Exception;
}
