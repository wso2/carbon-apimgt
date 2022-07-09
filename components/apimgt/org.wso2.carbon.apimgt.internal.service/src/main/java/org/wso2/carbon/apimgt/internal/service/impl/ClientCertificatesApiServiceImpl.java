package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.subscription.Application;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.internal.service.ClientCertificatesApiService;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class ClientCertificatesApiServiceImpl implements ClientCertificatesApiService {

    public Response clientCertificatesGet(String xWSO2Tenant, String uuid, String serialNumber, Integer applicationId,
            MessageContext messageContext) throws APIManagementException {

        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);

        if (applicationId != null && applicationId > 0) {
            List<ClientCertificateDTO> certificates = CertificateManagerImpl.getInstance()
                    .searchClientCertificatesOfApplication(uuid, serialNumber, applicationId);
            return Response.ok().entity(certificates).build();
        } else {
            List<ClientCertificateDTO> certificates = CertificateManagerImpl.getInstance()
                    .getAllClientCertificatesOfApplication(xWSO2Tenant);
            return Response.ok().entity(certificates).build();

        }

    }
}
