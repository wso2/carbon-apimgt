/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.impl.restapi.publisher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.restapi.CommonUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import javax.ws.rs.core.Response;
import java.util.List;

public class EndpointCertificatesApiServiceImplUtils {

    private EndpointCertificatesApiServiceImplUtils() {
    }

    private static final Log log = LogFactory.getLog(EndpointCertificatesApiServiceImplUtils.class);


    /**
     * @param alias alias of the certificate
     * @return certificate
     * @throws APIManagementException when certificate is not found
     */
    public static Object getEndpointCertificateContentByAlias(String alias) throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        int tenantId = APIUtil.getTenantId(CommonUtils.getLoggedInUsername());
        validateCertOperations(alias, tenantId);

        return apiProvider.getCertificateContent(alias);

    }

    public static int deleteEndpointCertificateByAlias(String alias) throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        String username = CommonUtils.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(username);
        validateCertOperations(alias, tenantId);

        int responseCode = apiProvider.deleteCertificate(username, alias, null);
        if (responseCode == ResponseCode.SUCCESS.getResponseCode()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("The certificate which belongs to tenant : %d represented by the alias : " +
                        "%s is deleted successfully", tenantId, alias));
            }
            return responseCode;
        } else {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.DELETE_CERT, alias));
        }
    }

    public static CertificateInformationDTO getEndpointCertificateByAlias(String alias) throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        int tenantId = APIUtil.getTenantId(CommonUtils.getLoggedInUsername());
        validateCertOperations(alias, tenantId);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Retrieving the common information of the certificate which is represented by the" +
                    " alias : %s", alias));
        }
        return apiProvider.getCertificateStatus(alias);
    }

    public static int updateEndpointCertificateByAlias(String alias, String fileName,
                                                       String base64EncodedCert)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        int tenantId = APIUtil.getTenantId(CommonUtils.getLoggedInUsername());
        validateCertOperations(alias, tenantId, fileName);
        return apiProvider.updateCertificate(base64EncodedCert, alias);
    }

    private static void validateCertOperations(String alias, int tenantId) throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        if (!StringUtils.isNotEmpty(alias)) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.CERT_BAD_REQUEST, "The alias cannot be empty"));
        }

        if (!apiProvider.isCertificatePresent(tenantId, alias)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Could not find a certificate in truststore which belongs to tenant : %d " +
                        "and with alias : %s. Hence the operation is terminated.", tenantId, alias));
            }
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.CERT_NOT_FOUND, alias));
        }
    }

    private static void validateCertOperations(String alias, int tenantId, String fileName) throws APIManagementException {
        validateCertOperations(alias, tenantId);
        if (StringUtils.isBlank(fileName)) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.CERT_BAD_REQUEST, "The Certificate should not be empty"));
        }
    }
}
