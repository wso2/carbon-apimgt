package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.internal.service.EndpointCertificatesApiService;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;

import java.util.List;

import javax.ws.rs.core.Response;

public class EndpointCertificatesApiServiceImpl implements EndpointCertificatesApiService {

    public Response endpointCertificatesGet(String xWSO2Tenant, String alias, MessageContext messageContext)
            throws APIManagementException {

        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);

        int tenantId = APIUtil.getTenantIdFromTenantDomain(xWSO2Tenant);
        List<CertificateMetadataDTO> certificates =
                CertificateManagerImpl.getInstance().getCertificates(tenantId, alias, null);
        return Response.ok().entity(certificates).build();
    }
}
