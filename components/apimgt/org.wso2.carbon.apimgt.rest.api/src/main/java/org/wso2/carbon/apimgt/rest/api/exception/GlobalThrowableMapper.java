/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Created by jo on 9/16/15.
 */
public class GlobalThrowableMapper implements ExceptionMapper<Throwable>{

    private static final Log log = LogFactory.getLog(GlobalThrowableMapper.class);


    @Override
    public Response toResponse(Throwable e) {
        log.error("An Error has been captured by global exception mapper.", e);

        ErrorDTO error = new ErrorDTO();
        error.setCode(new Long(500));
        error.setMessage("Internal server error please contact administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(error).build();
    }
}
