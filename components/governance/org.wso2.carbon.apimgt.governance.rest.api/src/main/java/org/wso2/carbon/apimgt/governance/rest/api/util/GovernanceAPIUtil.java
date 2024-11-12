/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.rest.api.util;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.GovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.ErrorHandler;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.context.CarbonContext;

public class GovernanceAPIUtil {

    /**
     * Method to extract the validated organization
     *
     * @param ctx MessageContext
     * @return organization
     */
    public static String getValidatedOrganization(MessageContext ctx) throws GovernanceException {
        String organization = (String) ctx.get(GovernanceAPIConstants.ORGANIZATION);
        if (organization == null) {
            throw new GovernanceException(
                    "Organization is not found in the request", GovernanceExceptionCodes
                    .ORGANIZATION_NOT_FOUND);
        }
        return organization;
    }


    /**
     * Get logged in User
     *
     * @return String username
     */
    public static String getLoggedInUsername() {

        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    /**
     * Returns a generic errorDTO from an Error Handler
     *
     * @param errorHandler ErrorHandler object containing the error information
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(ErrorHandler errorHandler) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(errorHandler.getErrorCode());
        errorDTO.setMessage(errorHandler.getErrorMessage());
        errorDTO.setDescription(errorHandler.getErrorDescription());
        return errorDTO;
    }
}
