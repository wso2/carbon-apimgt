package org.wso2.carbon.apimgt.rest.api.util.interceptors.quotaLimit;

import org.apache.cxf.common.util.UrlUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.quotaLimiter.ResourceQuotaLimiter;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import javax.ws.rs.core.Response;
import java.util.Map;

public class ResourceQuotaLimitInterceptor extends AbstractPhaseInterceptor {

    public ResourceQuotaLimitInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        if (getQuotaLimitEnabled() && getToBeQuotaLimited(message)) {
            Map<String, String> queryParamsMap = UrlUtils.parseQueryString(
                    (String) message.get(QuotaLimitInterceptorConstants.QUERY_PARAM_STRING));
            if (queryParamsMap.containsKey(QuotaLimitInterceptorConstants.QUERY_PARAM_ORGANIZATION_ID)) {
                String organizationId = queryParamsMap.get(QuotaLimitInterceptorConstants.QUERY_PARAM_ORGANIZATION_ID);
                String s = "AAA";
                String userId = s;
                String resourceType = QuotaLimitInterceptorConstants.QUOTA_LIMIT_RESOURCE_TYPE;
                try {
                    ResourceQuotaLimiter quotaLimiter = APIUtil.getResourceQuotaLimiter();
                    boolean extensionReturnedValue = quotaLimiter.GetAPIRateLimitStatus(organizationId, userId, resourceType);
                    if (quotaLimiter.GetAPIRateLimitStatus(organizationId, userId, resourceType)) {
                        Response response = Response.status(Response.Status.TOO_MANY_REQUESTS).build();
                        message.getExchange().put(Response.class, response);
                    }
                } catch (APIManagementException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
    }

    private boolean getQuotaLimitEnabled() {
        APIManagerConfiguration configurations = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        boolean isRateLimitEnabled = Boolean.parseBoolean(configurations.getFirstProperty(APIConstants.API_QUOTA_LIMIT_ENABLE));
        return isRateLimitEnabled;
    }

    private boolean getToBeQuotaLimited(Message message) {
        String httpMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
        String matchingPath = (String) message.get(QuotaLimitInterceptorConstants.PATH_TO_MATCH_SLASH);

        boolean isCreateAPI = QuotaLimitInterceptorConstants.HTTP_POST.equals(httpMethod) &&
                QuotaLimitInterceptorConstants.API_FROM_SCRATCH_PATH.equals(matchingPath);
        boolean isCreateVersion = QuotaLimitInterceptorConstants.HTTP_POST.equals(httpMethod) &&
                QuotaLimitInterceptorConstants.NEW_API_VERSION_PATH.equals(matchingPath);
        boolean isImportAPI = QuotaLimitInterceptorConstants.HTTP_POST.equals(httpMethod) &&
                QuotaLimitInterceptorConstants.IMPORT_OPENAPI_PATH.equals(matchingPath);
        boolean isRegularType = false;

        if (isImportAPI) {
            Map<String, String> queryParamsMap = UrlUtils.parseQueryString(
                    (String) message.get(QuotaLimitInterceptorConstants.QUERY_PARAM_STRING));
            if (queryParamsMap.containsKey(QuotaLimitInterceptorConstants.QUERY_PARAM_API_TYPE)) {
                String type = queryParamsMap.get(QuotaLimitInterceptorConstants.QUERY_PARAM_API_TYPE);
                isRegularType = QuotaLimitInterceptorConstants.API_TYPE_REGULAR.equals(type);
            }
        }

        return isCreateAPI || isCreateVersion || (isImportAPI && isRegularType);
    }
}
