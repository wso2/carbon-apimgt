package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log log = LogFactory.getLog(ScopesApiServiceImpl.class);

    public Response scopesGet(String xWSO2Tenant, String scopeKey, MessageContext messageContext)
            throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving scopes. ScopeKey: " + scopeKey + ", Tenant: " + xWSO2Tenant);
        }

        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        ScopesDAO scopesDAO = ScopesDAO.getInstance();
        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            int tenantId = APIUtil.getTenantIdFromTenantDomain(xWSO2Tenant);
            if (StringUtils.isNotEmpty(scopeKey)) {
                log.info("Retrieving scope by key: " + scopeKey + " for tenant: " + xWSO2Tenant);
                List<Scope> model = new ArrayList<>();
                Scope scope = scopesDAO.getScope(scopeKey, tenantId);
                if (scope != null) {
                    model.add(scope);
                }
                return Response.ok().entity(SubscriptionValidationDataUtil.fromScopeListToScopeDtoList(model)).build();

            } else {
                log.info("Retrieving all scopes for tenant: " + xWSO2Tenant);
                return Response.ok().entity(SubscriptionValidationDataUtil
                        .fromScopeListToScopeDtoList(scopesDAO.getScopes(tenantId))).build();
            }
        } else {
            log.warn("X-WSO2-Tenant header is missing for scope request");
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(),
                    "X-WSo2-Tenant header is missing.").build();
        }
    }
}
