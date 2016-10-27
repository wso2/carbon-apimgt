/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.core.util.exception;


import org.wso2.carbon.apimgt.core.util.Constants;
import org.wso2.carbon.apimgt.core.util.dto.ErrorDTO;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Exception class that is corresponding to 404 NotFound response
 */
public class NotFoundException extends WebApplicationException {

    private String message;
    public NotFoundException() {
        super(Response.Status.NOT_FOUND);
    }

    public NotFoundException(ErrorDTO errorDTO) {
        super(Response.status(Response.Status.NOT_FOUND)
                .entity(errorDTO)
                .header(Constants.HEADER_CONTENT_TYPE, Constants.DEFAULT_RESPONSE_CONTENT_TYPE)
                .build());
        message = errorDTO.getDescription();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
