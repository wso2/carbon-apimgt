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

package org.wso2.carbon.apimgt.rest.api.util.interceptors.validation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.rest.api.util.exception.ConstraintViolationException;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

public class ValidationInInterceptor extends AbstractPhaseInterceptor<Message> {
    private Log log = LogFactory.getLog(getClass());
    private Validator validator = null; //validator interface is thread-safe

    public ValidationInInterceptor() {
        super(Phase.PRE_INVOKE);
        ValidatorFactory defaultFactory = Validation.buildDefaultValidatorFactory();
        validator = defaultFactory.getValidator();
        if (validator == null) {
            log.warn("Bean Validation provider could not be found, no validation will be performed");
        } else {
            log.debug("Validation In-Interceptor initialized successfully");
        }
    }

    public void handleMessage(Message message) {

        final OperationResourceInfo operationResource = message.getExchange().get(OperationResourceInfo.class);
        if (operationResource == null) {
            log.info("OperationResourceInfo is not available, skipping validation");
            return;
        }

        final ClassResourceInfo classResource = operationResource.getClassResourceInfo();
        if (classResource == null) {
            log.info("ClassResourceInfo is not available, skipping validation");
            return;
        }

        final ResourceProvider resourceProvider = classResource.getResourceProvider();
        if (resourceProvider == null) {
            log.info("ResourceProvider is not available, skipping validation");
            return;
        }

        final List<Object> arguments = MessageContentsList.getContentsList(message);
        final Method method = operationResource.getAnnotatedMethod();
        final Object instance = resourceProvider.getInstance(message);
        if (method != null && arguments != null) {
            //validate the parameters(arguments) over the invoked method
            validate(method, arguments.toArray(), instance);

            //validate the fields of each argument
            for (Object arg : arguments) {
                if (arg != null)
                    validate(arg);
            }
        }
    }

    public <T> void validate(final Method method, final Object[] arguments, final T instance) {
            if (validator == null) {
                log.warn("Bean Validation provider could not be found, no validation will be performed");
                return;
            }

            ExecutableValidator methodValidator = validator.forExecutables();
            Set<ConstraintViolation<T>> violations = methodValidator.validateParameters(instance,
                    method, arguments);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
    }

    public <T> void validate(final T object) {
            if (validator == null) {
                log.warn("Bean Validation provider could be found, no validation will be performed");
                return;
            }

            Set<ConstraintViolation<T>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
    }

    public void handleFault(Message messageParam) {
    }
}