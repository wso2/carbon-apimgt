package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.internal.service.ScopesApiService;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

public class ScopesApiServiceImpl implements ScopesApiService {

    public Response scopesGet(String xWSO2Tenant, String scopeKey, MessageContext messageContext)
            throws APIManagementException {

        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        ScopesDAO scopesDAO = ScopesDAO.getInstance();
        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            int tenantId = APIUtil.getTenantIdFromTenantDomain(xWSO2Tenant);
            if (StringUtils.isNotEmpty(scopeKey)) {
                List<Scope> model = new ArrayList<>();
                Scope scope = scopesDAO.getScope(scopeKey, tenantId);
                if (scope != null) {
                    model.add(scope);
                }
                return Response.ok().entity(SubscriptionValidationDataUtil.fromScopeListToScopeDtoList(model)).build();

            } else {
                return Response.ok().entity(SubscriptionValidationDataUtil
                        .fromScopeListToScopeDtoList(scopesDAO.getScopes(tenantId))).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(),
                    "X-WSo2-Tenant header is missing.").build();
        }
    }
}
