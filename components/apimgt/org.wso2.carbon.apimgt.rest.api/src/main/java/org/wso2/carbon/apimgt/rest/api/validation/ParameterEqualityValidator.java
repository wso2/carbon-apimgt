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

package org.wso2.carbon.apimgt.rest.api.validation;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.rest.api.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.validation.constraints.ValidateParamEquality;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.lang.reflect.InvocationTargetException;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class ParameterEqualityValidator implements ConstraintValidator<ValidateParamEquality, Object[]> {

    private int firstParamIndex;
    private int secondParamIndex;
    private String firstField;
    private String secondField;
    private String errorMessage;

    @Override
    public void initialize(ValidateParamEquality validateEquality) {
        firstParamIndex = validateEquality.index0();
        secondParamIndex = validateEquality.index1();
        firstField = validateEquality.index0Field();
        secondField = validateEquality.index1Field();
        errorMessage = validateEquality.message();
    }

    @Override
    public boolean isValid(Object object[], ConstraintValidatorContext constraintValidatorContext) {

        try {
            Object firstObj = object[firstParamIndex];
            Object secondObj = object[secondParamIndex];

            if (!StringUtils.isEmpty(firstField)) {
                firstObj = BeanUtils.getProperty(firstObj,
                        firstField); //if first field is present, take first object as the field value of the firstObj
            }

            if (!StringUtils.isEmpty(secondField)) {
                secondObj = BeanUtils.getProperty(secondObj,
                        secondField); //if second field is present, take second object as the field value of the secondObj
            }

            //let @NotNull for checking null values
            if (firstObj == null || secondObj == null) {
                return true;
            }

            boolean isValid = firstObj.equals(secondObj);

            if (!isValid && ValidateParamEquality.DEFAULT_ERROR_MESSAGE.equals(errorMessage)) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate(
                        "Values \\{" + firstObj + "," + secondObj + "\\} must be equal").addConstraintViolation();
            }

            return isValid;

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InternalServerErrorException(e);
        }
    }

}
