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

import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ResponseContextDTO;

/**
 * This Interface is providing functionality to listen and extend request/response
 * handler/filter flows in API Gateway.
 * You can implement pre-process request, post-process request, pre-process response and post-process response flows
 * of gateway handlers/filters by implementing this interface. Furthermore you need to register your implementation as
 * an OSGI service for this interface with the type using ExtensionType enum.
 * This interface provides a method to read the ExtensionType.
 */
public interface ExtensionListener {

    /***
     * Pre process Request.
     * @param requestContextDTO RequestContextDTO
     */

    ExtensionResponseDTO preProcessRequest(RequestContextDTO requestContextDTO);

    /***
     * Post process Request.
     * @param requestContextDTO RequestContextDTO
     */
    ExtensionResponseDTO postProcessRequest(RequestContextDTO requestContextDTO);

    /***
     * Pre process Response.
     * @param responseContextDTO ResponseContextDTO
     */
    ExtensionResponseDTO preProcessResponse(ResponseContextDTO responseContextDTO);

    /***
     * Post process Response.
     * @param responseContextDTO ResponseContextDTO
     */
    ExtensionResponseDTO postProcessResponse(ResponseContextDTO responseContextDTO);

    /**
     * Returns the extension listener type. This should be a value from ExtensionType enum.
     *
     * @return ExtensionType enum value
     */
    String getType();

}

