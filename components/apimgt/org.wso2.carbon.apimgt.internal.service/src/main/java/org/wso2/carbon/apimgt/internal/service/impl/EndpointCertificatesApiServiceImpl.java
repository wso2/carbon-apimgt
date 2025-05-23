package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.internal.service.EndpointCertificatesApiService;
import org.wso2.carbon.apimgt.internal.service.dto.EndpointCertificateDTO;
import org.wso2.carbon.apimgt.internal.service.dto.EndpointCertificateListDTO;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

public class EndpointCertificatesApiServiceImpl implements EndpointCertificatesApiService {

    public Response endpointCertificatesGet(String xWSO2Tenant, String alias, MessageContext messageContext) throws APIManagementException {

        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        List<CertificateMetadataDTO> certificates = new ArrayList<CertificateMetadataDTO>();
        if (APIConstants.ORG_ALL_QUERY_PARAM.equals(xWSO2Tenant)) {
            certificates = CertificateManagerImpl.getInstance().getAllCertificates();
        } else {
            xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(xWSO2Tenant);
            certificates = CertificateManagerImpl.getInstance().getCertificates(tenantId, alias, null);
        }
        return Response.ok().entity(toEndpointCertificateListDTO(certificates)).build();
    }

    private List<EndpointCertificateDTO> toEndpointCertificateListDTO(List<CertificateMetadataDTO> certificates) {
        List<EndpointCertificateDTO> endpointCertificateDTOList = new ArrayList<>();
        certificates.forEach(certificateMetadataDTO -> {
            endpointCertificateDTOList.add(new EndpointCertificateDTO()
                    .endpoint(certificateMetadataDTO.getEndpoint())
                    .certificate(certificateMetadataDTO.getCertificate())
                    .alias(certificateMetadataDTO.getAlias())
                    .organization(certificateMetadataDTO.getOrganization())
                    .tenantId(certificateMetadataDTO.getTenantId()));
        });
        return endpointCertificateDTOList;
    }
}
