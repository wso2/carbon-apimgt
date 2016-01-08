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

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


public class GlobalThrowableMapper implements ExceptionMapper<Throwable>{

    private static final Log log = LogFactory.getLog(GlobalThrowableMapper.class);

    private ErrorDTO e500 = new ErrorDTO();
    private ErrorDTO e404 = new ErrorDTO();

    GlobalThrowableMapper(){
        e500.setCode(new Long(500));
        e500.setMessage("Internal server error please contact administrator.");

        e404.setCode(new Long(404));
        e404.setMessage("Resource not found.");
    }

    @Override
    public Response toResponse(Throwable e) {

        if(e instanceof ClientErrorException){
            return ((ClientErrorException) e).getResponse();
        }

        if(e instanceof NotFoundException){
            return ((NotFoundException) e).getResponse();
        }

        if(e instanceof PreconditionFailedException){
            return ((PreconditionFailedException) e).getResponse();
        }

        if(e instanceof BadRequestException){
            return ((BadRequestException) e).getResponse();
        }

        if(e instanceof ConstraintViolationException){
            return ((ConstraintViolationException) e).getResponse();
        }

        //unknown exception log and return
        log.error("An Unknown exception has been captured by global exception mapper.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Content-Type", "application/json").entity(e500).build();
    }
}
