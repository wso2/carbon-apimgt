/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.kmvalidator;

import org.wso2.carbon.apimgt.api.model.AppConfigConstraintType;
import org.wso2.carbon.apimgt.api.model.KeyManagerApplicationConfigValidator;

/**
 * Factory class to retrieve the appropriate validator for a given constraint type.
 */
public class KeyManagerApplicationConfigValidatorFactory {

    /**
     * Returns a new instance of the validator for the specified type.
     * New instances are returned because validators maintain state (error messages).
     *
     * @param type The {@link AppConfigConstraintType}
     * @return {@link KeyManagerApplicationConfigValidator} implementation
     */
    public static KeyManagerApplicationConfigValidator getValidator(AppConfigConstraintType type) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case RANGE:
            case RANGE_MIN:
            case RANGE_MAX:
                return new RangeValidator(type);
            case REGEX:
                return new RegexValidator();
            case ENUM:
                return new EnumValidator();
            default:
                return null;
        }
    }
}
