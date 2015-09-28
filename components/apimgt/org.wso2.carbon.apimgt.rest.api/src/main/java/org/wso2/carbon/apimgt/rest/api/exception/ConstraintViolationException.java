/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.apimgt.rest.api.utils.RestApiUtil;
import javax.validation.ConstraintViolation;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Set;

public class ConstraintViolationException extends WebApplicationException {

    protected Log log = LogFactory.getLog(getClass());

    public <T> ConstraintViolationException(Set<ConstraintViolation<T>> violations) {
        super(Response.status(Response.Status.BAD_REQUEST).entity(
                RestApiUtil.getConstraintViolationErrorDTO(violations)).build());
        for (ConstraintViolation violation : violations) {
            log.info(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath()
                    + ": " + violation.getMessage());
        }
    }
    
}
