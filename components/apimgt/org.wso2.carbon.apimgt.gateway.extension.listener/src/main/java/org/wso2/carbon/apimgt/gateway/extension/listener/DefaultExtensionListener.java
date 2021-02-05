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
package org.wso2.carbon.apimgt.gateway.extension.listener;

import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.ExtensionResponseStatus;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.ResponseContextDTO;

public class DefaultExtensionListener implements ExtensionListener {

    public DefaultExtensionListener() {

    }

    /***
     * Process response
     *
     * @param responseContextDTO
     */
    @Override
    public ExtensionResponseDTO preProcessResponse(ResponseContextDTO responseContextDTO) {

        return null;
    }

    @Override
    public ExtensionResponseDTO postProcessResponse(ResponseContextDTO responseContextDTO) {

        ExtensionResponseDTO responseDTO = new ExtensionResponseDTO();
//        ExtensionErrorResponseDTO errorResponseDTO = new ExtensionErrorResponseDTO();
//        errorResponseDTO.setErrorMessage("custom error message");
//        errorResponseDTO.setErrorDescription("custom error desc");
//        errorResponseDTO.setErrorCode(90111);
//        responseDTO.setErrorResponse(errorResponseDTO);
        responseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_RESPONSE.toString());
        responseDTO.setStatusCode(500);
        return responseDTO;
    }

    /**
     * TODO:// comment
     *
     * @return
     */
    @Override
    public String getType() {

        return null;
    }

    /***
     * Pre process Request
     * @param requestDTO
     * @throws Exception
     */
    @Override
    public ExtensionResponseDTO preProcessRequest(RequestContextDTO requestDTO) {

        return null;
    }

    /***
     * Post process request
     * @param requestDTO
     * @throws Exception
     */
    @Override
    public ExtensionResponseDTO postProcessRequest(RequestContextDTO requestDTO) {

        return null;
    }

    /**
     * TODO://
     *
     * @return
     */
    @Override
    public String getErrorHandler() {

        return null;
    }
}

