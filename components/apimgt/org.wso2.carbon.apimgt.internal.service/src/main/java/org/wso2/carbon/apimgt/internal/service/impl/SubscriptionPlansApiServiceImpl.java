package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.model.subscription.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;
import org.wso2.carbon.apimgt.internal.service.SubscriptionPlansApiService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;


public class SubscriptionPlansApiServiceImpl implements SubscriptionPlansApiService {

    public Response subscriptionPlansGet(MessageContext messageContext) {
        String organization = resolveGatewayOrganization(messageContext);
        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        List<SubscriptionPolicy> policies;
        if (organization != null && !organization.isEmpty()) {
            policies = subscriptionValidationDAO.getAllSubscriptionPolicies(organization);
        } else {
            policies = subscriptionValidationDAO.getAllSubscriptionPolicies(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        }

        List<PlatformGatewaySubscriptionPlanDTO> plans = new ArrayList<>();
        if (policies != null) {
            for (SubscriptionPolicy policy : policies) {
                if (policy == null || policy.getName() == null || policy.getName().isEmpty()) {
                    continue;
                }
                PlatformGatewaySubscriptionPlanDTO dto = new PlatformGatewaySubscriptionPlanDTO();
                dto.setId(policy.getName());
                dto.setGatewayId("");
                dto.setPlanName(policy.getName());
                dto.setStopOnQuotaReach(policy.isStopOnQuotaReach());
                if (policy.getRateLimitCount() >= 0) {
                    dto.setThrottleLimitCount(policy.getRateLimitCount());
                }
                if (policy.getRateLimitTimeUnit() != null && !policy.getRateLimitTimeUnit().isEmpty()) {
                    dto.setThrottleLimitUnit(policy.getRateLimitTimeUnit());
                }
                dto.setStatus("ACTIVE");
                plans.add(dto);
            }
        }
        return Response.ok(plans).build();
    }

    private String resolveGatewayOrganization(MessageContext messageContext) {
        if (messageContext == null) {
            return null;
        }
        HttpServletRequest request = (HttpServletRequest) messageContext.getHttpServletRequest();
        if (request == null) {
            return null;
        }
        String apiKey = request.getHeader("api-key");
        if (apiKey == null || apiKey.isEmpty()) {
            return null;
        }
        try {
            PlatformGatewayDAO.PlatformGateway gateway = PlatformGatewayTokenUtil.verifyToken(apiKey);
            if (gateway != null) {
                return gateway.organizationId;
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    /**
     * Minimal JSON shape expected by the current gateway bulk-sync endpoint.
     */
    public static class PlatformGatewaySubscriptionPlanDTO {

        private String id;
        private String gatewayId;
        private String planName;
        private String billingPlan;
        private boolean stopOnQuotaReach;
        private Integer throttleLimitCount;
        private String throttleLimitUnit;
        private String status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getGatewayId() {
            return gatewayId;
        }

        public void setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
        }

        public String getPlanName() {
            return planName;
        }

        public void setPlanName(String planName) {
            this.planName = planName;
        }

        public String getBillingPlan() {
            return billingPlan;
        }

        public void setBillingPlan(String billingPlan) {
            this.billingPlan = billingPlan;
        }

        public boolean isStopOnQuotaReach() {
            return stopOnQuotaReach;
        }

        public void setStopOnQuotaReach(boolean stopOnQuotaReach) {
            this.stopOnQuotaReach = stopOnQuotaReach;
        }

        public Integer getThrottleLimitCount() {
            return throttleLimitCount;
        }

        public void setThrottleLimitCount(Integer throttleLimitCount) {
            this.throttleLimitCount = throttleLimitCount;
        }

        public String getThrottleLimitUnit() {
            return throttleLimitUnit;
        }

        public void setThrottleLimitUnit(String throttleLimitUnit) {
            this.throttleLimitUnit = throttleLimitUnit;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
